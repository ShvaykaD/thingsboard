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
package org.thingsboard.server.dao.sql.rpc;

import org.springframework.stereotype.Repository;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.dao.model.sql.RpcEntity;
import org.thingsboard.server.dao.sqlts.insert.AbstractInsertRepository;

@Repository
public class RpcInsertRepository extends AbstractInsertRepository {

    // The create path must never overwrite an existing row: a re-delivered command has to be a no-op, not a
    // re-create. This is deliberately STRICTER than the guarded UPDATE in RpcUpdateRepository, which does allow
    // SENT/TIMEOUT -> QUEUED (the retry re-queue). Do not "unify" the two guards.
    // DO NOTHING rather than DO UPDATE keeps the hot path free of a row lock, and this single statement replaces
    // the Hibernate merge's SELECT + INSERT/UPDATE pair.
    private static final String INSERT_IF_ABSENT =
            "INSERT INTO rpc (id, created_time, tenant_id, device_id, expiration_time, request, response, " +
            "additional_info, status, request_id, oneway) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO NOTHING;";

    // Named save() to match EventInsertRepository / EdgeEventInsertRepository, the codebase's other
    // ON CONFLICT DO NOTHING repositories. Unlike those two it returns a result: the create path needs to know
    // whether this was a real insert or a duplicate delivery.
    boolean save(RpcEntity rpc) {
        return jdbcTemplate.update(INSERT_IF_ABSENT,
                rpc.getUuid(),
                rpc.getCreatedTime(),
                rpc.getTenantId(),
                rpc.getDeviceId(),
                rpc.getExpirationTime(),
                JacksonUtil.toString(rpc.getRequest()),
                JacksonUtil.toString(rpc.getResponse()),
                JacksonUtil.toString(rpc.getAdditionalInfo()),
                rpc.getStatus().name(),
                rpc.getRequestId(),
                rpc.getOneway()) > 0;
    }

}
