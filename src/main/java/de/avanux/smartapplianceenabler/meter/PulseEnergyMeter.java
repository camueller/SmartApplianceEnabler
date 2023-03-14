/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PulseEnergyMeter meters energy by counting pulses.
 */
public class PulseEnergyMeter implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(PulseEnergyMeter.class);
    private String applianceId;
    private Integer impulsesPerKwh;
    private int pulseCounter;
    private boolean started;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setImpulsesPerKwh(Integer impulsesPerKwh) {
        this.impulsesPerKwh = impulsesPerKwh;
    }

    public void increasePulseCounter() {
        if(started) {
            pulseCounter++;
        }
        logger.debug("{}: energy={}kWh started={} pulses={} pulses/kWh={}", applianceId, getEnergy(),
                started, pulseCounter, impulsesPerKwh);
    }

    public float getEnergy() {
        return Double.valueOf(pulseCounter / Double.valueOf(impulsesPerKwh)).floatValue();
    }

    public void startEnergyCounter() {
        logger.debug("{}: Start energy counter", applianceId);
        started = true;
    }

    public void stopEnergyCounter() {
        logger.debug("{}: Stop energy counter: pulses={}", applianceId, pulseCounter);
        started = false;
    }

    public void resetEnergyCounter() {
        logger.debug("{}: Reset energy counter", applianceId);
        pulseCounter = 0;
    }

}
