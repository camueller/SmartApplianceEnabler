/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;

import java.time.LocalDateTime;
import java.util.Timer;

public interface Meter extends ApplianceLifeCycle {

    String TOPIC = "Meter";
    int AVERAGING_INTERVAL = 60;

    /**
     * Start counting energy.
     */
    void startEnergyMeter();

    /**
     * Stop counting energy. The energy counter is not being reset so that count may continue
     * if {@link #startEnergyMeter()} is called.
     */
    void stopEnergyMeter();

    /**
     * Reset energy counter to 0.
     */
    void resetEnergyMeter();

    void setMqttTopic(String mqttTopic);
}
