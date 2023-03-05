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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

/**
 * A PollPowerMeter calculates power consumption by polling.
 */
public class PollPowerMeter implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(PollPowerMeter.class);
    private String applianceId;
    private PollPowerExecutor pollPowerExecutor;
    private transient Double currentPower;
    private transient LocalDateTime currentPowerTimestamp;
    private transient LocalDateTime previousPowerTimestamp;
    private transient double totalEnergy;
    private transient Double startEnergyCounter;
    private GuardedTimerTask pollTimerTask;
    private boolean started;
    private List<MeterUpdateListener> meterUpdateListeners = new ArrayList<>();
    private DecimalFormat energyFormat;

    public PollPowerMeter() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        energyFormat = (DecimalFormat) nf;
        energyFormat.applyPattern("#.#####");
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void start(Timer timer, Integer pollInterval, PollPowerExecutor pollPowerExecutor) {
        this.pollPowerExecutor = pollPowerExecutor;
        if(timer != null) {
            this.pollTimerTask = buildPollPowerTask(pollInterval, pollPowerExecutor);
            timer.schedule(this.pollTimerTask, 0, this.pollTimerTask.getPeriod());
        }
    }

    private GuardedTimerTask buildPollPowerTask(Integer pollInterval, PollPowerExecutor pollPowerExecutor) {
        return new GuardedTimerTask(this.applianceId, "PollPowerMeter", pollInterval * 1000) {
            @Override
            public void runTask() {
                LocalDateTime now = LocalDateTime.now();
                Double power = pollPowerExecutor.pollPower();
                if(power != null) {
                    previousPowerTimestamp = currentPowerTimestamp;
                    currentPower = power;
                    currentPowerTimestamp = now;
                    updateTotalEnergy();
                }
                meterUpdateListeners.forEach(
                    listener -> listener.onMeterUpdate(now, power != null ? power.intValue() : 0, totalEnergy)
                );
            }
        };
    }

    public void cancelTimer() {
        if(this.pollTimerTask != null) {
            this.pollTimerTask.cancel();
        }
    }

    public void updateTotalEnergy() {
        double diffEnergy = calculateDiffEnergy();
        if(this.started) {
            this.totalEnergy += diffEnergy;
        }
        else {
            diffEnergy = this.totalEnergy;
        }
        logger.trace("{}: totalEnergy={}Wh diffEnergy={}Wh started={}",
                applianceId, energyFormat.format(totalEnergy), energyFormat.format(diffEnergy), started);
    }

    private double calculateDiffEnergy() {
        double diffEnergy = 0.0f;
        if(this.previousPowerTimestamp != null
                && this.currentPower != null && this.currentPowerTimestamp != null) {
            long diffTime = Duration.between(previousPowerTimestamp, currentPowerTimestamp).toMillis();

            // diffEnergy kWh = power W * diffTime ms / (1000W/kW * 1000ms/1s * 3600s/1h)
            diffEnergy = currentPower * diffTime / (1000.0 * 1000.0 * 3600.0);

            logger.debug("{}: Calculating diffEnergy from power: diffEnergy={}kWh currentPower={}W previousPowerTimestamp={} currentPowerTimestamp={} diffTime={}ms",
                    applianceId, energyFormat.format(diffEnergy), currentPower.intValue(),
                    previousPowerTimestamp, currentPowerTimestamp, diffTime);
        }
        return diffEnergy;
    }

    public Double startEnergyCounter() {
        if(! this.started) {
            this.pollPowerExecutor.pollPower();
            this.startEnergyCounter = totalEnergy;
            this.started = true;
            logger.debug("{}: Start energy counter: {}kWh", applianceId, energyFormat.format(startEnergyCounter));
        }
        return startEnergyCounter;
    }

    public Double stopEnergyCounter() {
        this.pollPowerExecutor.pollPower();
        double stopEnergyCounter = totalEnergy;
        logger.debug("{}: Stop energy counter: totalEnergy={}kWh startEnergyCounter={}kWh stopEnergyCounter={}kWh",
                applianceId, energyFormat.format(totalEnergy), startEnergyCounter != null ? energyFormat.format(startEnergyCounter) : startEnergyCounter,
                energyFormat.format(stopEnergyCounter));
        this.started = false;
        this.startEnergyCounter = null;
        return stopEnergyCounter;
    }

    public void reset() {
        this.startEnergyCounter = null;
        this.totalEnergy = 0.0;
        this.previousPowerTimestamp = null;
    }

    public boolean isStarted() {
        return started;
    }

    public void addPowerUpateListener(MeterUpdateListener listener) {
        this.meterUpdateListeners.add(listener);
    }
}
