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
package org.thingsboard.rule.engine.metadata;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.ListeningExecutor;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DashboardId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.dao.device.DeviceService;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TbGetOriginatorFieldsNodeTest {

    private static final EntityId DUMMY_ENTITY_ID = new DeviceId(UUID.randomUUID());
    private static final ListeningExecutor DB_EXECUTOR = new ListeningExecutor() {
        @Override
        public <T> ListenableFuture<T> executeAsync(Callable<T> task) {
            try {
                return Futures.immediateFuture(task.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void execute(@NotNull Runnable command) {
            command.run();
        }
    };
    @Mock
    private TbContext ctxMock;
    @Mock
    private DeviceService deviceService;
    private TbGetOriginatorFieldsNode node;
    private TbGetOriginatorFieldsConfiguration config;
    private TbNodeConfiguration nodeConfiguration;
    private TbMsg msg;

    @BeforeEach
    public void setUp() {
        node = new TbGetOriginatorFieldsNode();
        config = new TbGetOriginatorFieldsConfiguration().defaultConfiguration();
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));
    }

    @Test
    public void givenConfigWithNullFetchTo_whenInit_thenException() {
        // GIVEN
        config.setFetchTo(null);
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));

        // WHEN
        var exception = assertThrows(TbNodeException.class, () -> node.init(ctxMock, nodeConfiguration));

        // THEN
        assertThat(exception.getMessage()).isEqualTo("FetchTo cannot be null!");
        verify(ctxMock, never()).tellSuccess(any());
    }

    @Test
    public void givenDefaultConfig_whenInit_thenOK() throws TbNodeException {
        // GIVEN

        // WHEN
        node.init(ctxMock, nodeConfiguration);

        // THEN
        assertThat(node.config).isEqualTo(config);
        assertThat(config.getFieldsMapping()).isEqualTo(Map.of(
                "name", "originatorName",
                "type", "originatorType"));
        assertThat(config.isIgnoreNullStrings()).isEqualTo(false);
        assertThat(node.fetchTo).isEqualTo(FetchTo.METADATA);
    }

    @Test
    public void givenCustomConfig_whenInit_thenOK() throws TbNodeException {
        // GIVEN
        config.setFieldsMapping(Map.of(
                "sourceField1", "targetKey1",
                "sourceField2", "targetKey2",
                "sourceField3", "targetKey3"));
        config.setIgnoreNullStrings(true);
        config.setFetchTo(FetchTo.DATA);
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));

        // WHEN
        node.init(ctxMock, nodeConfiguration);

        // THEN
        assertThat(node.config).isEqualTo(config);
        assertThat(config.getFieldsMapping()).isEqualTo(Map.of(
                "sourceField1", "targetKey1",
                "sourceField2", "targetKey2",
                "sourceField3", "targetKey3"));
        assertThat(config.isIgnoreNullStrings()).isEqualTo(true);
        assertThat(node.fetchTo).isEqualTo(FetchTo.DATA);
    }

    @Test
    public void givenMsgDataIsNotAnJsonObjectAndFetchToData_whenOnMsg_thenException() {
        // GIVEN
        node.fetchTo = FetchTo.DATA;
        msg = TbMsg.newMsg("SOME_MESSAGE_TYPE", DUMMY_ENTITY_ID, new TbMsgMetaData(), "[]");

        // WHEN
        var exception = assertThrows(IllegalArgumentException.class, () -> node.onMsg(ctxMock, msg));

        // THEN
        assertThat(exception.getMessage()).isEqualTo("Message body is not an object!");
        verify(ctxMock, never()).tellSuccess(any());
    }

    @Test
    public void givenEntityThatDoesNotBelongToTheCurrentTenant_whenOnMsg_thenException() {
        // SETUP
        var expectedExceptionMessage = "Entity with id: '" + DUMMY_ENTITY_ID +
                "' specified in the configuration doesn't belong to the current tenant.";

        // GIVEN
        doThrow(new RuntimeException(expectedExceptionMessage)).when(ctxMock).checkTenantEntity(DUMMY_ENTITY_ID);
        msg = TbMsg.newMsg("SOME_MESSAGE_TYPE", DUMMY_ENTITY_ID, new TbMsgMetaData(), "{}");

        // WHEN
        var exception = assertThrows(RuntimeException.class, () -> node.onMsg(ctxMock, msg));

        // THEN
        assertThat(exception.getMessage()).isEqualTo(expectedExceptionMessage);
        verify(ctxMock, never()).tellSuccess(any());
    }

    @Test
    public void givenValidMsgAndFetchToData_whenOnMsg_thenShouldTellSuccessAndFetchToData() {
        // GIVEN
        var device = new Device();
        device.setId((DeviceId) DUMMY_ENTITY_ID);
        device.setName("Test device");
        device.setType("Test device type");

        config.setFieldsMapping(Map.of(
                "name", "originatorName",
                "type", "originatorType",
                "label", "originatorLabel"));
        config.setIgnoreNullStrings(true);
        config.setFetchTo(FetchTo.DATA);

        node.config = config;
        node.fetchTo = FetchTo.DATA;
        var msgMetaData = new TbMsgMetaData();
        var msgData = "{\"temp\":42,\"humidity\":77}";
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", DUMMY_ENTITY_ID, msgMetaData, msgData);

        when(ctxMock.getDeviceService()).thenReturn(deviceService);
        when(deviceService.findDeviceByIdAsync(any(), eq(device.getId()))).thenReturn(Futures.immediateFuture(device));

        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgData = "{\"temp\":42,\"humidity\":77,\"originatorName\":\"Test device\",\"originatorType\":\"Test device type\"}";

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(expectedMsgData);
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(msgMetaData);
    }

    @Test
    public void givenValidMsgAndFetchToMetaData_whenOnMsg_thenShouldTellSuccessAndFetchToMetaData() {
        // GIVEN
        var device = new Device();
        device.setId((DeviceId) DUMMY_ENTITY_ID);
        device.setName("Test device");
        device.setType("Test device type");

        config.setFieldsMapping(Map.of(
                "name", "originatorName",
                "type", "originatorType",
                "label", "originatorLabel"));
        config.setIgnoreNullStrings(true);
        config.setFetchTo(FetchTo.METADATA);

        node.config = config;
        node.fetchTo = FetchTo.METADATA;
        var msgMetaData = new TbMsgMetaData(Map.of(
                "testKey1", "testValue1",
                "testKey2", "123"));
        var msgData = "[\"value1\",\"value2\"]";
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", DUMMY_ENTITY_ID, msgMetaData, msgData);

        when(ctxMock.getDeviceService()).thenReturn(deviceService);
        when(deviceService.findDeviceByIdAsync(any(), eq(device.getId()))).thenReturn(Futures.immediateFuture(device));

        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgMetaData = new TbMsgMetaData(Map.of(
                "testKey1", "testValue1",
                "testKey2", "123",
                "originatorName", "Test device",
                "originatorType", "Test device type"
        ));

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(msgData);
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(expectedMsgMetaData);
    }

    @Test
    public void givenNullEntityFieldsAndIgnoreNullStringsFalse_whenOnMsg_thenShouldTellSuccessAndFetchNullField() {
        // GIVEN
        var device = new Device();
        device.setId((DeviceId) DUMMY_ENTITY_ID);
        device.setName("Test device");
        device.setType("Test device type");

        config.setFieldsMapping(Map.of(
                "name", "originatorName",
                "type", "originatorType",
                "label", "originatorLabel"));
        config.setIgnoreNullStrings(false);
        config.setFetchTo(FetchTo.METADATA);

        node.config = config;
        node.fetchTo = FetchTo.METADATA;
        var msgMetaData = new TbMsgMetaData(Map.of(
                "testKey1", "testValue1",
                "testKey2", "123"));
        var msgData = "[\"value1\",\"value2\"]";
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", DUMMY_ENTITY_ID, msgMetaData, msgData);

        when(ctxMock.getDeviceService()).thenReturn(deviceService);
        when(deviceService.findDeviceByIdAsync(any(), eq(device.getId()))).thenReturn(Futures.immediateFuture(device));

        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgMetaData = new TbMsgMetaData(Map.of(
                "testKey1", "testValue1",
                "testKey2", "123",
                "originatorName", "Test device",
                "originatorType", "Test device type",
                "originatorLabel", "null"
        ));

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(msgData);
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(expectedMsgMetaData);
    }

    @Test
    public void givenEmptyFieldsMapping_whenInit_thenException() {
        // GIVEN
        config = config.defaultConfiguration();
        config.setFieldsMapping(Collections.emptyMap());
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));

        // WHEN
        var exception = assertThrows(TbNodeException.class, () -> node.init(ctxMock, nodeConfiguration));

        // THEN
        assertThat(exception.getMessage()).isEqualTo("At least one field mapping should be specified!");
        verify(ctxMock, never()).tellSuccess(any());
    }

    @Test
    public void givenUnsupportedEntityType_whenOnMsg_thenShouldTellFailureWithSameMsg() {
        // GIVEN
        config.setFieldsMapping(Map.of(
                "name", "originatorName",
                "type", "originatorType",
                "label", "originatorLabel"));
        config.setIgnoreNullStrings(false);
        config.setFetchTo(FetchTo.METADATA);

        node.config = config;
        node.fetchTo = FetchTo.METADATA;
        var msgMetaData = new TbMsgMetaData(Map.of(
                "testKey1", "testValue1",
                "testKey2", "123"));
        var msgData = "[\"value1\",\"value2\"]";
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", new DashboardId(UUID.randomUUID()), msgMetaData, msgData);

        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellFailure(actualMessageCaptor.capture(), any());
        verify(ctxMock, never()).tellSuccess(any());

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(msgData);
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(msgMetaData);
    }

}
