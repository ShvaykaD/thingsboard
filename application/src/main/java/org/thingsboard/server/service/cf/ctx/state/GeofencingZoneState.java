/**
 * Copyright © 2016-2025 The Thingsboard Authors
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
package org.thingsboard.server.service.cf.ctx.state;

import lombok.Data;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.geo.Coordinates;
import org.thingsboard.common.util.geo.PerimeterDefinition;
import org.thingsboard.rule.engine.geo.EntityGeofencingState;
import org.thingsboard.rule.engine.util.GpsGeofencingEvents;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.BasicKvEntry;
import org.thingsboard.server.common.data.kv.KvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.util.ProtoUtils;

@Data
public class GeofencingZoneState {

    private final EntityId zoneId;

    private long ts;
    private Long version;
    private BasicKvEntry kvEntry;
    private PerimeterDefinition perimeterDefinition;

    private EntityGeofencingState state;

    public GeofencingZoneState(EntityId zoneId, KvEntry entry) {
        this.zoneId = zoneId;
        if (entry instanceof TsKvEntry tsKvEntry) {
            this.ts = tsKvEntry.getTs();
            this.version = tsKvEntry.getVersion();
        } else if (entry instanceof AttributeKvEntry attributeKvEntry) {
            this.ts = attributeKvEntry.getLastUpdateTs();
            this.version = attributeKvEntry.getVersion();
        }
        this.kvEntry = ProtoUtils.basicKvEntryFromKvEntry(entry);
        this.perimeterDefinition = JacksonUtil.fromString(kvEntry.getJsonValue().get(), PerimeterDefinition.class);
    }


    public boolean update(GeofencingZoneState newZoneState) {
        if (newZoneState.getTs() <= this.ts) {
            return false;
        }
        Long newVersion = newZoneState.getVersion();
        if (newVersion == null || this.version == null || newVersion > this.version) {
            this.ts = newZoneState.getTs();
            this.version = newVersion;
            this.kvEntry = newZoneState.getKvEntry();
            this.perimeterDefinition = newZoneState.getPerimeterDefinition();
            // TODO: should we reinitialize state if zone changed?
            return true;
        }
        return false;
    }

    public String evaluate(Coordinates entityCoordinates, long currentTs) {
        boolean inside = perimeterDefinition.checkMatches(entityCoordinates);
        if (state == null) {
            state = new EntityGeofencingState(inside, ts, false);
        }
        if (state.getStateSwitchTime() == 0L || state.isInside() != inside) {
            state.setInside(inside);
            state.setStateSwitchTime(currentTs);
            state.setStayed(false);
            return inside ? GpsGeofencingEvents.ENTERED : GpsGeofencingEvents.LEFT;
        }
        return inside ? GpsGeofencingEvents.INSIDE : GpsGeofencingEvents.OUTSIDE;
    }

}
