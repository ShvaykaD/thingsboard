package org.thingsboard.rule.engine.util;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.data.RelationsQuery;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntityRelationsQuery;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;
import org.thingsboard.server.common.data.relation.RelationEntityTypeFilter;
import org.thingsboard.server.common.data.relation.RelationsSearchParameters;
import org.thingsboard.server.dao.relation.RelationService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntitiesRelatedEntitiesIdAsyncLoaderTest {

    private static final EntityId DUMMY_ORIGINATOR = new DeviceId(UUID.randomUUID());
    private static final TenantId TENANT_ID = new TenantId(UUID.randomUUID());
    @Mock
    private TbContext ctxMock;
    @Mock
    private RelationService relationServiceMock;

    @Test
    public void givenRelationsQuery_whenFindEntityAsync_ShouldBuildCorrectEntityRelationsQuery() {
        // GIVEN
        var relationsQuery = new RelationsQuery();
        var relationEntityTypeFilter = new RelationEntityTypeFilter(EntityRelation.CONTAINS_TYPE, Collections.emptyList());
        relationsQuery.setDirection(EntitySearchDirection.FROM);
        relationsQuery.setMaxLevel(1);
        relationsQuery.setFilters(Collections.singletonList(relationEntityTypeFilter));

        var expectedEntityRelationsQuery = new EntityRelationsQuery();
        var parameters = new RelationsSearchParameters(
                DUMMY_ORIGINATOR,
                relationsQuery.getDirection(),
                relationsQuery.getMaxLevel(),
                relationsQuery.isFetchLastLevelOnly()
        );
        expectedEntityRelationsQuery.setParameters(parameters);
        expectedEntityRelationsQuery.setFilters(relationsQuery.getFilters());

        when(ctxMock.getTenantId()).thenReturn(TENANT_ID);
        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        when(relationServiceMock.findByQuery(eq(TENANT_ID), eq(expectedEntityRelationsQuery)))
                .thenReturn(Futures.immediateFuture(null));

        // WHEN
        EntitiesRelatedEntityIdAsyncLoader.findEntityAsync(ctxMock, DUMMY_ORIGINATOR, relationsQuery);

        // THEN
        verify(relationServiceMock, times(1)).findByQuery(eq(TENANT_ID), eq(expectedEntityRelationsQuery));
    }

    @Test
    public void givenSeveralEntitiesFound_whenFindEntityAsync_ShouldKeepOneAndDiscardOthers() throws Exception {
        // GIVEN
        var relationsQuery = new RelationsQuery();
        var relationEntityTypeFilter = new RelationEntityTypeFilter(
                EntityRelation.CONTAINS_TYPE,
                List.of(EntityType.DEVICE, EntityType.ASSET)
        );
        relationsQuery.setDirection(EntitySearchDirection.FROM);
        relationsQuery.setMaxLevel(2);
        relationsQuery.setFilters(Collections.singletonList(relationEntityTypeFilter));

        var expectedEntityRelationsQuery = new EntityRelationsQuery();
        var parameters = new RelationsSearchParameters(
                DUMMY_ORIGINATOR,
                relationsQuery.getDirection(),
                relationsQuery.getMaxLevel(),
                relationsQuery.isFetchLastLevelOnly()
        );
        expectedEntityRelationsQuery.setParameters(parameters);
        expectedEntityRelationsQuery.setFilters(relationsQuery.getFilters());

        var device1 = new Device(new DeviceId(UUID.randomUUID()));
        device1.setName("Device 1");
        var device2 = new Device(new DeviceId(UUID.randomUUID()));
        device1.setName("Device 2");
        var asset = new Asset(new AssetId(UUID.randomUUID()));
        asset.setName("Asset");

        var entityRelationDevice1 = new EntityRelation();
        entityRelationDevice1.setFrom(DUMMY_ORIGINATOR);
        entityRelationDevice1.setTo(device1.getId());
        entityRelationDevice1.setType(EntityRelation.CONTAINS_TYPE);

        var entityRelationDevice2 = new EntityRelation();
        entityRelationDevice2.setFrom(DUMMY_ORIGINATOR);
        entityRelationDevice2.setTo(device2.getId());
        entityRelationDevice2.setType(EntityRelation.CONTAINS_TYPE);

        var entityRelationAsset = new EntityRelation();
        entityRelationAsset.setFrom(DUMMY_ORIGINATOR);
        entityRelationAsset.setTo(asset.getId());
        entityRelationAsset.setType(EntityRelation.CONTAINS_TYPE);

        var expectedEntityRelationsList = List.of(entityRelationDevice1, entityRelationDevice2, entityRelationAsset);

        when(ctxMock.getTenantId()).thenReturn(TENANT_ID);
        when(ctxMock.getRelationService()).thenReturn(relationServiceMock);
        when(relationServiceMock.findByQuery(eq(TENANT_ID), eq(expectedEntityRelationsQuery)))
                .thenReturn(Futures.immediateFuture(expectedEntityRelationsList));

        // WHEN
        var deviceIdFuture = EntitiesRelatedEntityIdAsyncLoader.findEntityAsync(ctxMock, DUMMY_ORIGINATOR, relationsQuery);

        // THEN
        assertNotNull(deviceIdFuture);

        var actualDeviceId = deviceIdFuture.get();
        assertNotNull(actualDeviceId);
        assertEquals(device1.getId(), actualDeviceId);
    }

}
