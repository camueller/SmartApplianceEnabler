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
import de.avanux.smartapplianceenabler.util.TimestampBasedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * A PollPowerMeter calculates power consumption by polling.
 */
public class PollPowerMeter implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(PollPowerMeter.class);
    private TimestampBasedCache<Float> cache = new TimestampBasedCache<>("Power");
    private String applianceId;
    private GuardedTimerTask pollTimerTask;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.cache.setApplianceId(applianceId);
    }

    public void start(Timer timer, Integer pollInterval, Integer measurementInterval, PollPowerExecutor pollPowerExecutor) {
        this.cache.setMaxAgeSeconds(measurementInterval);
        this.pollTimerTask = new GuardedTimerTask(this.applianceId, "PollPowerMeter", pollInterval * 1000) {
            @Override
            public void runTask() {
                addValue(System.currentTimeMillis(), pollPowerExecutor);
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

    public void addValue(long timestamp, PollPowerExecutor pollPowerExecutor) {
        Float power = pollPowerExecutor.pollPower();
        if(power != null) {
            cache.addValue(timestamp, power);
        }
    }

    public void addValue(long timestamp, float power) {
        cache.addValue(timestamp, power);
    }

    public int getAveragePower() {
        double sum = 0.0f;
        if(!cache.isEmpty()) {
            for (Float value : cache.values()) {
                sum += value;
            }
            return Double.valueOf(sum / cache.size()).intValue();
        }
        return 0;
    }

    public int getMinPower() {
        float minValue = Float.MAX_VALUE;
        if(!cache.isEmpty()) {
            for (Float value : cache.values()) {
                if(value < minValue) {
                    minValue = value;
                }
            }
            return Float.valueOf(minValue).intValue();
        }
        return 0;
    }

    public int getMaxPower() {
        float maxValue = Float.MIN_VALUE;
        if(!cache.isEmpty()) {
            for (Float value : cache.values()) {
                if(value > maxValue) {
                    maxValue = value;
                }
            }
            return Float.valueOf(maxValue).intValue();
        }
        return 0;
    }
}
