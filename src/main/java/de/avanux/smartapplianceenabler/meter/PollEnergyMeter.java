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
    private TimestampBasedCache<Double> cache = new TimestampBasedCache<>("Energy");
    private GuardedTimerTask pollTimerTask;
    private long lastPollDurationMillis = 0;
    private boolean started;
    private List<MeterUpdateListener> meterUpdateListeners = new ArrayList<>();
    private DecimalFormat energyFormat;

    public PollEnergyMeter() {
        this.cache.setMaxAgeSeconds(Double.valueOf(1.9 * Meter.AVERAGING_INTERVAL).intValue());
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        energyFormat = (DecimalFormat) nf;
        energyFormat.applyPattern("#.#####");
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.cache.setApplianceId(applianceId);
    }

    public void setPollEnergyExecutor(PollEnergyExecutor pollEnergyExecutor) {
        this.pollEnergyExecutor = pollEnergyExecutor;
    }

    public void start(Timer timer, PollEnergyExecutor pollEnergyExecutor) {
        if(timer != null) {
            this.pollTimerTask = buildPollTimerTask();
            timer.schedule(this.pollTimerTask, 0, this.pollTimerTask.getPeriod());
        }
    }

    public void scheduleNext(Timer timer, int nextPollCompletedSecondsFromNow, int averagingInterval) {
        long nextPollMillisFromNow = nextPollCompletedSecondsFromNow * 1000L - lastPollDurationMillis;
        if(timer != null && nextPollMillisFromNow > 0) {
            logger.trace("{}: Schedule next poll in {}ms lastPollDuration={}ms", applianceId, nextPollMillisFromNow, lastPollDurationMillis);
            cancelTimer();
            this.pollTimerTask = buildPollTimerTask();
            timer.schedule(this.pollTimerTask, nextPollMillisFromNow, averagingInterval * 1000L);
        }
        else {
            logger.error("{}: Skipping rescheduling of next poll due to negative value for nextPollMillisFromNow={}ms", applianceId, nextPollMillisFromNow);
        }
    }

    private GuardedTimerTask buildPollTimerTask() {
        return new GuardedTimerTask(this.applianceId, "PollEnergyMeter", Meter.AVERAGING_INTERVAL * 1000) {
            @Override
            public void runTask() {
                LocalDateTime now = LocalDateTime.now();
                double energy = getEnergy();
                setLastPollDurationMillis(Duration.between(now, LocalDateTime.now()).toMillis());
                if (energy > 0.0f) {
                    // the energy counter we poll might already have been reset and we don't want to add 0 to the cache
                    // except we reset the counter ourselves
                    addValue(now, energy);
                }
                meterUpdateListeners.forEach(listener -> listener.onMeterUpdate(now, getAveragePower(), energy));
            }
        };
    }

    public void cancelTimer() {
        if(this.pollTimerTask != null) {
            this.pollTimerTask.cancel();
            this.pollTimerTask = null;
        }
    }

    public void setLastPollDurationMillis(long lastPollDurationMillis) {
        this.lastPollDurationMillis = lastPollDurationMillis;
    }

    public void addValue(LocalDateTime now) {
        Double value = pollEnergyExecutor.pollEnergy(now);
        addValue(now, value);
    }

    public void addValue(LocalDateTime now, double value) {
        logger.debug("{}: Adding value: timestamp={} value={}", applianceId, now, value);
        cache.addValue(now, value);
    }

    public int getAveragePower() {
        Vector<Integer> powerValues = new Vector<>();
        LocalDateTime previousTimestamp = null;
        Double previousEnergy = null;
        if(this.cache.getTimestampWithValue().size() == 0) {
            logger.debug("{}: Energy cache is empty", applianceId);
        }
        else {
            List<LocalDateTime> timestamps = new ArrayList<>(this.cache.getTimestampWithValue().keySet());
            for(LocalDateTime timestamp: timestamps) {
                Double energy = this.cache.getTimestampWithValue().get(timestamp);
                logger.trace("{}: Energy timestamp={} energy={}", applianceId, timestamp, energy);
                if (previousTimestamp != null && previousEnergy != null) {
                    long diffTime = Duration.between(previousTimestamp, timestamp).toMillis();
                    double diffEnergy = energy - previousEnergy;
                    // diffEnergy kWh * 1000W/kW * 3600s/1h * 1000ms/1s / diffTime ms
                    double power = diffEnergy * 1000.0 * 3600.0 * 1000.0 / diffTime;
                    logger.debug("{}: Calculating power from energy: power={} energy={} previousEnergy={} diffEnergy={} diffTime={}",
                            applianceId, (int) power, energyFormat.format(energy), energyFormat.format(previousEnergy),
                            energyFormat.format(diffEnergy), diffTime);
                    powerValues.add(power > 0 ? Double.valueOf(power).intValue() : 0);
                }
                previousTimestamp = timestamp;
                previousEnergy = energy;
            }
        }
        return powerValues.size() > 0 ? powerValues.lastElement() : 0;
    }

    public double getEnergy() {
        double currentEnergyCounter = this.pollEnergyExecutor.pollEnergy(LocalDateTime.now());
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
        if(stopEnergyCounter == 0.0f) {
            // the event causing the the counter to stop may have already reset the counter we poll
            // in this case we use the last value from cache
            Double lastValue = this.cache.getLastValue();
            if(lastValue != null) {
                stopEnergyCounter = lastValue;
            }
        }
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
        this.startEnergyCounter = null;
        this.totalEnergy = null;
        this.cache.clear();
    }

    public boolean isStarted() {
        return started;
    }

    public void addMeterUpateListener(MeterUpdateListener listener) {
        this.meterUpdateListeners.add(listener);
    }
}
