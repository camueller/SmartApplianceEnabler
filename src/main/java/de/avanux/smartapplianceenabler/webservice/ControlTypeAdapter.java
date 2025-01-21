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
import de.avanux.smartapplianceenabler.control.*;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.meter.*;

import java.lang.reflect.Type;

public class ControlTypeAdapter implements
        JsonSerializer<Control>, JsonDeserializer<Control> {

    public Control deserialize(JsonElement jsonElement, Type
            type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement clazz = jsonObject.get("@class");
        if(clazz != null) {
            switch (clazz.getAsString()) {
                case "de.avanux.smartapplianceenabler.control.AlwaysOnSwitch":
                    return context.deserialize(jsonObject, AlwaysOnSwitch.class);
                case "de.avanux.smartapplianceenabler.control.HttpSwitch":
                    return context.deserialize(jsonObject, HttpSwitch.class);
                case "de.avanux.smartapplianceenabler.control.MeterReportingSwitch":
                    return context.deserialize(jsonObject, MeterReportingSwitch.class);
                case "de.avanux.smartapplianceenabler.control.MockSwitch":
                    return context.deserialize(jsonObject, MockSwitch.class);
                case "de.avanux.smartapplianceenabler.control.ModbusSwitch":
                    return context.deserialize(jsonObject, ModbusSwitch.class);
                case "de.avanux.smartapplianceenabler.control.MqttSwitch":
                    return context.deserialize(jsonObject, MqttSwitch.class);
                case "de.avanux.smartapplianceenabler.control.LevelSwitch":
                    return context.deserialize(jsonObject, LevelSwitch.class);
                case "de.avanux.smartapplianceenabler.control.StartingCurrentSwitch":
                    return context.deserialize(jsonObject, StartingCurrentSwitch.class);
                case "de.avanux.smartapplianceenabler.control.Switch":
                    return context.deserialize(jsonObject, Switch.class);
                case "de.avanux.smartapplianceenabler.control.SwitchOption":
                    return context.deserialize(jsonObject, SwitchOption.class);
                case "de.avanux.smartapplianceenabler.control.PwmSwitch":
                    return context.deserialize(jsonObject, PwmSwitch.class);
                case "de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger":
                    return context.deserialize(jsonObject, ElectricVehicleCharger.class);
            }
        }
        throw new RuntimeException("Unhandled Control " + clazz);
    }

    public JsonElement serialize(Control object, Type type, JsonSerializationContext context) {
        JsonElement element = context.serialize(object, object.getClass());
        element.getAsJsonObject().add("@class", new JsonPrimitive(object.getClass().getName()));
        return element;
    }
}
