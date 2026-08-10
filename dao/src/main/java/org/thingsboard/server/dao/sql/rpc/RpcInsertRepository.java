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

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.dao.model.sql.RpcEntity;

@Repository
@RequiredArgsConstructor
public class RpcInsertRepository {

    private static final String INSERT_IF_ABSENT =
            "INSERT INTO rpc (id, created_time, tenant_id, device_id, expiration_time, request, response, " +
            "additional_info, status, request_id, oneway) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO NOTHING;";

    private final JdbcTemplate jdbcTemplate;

    boolean insertIfAbsent(RpcEntity rpc) {
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
