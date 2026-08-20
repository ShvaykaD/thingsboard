--
-- Copyright © 2016-2026 The Thingsboard Authors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- RPC REQUEST ID ADDITION START

ALTER TABLE rpc ADD COLUMN IF NOT EXISTS request_id integer;

ALTER TABLE rpc ADD COLUMN IF NOT EXISTS oneway boolean;

-- RPC REQUEST ID ADDITION END

-- RPC CALL REQUEST NODE FORCE ACK START

WITH rpc_node AS (
    SELECT id, configuration::jsonb AS config
    FROM rule_node
    WHERE type = 'org.thingsboard.rule.engine.rpc.TbSendRPCRequestNode' AND configuration_version = 0
)
UPDATE rule_node SET
    configuration = (rpc_node.config || jsonb_build_object(
        'forceAck', COALESCE((rpc_node.config ->> 'forceAck')::boolean, true),
        'overrideResponseTimeout', COALESCE((rpc_node.config ->> 'overrideResponseTimeout')::boolean, false)))::varchar,
    configuration_version = 1
FROM rpc_node
WHERE rule_node.id = rpc_node.id;

-- RPC CALL REQUEST NODE FORCE ACK END
