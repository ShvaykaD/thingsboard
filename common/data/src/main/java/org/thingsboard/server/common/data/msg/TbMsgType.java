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
package org.thingsboard.server.common.data.msg;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum TbMsgType {

    POST_ATTRIBUTES_REQUEST("Post attributes"),
    POST_TELEMETRY_REQUEST("Post telemetry"),
    TO_SERVER_RPC_REQUEST("RPC Request from Device"),
    ACTIVITY_EVENT("Activity Event"),
    INACTIVITY_EVENT("Inactivity Event"),
    CONNECT_EVENT("Connect Event"),
    DISCONNECT_EVENT("Disconnect Event"),
    ENTITY_CREATED("Entity Created"),
    ENTITY_UPDATED("Entity Updated"),
    ENTITY_DELETED("Entity Deleted"),
    ENTITY_ASSIGNED("Entity Assigned"),
    ENTITY_UNASSIGNED("Entity Unassigned"),
    ATTRIBUTES_UPDATED("Attributes Updated"),
    ATTRIBUTES_DELETED("Attributes Deleted"),
    ALARM(null),
    ALARM_ACK("Alarm Acknowledged"),
    ALARM_CLEAR("Alarm Cleared"),
    ALARM_DELETE("Alarm Deleted"),
    ALARM_ASSIGNED("Alarm Assigned"),
    ALARM_UNASSIGNED("Alarm Unassigned"),
    COMMENT_CREATED("Comment Created"),
    COMMENT_UPDATED("Comment Updated"),
    RPC_CALL_FROM_SERVER_TO_DEVICE("RPC Request to Device"),
    ENTITY_ASSIGNED_FROM_TENANT("Entity Assigned From Tenant"),
    ENTITY_ASSIGNED_TO_TENANT("Entity Assigned To Tenant"),
    ENTITY_ASSIGNED_TO_EDGE(null),
    ENTITY_UNASSIGNED_FROM_EDGE(null),
    TIMESERIES_UPDATED("Timeseries Updated"),
    TIMESERIES_DELETED("Timeseries Deleted"),
    RPC_QUEUED("RPC Queued"),
    RPC_SENT("RPC Sent"),
    RPC_DELIVERED("RPC Delivered"),
    RPC_SUCCESSFUL("RPC Successful"),
    RPC_TIMEOUT("RPC Timeout"),
    RPC_EXPIRED("RPC Expired"),
    RPC_FAILED("RPC Failed"),
    RPC_DELETED("RPC Deleted"),
    RELATION_ADD_OR_UPDATE("Relation Added or Updated"),
    RELATION_DELETED("Relation Deleted"),
    RELATIONS_DELETED("All Relations Deleted"),
    PROVISION_SUCCESS(null),
    PROVISION_FAILURE(null);

    public static final List<String> NODE_CONNECTIONS = EnumSet.allOf(TbMsgType.class).stream()
            .map(TbMsgType::getRuleNodeConnection).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());

    @Getter
    private final String ruleNodeConnection;

    TbMsgType(String ruleNodeConnection) {
        this.ruleNodeConnection = ruleNodeConnection;
    }

    public static String getRuleNodeConnection(String msgType) {
        if (msgType == null) {
            return TbNodeConnectionType.OTHER;
        } else {
            return Arrays.stream(TbMsgType.values())
                    .filter(type -> type.name().equals(msgType))
                    .findFirst()
                    .map(TbMsgType::getRuleNodeConnection)
                    .orElse(TbNodeConnectionType.OTHER);
        }
    }

}