/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.server.actors.ruleChain;

import lombok.Data;
import org.thingsboard.server.actors.TbActorRef;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.rule.RuleNode;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ashvayka on 19.03.18.
 */
@Data
public final class RuleNodeCtx {
    private final TenantId tenantId;
    private final TbActorRef chainActor;
    private final TbActorRef selfActor;
    private RuleNode self;
    private boolean debugRuleNodeFailures;
    private ConcurrentMap<UUID, Runnable> failureSubscribers;

    public RuleNodeCtx(TenantId tenantId, TbActorRef chainActor, TbActorRef selfActor, RuleNode self, boolean debugRuleNodeFailures) {
        this.tenantId = tenantId;
        this.chainActor = chainActor;
        this.selfActor = selfActor;
        this.self = self;
        this.debugRuleNodeFailures = debugRuleNodeFailures;
        if (debugRuleNodeFailures) {
            failureSubscribers = new ConcurrentHashMap<>();
        }
    }

    public void subscribeForFailure(UUID msgId, Runnable onFailure) {
        if (!debugRuleNodeFailures) {
            return;
        }
        failureSubscribers.putIfAbsent(msgId, onFailure);
    }

    public void onFailure(UUID msgId) {
        Runnable action = onProcessingEnd(msgId);
        if (action != null) {
            action.run();
        }
    }

    public void onSuccess(UUID msgId) {
        onProcessingEnd(msgId);
    }

    private Runnable onProcessingEnd(UUID msgId) {
        if (!debugRuleNodeFailures) {
            return null;
        }
        return failureSubscribers.remove(msgId);
    }

}
