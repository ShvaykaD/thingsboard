/**
 * Copyright © 2016-2024 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.rule.engine.deduplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.RuleNodeCacheService;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.rule.engine.cache.TbAbstractCacheBasedRuleNode;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.data.msg.TbNodeConnectionType;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.util.TbPair;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.thingsboard.server.common.data.DataConstants.QUEUE_NAME;

@RuleNode(
        type = ComponentType.TRANSFORMATION,
        name = "deduplication",
        configClazz = TbMsgDeduplicationNodeConfiguration.class,
        version = 1,
        hasQueueName = true,
        nodeDescription = "Deduplicate messages within the same originator entity for a configurable period " +
                "based on a specified deduplication strategy.",
        nodeDetails = "Deduplication strategies: <ul><li><strong>FIRST</strong> - return first message that arrived during deduplication period.</li>" +
                "<li><strong>LAST</strong> - return last message that arrived during deduplication period.</li>" +
                "<li><strong>ALL</strong> - return all messages as a single JSON array message. " +
                "Where each element represents object with <strong><i>msg</i></strong> and <strong><i>metadata</i></strong> inner properties.</li></ul>" +
                "<b>Important note:</b> If the incoming message is processed in the queue with a sequential processing strategy configured, " +
                "the message acknowledgment that used in the rule node logic will trigger the next message to be processed by the queue.",
        icon = "content_copy",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbActionNodeMsgDeduplicationConfig"
)
@Slf4j
public class TbMsgDeduplicationNode extends TbAbstractCacheBasedRuleNode<TbMsgDeduplicationNodeConfiguration, DeduplicationData> {

    private static final String DEDUPLICATION_IDS_CACHE_KEY = "deduplication_ids";
    private static final int TB_MSG_DEDUPLICATION_RETRY_DELAY = 10;

    private long deduplicationInterval;
    private String queueName;

    @Override
    protected TbMsgDeduplicationNodeConfiguration loadRuleNodeConfiguration(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        TbMsgDeduplicationNodeConfiguration config = TbNodeUtils.convert(configuration, TbMsgDeduplicationNodeConfiguration.class);
        this.deduplicationInterval = TimeUnit.SECONDS.toMillis(config.getInterval());
        this.queueName = ctx.getQueueName();
        return config;
    }

    @Override
    protected void getValuesFromCacheAndSchedule(TbContext ctx, RuleNodeCacheService cache, Integer partition, EntityId id) {
        Set<TbMsg> tbMsgs = cache.getTbMsgs(id, partition);
        DeduplicationData deduplicationData = entityIdValuesMap.computeIfAbsent(id, k -> new DeduplicationData());
        if (deduplicationData.isEmpty() && tbMsgs.isEmpty()) {
            return;
        }
        deduplicationData.addAll(tbMsgs);
        scheduleTickMsg(ctx, id, deduplicationData, 0);
    }

    @Override
    protected void processOnRegularMsg(TbContext ctx, TbMsg msg) {
        EntityId id = msg.getOriginator();
        TopicPartitionInfo tpi = ctx.getTopicPartitionInfo(id);
        if (!tpi.isMyPartition()) {
            log.trace("[{}][{}][{}] Ignore msg from entity that doesn't belong to local partition!", ctx.getSelfId(), tpi.getFullTopicName(), id);
            return;
        }
        Integer partition = tpi.getPartition().orElse(DEFAULT_PARTITION);
        Set<EntityId> entityIds = partitionsEntityIdsMap.computeIfAbsent(partition, k -> new HashSet<>());
        boolean entityIdAdded = entityIds.add(id);
        DeduplicationData deduplicationMsgs = entityIdValuesMap.computeIfAbsent(id, k -> new DeduplicationData());
        if (deduplicationMsgs.size() >= config.getMaxPendingMsgs()) {
            log.trace("[{}] Max limit of pending messages reached for deduplication id: [{}]", ctx.getSelfId(), id);
            ctx.tellFailure(msg, new RuntimeException("[" + ctx.getSelfId() + "] Max limit of pending messages reached for deduplication id: [" + id + "]"));
            return;
        }
        log.trace("[{}][{}] Adding msg: [{}][{}] to the pending msgs map ...", ctx.getSelfId(), id, msg.getId(), msg.getMetaDataTs());
        deduplicationMsgs.add(msg);
        getCacheIfPresentAndExecute(ctx, cache -> {
            if (entityIdAdded) {
                cache.add(getEntityIdsCacheKey(), id);
            }
            cache.add(id, partition, msg);
        });
        ctx.ack(msg);
        scheduleTickMsg(ctx, id, deduplicationMsgs);
    }

    @Override
    protected void processOnTickMsg(TbContext ctx, TbMsg msg) {
        EntityId deduplicationId = msg.getOriginator();
        TopicPartitionInfo tpi = ctx.getTopicPartitionInfo(deduplicationId);
        if (!tpi.isMyPartition()) {
            log.trace("[{}][{}][{}] Ignore msg from entity that doesn't belong to local partition!", ctx.getSelfId(), tpi.getFullTopicName(), deduplicationId);
            return;
        }
        Integer partition = tpi.getPartition().orElse(DEFAULT_PARTITION);
        DeduplicationData data = entityIdValuesMap.get(deduplicationId);
        if (data == null) {
            return;
        }
        data.setTickScheduled(false);
        if (data.isEmpty()) {
            return;
        }
        long deduplicationTimeoutMs = System.currentTimeMillis();
        try {
            List<TbPair<TbMsg, List<TbMsg>>> deduplicationResults = new ArrayList<>();
            List<TbMsg> msgList = data.getMsgList();
            Optional<TbPair<Long, Long>> packBoundsOpt = findValidPack(msgList, deduplicationTimeoutMs);
            while (packBoundsOpt.isPresent()) {
                TbPair<Long, Long> packBounds = packBoundsOpt.get();
                List<TbMsg> pack = new ArrayList<>();
                if (DeduplicationStrategy.ALL.equals(config.getStrategy())) {
                    for (Iterator<TbMsg> iterator = msgList.iterator(); iterator.hasNext(); ) {
                        TbMsg next = iterator.next();
                        long msgTs = next.getMetaDataTs();
                        if (msgTs >= packBounds.getFirst() && msgTs < packBounds.getSecond()) {
                            pack.add(next);
                            iterator.remove();
                        }
                    }
                    deduplicationResults.add(new TbPair<>(TbMsg.newMsg(
                            queueName,
                            config.getOutMsgType(),
                            deduplicationId,
                            getMetadata(packBounds.getFirst()),
                            getMergedData(pack)), pack));
                } else {
                    TbMsg resultMsg = null;
                    boolean searchMin = DeduplicationStrategy.FIRST.equals(config.getStrategy());
                    for (Iterator<TbMsg> iterator = msgList.iterator(); iterator.hasNext(); ) {
                        TbMsg next = iterator.next();
                        long msgTs = next.getMetaDataTs();
                        if (msgTs >= packBounds.getFirst() && msgTs < packBounds.getSecond()) {
                            pack.add(next);
                            iterator.remove();
                            if (resultMsg == null
                                    || (searchMin && next.getMetaDataTs() < resultMsg.getMetaDataTs())
                                    || (!searchMin && next.getMetaDataTs() > resultMsg.getMetaDataTs())) {
                                resultMsg = next;
                            }
                        }
                    }
                    if (resultMsg != null) {
                        deduplicationResults.add(new TbPair<>(TbMsg.newMsg(
                                queueName != null ? queueName : resultMsg.getQueueName(),
                                resultMsg.getType(),
                                resultMsg.getOriginator(),
                                resultMsg.getCustomerId(),
                                resultMsg.getMetaData(),
                                resultMsg.getData()
                        ), pack));
                    }
                }
                packBoundsOpt = findValidPack(msgList, deduplicationTimeoutMs);
            }
            deduplicationResults.forEach(result -> enqueueForTellNextWithRetry(ctx, partition, result, 0));
        } finally {
            if (!data.isEmpty()) {
                scheduleTickMsg(ctx, deduplicationId, data);
            }
        }
    }

    @Override
    protected TbMsgType getTickMsgType() {
        return TbMsgType.DEDUPLICATION_TIMEOUT_SELF_MSG;
    }

    @Override
    protected String getEntityIdsCacheKey() {
        return DEDUPLICATION_IDS_CACHE_KEY;
    }

    private void scheduleTickMsg(TbContext ctx, EntityId deduplicationId, DeduplicationData data, long delayMs) {
        if (!data.isTickScheduled()) {
            log.trace("[{}] Schedule deduplication tick msg for entity: [{}], delay: [{}]", ctx.getSelfId(), deduplicationId, delayMs);
            scheduleTickMsg(ctx, deduplicationId, delayMs);
            data.setTickScheduled(true);
        }
    }

    private void scheduleTickMsg(TbContext ctx, EntityId deduplicationId, DeduplicationData data) {
        scheduleTickMsg(ctx, deduplicationId, data, deduplicationInterval + 1);
    }

    private void scheduleTickMsg(TbContext ctx, EntityId deduplicationId, long delayMs) {
        ctx.tellSelf(ctx.newMsg(null, getTickMsgType(), deduplicationId, TbMsgMetaData.EMPTY, TbMsg.EMPTY_STRING), delayMs);
    }

    private Optional<TbPair<Long, Long>> findValidPack(List<TbMsg> msgs, long deduplicationTimeoutMs) {
        Optional<TbMsg> min = msgs.stream().min(Comparator.comparing(TbMsg::getMetaDataTs));
        return min.map(minTsMsg -> {
            long packStartTs = minTsMsg.getMetaDataTs();
            long packEndTs = packStartTs + deduplicationInterval;
            if (packEndTs <= deduplicationTimeoutMs) {
                return new TbPair<>(packStartTs, packEndTs);
            }
            return null;
        });
    }

    private void enqueueForTellNextWithRetry(TbContext ctx, Integer partition, TbPair<TbMsg, List<TbMsg>> result, int retryAttempt) {
        TbMsg outMsg = result.getFirst();
        List<TbMsg> msgsToRemoveFromCache = result.getSecond();
        if (retryAttempt >= config.getMaxRetries()) {
            log.trace("[{}][{}] Removing deduplication messages pack due to max enqueue retry attempts exhausted!", ctx.getSelfId(), outMsg.getOriginator());
            getCacheIfPresentAndExecute(ctx, cache -> cache.removeTbMsgList(outMsg.getOriginator(), partition, msgsToRemoveFromCache));
        } else {
            ctx.enqueueForTellNext(outMsg, TbNodeConnectionType.SUCCESS,
                    () -> {
                        log.trace("[{}][{}][{}] Successfully enqueue deduplication result message!", ctx.getSelfId(), outMsg.getOriginator(), retryAttempt);
                        getCacheIfPresentAndExecute(ctx, cache -> cache.removeTbMsgList(outMsg.getOriginator(), partition, msgsToRemoveFromCache));
                    },
                    throwable -> {
                        log.trace("[{}][{}][{}] Failed to enqueue deduplication output message due to: ", ctx.getSelfId(), outMsg.getOriginator(), retryAttempt, throwable);
                        ctx.schedule(() -> enqueueForTellNextWithRetry(ctx, partition, result, retryAttempt + 1), TB_MSG_DEDUPLICATION_RETRY_DELAY, TimeUnit.SECONDS);
                    });
        }
    }

    private String getMergedData(List<TbMsg> msgs) {
        ArrayNode mergedData = JacksonUtil.newArrayNode();
        msgs.forEach(msg -> {
            ObjectNode msgNode = JacksonUtil.newObjectNode();
            msgNode.set("msg", JacksonUtil.toJsonNode(msg.getData()));
            msgNode.set("metadata", JacksonUtil.valueToTree(msg.getMetaData().getData()));
            mergedData.add(msgNode);
        });
        return JacksonUtil.toString(mergedData);
    }

    private TbMsgMetaData getMetadata(long packStartTs) {
        TbMsgMetaData metaData = new TbMsgMetaData();
        metaData.putValue("ts", String.valueOf(packStartTs));
        return metaData;
    }

    @Override
    public TbPair<Boolean, JsonNode> upgrade(int fromVersion, JsonNode oldConfiguration) throws TbNodeException {
        boolean hasChanges = false;
        switch (fromVersion) {
            case 0:
                if (oldConfiguration.has(QUEUE_NAME)) {
                    hasChanges = true;
                    ((ObjectNode) oldConfiguration).remove(QUEUE_NAME);
                }
                break;
            default:
                break;
        }
        return new TbPair<>(hasChanges, oldConfiguration);
    }

}
