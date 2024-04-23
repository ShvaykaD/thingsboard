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
package org.thingsboard.server.service.ruleengine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thingsboard.common.util.ThingsBoardThreadFactory;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.common.msg.queue.TbCallback;
import org.thingsboard.server.gen.transport.TransportProtos;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DefaultRuleEngineCallServiceTest {

    private static final TenantId TENANT_ID = TenantId.fromUUID(UUID.fromString("d7210c7f-a152-4e91-8186-19ae85499a6b"));

    private final ConcurrentMap<UUID, Consumer<TbMsg>> requests = new ConcurrentHashMap<>();

    @Mock
    private TbClusterService tbClusterServiceMock;

    private DefaultRuleEngineCallService ruleEngineCallService;
    private ScheduledExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadScheduledExecutor(ThingsBoardThreadFactory.forName("rpc-callback"));
        ruleEngineCallService = new DefaultRuleEngineCallService(tbClusterServiceMock);
        ReflectionTestUtils.setField(ruleEngineCallService, "rpcCallBackExecutor", executor);
        ReflectionTestUtils.setField(ruleEngineCallService, "requests", requests);
    }

    @AfterEach
    void tearDown() {
        requests.clear();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10L, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    void givenRequest_whenProcessRestApiCallToRuleEngine_thenPushMsgToRuleEngine() {
        long timeout = 10000L;
        long expTime = System.currentTimeMillis() + timeout;
        HashMap<String, String> metaData = new HashMap<>();
        UUID requestId = UUID.randomUUID();
        metaData.put("serviceId", "core");
        metaData.put("requestUUID", requestId.toString());
        metaData.put("expirationTime", Long.toString(expTime));
        TbMsg msg = TbMsg.newMsg("main", TbMsgType.REST_API_REQUEST, TENANT_ID, null, new TbMsgMetaData(metaData), "{\"key\":\"value\"}");

        doNothing().when(tbClusterServiceMock).pushMsgToRuleEngine(any(), any(), any(),anyBoolean(), any());
        ruleEngineCallService.processRestAPICallToRuleEngine(TENANT_ID, requestId, msg, true, TbMsg::getData);

        assertThat(requests.size()).isEqualTo(1);
        verify(tbClusterServiceMock).pushMsgToRuleEngine(eq(TENANT_ID), eq(TENANT_ID), eq(msg), eq(true), isNull());
    }

    @Test
    void givenSmallTimeout_whenProcessRestApiCallToRuleEngine_thenDoesNotReturnResponse() {
        long timeout = 1L;
        long expTime = System.currentTimeMillis() + timeout;
        HashMap<String, String> metaData = new HashMap<>();
        UUID requestId = UUID.randomUUID();
        metaData.put("serviceId", "core");
        metaData.put("requestUUID", requestId.toString());
        metaData.put("expirationTime", Long.toString(expTime));
        TbMsg msg = TbMsg.newMsg("main", TbMsgType.REST_API_REQUEST, TENANT_ID, null, new TbMsgMetaData(metaData), "{\"key\":\"value\"}");

        doAnswer(invocation -> {
            //check the presence of request in the map after put()
            assertThat(requests.size()).isEqualTo(1);
            return null;
        }).when(tbClusterServiceMock).pushMsgToRuleEngine(any(), any(), any(), anyBoolean(), any());
        ruleEngineCallService.processRestAPICallToRuleEngine(TENANT_ID, requestId, msg, true, TbMsg::getData);

        verify(tbClusterServiceMock).pushMsgToRuleEngine(eq(TENANT_ID), eq(TENANT_ID), eq(msg), eq(true), isNull());
        //check map is empty after scheduleTimeout()
        assertThat(requests.size()).isEqualTo(0);
    }

    @Test
    void givenResponse_whenOnQueue_thenAcceptTbMsgResponse() {
        long timeout = 10000L;
        long expTime = System.currentTimeMillis() + timeout;
        HashMap<String, String> metaData = new HashMap<>();
        UUID requestId = UUID.randomUUID();
        metaData.put("serviceId", "core");
        metaData.put("requestUUID", requestId.toString());
        metaData.put("expirationTime", Long.toString(expTime));
        TbMsg msg = TbMsg.newMsg("main", TbMsgType.REST_API_REQUEST, TENANT_ID, null, new TbMsgMetaData(metaData), "{\"key\":\"value\"}");

        doAnswer(invocation -> {
            //check the presence of request in the map after put()
            assertThat(requests.size()).isEqualTo(1);
            ruleEngineCallService.onQueueMsg(getResponse(requestId, msg), TbCallback.EMPTY);
            //check map is empty after onQueueMsg()
            assertThat(requests.size()).isEqualTo(0);
            return null;
        }).when(tbClusterServiceMock).pushMsgToRuleEngine(any(), any(), any(), anyBoolean(), any());
        ruleEngineCallService.processRestAPICallToRuleEngine(TENANT_ID, requestId, msg, true, TbMsg::getData);

        verify(tbClusterServiceMock).pushMsgToRuleEngine(eq(TENANT_ID), eq(TENANT_ID), eq(msg), eq(true), isNull());
    }

    private TransportProtos.RestApiCallResponseMsgProto getResponse(UUID requestId, TbMsg msg) {
        return TransportProtos.RestApiCallResponseMsgProto.newBuilder()
                .setResponse(TbMsg.toByteString(msg))
                .setRequestIdMSB(requestId.getMostSignificantBits())
                .setRequestIdLSB(requestId.getLeastSignificantBits())
                .build();
    }
}
