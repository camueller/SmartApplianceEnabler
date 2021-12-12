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

import de.avanux.smartapplianceenabler.control.ev.EVChargerState;

import java.time.LocalDateTime;

public class EVChargerStateChangedEvent extends MqttEvent {
    public EVChargerState previousState;
    public EVChargerState newState;
    public Integer evId;

    public EVChargerStateChangedEvent() {
    }

    public EVChargerStateChangedEvent(LocalDateTime time, EVChargerState previousState, EVChargerState newState, Integer evId) {
        setTime(time);
        this.previousState = previousState;
        this.newState = newState;
        this.evId = evId;
    }

    @Override
    public String toString() {
        return "ControlStateChangedEvent{" +
                "time=" + getTime() +
                ", previousState=" + previousState +
                ", newState=" + newState +
                ", evId=" + evId +
                '}';
    }
}
