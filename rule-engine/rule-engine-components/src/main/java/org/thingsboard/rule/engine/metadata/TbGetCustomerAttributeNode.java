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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.rule.engine.util.EntitiesCustomerIdAsyncLoader;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.plugin.ComponentType;

@RuleNode(
        type = ComponentType.ENRICHMENT,
        name = "customer attributes",
        configClazz = TbGetEntityAttrNodeConfiguration.class,
        nodeDescription = "Add Originators Customer Attributes or Latest Telemetry into Message or Metadata",
        nodeDetails = "Enrich the Message or Metadata with the corresponding customer's latest attributes or telemetry value. " +
                "The customer is selected based on the originator of the message: device, asset, etc. " +
                "</br>" +
                "Useful when you store some parameters on the customer level and would like to use them for message processing.",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbEnrichmentNodeCustomerAttributesConfig")
public class TbGetCustomerAttributeNode extends TbAbstractGetEntityAttrNode<CustomerId> {

    private static final String CUSTOMER_NOT_FOUND_MESSAGE = "Failed to find customer for entity with id %s and type %s";

    @Override
    protected TbGetEntityAttrNodeConfiguration loadNodeConfiguration(TbNodeConfiguration configuration) throws TbNodeException {
        var config = TbNodeUtils.convert(configuration, TbGetEntityAttrNodeConfiguration.class);
        checkIfMappingIsNotEmptyOrElseThrow(config.getAttrMapping());
        return config;
    }

    @Override
    protected ListenableFuture<CustomerId> findEntityAsync(TbContext ctx, EntityId originator) {
        return Futures.transformAsync(EntitiesCustomerIdAsyncLoader.findEntityIdAsync(ctx, originator),
                checkIfEntityIsPresentOrThrow(String.format(CUSTOMER_NOT_FOUND_MESSAGE, originator.getId(), originator.getEntityType().getNormalName())),
                ctx.getDbCallbackExecutor()
        );
    }

}
