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
package org.thingsboard.server.service.install.lts;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.AbstractControllerTest;
import org.thingsboard.server.dao.service.DaoSqlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Drives {@link V4_3_1_4Migration#applyAfterCommit()} against a real Postgres instance with a batch size far
 * smaller than the legacy backlog seeded here, so the multi-batch keyset-pagination loop (cursor advance, guard,
 * termination) actually exercises more than one window -- not just the single-batch happy path.
 */
@DaoSqlTest
public class V4_3_1_4MigrationIntegrationTest extends AbstractControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<UUID> seededIds = new ArrayList<>();

    @After
    public void tearDown() {
        if (!seededIds.isEmpty()) {
            jdbcTemplate.batchUpdate("DELETE FROM rpc WHERE id = ?",
                    seededIds.stream().map(id -> new Object[]{id}).toList());
        }
    }

    @Test
    public void batchLoopClosesAllStuckRowsAcrossMultipleWindowsAndLeavesOthersUntouched() {
        V4_3_1_4Migration migration = new V4_3_1_4Migration(jdbcTemplate, transactionManager);
        // Force a tiny batch size (< the 5 stuck rows seeded below) so applyAfterCommit() must run several
        // keyset-paginated windows, proving the cursor actually advances across batches instead of just once.
        ReflectionTestUtils.setField(migration, "batchSize", 2);

        DeviceId deviceId = new DeviceId(UUID.randomUUID());
        long now = System.currentTimeMillis();
        long past = now - 60_000;
        long future = now + 60_000;

        // Legacy (request_id IS NULL) rows the backfill MUST close -- 5 rows against a batch size of 2, so the
        // loop must span 3 windows (2 + 2 + 1) for the cursor logic to be genuinely exercised.
        List<UUID> stuck = List.of(
                saveLegacy(deviceId, "DELIVERED", false, past, null),
                saveLegacy(deviceId, "DELIVERED", false, past, null),
                saveLegacy(deviceId, "DELIVERED", false, past, null),
                saveLegacy(deviceId, "DELIVERED", false, past, null),
                saveLegacy(deviceId, "SENT", true, past, null));

        // Rows the backfill MUST leave untouched.
        UUID oneWayDeliveredPastExpiry = saveLegacy(deviceId, "DELIVERED", true, past, null); // terminal success
        UUID twoWayDeliveredFutureExpiry = saveLegacy(deviceId, "DELIVERED", false, future, null); // not expired yet
        UUID twoWayDeliveredWithRequestId = saveLegacy(deviceId, "DELIVERED", false, past, 7); // tracked, not legacy
        UUID queuedPastExpiry = saveLegacy(deviceId, "QUEUED", false, past, null); // never sent

        migration.applyAfterCommit();

        for (UUID id : stuck) {
            assertEquals("EXPIRED", statusOf(id));
            assertNotNull(responseOf(id));
        }

        assertEquals("DELIVERED", statusOf(oneWayDeliveredPastExpiry));
        assertEquals("DELIVERED", statusOf(twoWayDeliveredFutureExpiry));
        assertEquals("DELIVERED", statusOf(twoWayDeliveredWithRequestId));
        assertEquals("QUEUED", statusOf(queuedPastExpiry));
    }

    private String statusOf(UUID id) {
        return jdbcTemplate.queryForObject("SELECT status FROM rpc WHERE id = ?", String.class, id);
    }

    private String responseOf(UUID id) {
        return jdbcTemplate.queryForObject("SELECT response FROM rpc WHERE id = ?", String.class, id);
    }

    // Seeds a legacy row (bypassing the entity/JPA layer to write raw request JSON) exactly as a pre-request_id
    // server version would have left it: request_id NULL, request JSON carrying the oneway flag the migration's
    // CLEANUP_BATCH_SQL extracts via request::jsonb ->> 'oneway'.
    private UUID saveLegacy(DeviceId deviceId, String status, boolean oneway, long expirationTime, Integer requestId) {
        UUID id = UUID.randomUUID();
        seededIds.add(id);
        String request = "{\"oneway\":" + oneway + ",\"method\":\"x\"}";
        jdbcTemplate.update("INSERT INTO rpc (id, created_time, tenant_id, device_id, expiration_time, request, " +
                        "response, status, request_id, oneway) VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?, ?)",
                id, System.currentTimeMillis(), TenantId.SYS_TENANT_ID.getId(), deviceId.getId(), expirationTime,
                request, status, requestId, oneway);
        return id;
    }
}
