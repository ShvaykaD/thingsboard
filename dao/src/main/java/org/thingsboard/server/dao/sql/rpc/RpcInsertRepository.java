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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.dao.model.sql.RpcEntity;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

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

    /**
     * Batch form of the same statement. Deliberately has no transaction of its own: {@link RpcWriteRepository}
     * owns the transaction so that an insert and a later status update for one rpcId apply in the right order
     * within a single batch.
     */
    int[] insertIfAbsent(List<RpcEntity> rpcs) {
        return jdbcTemplate.batchUpdate(INSERT_IF_ABSENT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RpcEntity rpc = rpcs.get(i);
                ps.setObject(1, rpc.getUuid());
                ps.setLong(2, rpc.getCreatedTime());
                ps.setObject(3, rpc.getTenantId());
                ps.setObject(4, rpc.getDeviceId());
                ps.setLong(5, rpc.getExpirationTime());
                // The json columns take a plain String and let PostgreSQL infer jsonb, as the response column
                // already does in RpcUpdateRepository.
                ps.setString(6, JacksonUtil.toString(rpc.getRequest()));
                ps.setString(7, JacksonUtil.toString(rpc.getResponse()));
                ps.setString(8, JacksonUtil.toString(rpc.getAdditionalInfo()));
                ps.setString(9, rpc.getStatus().name());
                ps.setObject(10, rpc.getRequestId());
                ps.setObject(11, rpc.getOneway());
            }

            @Override
            public int getBatchSize() {
                return rpcs.size();
            }
        });
    }

}
