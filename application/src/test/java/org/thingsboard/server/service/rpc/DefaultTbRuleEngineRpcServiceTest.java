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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thingsboard.rule.engine.api.RuleEngineDeviceRpcRequest;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.common.msg.queue.ServiceType;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.queue.discovery.PartitionService;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultTbRuleEngineRpcServiceTest {

    private static final TenantId TENANT_ID = TenantId.fromUUID(UUID.fromString("d5a6e5c0-1f68-4b0e-8f9a-2f9b6a7a1a01"));
    private static final DeviceId DEVICE_ID = new DeviceId(UUID.fromString("1d9f771a-7cdc-4ac7-838c-ba193d05a012"));
    private static final UUID REQUEST_UUID = UUID.fromString("f64a20df-eb1e-46a3-ba6f-0b3ae053ee0a");

    @Mock
    private PartitionService partitionServiceMock;
    @Mock
    private TbClusterService tbClusterServiceMock;

    @InjectMocks
    private DefaultTbRuleEngineRpcService tbRuleEngineRpcService;

    @Test
    public void givenTbMsg_whenSendRestApiCallReply_thenPushNotificationToCore() {
        // GIVEN
        String serviceId = "tb-core-0";
        UUID requestId = UUID.fromString("f64a20df-eb1e-46a3-ba6f-0b3ae053ee0a");
        DeviceId deviceId = new DeviceId(UUID.fromString("1d9f771a-7cdc-4ac7-838c-ba193d05a012"));
        TbMsg msg = TbMsg.newMsg()
                .type(TbMsgType.REST_API_REQUEST)
                .originator(deviceId)
                .copyMetaData(TbMsgMetaData.EMPTY)
                .data(TbMsg.EMPTY_JSON_OBJECT)
                .build();
        var restApiCallResponseMsgProto = TransportProtos.RestApiCallResponseMsgProto.newBuilder()
                .setRequestIdMSB(requestId.getMostSignificantBits())
                .setRequestIdLSB(requestId.getLeastSignificantBits())
                .setResponseProto(TbMsg.toProto(msg))
                .build();

        // WHEN
        tbRuleEngineRpcService.sendRestApiCallReply(serviceId, requestId, msg);

        // THEN
        then(tbClusterServiceMock).should().pushNotificationToCore(serviceId, restApiCallResponseMsgProto, null);
    }

    @ParameterizedTest
    @MethodSource
    public void givenRpcRequest_whenSendRpcRequestToDevice_thenSchedulesTimeoutUsingResponseDeadline(
            Long deadlineOffset, long expirationOffset, long expectedDelay) {
        // GIVEN
        ScheduledExecutorService schedulerMock = mock(ScheduledExecutorService.class);
        ReflectionTestUtils.setField(tbRuleEngineRpcService, "scheduler", schedulerMock);

        TopicPartitionInfo tpi = mock(TopicPartitionInfo.class);
        given(tpi.isMyPartition()).willReturn(false);
        given(partitionServiceMock.resolve(eq(ServiceType.TB_CORE), any(TenantId.class), any(DeviceId.class))).willReturn(tpi);

        long now = System.currentTimeMillis();
        var requestBuilder = RuleEngineDeviceRpcRequest.builder()
                .tenantId(TENANT_ID)
                .deviceId(DEVICE_ID)
                .requestUUID(REQUEST_UUID)
                .method("getValue")
                .body("{}")
                .expirationTime(now + expirationOffset);
        if (deadlineOffset != null) {
            requestBuilder.ruleEngineResponseDeadline(now + deadlineOffset);
        }

        // WHEN
        tbRuleEngineRpcService.sendRpcRequestToDevice(requestBuilder.build(), response -> {});

        // THEN
        ArgumentCaptor<Long> delayCaptor = ArgumentCaptor.forClass(Long.class);
        then(schedulerMock).should().schedule(any(Runnable.class), delayCaptor.capture(), eq(TimeUnit.MILLISECONDS));
        // the delay can only be lower than expected by however long it took to reach scheduleTimeout
        assertThat(delayCaptor.getValue()).isGreaterThan(expectedDelay - 500).isLessThanOrEqualTo(expectedDelay);
    }

    private static Stream<Arguments> givenRpcRequest_whenSendRpcRequestToDevice_thenSchedulesTimeoutUsingResponseDeadline() {
        return Stream.of(
                // deadline set explicitly: waits until the deadline, plus the one second grace period
                Arguments.of(15_000L, 600_000L, 16_000L),
                // deadline omitted: falls back to the expiration time, plus the one second grace period
                Arguments.of(null, 30_000L, 31_000L)
        );
    }
}
