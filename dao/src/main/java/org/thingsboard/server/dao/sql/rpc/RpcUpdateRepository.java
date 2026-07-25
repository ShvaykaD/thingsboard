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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.dao.model.sql.RpcEntity;
import org.thingsboard.server.dao.sqlts.insert.AbstractInsertRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RpcUpdateRepository extends AbstractInsertRepository {

    private static final String UPDATE =
            "UPDATE rpc SET status = ?, response = COALESCE(?, response) " +
            "WHERE id = ? AND status = ANY(?) AND NOT (status = 'DELIVERED' AND COALESCE(oneway, FALSE) = TRUE);";

    List<Boolean> update(List<RpcEntity> updates) {
        return transactionTemplate.execute(status -> {
            int[] updateCounts = jdbcTemplate.batchUpdate(UPDATE, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    RpcEntity rpc = updates.get(i);
                    ps.setString(1, rpc.getStatus().name());
                    ps.setString(2, toJsonStr(rpc.getResponse()));
                    ps.setObject(3, rpc.getUuid());
                    String[] allowedFrom = rpc.getStatus().getAllowedFromStatuses().stream()
                            .map(Enum::name).toArray(String[]::new);
                    ps.setArray(4, ps.getConnection().createArrayOf("varchar", allowedFrom));
                }

                @Override
                public int getBatchSize() {
                    return updates.size();
                }
            });

            List<Boolean> persisted = new ArrayList<>(updateCounts.length);
            for (int updateCount : updateCounts) {
                persisted.add(updateCount > 0);
            }
            return persisted;
        });
    }

    private String toJsonStr(JsonNode node) {
        return node == null ? null : replaceNullChars(JacksonUtil.toString(node));
    }
}
