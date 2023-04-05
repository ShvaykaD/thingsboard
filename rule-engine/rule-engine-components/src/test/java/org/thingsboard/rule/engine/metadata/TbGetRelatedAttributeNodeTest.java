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
import org.thingsboard.rule.engine.data.RelationsQuery;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.BaseAttributeKvEntry;
import org.thingsboard.server.common.data.kv.BasicTsKvEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;
import org.thingsboard.server.common.data.relation.RelationEntityTypeFilter;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.dao.attributes.AttributesService;
import org.thingsboard.server.dao.relation.RelationService;
import org.thingsboard.server.dao.timeseries.TimeseriesService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.thingsboard.server.common.data.DataConstants.SERVER_SCOPE;

@ExtendWith(MockitoExtension.class)
public class TbGetRelatedAttributeNodeTest {

    private static final EntityId DUMMY_ENTITY_ID = new DeviceId(UUID.randomUUID());
    private static final TenantId TENANT_ID = new TenantId(UUID.randomUUID());
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
    private AttributesService attributesServiceMock;
    @Mock
    private TimeseriesService timeseriesServiceMock;
    @Mock
    private RelationService relationServiceMock;
    private TbGetRelatedAttributeNode node;
    private TbGetRelatedAttrNodeConfiguration config;
    private TbNodeConfiguration nodeConfiguration;
    private EntityRelation entityRelation;
    private TbMsg msg;

    @BeforeEach
    public void setUp() {
        node = new TbGetRelatedAttributeNode();
        config = new TbGetRelatedAttrNodeConfiguration().defaultConfiguration();
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));
        entityRelation = new EntityRelation();
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
        var nodeConfig = (TbGetRelatedAttrNodeConfiguration) node.config;
        assertThat(nodeConfig).isEqualTo(config);
        assertThat(nodeConfig.getAttrMapping()).isEqualTo(Map.of("serialNumber", "sn"));
        assertThat(nodeConfig.isTelemetry()).isEqualTo(false);
        assertThat(node.fetchTo).isEqualTo(FetchTo.METADATA);

        var relationsQuery = new RelationsQuery();
        var relationEntityTypeFilter = new RelationEntityTypeFilter(EntityRelation.CONTAINS_TYPE, Collections.emptyList());
        relationsQuery.setDirection(EntitySearchDirection.FROM);
        relationsQuery.setMaxLevel(1);
        relationsQuery.setFilters(Collections.singletonList(relationEntityTypeFilter));

        assertThat(nodeConfig.getRelationsQuery()).isEqualTo(relationsQuery);
    }

    @Test
    public void givenCustomConfig_whenInit_thenOK() throws TbNodeException {
        // GIVEN
        config.setAttrMapping(Map.of(
                "sourceAttr1", "targetKey1",
                "sourceAttr2", "targetKey2",
                "sourceAttr3", "targetKey3"));
        config.setTelemetry(true);
        config.setFetchTo(FetchTo.DATA);

        var relationsQuery = new RelationsQuery();
        var relationEntityTypeFilter = new RelationEntityTypeFilter(EntityRelation.CONTAINS_TYPE, Collections.emptyList());
        relationsQuery.setDirection(EntitySearchDirection.FROM);
        relationsQuery.setMaxLevel(1);
        relationsQuery.setFilters(Collections.singletonList(relationEntityTypeFilter));

        config.setRelationsQuery(relationsQuery);
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));

        // WHEN
        node.init(ctxMock, nodeConfiguration);

        // THEN
        var nodeConfig = (TbGetRelatedAttrNodeConfiguration) node.config;
        assertThat(nodeConfig).isEqualTo(config);
        assertThat(nodeConfig.getAttrMapping()).isEqualTo(Map.of(
                "sourceAttr1", "targetKey1",
                "sourceAttr2", "targetKey2",
                "sourceAttr3", "targetKey3"
        ));
        assertThat(nodeConfig.isTelemetry()).isEqualTo(true);
        assertThat(node.fetchTo).isEqualTo(FetchTo.DATA);
        assertThat(nodeConfig.getRelationsQuery()).isEqualTo(relationsQuery);
    }

    @Test
    public void givenEmptyAttributesMapping_whenInit_thenException() {
        // SETUP
        var expectedExceptionMessage = "At least one attribute mapping should be specified!";

        // GIVEN
        config.setAttrMapping(Collections.emptyMap());
        nodeConfiguration = new TbNodeConfiguration(JacksonUtil.valueToTree(config));

        // WHEN
        var exception = assertThrows(TbNodeException.class, () -> node.init(ctxMock, nodeConfiguration));

        // THEN
        assertThat(exception.getMessage()).isEqualTo(expectedExceptionMessage);
        verify(ctxMock, never()).tellSuccess(any());
    }

    @Test
    public void givenMsgDataIsNotAnJsonObjectAndFetchToData_whenOnMsg_thenException() {
        // GIVEN
        node.fetchTo = FetchTo.DATA;
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", DUMMY_ENTITY_ID, new TbMsgMetaData(), "[]");

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
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", DUMMY_ENTITY_ID, new TbMsgMetaData(), "{}");

        // WHEN
        var exception = assertThrows(RuntimeException.class, () -> node.onMsg(ctxMock, msg));

        // THEN
        assertThat(exception.getMessage()).isEqualTo(expectedExceptionMessage);
        verify(ctxMock, never()).tellSuccess(any());
    }

    @Test
    public void givenDidNotFindEntity_whenOnMsg_thenShouldTellFailure() {
        // GIVEN
        prepareMsgAndConfig(FetchTo.METADATA, false, DUMMY_ENTITY_ID);

        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);
        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        doReturn(Futures.immediateFuture(null)).when(relationServiceMock).findByQuery(any(), any());

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        var actualExceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(ctxMock, never()).tellSuccess(any());
        verify(ctxMock, times(1))
                .tellFailure(actualMessageCaptor.capture(), actualExceptionCaptor.capture());

        var actualMessage = actualMessageCaptor.getValue();
        var actualException = actualExceptionCaptor.getValue();

        var expectedExceptionMessage = "Failed to find related entity to message originator using relation query specified in the configuration!";

        assertEquals(msg, actualMessage);
        assertEquals(expectedExceptionMessage, actualException.getMessage());
        assertInstanceOf(NoSuchElementException.class, actualException);
    }

    @Test
    public void givenFetchAttributesToData_whenOnMsg_thenShouldFetchAttributesToData() {
        // GIVEN
        var customer = new Customer(new CustomerId(UUID.randomUUID()));
        var user = new User(new UserId(UUID.randomUUID()));

        prepareMsgAndConfig(FetchTo.DATA, false, user.getId());

        entityRelation.setFrom(user.getId());
        entityRelation.setTo(customer.getId());
        entityRelation.setType(EntityRelation.CONTAINS_TYPE);

        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        doReturn(Futures.immediateFuture(List.of(entityRelation))).when(relationServiceMock).findByQuery(eq(TENANT_ID), any());

        when(ctxMock.getTenantId()).thenReturn(TENANT_ID);
        when(ctxMock.getAttributesService()).thenReturn(attributesServiceMock);

        List<AttributeKvEntry> attributes = List.of(
                new BaseAttributeKvEntry(new StringDataEntry("sourceKey1", "sourceValue1"), 1L),
                new BaseAttributeKvEntry(new StringDataEntry("sourceKey2", "sourceValue2"), 2L),
                new BaseAttributeKvEntry(new StringDataEntry("sourceKey3", "sourceValue3"), 3L)
        );
        when(attributesServiceMock.find(eq(TENANT_ID), eq(customer.getId()), eq(SERVER_SCOPE), anyList()))
                .thenReturn(Futures.immediateFuture(attributes));
        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgData = "{\"temp\":42," +
                "\"humidity\":77," +
                "\"messageBodyPattern1\":\"targetKey2\"," +
                "\"messageBodyPattern2\":\"sourceKey3\"," +
                "\"targetKey1\":\"sourceValue1\"," +
                "\"targetKey2\":\"sourceValue2\"," +
                "\"targetKey3\":\"sourceValue3\"}";

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(expectedMsgData);
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(msg.getMetaData());
    }

    @Test
    public void givenFetchAttributesToMetaData_whenOnMsg_thenShouldFetchAttributesToMetaData() {
        // GIVEN
        var customer = new Customer(new CustomerId(UUID.randomUUID()));

        prepareMsgAndConfig(FetchTo.METADATA, false, customer.getId());

        entityRelation.setFrom(customer.getId());
        entityRelation.setTo(customer.getId());
        entityRelation.setType(EntityRelation.CONTAINS_TYPE);

        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        doReturn(Futures.immediateFuture(List.of(entityRelation))).when(relationServiceMock).findByQuery(eq(TENANT_ID), any());

        when(ctxMock.getTenantId()).thenReturn(TENANT_ID);
        when(ctxMock.getAttributesService()).thenReturn(attributesServiceMock);
        List<AttributeKvEntry> attributes = List.of(
                new BaseAttributeKvEntry(new StringDataEntry("sourceKey1", "sourceValue1"), 1L),
                new BaseAttributeKvEntry(new StringDataEntry("sourceKey2", "sourceValue2"), 2L),
                new BaseAttributeKvEntry(new StringDataEntry("sourceKey3", "sourceValue3"), 3L)
        );
        when(attributesServiceMock.find(eq(TENANT_ID), eq(customer.getId()), eq(SERVER_SCOPE), anyList()))
                .thenReturn(Futures.immediateFuture(attributes));
        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgMetaData = new TbMsgMetaData(Map.of(
                "metaDataPattern1", "sourceKey2",
                "metaDataPattern2", "targetKey3",
                "targetKey1", "sourceValue1",
                "targetKey2", "sourceValue2",
                "targetKey3", "sourceValue3"
        ));

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(msg.getData());
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(expectedMsgMetaData);
    }

    @Test
    public void givenFetchTelemetryToData_whenOnMsg_thenShouldFetchTelemetryToData() {
        // GIVEN
        var customer = new Customer(new CustomerId(UUID.randomUUID()));
        var asset = new Asset(new AssetId(UUID.randomUUID()));

        prepareMsgAndConfig(FetchTo.DATA, true, asset.getId());

        entityRelation.setFrom(asset.getId());
        entityRelation.setTo(customer.getId());
        entityRelation.setType(EntityRelation.CONTAINS_TYPE);

        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        doReturn(Futures.immediateFuture(List.of(entityRelation))).when(relationServiceMock).findByQuery(eq(TENANT_ID), any());

        when(ctxMock.getTenantId()).thenReturn(TENANT_ID);
        when(ctxMock.getTimeseriesService()).thenReturn(timeseriesServiceMock);
        List<TsKvEntry> timeseries = List.of(
                new BasicTsKvEntry(1L, new StringDataEntry("sourceKey1", "sourceValue1")),
                new BasicTsKvEntry(1L, new StringDataEntry("sourceKey2", "sourceValue2")),
                new BasicTsKvEntry(1L, new StringDataEntry("sourceKey3", "sourceValue3"))
        );
        when(timeseriesServiceMock.findLatest(eq(TENANT_ID), eq(customer.getId()), anyList()))
                .thenReturn(Futures.immediateFuture(timeseries));
        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgData = "{\"temp\":42," +
                "\"humidity\":77," +
                "\"messageBodyPattern1\":\"targetKey2\"," +
                "\"messageBodyPattern2\":\"sourceKey3\"," +
                "\"targetKey1\":\"sourceValue1\"," +
                "\"targetKey2\":\"sourceValue2\"," +
                "\"targetKey3\":\"sourceValue3\"}";

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(expectedMsgData);
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(msg.getMetaData());
    }

    @Test
    public void givenFetchTelemetryToMetaData_whenOnMsg_thenShouldFetchTelemetryToMetaData() {
        // GIVEN
        var customer = new Customer(new CustomerId(UUID.randomUUID()));
        var device = new Device(new DeviceId(UUID.randomUUID()));

        prepareMsgAndConfig(FetchTo.METADATA, true, device.getId());

        entityRelation.setFrom(device.getId());
        entityRelation.setTo(customer.getId());
        entityRelation.setType(EntityRelation.CONTAINS_TYPE);

        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        doReturn(Futures.immediateFuture(List.of(entityRelation))).when(relationServiceMock).findByQuery(eq(TENANT_ID), any());

        when(ctxMock.getTenantId()).thenReturn(TENANT_ID);
        when(ctxMock.getTimeseriesService()).thenReturn(timeseriesServiceMock);
        List<TsKvEntry> timeseries = List.of(
                new BasicTsKvEntry(1L, new StringDataEntry("sourceKey1", "sourceValue1")),
                new BasicTsKvEntry(1L, new StringDataEntry("sourceKey2", "sourceValue2")),
                new BasicTsKvEntry(1L, new StringDataEntry("sourceKey3", "sourceValue3"))
        );
        when(timeseriesServiceMock.findLatest(eq(TENANT_ID), eq(customer.getId()), anyList()))
                .thenReturn(Futures.immediateFuture(timeseries));
        when(ctxMock.getDbCallbackExecutor()).thenReturn(DB_EXECUTOR);

        // WHEN
        node.onMsg(ctxMock, msg);

        // THEN
        var actualMessageCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctxMock, times(1)).tellSuccess(actualMessageCaptor.capture());
        verify(ctxMock, never()).tellFailure(any(), any());

        var expectedMsgMetaData = new TbMsgMetaData(Map.of(
                "metaDataPattern1", "sourceKey2",
                "metaDataPattern2", "targetKey3",
                "targetKey1", "sourceValue1",
                "targetKey2", "sourceValue2",
                "targetKey3", "sourceValue3"
        ));

        assertThat(actualMessageCaptor.getValue().getData()).isEqualTo(msg.getData());
        assertThat(actualMessageCaptor.getValue().getMetaData()).isEqualTo(expectedMsgMetaData);
    }

    private void prepareMsgAndConfig(FetchTo fetchTo, boolean isTelemetry, EntityId entityId) {
        config.setAttrMapping(Map.of(
                "sourceKey1", "targetKey1",
                "${metaDataPattern1}", "$[messageBodyPattern1]",
                "$[messageBodyPattern2]", "${metaDataPattern2}"));
        config.setTelemetry(isTelemetry);
        config.setFetchTo(fetchTo);

        node.config = config;
        node.fetchTo = fetchTo;
        var msgMetaData = new TbMsgMetaData();
        msgMetaData.putValue("metaDataPattern1", "sourceKey2");
        msgMetaData.putValue("metaDataPattern2", "targetKey3");
        var msgData = "{\"temp\":42,\"humidity\":77,\"messageBodyPattern1\":\"targetKey2\",\"messageBodyPattern2\":\"sourceKey3\"}";
        msg = TbMsg.newMsg("POST_TELEMETRY_REQUEST", entityId, msgMetaData, msgData);
    }

}
