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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.validator.internal.util.privilegedactions.GetConstraintValidatorList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@XmlType(propOrder={"pin", "pinPullResistance", "impulsesPerKwh", "measurementInterval"})
public class S0ElectricityMeter extends GpioControllable implements Meter {
    private static final int MAX_AGE = 3600; // seconds
    @XmlTransient
    private Logger logger = LoggerFactory.getLogger(S0ElectricityMeter.class); 
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    @XmlTransient
    private List<Long> impulseTimestamps = new ArrayList<Long>();
    @XmlTransient
    private Control control;

    
    public Integer getImpulsesPerKwh() {
        return impulsesPerKwh;
    }

    public void setImpulsesPerKwh(Integer impulsesPerKwh) {
        this.impulsesPerKwh = impulsesPerKwh;
    }
    
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void setMeasurementInterval(Integer measurementInterval) {
        this.measurementInterval = measurementInterval;
    }
    
    public void setControl(Control control) {
        this.control = control;
    }

    public void start() {
        if(getGpioController() != null) {
            final GpioPinDigitalInput input = getGpioController().provisionDigitalInputPin(getPin(), getPinPullResistance());
            input.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    logger.debug("Pin " + event.getPin() + " changed to " + event.getState());
                    if(event.getState() == PinState.HIGH) {
                        addTimestampAndMaintain(System.currentTimeMillis());
                    }
                }
            });
            logger.info("Start metering using pin " + getPin());
        }
        else { 
            logger.warn("Configured for pin " + getPin()+ ", but not GPIO access disabled.");
        }
    }
    
    private void addTimestampAndMaintain(long timestampMillis) {
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
            // for meter with 1000 imp/kWh and measurement interval 60s: 
            // 1 kW = 1000imp * 1h
            // 1 kW = count/1000imp * 60s/3600s
            // 1  W = count/1000imp * 60s/3600s / 1000
            int impulsesInMeasurementInterval = getImpulseTimestampsInMeasurementInterval(System.currentTimeMillis()).size();
            if(impulsesInMeasurementInterval < 2) {
                // less than 2 timestamps in measurement interval
                return getCurrentPowerIgnoringMeasurementInterval();
            }
            else {
                int averagePower = impulsesInMeasurementInterval/impulsesPerKwh * measurementInterval/3600 / 1000;
                logger.debug("average power = " + averagePower + "W");
                return averagePower;
            }
        }
        else {
            logger.warn("Configuration attributes impulsesPerKwh and/or measurementInterval not set!");
        }
        return 0;
    }

    public int getMinPower() {
        // min power = longest gap between two timestamps
        List<Long> timestamps = getImpulseTimestampsInMeasurementInterval(System.currentTimeMillis());
        if(timestamps.size() > 1) {
            long longestGap = 0;
            for(int i=1;i<timestamps.size();i++) {
                long gap = timestamps.get(i) - timestamps.get(i-1);
                if(gap > longestGap) {
                    longestGap = gap;
                }
            }
            int minPower = getPower(longestGap);
            logger.debug("Min power = " + minPower + "W : longestGap=" + longestGap);
            return minPower;
        }
        else if (impulseTimestamps.size() > 1 && control.isOn()) {
            // less than 2 timestamps in measurement interval
            return getCurrentPowerIgnoringMeasurementInterval();
        }
        return 0;
    }

    public int getMaxPower() {
        // max power = shortest gap between two timestamps
        List<Long> timestamps = getImpulseTimestampsInMeasurementInterval(System.currentTimeMillis());
        if(timestamps.size() > 1) {
            long shortestGap = Long.MAX_VALUE;
            for(int i=1;i<timestamps.size();i++) {
                long gap = timestamps.get(i) - timestamps.get(i-1);
                if(gap < shortestGap) {
                    shortestGap = gap;
                }
            }
            int maxPower = getPower(shortestGap);
            logger.debug("Max power = " + maxPower + "W : shortestGap=" + shortestGap);
            return maxPower;
        }
        else if (impulseTimestamps.size() > 1 && control.isOn()) {
            // less than 2 timestamps in measurement interval
            return getCurrentPowerIgnoringMeasurementInterval();
        }
        return 0;
    }

    private int getCurrentPowerIgnoringMeasurementInterval() {
        if (impulseTimestamps.size() > 1) {
            if(control.isOn()) {
                // calculate power from 2 most recent timestamps, but only if the device is still switched on
                long mostRecentTimestamp = impulseTimestamps.get(impulseTimestamps.size() - 1);
                long secondMostRecentTimestamp = impulseTimestamps.get(impulseTimestamps.size() - 2);
                int currentPower = getPower(mostRecentTimestamp - secondMostRecentTimestamp);
                logger.debug("Current power = " + currentPower + " W");
                return currentPower;
            }
            else {
                logger.warn("Appliance not switched on.");
            }
        }
        else {
            logger.warn("No or less than 2 impules received.");
        }
        return 0;
    }
    
    private List<Long> getImpulseTimestampsInMeasurementInterval(long referenceTimestamp) {
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
    
    private int getPower(long gapBetweenTwoImpulses) {
        return Double.valueOf(3600000 / gapBetweenTwoImpulses).intValue();
    }
}
