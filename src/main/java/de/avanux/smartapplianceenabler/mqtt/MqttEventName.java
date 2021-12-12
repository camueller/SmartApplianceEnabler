/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.mqtt;

public class MqttEventName {
    public static final String TOPIC = "Event";

    public static final MqttEventName StartingCurrentDetected = new MqttEventName("StartingCurrentDetected");
    public static final MqttEventName FinishedCurrentDetected = new MqttEventName("FinishedCurrentDetected");
    public static final MqttEventName ControlStateChanged = new MqttEventName("ControlStateChanged");
    public static final MqttEventName EVChargerStateChanged = new MqttEventName("EVChargerStateChanged");
    public static final MqttEventName EVChargerSocChanged = new MqttEventName("EVChargerSocChanged");
    private String name;

    public MqttEventName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
