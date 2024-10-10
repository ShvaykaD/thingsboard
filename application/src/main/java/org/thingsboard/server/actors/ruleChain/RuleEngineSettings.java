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
import org.thingsboard.server.actors.ActorSystemContext;
import org.thingsboard.server.common.data.TenantProfile;
import org.thingsboard.server.common.data.tenant.profile.TenantProfileConfiguration;

@Data
public class RuleEngineSettings {

    private final int maxRuleNodeExecPerMsg;
    private final int debugModeDurationMinutes;
    
    public static RuleEngineSettings fromTenantProfileOrElseSystemSettings(ActorSystemContext systemContext, TenantProfile tenantProfile) {
        var tenantProfileConfiguration = tenantProfile.getProfileData().getConfiguration();
        return new RuleEngineSettings(tenantProfileConfiguration.getMaxRuleNodeExecsPerMessage(), 
                getDebugModeDurationMinutes(systemContext, tenantProfileConfiguration));
    }

    // TODO: consider if tenantProfileThreshold should be Integer(Wrapper) to have ability make it unset and use systemContext value.
    private static int getDebugModeDurationMinutes(ActorSystemContext systemContext, TenantProfileConfiguration tenantProfileConfiguration) {
        int tenantProfileThreshold = tenantProfileConfiguration.getRuleNodeDebugModeDurationMinutes();
        return tenantProfileThreshold > 0 ? tenantProfileThreshold : systemContext.getRuleNodeDebugModeDurationMinutes();
    }
    
}
