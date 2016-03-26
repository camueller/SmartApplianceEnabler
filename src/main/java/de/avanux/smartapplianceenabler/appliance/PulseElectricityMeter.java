/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.appliance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A PulseElectricityMeter monitors power consumption by pulses received.
 */
public class PulseElectricityMeter implements Meter {
    private static final int MAX_AGE = 3600; // seconds
    private Logger logger = LoggerFactory.getLogger(PulseElectricityMeter.class);
    private List<Long> impulseTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    private Integer impulsesPerKwh;
    private Integer measurementInterval; // seconds
    private Control control;

    public void setImpulsesPerKwh(Integer impulsesPerKwh) {
        this.impulsesPerKwh = impulsesPerKwh;
    }

    public void setMeasurementInterval(Integer measurementInterval) {
        this.measurementInterval = measurementInterval;
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    /**
     * Returns true if the appliance has a control and the control is switched on.
     * If the appliance has no control the appliance state is derived from its power consumption.
     * The device considered to be switched on if the age of the most recent impulse
     * is less than double the interval between the most recent and the second most recent impulse.
     * @return true, if the device is considered to be switched on.
     */
    public boolean isOn() {
        if(control != null) {
            return control.isOn();
        }
        Long intervalBetweenTwoMostRecentImpulses = getIntervalBetweenTwoMostRecentImpulses();
        if(intervalBetweenTwoMostRecentImpulses != null) {
            // at this point we can be sure that there are at least two timestamps cached
            long mostRecentTimestamp = impulseTimestamps.get(impulseTimestamps.size() - 1);
            long intervalSinceMostRecentTimestamp = System.currentTimeMillis() - mostRecentTimestamp;
            if(intervalSinceMostRecentTimestamp < intervalBetweenTwoMostRecentImpulses * 2) {
                return true;
            }
        }
        return false;
    }

    public void addTimestampAndMaintain(long timestampMillis) {
        // add new timestamp
        impulseTimestamps.add(timestampMillis);
        // remove timestamps older than MAX_AGE
        List<Long> impulseTimestampsForRemoval = new ArrayList<Long>();
        for(Long impulseTimestamp : impulseTimestamps) {
            if(timestampMillis - impulseTimestamp > MAX_AGE * 1000) {
                impulseTimestampsForRemoval.add(impulseTimestamp);
            }
            else {
                break;
            }
        }
        if(impulseTimestampsForRemoval.size() > 0) {
            impulseTimestamps.removeAll(impulseTimestampsForRemoval);
        }
        logger.debug("timestamps added/removed/total: 1/" + impulseTimestampsForRemoval.size() + "/" + impulseTimestamps.size());
    }

    public int getAveragePower() {
        if(impulsesPerKwh != null && measurementInterval != null) {
            int timestampsInMeasurementInterval = getImpulsesInMeasurementInterval(System.currentTimeMillis()).size();
            if(timestampsInMeasurementInterval > 1) {
                logger.debug("imp=" + timestampsInMeasurementInterval + ", impulsesPerKwh=" + impulsesPerKwh + ", measurementInterval=" + measurementInterval + ")");
                int averagePower = (timestampsInMeasurementInterval * 1000/impulsesPerKwh) * (3600/measurementInterval);
                logger.debug("average power = " + averagePower + "W");
                return averagePower;
            }
            else {
                // less than 2 timestamps in measurement interval
                return getCurrentPowerIgnoringMeasurementInterval();
            }
        }
        else {
            logger.warn("Configuration attributes impulsesPerKwh and/or measurementInterval not set!");
        }
        return 0;
    }

    public int getMinPower() {
        // min power = longest interval between two timestamps
        List<Long> timestamps = getImpulsesInMeasurementInterval(System.currentTimeMillis());
        if(timestamps.size() > 1) {
            long longestInterval = 0;
            for(int i=1;i<timestamps.size();i++) {
                long interval = timestamps.get(i) - timestamps.get(i-1);
                if(interval > longestInterval) {
                    longestInterval = interval;
                }
            }
            int minPower = getPower(longestInterval);
            logger.debug("Min power = " + minPower + "W (longestInterval=" + longestInterval + ")");
            return minPower;
        }
        else if (impulseTimestamps.size() > 1 && isOn()) {
            // less than 2 timestamps in measurement interval
            return getCurrentPowerIgnoringMeasurementInterval();
        }
        return 0;
    }

    public int getMaxPower() {
        // max power = shortest interval between two timestamps
        List<Long> timestamps = getImpulsesInMeasurementInterval(System.currentTimeMillis());
        if(timestamps.size() > 1) {
            long shortestInterval = Long.MAX_VALUE;
            for(int i=1;i<timestamps.size();i++) {
                long interval = timestamps.get(i) - timestamps.get(i-1);
                if(interval < shortestInterval) {
                    shortestInterval = interval;
                }
            }
            int maxPower = getPower(shortestInterval);
            logger.debug("Max power = " + maxPower + "W (shortestInterval=" + shortestInterval + ")");
            return maxPower;
        }
        else if (impulseTimestamps.size() > 1 && isOn()) {
            // less than 2 timestamps in measurement interval
            return getCurrentPowerIgnoringMeasurementInterval();
        }
        return 0;
    }

    private int getCurrentPowerIgnoringMeasurementInterval() {
        if (impulseTimestamps.size() > 1) {
            if(isOn()) {
                // calculate power from 2 most recent timestamps, but only if the device is still switched on
                Long intervalBetweenTwoMostRecentImpulseTimestamps = getIntervalBetweenTwoMostRecentImpulses();
                if(intervalBetweenTwoMostRecentImpulseTimestamps != null) {
                    int currentPower = getPower(intervalBetweenTwoMostRecentImpulseTimestamps);
                    logger.debug("Current power = " + currentPower + " W");
                    return currentPower;
                }
            }
            else {
                logger.warn("Appliance not switched on.");
            }
        }
        else {
            logger.warn("No or less than 2 impules cached.");
        }
        return 0;
    }

    private List<Long> getImpulsesInMeasurementInterval(long referenceTimestamp) {
        List<Long> timestamps = new ArrayList<Long>();
        if(measurementInterval != null) {
            for(Long timestamp : impulseTimestamps) {
                if(referenceTimestamp - timestamp < measurementInterval * 1000) {
                    timestamps.add(timestamp);
                }
            }
        }
        logger.debug(timestamps.size() + " timestamps in measurement interval");
        return timestamps;
    }

    /**
     * Returns the interval between the most recent and the second most recent impulse.
     * @return the interval in milliseconds or null; if there are less than 2 impulse timestamps cached
     */
    private Long getIntervalBetweenTwoMostRecentImpulses() {
        if(impulseTimestamps.size() > 1) {
            long mostRecentTimestamp = impulseTimestamps.get(impulseTimestamps.size() - 1);
            long secondMostRecentTimestamp = impulseTimestamps.get(impulseTimestamps.size() - 2);
            return mostRecentTimestamp - secondMostRecentTimestamp;
        }
        return null;
    }

    /**
     * Returns the power cased on the time interval between two impulses.
     * @param intervalBetweenTwoImpulses
     * @return
     */
    private int getPower(long intervalBetweenTwoImpulses) {
        return Double.valueOf(3600000 / (intervalBetweenTwoImpulses * 1000/impulsesPerKwh)).intValue();
    }

}
