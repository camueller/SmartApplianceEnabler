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
 * A PollEnergyMeter meters energy by polling the energy count.
 */
public class PollEnergyMeter implements ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(PollEnergyMeter.class);
    private String applianceId;
    private PollEnergyExecutor pollEnergyExecutor;
    private transient Float startEnergyCounter;
    private transient Float totalEnergy;
    private boolean started;


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setPollEnergyExecutor(PollEnergyExecutor pollEnergyExecutor) {
        this.pollEnergyExecutor = pollEnergyExecutor;
    }

    public float getEnergy() {
        float currentEnergyCounter = this.pollEnergyExecutor.pollEnergy();
        float energy = 0.0f;
        if(this.startEnergyCounter != null) {
            if(this.totalEnergy != null) {
                energy = this.totalEnergy + currentEnergyCounter - this.startEnergyCounter;
            }
            else {
                energy = currentEnergyCounter - this.startEnergyCounter;
            }
        }
        else if (this.totalEnergy != null) {
            energy = this.totalEnergy;
        }

        logger.debug("{}: energy={}kWh totalEnergy={} startEnergyCounter={} currentEnergyCounter={} started={}",
                applianceId, energy, totalEnergy, startEnergyCounter, currentEnergyCounter, started);

        return energy;
    }

    public void startEnergyCounter() {
        this.startEnergyCounter = this.pollEnergyExecutor.pollEnergy();
        this.started = true;
    }

    public void stopEnergyCounter() {
        float stopEnergyCounter = this.pollEnergyExecutor.pollEnergy();
        if(this.startEnergyCounter != null) {
            if(this.totalEnergy != null) {
                this.totalEnergy += stopEnergyCounter - this.startEnergyCounter;
            }
            else {
                this.totalEnergy = stopEnergyCounter - this.startEnergyCounter;
            }
        }
        this.started = false;
        this.startEnergyCounter = null;
    }

    public void resetEnergyCounter() {
        this.startEnergyCounter = null;
        this.totalEnergy = null;
    }

    public boolean isStarted() {
        return started;
    }
}
