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

package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class MockElectricityMeter implements Meter, ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(MockElectricityMeter.class);
    private transient PollEnergyMeter pollEnergyMeter = new PollEnergyMeter();
    private String applianceId;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.pollEnergyMeter.setApplianceId(applianceId);
    }

    public PollEnergyMeter getPollEnergyMeter() {
        return pollEnergyMeter;
    }

    public void setPollEnergyMeter(PollEnergyMeter pollEnergyMeter) {
        this.pollEnergyMeter = pollEnergyMeter;
        this.pollEnergyMeter.setApplianceId(applianceId);
    }

    @Override
    public void init() {
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
    }

    @Override
    public void stop(LocalDateTime now) {
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    @Override
    public void startEnergyMeter() {
        logger.debug("{}: Start energy meter ...", applianceId);
        this.pollEnergyMeter.startEnergyCounter();
    }

    @Override
    public void stopEnergyMeter() {
        logger.debug("{}: Stop energy meter ...", applianceId);
        this.pollEnergyMeter.stopEnergyCounter();
    }

    @Override
    public void resetEnergyMeter() {
        logger.debug("{}: Reset energy meter ...", applianceId);
        this.pollEnergyMeter.reset();
    }
}
