/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

public class ElectricVehicleChargerDefaults {
    // static members won't be serialized but we need those values on the client
    private Integer voltage = 230;
    private Integer phases = 1;
    private Integer chargeLoss = 10;
    private Integer pollInterval = 20; // seconds
    private Integer startChargingStateDetectionDelay = 30;
    private Boolean forceInitialCharging = false;
    private Integer updateSocAfterIncrease = 20;
    private Integer socScriptTimeoutSeconds = 180;
    private static ElectricVehicleChargerDefaults instance = new ElectricVehicleChargerDefaults();

    public static Integer getVoltage() {
        return instance.voltage;
    }

    public static Integer getPhases() {
        return instance.phases;
    }

    public static Integer getChargeLoss() {
        return instance.chargeLoss;
    }

    public static Integer getPollInterval() {
        return instance.pollInterval;
    }

    public static Integer getStartChargingStateDetectionDelay() {
        return instance.startChargingStateDetectionDelay;
    }

    public static Boolean getForceInitialCharging() {
        return instance.forceInitialCharging;
    }

    public static Integer getUpdateSocAfterIncrease() {
        return instance.updateSocAfterIncrease;
    }

    public static Integer getSocScriptTimeoutSeconds() {
        return instance.socScriptTimeoutSeconds;
    }
}
