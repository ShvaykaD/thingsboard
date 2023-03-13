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
package org.thingsboard.rule.engine.api;

import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.msg.TbMsg;

import java.util.List;
import java.util.Set;

public interface RuleNodeCacheService {

    void add(String key, String value);

    void add(String key, EntityId id);

    void add(EntityId key, Integer partition, TbMsg value);

    void removeStringList(String key, List<String> values);

    void removeEntityIdList(String key, List<EntityId> values);

    void removeTbMsgList(EntityId key, Integer partition, List<TbMsg> values);

    Set<String> getStrings(String key);

    Set<EntityId> getEntityIds(String key);

    Set<TbMsg> getTbMsgs(EntityId key, Integer partition);

    void evictTbMsgs(EntityId key, Integer partition);

    void evict(String key);

}
