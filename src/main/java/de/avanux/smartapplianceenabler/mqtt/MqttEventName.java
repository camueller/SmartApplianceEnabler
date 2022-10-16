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

    public static final MqttEventName WrappedControlSwitchOnDetected = new MqttEventName("WrappedControlSwitchOnDetected");
    public static final MqttEventName WrappedControlSwitchOffDetected = new MqttEventName("WrappedControlSwitchOffDetected");
    public static final MqttEventName EVChargerStateChanged = new MqttEventName("EVChargerStateChanged");
    public static final MqttEventName EVChargerSocChanged = new MqttEventName("EVChargerSocChanged");
    public static final MqttEventName SempDevice2EM = new MqttEventName("SempDevice2EM");
    public static final MqttEventName SempEM2Device = new MqttEventName("SempEM2Device");
    public static final MqttEventName SempGetDeviceInfo = new MqttEventName("SempGetDeviceInfo");
    public static final MqttEventName SempGetDeviceStatus = new MqttEventName("SempGetDeviceStatus");
    private String name;

    public MqttEventName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
