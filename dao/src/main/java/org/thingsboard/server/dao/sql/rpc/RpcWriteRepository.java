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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;
import org.thingsboard.server.dao.model.sql.RpcEntity;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RpcWriteRepository {

    private final RpcInsertRepository insertRepository;
    private final RpcUpdateRepository updateRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Applies one batch in a single transaction, all inserts before all updates. That ordering is the reason
     * creates and updates share one queue: a status update coalesced into the same flush as its create would
     * otherwise match no row, be reported as non-persisted, and leave a stranded QUEUED row behind.
     * <p>
     * Deliberately not built on {@code AbstractVersionedInsertRepository}: that base updates first and then
     * inserts any row whose update matched nothing, which would resurrect an RPC deleted in the meantime - the
     * bug {@code saveAsyncUpdateForDeletedRpcDoesNotResurrect} guards. It also reports version numbers where this
     * path needs a per-row boolean.
     * <p>
     * Results are returned positionally against {@code writes}, not against the execution order. Each row's own
     * affected-row count decides its result: an insert that conflicted reports false, which is how a
     * redelivered command is recognised as a duplicate rather than treated as a fresh create.
     */
    List<Boolean> write(List<RpcWrite> writes) {
        return transactionTemplate.execute(status -> {
            List<RpcEntity> inserts = new ArrayList<>();
            List<RpcEntity> updates = new ArrayList<>();
            for (RpcWrite write : writes) {
                (write.op() == RpcWrite.Op.INSERT ? inserts : updates).add(write.entity());
            }

            int[] insertCounts = inserts.isEmpty() ? new int[0] : insertRepository.insertIfAbsent(inserts);
            int[] updateCounts = updates.isEmpty() ? new int[0] : updateRepository.update(updates);

            List<Boolean> persisted = new ArrayList<>(writes.size());
            int insertIdx = 0;
            int updateIdx = 0;
            for (RpcWrite write : writes) {
                persisted.add(write.op() == RpcWrite.Op.INSERT
                        ? insertCounts[insertIdx++] > 0
                        : updateCounts[updateIdx++] > 0);
            }
            return persisted;
        });
    }

}
