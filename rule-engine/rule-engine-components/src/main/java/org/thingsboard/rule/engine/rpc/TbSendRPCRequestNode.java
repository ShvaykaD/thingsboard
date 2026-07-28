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
package org.thingsboard.rule.engine.rpc;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.thingsboard.rule.engine.api.RuleEngineDeviceRpcRequest;
import org.thingsboard.rule.engine.api.RuleEngineDeviceRpcResponse;
import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.msg.TbMsgType;
import org.thingsboard.server.common.data.msg.TbNodeConnectionType;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.data.rpc.RpcError;
import org.thingsboard.server.common.data.util.TbPair;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RuleNode(
        type = ComponentType.ACTION,
        name = "rpc call request",
        configClazz = TbSendRpcRequestNodeConfiguration.class,
        nodeDescription = "Sends RPC call to device",
        nodeDetails = "Expects messages with \"method\" and \"params\". Will forward response from device to next nodes." +
                "If the RPC call request is originated by REST API call from user, will forward the response to user immediately.",
        configDirective = "tbActionNodeRpcRequestConfig",
        icon = "call_made",
        docUrl = "https://thingsboard.io/docs/user-guide/rule-engine-2-0/nodes/action/rpc-call-request/",
        version = 1
)
public class TbSendRPCRequestNode implements TbNode {

    private static final String FORCE_ACK_KEY = "forceAck";
    private static final String OVERRIDE_RESPONSE_TIMEOUT_KEY = "overrideResponseTimeout";

    private final Random random = new Random();
    private final Gson gson = new Gson();
    private TbSendRpcRequestNodeConfiguration config;

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        config = TbNodeUtils.convert(configuration, TbSendRpcRequestNodeConfiguration.class);
        if (config.getTimeoutInSeconds() < 0) {
            throw new TbNodeException("Timeout in seconds must be non-negative!", true);
        }
    }

    @Override
    public TbPair<Boolean, JsonNode> upgrade(int fromVersion, JsonNode oldConfiguration) throws TbNodeException {
        boolean hasChanges = false;
        switch (fromVersion) {
            case 0:
                if (!oldConfiguration.has(FORCE_ACK_KEY)) {
                    hasChanges = true;
                    ((ObjectNode) oldConfiguration).put(FORCE_ACK_KEY, true);
                }
                if (!oldConfiguration.has(OVERRIDE_RESPONSE_TIMEOUT_KEY)) {
                    hasChanges = true;
                    ((ObjectNode) oldConfiguration).put(OVERRIDE_RESPONSE_TIMEOUT_KEY, false);
                }
                break;
            default:
                break;
        }
        return new TbPair<>(hasChanges, oldConfiguration);
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) {
        JsonObject json = JsonParser.parseString(msg.getData()).getAsJsonObject();
        String tmp;
        if (msg.getOriginator().getEntityType() != EntityType.DEVICE) {
            ctx.tellFailure(msg, new RuntimeException("Message originator is not a device entity!"));
        } else if (!json.has("method")) {
            ctx.tellFailure(msg, new RuntimeException("Method is not present in the message!"));
        } else if (!json.has("params")) {
            ctx.tellFailure(msg, new RuntimeException("Params are not present in the message!"));
        } else {
            int requestId = json.has("requestId") ? json.get("requestId").getAsInt() : random.nextInt();
            boolean restApiCall = msg.isTypeOf(TbMsgType.RPC_CALL_FROM_SERVER_TO_DEVICE);

            tmp = msg.getMetaData().getValue("oneway");
            boolean oneway = !StringUtils.isEmpty(tmp) && Boolean.parseBoolean(tmp);

            tmp = msg.getMetaData().getValue(DataConstants.PERSISTENT);
            boolean persisted = !StringUtils.isEmpty(tmp) && Boolean.parseBoolean(tmp);

            tmp = msg.getMetaData().getValue("requestUUID");
            UUID requestUUID = !StringUtils.isEmpty(tmp) ? UUID.fromString(tmp) : Uuids.timeBased();
            tmp = msg.getMetaData().getValue("originServiceId");
            String originServiceId = !StringUtils.isEmpty(tmp) ? tmp : null;

            tmp = msg.getMetaData().getValue(DataConstants.EXPIRATION_TIME);
            long expirationTime = !StringUtils.isEmpty(tmp) ? Long.parseLong(tmp) : (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(config.getTimeoutInSeconds()));

            tmp = msg.getMetaData().getValue(DataConstants.RETRIES);
            Integer retries = !StringUtils.isEmpty(tmp) ? Integer.parseInt(tmp) : null;

            String params = parseJsonData(json.get("params"));
            String additionalInfo = parseJsonData(json.get(DataConstants.ADDITIONAL_INFO));

            long ruleEngineResponseDeadline = config.isOverrideResponseTimeout()
                    ? System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(config.getTimeoutInSeconds())
                    : expirationTime;

            RuleEngineDeviceRpcRequest request = RuleEngineDeviceRpcRequest.builder()
                    .oneway(oneway)
                    .method(json.get("method").getAsString())
                    .body(params)
                    .tenantId(ctx.getTenantId())
                    .deviceId(new DeviceId(msg.getOriginator().getId()))
                    .requestId(requestId)
                    .requestUUID(requestUUID)
                    .originServiceId(originServiceId)
                    .expirationTime(expirationTime)
                    .retries(retries)
                    .restApiCall(restApiCall)
                    .persisted(persisted)
                    .additionalInfo(additionalInfo)
                    .ruleEngineResponseDeadline(ruleEngineResponseDeadline)
                    .build();

            ctx.getRpcService().sendRpcRequestToDevice(request, response -> processRpcResponse(ctx, msg, response));
            if (config.isForceAck()) {
                ctx.ack(msg);
            }
        }
    }

    // Not reusing TbAbstractExternalNode: its forceAck comes from the system-wide property, and its helpers rebuild
    // the outgoing message with copyWithNewCtx(), which would change ts and correlationId on the force ack path.
    private void processRpcResponse(TbContext ctx, TbMsg msg, RuleEngineDeviceRpcResponse response) {
        Optional<RpcError> error = response.getError();
        String data = error.map(rpcError -> wrap("error", rpcError.name()))
                .orElseGet(() -> response.getResponse().orElse(TbMsg.EMPTY_JSON_OBJECT));
        if (config.isForceAck()) {
            TbMsg next = ctx.newMsg(msg.getQueueName(), msg.getType(), msg.getOriginator(), msg.getCustomerId(), msg.getMetaData(), data);
            if (error.isEmpty()) {
                ctx.enqueueForTellNext(next, TbNodeConnectionType.SUCCESS);
            } else {
                ctx.enqueueForTellFailure(next, error.get().name());
            }
        } else {
            TbMsg next = msg.transform().data(data).build();
            if (error.isEmpty()) {
                ctx.tellSuccess(next);
            } else {
                ctx.tellFailure(next, new RuntimeException(error.get().name()));
            }
        }
    }

    private String wrap(String name, String body) {
        JsonObject json = new JsonObject();
        json.addProperty(name, body);
        return gson.toJson(json);
    }

    private String parseJsonData(JsonElement paramsEl) {
        if (paramsEl != null) {
            return paramsEl.isJsonPrimitive() ? paramsEl.getAsString() : gson.toJson(paramsEl);
        } else {
            return null;
        }
    }

}
