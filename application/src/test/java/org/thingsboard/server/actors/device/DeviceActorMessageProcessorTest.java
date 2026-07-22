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
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.LinkedHashMapRemoveEldest;
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.actors.TbActorCtx;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.RpcId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.rpc.Rpc;
import org.thingsboard.server.common.data.rpc.RpcStatus;
import org.thingsboard.server.common.data.rpc.ToDeviceRpcRequestBody;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequest;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequestActorMsg;
import org.thingsboard.server.dao.device.DeviceService;
import org.thingsboard.server.gen.transport.TransportProtos.SessionInfoProto;
import org.thingsboard.server.gen.transport.TransportProtos.ToDeviceRpcResponseMsg;
import org.thingsboard.server.service.rpc.TbCoreDeviceRpcService;
import org.thingsboard.server.service.rpc.TbRpcService;
import org.thingsboard.server.service.transport.TbCoreToTransportService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    public void reloadedDeliveredRpcMatchesDeviceResponse() {
        TbRpcService rpcService = mock(TbRpcService.class);
        willReturn(rpcService).given(systemContext).getTbRpcService();
        TbCoreDeviceRpcService coreRpc = mock(TbCoreDeviceRpcService.class);
        willReturn(coreRpc).given(systemContext).getTbCoreDeviceRpcService();
        willReturn("svc").given(systemContext).getServiceId();

        UUID rpcUuid = UUID.randomUUID();
        ToDeviceRpcRequest req = new ToDeviceRpcRequest(rpcUuid, tenantId, deviceId, false,
                System.currentTimeMillis() + 60_000, new ToDeviceRpcRequestBody("m", "{}"), true, null, null);
        Rpc row = new Rpc(new RpcId(rpcUuid));
        row.setCreatedTime(System.currentTimeMillis());
        row.setExpirationTime(System.currentTimeMillis() + 60_000);
        row.setStatus(RpcStatus.DELIVERED);
        row.setRequestId(7);
        row.setRequest(JacksonUtil.valueToTree(req));

        // QUEUED/SENT empty, DELIVERED returns our row:
        willReturn(new PageData<>(List.of(), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.QUEUED), any());
        willReturn(new PageData<>(List.of(), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.SENT), any());
        willReturn(new PageData<>(List.of(row), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.DELIVERED), any());

        TbActorCtx ctx = mock(TbActorCtx.class);
        processor.init(ctx);

        // device replies with the OLD id 7:
        processor.processRpcResponses(sessionInfoProto(), ToDeviceRpcResponseMsg.newBuilder()
                .setRequestId(7).setPayload("{\"ok\":true}").build());

        // matched → row updated to SUCCESSFUL (not "stale"):
        ArgumentCaptor<Rpc> captor = ArgumentCaptor.forClass(Rpc.class);
        verify(rpcService).update(eq(tenantId), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(RpcStatus.SUCCESSFUL);
    }

    @Test
    public void oneWayDeliveredRowNotReExpiredOnReload() {
        TbRpcService rpcService = mock(TbRpcService.class);
        willReturn(rpcService).given(systemContext).getTbRpcService();
        willReturn(mock(TbCoreDeviceRpcService.class)).given(systemContext).getTbCoreDeviceRpcService();
        willReturn("svc").given(systemContext).getServiceId();

        UUID rpcUuid = UUID.randomUUID();
        long pastExp = System.currentTimeMillis() - 60_000; // already expired
        ToDeviceRpcRequest req = new ToDeviceRpcRequest(rpcUuid, tenantId, deviceId, true /*oneway*/,
                pastExp, new ToDeviceRpcRequestBody("m", "{}"), true, null, null);
        Rpc row = new Rpc(new RpcId(rpcUuid));
        row.setCreatedTime(System.currentTimeMillis() - 120_000);
        row.setExpirationTime(pastExp);
        row.setStatus(RpcStatus.DELIVERED);
        row.setRequestId(9);
        row.setRequest(JacksonUtil.valueToTree(req));

        // QUEUED/SENT empty, DELIVERED returns our past-due one-way row:
        willReturn(new PageData<>(List.of(), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.QUEUED), any());
        willReturn(new PageData<>(List.of(), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.SENT), any());
        willReturn(new PageData<>(List.of(row), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.DELIVERED), any());

        TbActorCtx ctx = mock(TbActorCtx.class);
        processor.init(ctx);

        // terminal one-way DELIVERED row, past expiry: must be left untouched, no EXPIRED overwrite
        Mockito.verify(rpcService, Mockito.never()).update(any(), any());
    }

    @Test
    public void seedsCounterPastHighestReloadedId() {
        TbRpcService rpcService = mock(TbRpcService.class);
        willReturn(rpcService).given(systemContext).getTbRpcService();
        willReturn(mock(TbCoreDeviceRpcService.class)).given(systemContext).getTbCoreDeviceRpcService();
        willReturn("svc").given(systemContext).getServiceId();

        Rpc row = new Rpc(new RpcId(UUID.randomUUID()));
        row.setCreatedTime(System.currentTimeMillis());
        row.setExpirationTime(System.currentTimeMillis() + 60_000);
        row.setStatus(RpcStatus.SENT);
        row.setRequestId(5);
        row.setRequest(JacksonUtil.valueToTree(new ToDeviceRpcRequest(row.getUuidId(), tenantId, deviceId,
                false, row.getExpirationTime(), new ToDeviceRpcRequestBody("m", "{}"), true, null, null)));
        stubReload(rpcService, RpcStatus.QUEUED); // empty
        stubReload(rpcService, RpcStatus.DELIVERED); // empty
        willReturn(new PageData<>(List.of(row), 1, 0, false)).given(rpcService)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(RpcStatus.SENT), any());

        TbActorCtx ctx = mock(TbActorCtx.class);
        processor.init(ctx);

        // next brand-new persistent RPC must get id 6, not 0:
        ToDeviceRpcRequest req = new ToDeviceRpcRequest(UUID.randomUUID(), tenantId, deviceId, false,
                System.currentTimeMillis() + 60_000, new ToDeviceRpcRequestBody("m", "{}"), true, null, null);
        processor.processRpcRequest(ctx, new ToDeviceRpcRequestActorMsg("svc", req));

        ArgumentCaptor<Rpc> captor = ArgumentCaptor.forClass(Rpc.class);
        verify(rpcService).create(eq(tenantId), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getRequestId()).isEqualTo(6);
    }

    private void stubReload(TbRpcService s, RpcStatus st) {
        willReturn(new PageData<>(List.of(), 1, 0, false)).given(s)
                .findAllByDeviceIdAndStatus(eq(tenantId), eq(deviceId), eq(st), any());
    }

    private SessionInfoProto sessionInfoProto() {
        UUID sid = UUID.randomUUID();
        return SessionInfoProto.newBuilder()
                .setNodeId("svc")
                .setSessionIdMSB(sid.getMostSignificantBits())
                .setSessionIdLSB(sid.getLeastSignificantBits())
                .build();
    }
}