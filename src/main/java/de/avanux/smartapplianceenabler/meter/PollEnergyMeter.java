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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A PollEnergyMeter meters energy by polling the energy count.
 */
public class PollEnergyMeter implements ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(PollEnergyMeter.class);
    private String applianceId;
    private PollEnergyExecutor pollEnergyExecutor;
    private transient Double startEnergyCounter;
    private transient Double totalEnergy;
    private transient Double currentEnergyCounter;
    private transient LocalDateTime currentEnergyCounterTimestamp;
    private transient Double previousEnergyCounter;
    private transient LocalDateTime previousEnergyCounterTimestamp;
    private GuardedTimerTask pollTimerTask;
    private boolean started;
    private List<MeterUpdateListener> meterUpdateListeners = new ArrayList<>();
    private DecimalFormat energyFormat;

    public PollEnergyMeter() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        energyFormat = (DecimalFormat) nf;
        energyFormat.applyPattern("#.#####");
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void start(Timer timer, PollEnergyExecutor pollEnergyExecutor) {
        this.pollEnergyExecutor = pollEnergyExecutor;
        if(timer != null) {
            this.pollTimerTask = buildPollTimerTask();
            timer.schedule(this.pollTimerTask, 0, this.pollTimerTask.getPeriod());
        }
    }

    private GuardedTimerTask buildPollTimerTask() {
        return new GuardedTimerTask(this.applianceId, "PollEnergyMeter", Meter.AVERAGING_INTERVAL * 1000) {
            @Override
            public void runTask() {
                pollEnergy(LocalDateTime.now());
            }
        };
    }

    public void cancelTimer() {
        if(this.pollTimerTask != null) {
            this.pollTimerTask.cancel();
            this.pollTimerTask = null;
        }
    }

    protected void pollEnergy(LocalDateTime now) {
        if(pollEnergyExecutor != null) {
            Double energy = pollEnergyExecutor.pollEnergy(now);
            if(energy != null) {
                previousEnergyCounter = currentEnergyCounter;
                previousEnergyCounterTimestamp = currentEnergyCounterTimestamp;
                currentEnergyCounter = energy;
                currentEnergyCounterTimestamp = now;
            }
            meterUpdateListeners.forEach(listener -> listener.onMeterUpdate(now, getAveragePower(), getEnergy()));
        }
    }

    public int getAveragePower() {
        if(this.previousEnergyCounter != null && this.previousEnergyCounterTimestamp != null
                && this.currentEnergyCounter != null && this.currentEnergyCounterTimestamp != null) {
            double diffEnergy = currentEnergyCounter - previousEnergyCounter;
            long diffTime = Duration.between(previousEnergyCounterTimestamp, currentEnergyCounterTimestamp).toMillis();
            // power W = diffEnergy kWh * 1000W/kW * 3600s/1h * 1000ms/1s / diffTime ms
            double power = diffEnergy * 1000.0 * 3600.0 * 1000.0 / diffTime;
            logger.debug("{}: Calculating power from energy: power={} currentEnergyCounter={} previousEnergyCounter={} diffEnergy={} diffTime={}",
                    applianceId, (int) power, energyFormat.format(currentEnergyCounter), energyFormat.format(previousEnergyCounter),
                    energyFormat.format(diffEnergy), diffTime);
            return Double.valueOf(power).intValue();
        }
        return 0;
    }

    public double getEnergy() {
        double energy = 0.0f;
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

        logger.trace("{}: energy={}kWh totalEnergy={} startEnergyCounter={} currentEnergyCounter={} started={}",
                applianceId, energy, totalEnergy, startEnergyCounter, currentEnergyCounter, started);

        return energy;
    }

    public Double startEnergyCounter() {
        if(! this.started) {
            this.startEnergyCounter = this.pollEnergyExecutor.pollEnergy(LocalDateTime.now());
            logger.debug("{}: Start energy counter: {}", applianceId, startEnergyCounter);
            this.started = true;
        }
        return startEnergyCounter;
    }

    public Double stopEnergyCounter() {
        double stopEnergyCounter = this.pollEnergyExecutor.pollEnergy(LocalDateTime.now());
        if(this.startEnergyCounter != null) {
            if(this.totalEnergy != null) {
                this.totalEnergy += stopEnergyCounter - this.startEnergyCounter;
            }
            else {
                this.totalEnergy = stopEnergyCounter - this.startEnergyCounter;
            }
        }
        logger.debug("{}: Stop energy counter: totalEnergy={} startEnergyCounter={} stopEnergyCounter={}",
                applianceId, totalEnergy, startEnergyCounter, stopEnergyCounter);
        this.started = false;
        this.startEnergyCounter = null;
        return stopEnergyCounter;
    }

    public void reset() {
        logger.debug("{}: Reset energy counter", applianceId);
        var wasStarted = this.started;
        if(wasStarted) {
            stopEnergyCounter();
        }
        this.startEnergyCounter = null;
        this.totalEnergy = null;
        if(wasStarted) {
            startEnergyCounter();
        }
    }

    public boolean isStarted() {
        return started;
    }

    public void addMeterUpateListener(MeterUpdateListener listener) {
        this.meterUpdateListeners.add(listener);
    }

    public void removeMeterUpateListener(MeterUpdateListener listener) {
        this.meterUpdateListeners.remove(listener);
    }
}
