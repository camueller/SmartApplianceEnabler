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
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.TimestampBasedCache;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TreeMap;

/**
 * A PollEnergyMeter meters energy by polling the energy count.
 */
public class PollEnergyMeter implements ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(PollEnergyMeter.class);
    private String applianceId;
    private PollEnergyExecutor pollEnergyExecutor;
    private transient Float startEnergyCounter;
    private transient Float totalEnergy;
    private TimestampBasedCache<Float> cache = new TimestampBasedCache<>("Energy");
    private GuardedTimerTask pollTimerTask;
    private boolean started;


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.cache.setApplianceId(applianceId);
    }

    public void setPollEnergyExecutor(PollEnergyExecutor pollEnergyExecutor) {
        this.pollEnergyExecutor = pollEnergyExecutor;
    }

    public void start(Timer timer, Integer pollInterval, Integer measurementInterval, PollEnergyExecutor pollEnergyExecutor) {
        this.cache.setMaxAgeSeconds(measurementInterval);
        this.pollTimerTask = new GuardedTimerTask(this.applianceId, "PollEnergyMeter", pollInterval * 1000) {
            @Override
            public void runTask() {
                LocalDateTime now = new LocalDateTime();
                addValue(now.toDateTime().getMillis(), pollEnergyExecutor.pollEnergy(now));
            }
        };
        if(timer != null) {
            timer.schedule(this.pollTimerTask, 0, this.pollTimerTask.getPeriod());
        }
    }

    public void cancelTimer() {
        if(this.pollTimerTask != null) {
            this.pollTimerTask.cancel();
        }
    }

    public void addValue(long timestamp) {
        cache.addValue(timestamp, pollEnergyExecutor.pollEnergy(new LocalDateTime()));
    }

    public void addValue(long timestamp, float power) {
        cache.addValue(timestamp, power);
    }

    public TreeMap<Long, Float> getValuesInMeasurementInterval() {
        return this.cache.getTimestampWithValue();
    }

    public float getEnergy() {
        float currentEnergyCounter = this.pollEnergyExecutor.pollEnergy(new LocalDateTime());
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

    public Float startEnergyCounter() {
        this.startEnergyCounter = this.pollEnergyExecutor.pollEnergy(new LocalDateTime());
        this.started = true;
        return startEnergyCounter;
    }

    public Float stopEnergyCounter() {
        float stopEnergyCounter = this.pollEnergyExecutor.pollEnergy(new LocalDateTime());
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
        return stopEnergyCounter;
    }

    public void resetEnergyCounter() {
        this.startEnergyCounter = null;
        this.totalEnergy = null;
    }

    public boolean isStarted() {
        return started;
    }
}
