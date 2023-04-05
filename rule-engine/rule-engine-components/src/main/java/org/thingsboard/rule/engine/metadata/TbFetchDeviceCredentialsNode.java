/**
 * Copyright © 2016-2023 The Thingsboard Authors
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
package org.thingsboard.rule.engine.metadata;

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.security.DeviceCredentialsType;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.concurrent.ExecutionException;

@Slf4j
@RuleNode(
        type = ComponentType.ENRICHMENT,
        name = "fetch device credentials",
        configClazz = TbFetchDeviceCredentialsNodeConfiguration.class,
        nodeDescription = "Enrich the message body or metadata with the device credentials",
        nodeDetails = "Adds <b>credentialsType</b> and <b>credentials</b> properties to the message metadata if the " +
                "configuration parameter <b>fetchToMetadata</b> is set to <code>true</code>, otherwise, adds properties " +
                "to the message data. If originator type is not <b>DEVICE</b> or rule node failed to get device credentials " +
                "- send Message via <code>Failure</code> chain, otherwise <code>Success</code> chain is used.",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbEnrichmentNodeFetchDeviceCredentialsConfig")
public class TbFetchDeviceCredentialsNode extends TbAbstractNodeWithFetchTo<TbFetchDeviceCredentialsNodeConfiguration> {

    private static final String CREDENTIALS = "credentials";
    private static final String CREDENTIALS_TYPE = "credentialsType";

    @Override
    protected TbFetchDeviceCredentialsNodeConfiguration loadNodeConfiguration(TbNodeConfiguration configuration) throws TbNodeException {
        return TbNodeUtils.convert(configuration, TbFetchDeviceCredentialsNodeConfiguration.class);
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException {
        var originator = msg.getOriginator();
        ctx.checkTenantEntity(originator);
        var msgDataAsObjectNode = FetchTo.DATA.equals(fetchTo) ? getMsgDataAsObjectNode(msg) : null;
        if (!EntityType.DEVICE.equals(originator.getEntityType())) {
            ctx.tellFailure(msg, new RuntimeException("Unsupported originator type: " + originator.getEntityType() + "!"));
            return;
        }

        var deviceId = new DeviceId(msg.getOriginator().getId());
        var deviceCredentials = ctx.getDeviceCredentialsService().findDeviceCredentialsByDeviceId(ctx.getTenantId(), deviceId);
        if (deviceCredentials == null) {
            ctx.tellFailure(msg, new RuntimeException("Failed to get Device Credentials for device: " + deviceId + "!"));
            return;
        }
        var credentialsType = deviceCredentials.getCredentialsType();
        var credentialsInfo = ctx.getDeviceCredentialsService().toCredentialsInfo(deviceCredentials);
        var metaData = msg.getMetaData().copy();
        if (FetchTo.METADATA.equals(fetchTo)) {
            metaData.putValue(CREDENTIALS_TYPE, credentialsType.name());
            if (credentialsType.equals(DeviceCredentialsType.ACCESS_TOKEN) || credentialsType.equals(DeviceCredentialsType.X509_CERTIFICATE)) {
                metaData.putValue(CREDENTIALS, credentialsInfo.asText());
            } else {
                metaData.putValue(CREDENTIALS, JacksonUtil.toString(credentialsInfo));
            }
        } else if (FetchTo.DATA.equals(fetchTo)) {
            msgDataAsObjectNode.put(CREDENTIALS_TYPE, credentialsType.name());
            msgDataAsObjectNode.set(CREDENTIALS, credentialsInfo);
        }
        TbMsg transformedMsg = transformMessage(msg, msgDataAsObjectNode, metaData);
        ctx.tellSuccess(transformedMsg);
    }

}
