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

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

public enum RpcStatus {

    QUEUED(true),
    SENT(true),
    DELIVERED(true),
    SUCCESSFUL(false),
    TIMEOUT(false),
    EXPIRED(false),
    FAILED(false),
    DELETED(false);

    @Getter
    private final boolean pushDeleteNotificationToCore;

    RpcStatus(boolean pushDeleteNotificationToCore) {
        this.pushDeleteNotificationToCore = pushDeleteNotificationToCore;
    }

    /**
     * The set of CURRENT (persisted) statuses that a guarded status UPDATE to THIS (target) status is allowed to
     * overwrite. No terminal status appears in any set, so terminals are immutable. The one-way vs two-way
     * DELIVERED distinction is enforced separately in SQL, so it is intentionally absent here.
     */
    public Set<RpcStatus> getAllowedFromStatuses() {
        return switch (this) {
            case SENT -> EnumSet.of(QUEUED);
            case DELIVERED -> EnumSet.of(QUEUED, SENT);
            case QUEUED -> EnumSet.of(SENT);                                     // retry re-queue
            case SUCCESSFUL, FAILED, EXPIRED -> EnumSet.of(QUEUED, SENT, DELIVERED); // in-flight only
            case TIMEOUT, DELETED -> EnumSet.noneOf(RpcStatus.class);            // not written via the guarded UPDATE
        };
    }

}
