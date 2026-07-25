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
package org.thingsboard.server.common.data.rpc;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.thingsboard.server.common.data.rpc.RpcStatus.DELIVERED;
import static org.thingsboard.server.common.data.rpc.RpcStatus.QUEUED;
import static org.thingsboard.server.common.data.rpc.RpcStatus.SENT;
import static org.thingsboard.server.common.data.rpc.RpcStatus.EXPIRED;
import static org.thingsboard.server.common.data.rpc.RpcStatus.FAILED;
import static org.thingsboard.server.common.data.rpc.RpcStatus.SUCCESSFUL;
import static org.thingsboard.server.common.data.rpc.RpcStatus.DELETED;
import static org.thingsboard.server.common.data.rpc.RpcStatus.TIMEOUT;

class RpcStatusTest {

    private static final List<RpcStatus> pushDeleteNotificationToCoreStatuses = List.of(
            QUEUED,
            SENT,
            DELIVERED
    );

    @Test
    void isPushDeleteNotificationToCoreStatusTest() {
        var rpcStatuses = RpcStatus.values();
        for (var status : rpcStatuses) {
            if (pushDeleteNotificationToCoreStatuses.contains(status)) {
                assertThat(status.isPushDeleteNotificationToCore()).isTrue();
            } else {
                assertThat(status.isPushDeleteNotificationToCore()).isFalse();
            }
        }
    }

    @Test
    void allowedFromStatuses_matchesStateMachine() {
        assertThat(SENT.getAllowedFromStatuses()).containsExactlyInAnyOrder(QUEUED);
        assertThat(DELIVERED.getAllowedFromStatuses()).containsExactlyInAnyOrder(QUEUED, SENT);
        assertThat(QUEUED.getAllowedFromStatuses()).containsExactlyInAnyOrder(SENT);
        assertThat(SUCCESSFUL.getAllowedFromStatuses()).containsExactlyInAnyOrder(QUEUED, SENT, DELIVERED);
        assertThat(FAILED.getAllowedFromStatuses()).containsExactlyInAnyOrder(QUEUED, SENT, DELIVERED);
        assertThat(EXPIRED.getAllowedFromStatuses()).containsExactlyInAnyOrder(QUEUED, SENT, DELIVERED);
        assertThat(TIMEOUT.getAllowedFromStatuses()).isEmpty();
        assertThat(DELETED.getAllowedFromStatuses()).isEmpty();
    }

    @Test
    void terminalStatusesNeverAppearAsAllowedFromSource() {
        // A terminal status must never be overwritable by any transition -> it appears in no allowed-from set.
        Set<RpcStatus> terminals = Set.of(SUCCESSFUL, FAILED, EXPIRED, DELETED);
        for (RpcStatus target : RpcStatus.values()) {
            assertThat(target.getAllowedFromStatuses())
                    .as("target %s must not allow overwriting a terminal status", target)
                    .doesNotContainAnyElementsOf(terminals);
        }
    }

}
