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
package org.thingsboard.server.actors.device;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.thingsboard.common.util.LinkedHashMapRemoveEldest;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.TbActorCtx;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.rpc.Rpc;
import org.thingsboard.server.common.data.rpc.ToDeviceRpcRequestBody;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequest;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequestActorMsg;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.service.rpc.TbCoreDeviceRpcService;
import org.thingsboard.server.service.rpc.TbRpcService;
import org.thingsboard.server.service.transport.TbCoreToTransportService;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeviceActorMessageProcessorTest {

    public static final int MAX_CONCURRENT_SESSIONS_PER_DEVICE = 10;
    ActorSystemContext systemContext;
    DeviceService deviceService;
    TenantId tenantId = TenantId.SYS_TENANT_ID;
    DeviceId deviceId = DeviceId.fromString("78bf9b26-74ef-4af2-9cfb-ad6cf24ad2ec");

    DeviceActorMessageProcessor processor;

    @Before
    public void setUp() {
        systemContext = mock(ActorSystemContext.class);
        deviceService = mock(DeviceService.class);
        willReturn(MAX_CONCURRENT_SESSIONS_PER_DEVICE).given(systemContext).getMaxConcurrentSessionsPerDevice();
        willReturn(deviceService).given(systemContext).getDeviceService();
        willReturn("BURST").given(systemContext).getRpcSubmitStrategy();
        processor = new DeviceActorMessageProcessor(systemContext, tenantId, deviceId);
        willReturn(mock(TbCoreToTransportService.class)).given(systemContext).getTbCoreToTransportService();
    }

    @Test
    public void givenSystemContext_whenNewInstance_thenVerifySessionMapMaxSize() {
        assertThat(processor.sessions, instanceOf(LinkedHashMapRemoveEldest.class));
        assertThat(processor.sessions.getMaxEntries(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE));
        assertThat(processor.sessions.getRemovalConsumer(), notNullValue());
    }

    @Test
    public void givenFullSessionMap_whenSessionOverflow_thenShouldDeleteAttributeAndRPCSubscriptions() {
        //givenFullSessionMap
        for (int i = 0; i < MAX_CONCURRENT_SESSIONS_PER_DEVICE; i++) {
            UUID sessionID = UUID.randomUUID();
            processor.sessions.put(sessionID, Mockito.mock(SessionInfoMetaData.class, RETURNS_DEEP_STUBS));
            processor.attributeSubscriptions.put(sessionID, Mockito.mock(SessionInfo.class));
            processor.rpcSubscriptions.put(sessionID, Mockito.mock(SessionInfo.class));
        }
        assertThat(processor.sessions.size(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE));
        assertThat(processor.attributeSubscriptions.size(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE));
        assertThat(processor.rpcSubscriptions.size(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE));

        //add one more
        processor.sessions.put(UUID.randomUUID(), Mockito.mock(SessionInfoMetaData.class));

        assertThat(processor.sessions.size(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE));
        assertThat(processor.attributeSubscriptions.size(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE-1));
        assertThat(processor.rpcSubscriptions.size(), is(MAX_CONCURRENT_SESSIONS_PER_DEVICE-1));

    }

    @Test
    public void persistsRequestIdOnCreate() {
        TbRpcService rpcService = mock(TbRpcService.class);
        willReturn(rpcService).given(systemContext).getTbRpcService();
        willReturn(mock(TbCoreDeviceRpcService.class)).given(systemContext).getTbCoreDeviceRpcService();

        TbActorCtx ctx = mock(TbActorCtx.class);
        ToDeviceRpcRequest request = new ToDeviceRpcRequest(UUID.randomUUID(), tenantId, deviceId,
                false, System.currentTimeMillis() + 60_000, new ToDeviceRpcRequestBody("m", "{}"),
                true, null, null); // persisted=true, oneway=false
        processor.processRpcRequest(ctx, new ToDeviceRpcRequestActorMsg("svc", request));

        ArgumentCaptor<Rpc> captor = ArgumentCaptor.forClass(Rpc.class);
        verify(rpcService).create(eq(tenantId), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getRequestId()).isEqualTo(0); // first rpcSeq
    }
}