/*
 * Copyright (C) 2025 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.avanux.smartapplianceenabler.webservice;

import com.google.gson.*;
import de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe;
import de.avanux.smartapplianceenabler.schedule.DayTimeframe;
import de.avanux.smartapplianceenabler.schedule.Timeframe;

import java.lang.reflect.Type;

public class TimeframeTypeAdapter extends TypeAdapter<Timeframe> implements
        JsonSerializer<Timeframe>, JsonDeserializer<Timeframe> {

    public Timeframe deserialize(JsonElement jsonElement, Type
            type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement clazz = jsonObject.get("@class");
        if(clazz != null) {
            switch (clazz.getAsString()) {
                case "de.avanux.smartapplianceenabler.schedule.DayTimeframe":
                    return context.deserialize(jsonObject, DayTimeframe.class);
                case "de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe":
                    return context.deserialize(jsonObject, ConsecutiveDaysTimeframe.class);
            }
        }
        throw new RuntimeException("Unhandled Timeframe " + clazz);
    }
}
