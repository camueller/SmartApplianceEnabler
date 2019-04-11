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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A PollPowerMeter calculates power consumption by polling.
 */
public class PollPowerMeter implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(PollPowerMeter.class);
    private Map<Long,Float> timestampWithPower = new HashMap<Long,Float>();
    private Integer measurementInterval;
    private String applianceId;
    private TimerTask pollTimerTask;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void start(Timer timer, Integer pollInterval, Integer measurementInterval, PollPowerExecutor pollPowerExecutor) {
        this.measurementInterval = measurementInterval;
        String taskName = "PollPowerMeter";
        long period = pollInterval * 1000;
        logger.debug("{}: Starting timer task name={} period={}ms", applianceId, taskName, period);
        this.pollTimerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    addValue(pollPowerExecutor.getPower());
                }
                catch(Throwable e) {
                    logger.error(applianceId + ": Error executing timer task name=" + taskName, e);
                }
            }
        };
        timer.schedule(this.pollTimerTask, 0, period);
    }

    public void cancelTimer() {
        if(this.pollTimerTask != null) {
            logger.debug("{}: Cancel timer task", applianceId);
            this.pollTimerTask.cancel();
        }
    }

    public void addValue(float power) {
        long currentTimestamp = System.currentTimeMillis();
        // remove expired values
        Set<Long> expiredTimestamps = new HashSet<Long>();
        for(Long cachedTimeStamp : timestampWithPower.keySet()) {
            if(cachedTimeStamp < currentTimestamp - measurementInterval * 1000) {
                expiredTimestamps.add(cachedTimeStamp);
            }
        }
        for(Long expiredTimestamp : expiredTimestamps) {
            timestampWithPower.remove(expiredTimestamp);
        }
        // add new value
        timestampWithPower.put(currentTimestamp, power);
        logger.debug("{}: timestamps added/removed/total: 1/{}/{}", applianceId, expiredTimestamps.size(), timestampWithPower.size());
    }

    public int getAveragePower() {
        double sum = 0.0f;
        if(!timestampWithPower.isEmpty()) {
            for (Float value : timestampWithPower.values()) {
                sum += value;
            }
            return Double.valueOf(sum / timestampWithPower.size()).intValue();
        }
        return 0;
    }

    public int getMinPower() {
        float minValue = Float.MAX_VALUE;
        if(!timestampWithPower.isEmpty()) {
            for (Float value : timestampWithPower.values()) {
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
        if(!timestampWithPower.isEmpty()) {
            for (Float value : timestampWithPower.values()) {
                if(value > maxValue) {
                    maxValue = value;
                }
            }
            return Float.valueOf(maxValue).intValue();
        }
        return 0;
    }
}
