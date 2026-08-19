/**
 * Copyright © 2016-2026 The Thingsboard Authors
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
package org.thingsboard.server.service.rpc;

import com.google.common.util.concurrent.ListenableFuture;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.common.util.DonAsynchron;
import org.thingsboard.common.util.HashPartitioner;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.ThingsBoardThreadFactory;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.rpc.Rpc;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.common.msg.rpc.RpcPersistResult;
import org.thingsboard.server.dao.rpc.RpcService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@TbCoreComponent
@Service
@Slf4j
public class TbRpcService {
    private final RpcService rpcService;
    private final TbClusterService tbClusterService;

    private final ExecutorService[] callbackExecutors;
    // Resumes device actors once their persistent create is durable. Deliberately NOT the callback stripes
    // above: this runs on the command-delivery path, while those stripes carry full-request JSON serialization
    // plus a rule-engine publish per notification. Sharing them would put rule engine publishing on the
    // delivery path, so a queue stall or rule-engine backpressure would hold up device sends. Needs no rpcId
    // striping - delivery order is fixed by the actor before the write is even enqueued, and there is exactly
    // one continuation per rpcId - so a plain fixed pool is enough.
    private final ExecutorService continuationExecutor;

    public TbRpcService(RpcService rpcService, TbClusterService tbClusterService,
                        @Value("${sql.rpc.callback_threads:3}") int callbackThreads,
                        @Value("${sql.rpc.continuation_threads:3}") int continuationThreads) {
        if (callbackThreads < 1) {
            throw new IllegalArgumentException("sql.rpc.callback_threads must be >= 1, but was " + callbackThreads);
        }
        if (continuationThreads < 1) {
            throw new IllegalArgumentException("sql.rpc.continuation_threads must be >= 1, but was " + continuationThreads);
        }
        this.rpcService = rpcService;
        this.tbClusterService = tbClusterService;
        this.callbackExecutors = new ExecutorService[callbackThreads];
        for (int i = 0; i < callbackThreads; i++) {
            callbackExecutors[i] = Executors.newSingleThreadExecutor(
                    ThingsBoardThreadFactory.forName("rpc-persist-callback-" + i));
        }
        this.continuationExecutor = Executors.newFixedThreadPool(continuationThreads,
                ThingsBoardThreadFactory.forName("rpc-persist-continuation"));
    }

    @PreDestroy
    private void destroy() {
        for (ExecutorService executor : callbackExecutors) {
            executor.shutdownNow();
        }
        continuationExecutor.shutdownNow();
    }

    /**
     * Enqueues the create onto the batched write queue and resumes the caller once the row's fate is known.
     * The continuation is invoked exactly once, on the continuation pool rather than on a notification stripe,
     * because it is what releases the command for delivery to the device.
     */
    public void createIfAbsent(TenantId tenantId, Rpc rpc, Consumer<RpcPersistResult> continuation) {
        DonAsynchron.withCallback(rpcService.createIfAbsentAsync(rpc),
                inserted -> {
                    RpcPersistResult result = Boolean.TRUE.equals(inserted)
                            ? RpcPersistResult.INSERTED : RpcPersistResult.DUPLICATE;
                    if (RpcPersistResult.INSERTED == result) {
                        // Enqueue the notification on this rpcId's stripe BEFORE resuming the actor, so
                        // RPC_QUEUED is queued ahead of any status notification the resumed actor goes on to
                        // cause. Only the enqueue happens here; the push itself runs on the stripe, off this
                        // path. Doing both steps in one callback keeps that order deterministic - registering
                        // two separate future callbacks would rely on Guava listener ordering, which is not
                        // specified.
                        executorFor(rpc.getUuidId()).execute(() -> notifyRuleEngine(tenantId, rpc));
                    } else {
                        log.debug("[{}][{}][{}] Skipping RPC_QUEUED notification - a row for this RPC already existed",
                                tenantId, rpc.getDeviceId(), rpc.getId());
                    }
                    continuation.accept(result);
                },
                t -> {
                    log.error("[{}][{}][{}] Failed to persist RPC create with status [{}]",
                            tenantId, rpc.getDeviceId(), rpc.getId(), rpc.getStatus(), t);
                    continuation.accept(RpcPersistResult.FAILED);
                },
                continuationExecutor);
    }

    /**
     * Synchronous create, retained so reverting to the blocking persist-before-send behaviour is a one-method
     * change in the device actor. Not used by the actor's request path.
     */
    public boolean createIfAbsent(TenantId tenantId, Rpc rpc) {
        boolean inserted = rpcService.createIfAbsent(rpc);
        if (inserted) {
            executorFor(rpc.getUuidId()).execute(() -> notifyRuleEngine(tenantId, rpc));
        }
        return inserted;
    }

    public void update(TenantId tenantId, Rpc rpc) {
        persist(tenantId, rpc, rpcService.updateAsync(rpc));
    }

    private void persist(TenantId tenantId, Rpc rpc, ListenableFuture<Boolean> future) {
        DonAsynchron.withCallback(future,
                persisted -> {
                    if (Boolean.TRUE.equals(persisted)) {
                        notifyRuleEngine(tenantId, rpc);
                    } else {
                        log.debug("[{}][{}][{}] Skipping rule engine notification for status [{}] - RPC row is not updatable (already terminal or removed)",
                                tenantId, rpc.getDeviceId(), rpc.getId(), rpc.getStatus());
                    }
                },
                t -> log.error("[{}][{}][{}] Failed to persist RPC with status [{}]",
                        tenantId, rpc.getDeviceId(), rpc.getId(), rpc.getStatus(), t),
                executorFor(rpc.getUuidId()));
    }

    private Executor executorFor(UUID rpcId) {
        return callbackExecutors[HashPartitioner.resolvePartition(rpcId.hashCode(), callbackExecutors.length)];
    }

    private void notifyRuleEngine(TenantId tenantId, Rpc rpc) {
        try {
            pushRpcMsgToRuleEngine(tenantId, rpc);
        } catch (Throwable t) {
            log.error("[{}][{}][{}] Failed to push RPC with status [{}] to rule engine",
                    tenantId, rpc.getDeviceId(), rpc.getId(), rpc.getStatus(), t);
        }
    }

    private void pushRpcMsgToRuleEngine(TenantId tenantId, Rpc rpc) {
        TbMsg msg = TbMsg.newMsg()
                .type(TbMsgType.valueOf("RPC_" + rpc.getStatus().name()))
                .originator(rpc.getDeviceId())
                .copyMetaData(TbMsgMetaData.EMPTY)
                .data(JacksonUtil.toString(rpc))
                .build();
        tbClusterService.pushMsgToRuleEngine(tenantId, rpc.getDeviceId(), msg, null);
    }

    public PageData<Rpc> findInFlightForReload(TenantId tenantId, DeviceId deviceId, PageLink pageLink) {
        return rpcService.findInFlightForReload(tenantId, deviceId, pageLink);
    }

}
