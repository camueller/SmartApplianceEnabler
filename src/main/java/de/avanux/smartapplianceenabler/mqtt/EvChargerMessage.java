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

import java.time.LocalDateTime;

public class EvChargerMessage extends ControlMessage {
    private String state;
    private String stateSinceTime;
    private Integer socInitial;
    private String socInitialTime;
    private Integer socRetrieved;
    private String socRetrievedTime;
    private Integer socCurrent;
    private Integer batteryCapacity;
    private Double chargeLoss;
    private Integer chargePower;
    private Boolean useOptionalEnergy;

    public EvChargerMessage(
            LocalDateTime time,
            boolean on,
            String state,
            LocalDateTime stateSinceTime,
            Integer socInitial,
            LocalDateTime socInitialTime,
            Integer socRetrieved,
            LocalDateTime socRetrievedTime,
            Integer socCurrent,
            Integer batteryCapacity,
            Double chargeLoss,
            Integer chargePower,
            Boolean useOptionalEnergy
    ) {
        super(time, on);
        this.state = state;
        this.stateSinceTime = toString(stateSinceTime);
        this.socInitial = socInitial;
        this.socInitialTime = toString(socInitialTime);
        this.socRetrieved = socRetrieved;
        this.socRetrievedTime = toString(socRetrievedTime);
        this.socCurrent = socCurrent;
        this.batteryCapacity = batteryCapacity;
        this.chargeLoss = chargeLoss;
        this.chargePower = chargePower;
        this.useOptionalEnergy = useOptionalEnergy;
    }

    @Override
    public String toString() {
        return "EvChargerMessage{" +
                "on=" + on +
                ", state='" + state + '\'' +
                ", stateSinceTime='" + stateSinceTime + '\'' +
                ", socInitial=" + socInitial +
                ", socInitialTime='" + socInitialTime + '\'' +
                ", socRetrieved=" + socRetrieved +
                ", socRetrievedTime='" + socRetrievedTime + '\'' +
                ", socCurrent=" + socCurrent +
                ", batteryCapacity=" + batteryCapacity +
                ", chargeLoss=" + chargeLoss +
                ", chargePower=" + chargePower +
                ", useOptionalEnergy=" + useOptionalEnergy +
                '}';
    }
}
