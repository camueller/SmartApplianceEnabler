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

package de.avanux.smartapplianceenabler.control.ev;

import de.avanux.smartapplianceenabler.meter.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class EVControlMock implements EVControl, Meter {

    private Logger logger = LoggerFactory.getLogger(EVControlMock.class);
    private boolean charging;
    private boolean chargingCompleted;
    private Timer timer = new Timer();
    private float energyCounter = 0.0f;
    private TimerTask energyCounterTimerTask = new TimerTask() {
        @Override
        public void run() {
            energyCounter += 0.01f;
            logDebug("energyCounter=" + energyCounter);
        }
    };
    private TimerTask chargingCompletedTimerTask = new TimerTask() {
        @Override
        public void run() {
            logDebug("chargingCompleted");
            charging = false;
            chargingCompleted = true;
            energyCounterTimerTask.cancel();
        }
    };

    public EVControlMock() {
        logDebug("using EvControl Mock");
    }

    @Override
    public void setPollInterval(Integer pollInterval) {
    }

    @Override
    public void init(boolean checkRegisterConfiguration) {
    }

    @Override
    public boolean isVehicleNotConnected() {
        return false;
    }

    @Override
    public boolean isVehicleConnected() {
        return true;
    }

    @Override
    public boolean isCharging() {
        return this.charging;
    }

    @Override
    public boolean isChargingCompleted() {
        return this.chargingCompleted;
    }

    @Override
    public boolean isInErrorState() {
        return false;
    }

    @Override
    public void setChargeCurrent(int current) {

    }

    @Override
    public void startCharging() {
        logDebug("startCharging");
        this.charging = true;
    }

    @Override
    public void stopCharging() {
        logDebug("stopCharging");
        this.charging = false;
    }

    @Override
    public void setApplianceId(String applianceId) {

    }

    // --------- Meter ------------------------------------------------------

    @Override
    public void start(Timer timer) {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isOn() {
        return false;
    }

    @Override
    public int getAveragePower() {
        return 0;
    }

    @Override
    public int getMinPower() {
        return 0;
    }

    @Override
    public int getMaxPower() {
        return 0;
    }

    @Override
    public Integer getMeasurementInterval() {
        return null;
    }

    @Override
    public float getEnergy() {
        return energyCounter;
    }

    @Override
    public void startEnergyMeter() {
        logDebug("startEnergyMeter");
        if(energyCounterTimerTask.scheduledExecutionTime() == 0) {
            this.timer.schedule(energyCounterTimerTask, 0, 1000);
        }
        if(chargingCompletedTimerTask.scheduledExecutionTime() == 0) {
            this.timer.schedule(chargingCompletedTimerTask, 600000);
        }
    }

    @Override
    public void stopEnergyMeter() {
        logDebug("stopEnergyMeter");
        this.energyCounterTimerTask.cancel();
    }

    @Override
    public void resetEnergyMeter() {
        logDebug("resetEnergyMeter");
        energyCounter = 0.0f;
    }

    private void logDebug(String message) {
        logger.debug("##### " + message);
    }
}
