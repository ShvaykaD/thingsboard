/**
 * Copyright © 2016-2023 The Thingsboard Authors
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
package org.thingsboard.server.cache.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.queue.TbMsgCallback;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "cache", value = "type", havingValue = "redis")
@RequiredArgsConstructor
public class RedisRuleNodeCache implements RuleNodeCache {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void add(RuleNodeId ruleNodeId, String key, String value) {
        processAdd(ruleNodeId, key, value.getBytes());
    }

    @Override
    public void add(RuleNodeId ruleNodeId, String key, EntityId value) {
        processAdd(ruleNodeId, key, SerializationUtils.serialize(value));
    }

    @Override
    public void add(RuleNodeId ruleNodeId, Integer partition, EntityId key, TbMsg value) {
        processAdd(ruleNodeId, partition, key.getId().toString(), TbMsg.toByteArray(value));
    }

    @Override
    public void removeStringList(RuleNodeId ruleNodeId, String key, List<String> values) {
        processRemove(ruleNodeId, key, stringListToBytes(values));
    }

    @Override
    public void removeEntityIdList(RuleNodeId ruleNodeId, String key, List<EntityId> values) {
        processRemove(ruleNodeId, key, entityIdListToBytes(values));
    }

    @Override
    public void removeTbMsgList(RuleNodeId ruleNodeId, Integer partition, EntityId key, List<TbMsg> values) {
        processRemove(ruleNodeId, partition, key.getId().toString(), tbMsgListToBytes(values));
    }

    @Override
    public Set<String> getStringSetByKey(RuleNodeId ruleNodeId, String key) {
        return toStringSet(processGetMembers(ruleNodeId, key));
    }

    @Override
    public Set<EntityId> getEntityIdSetByKey(RuleNodeId ruleNodeId, String key) {
        return toEntityIdSet(processGetMembers(ruleNodeId, key));
    }

    @Override
    public Set<TbMsg> getTbMsgSetByKey(RuleNodeId ruleNodeId, Integer partition, EntityId key) {
        return toTbMsgSet(processGetMembers(ruleNodeId, partition, key.getId().toString()));
    }

    @Override
    public void evict(RuleNodeId ruleNodeId, String key) {
        processEvict(toRuleNodeCacheKey(ruleNodeId, null, key));
    }

    @Override
    public void evict(RuleNodeId ruleNodeId, Integer partition, EntityId key) {
        processEvict(toRuleNodeCacheKey(ruleNodeId, partition, key.getId().toString()));
    }

    private void processAdd(RuleNodeId ruleNodeId, String key, byte[] value) {
        processAdd(ruleNodeId, null, key, value);
    }

    private void processAdd(RuleNodeId ruleNodeId, Integer partition, String key, byte[] value) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.setCommands().sAdd(toRuleNodeCacheKey(ruleNodeId, partition, key).getBytes(), value);
        }
    }

    private void processRemove(RuleNodeId ruleNodeId, String key, byte[][] values) {
        processRemove(ruleNodeId, null, key, values);
    }

    private void processRemove(RuleNodeId ruleNodeId, Integer partition, String key, byte[][] values) {
        if (values.length == 0) {
            return;
        }
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.setCommands().sRem(toRuleNodeCacheKey(ruleNodeId, partition, key).getBytes(), values);
        }
    }

    private Set<byte[]> processGetMembers(RuleNodeId ruleNodeId, String key) {
        return processGetMembers(ruleNodeId, null, key);
    }

    private Set<byte[]> processGetMembers(RuleNodeId ruleNodeId, Integer partition, String key) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            Set<byte[]> bytes = connection.setCommands().sMembers(toRuleNodeCacheKey(ruleNodeId, partition, key).getBytes());
            if (bytes == null) {
                return Collections.emptySet();
            }
            return bytes;
        }
    }

    private void processEvict(String ruleNodeId) {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.del(ruleNodeId.getBytes());
        }
    }

    private byte[][] stringListToBytes(List<String> values) {
        return values.stream()
                .map(String::getBytes)
                .toArray(byte[][]::new);
    }

    private byte[][] entityIdListToBytes(List<EntityId> values) {
        return values.stream()
                .map(SerializationUtils::serialize)
                .toArray(byte[][]::new);
    }

    private byte[][] tbMsgListToBytes(List<TbMsg> values) {
        return values.stream()
                .map(TbMsg::toByteArray)
                .toArray(byte[][]::new);
    }

    private Set<String> toStringSet(Set<byte[]> values) {
        return values.stream()
                .map(String::new)
                .collect(Collectors.toSet());
    }

    private Set<EntityId> toEntityIdSet(Set<byte[]> values) {
        return values.stream()
                .map(bytes -> (EntityId) SerializationUtils.deserialize(bytes))
                .collect(Collectors.toSet());
    }

    private Set<TbMsg> toTbMsgSet(Set<byte[]> values) {
        return values.stream()
                .map(bytes -> TbMsg.fromBytes(bytes, TbMsgCallback.EMPTY))
                .collect(Collectors.toSet());
    }

    private String toRuleNodeCacheKey(RuleNodeId ruleNodeId, Integer partition, String key) {
        return partition == null ?
                String.format("%s::%s", ruleNodeId.getId().toString(), key) :
                String.format("%s::%s::%s", ruleNodeId.getId().toString(), partition, key);
    }

}
