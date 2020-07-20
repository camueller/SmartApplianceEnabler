/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PulsePowerMeter implements ApplianceIdConsumer, Validateable {
    private Logger logger = LoggerFactory.getLogger(PulsePowerMeter.class);
    private String applianceId;
    private List<Long> impulseTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    private Integer measurementInterval; // seconds
    private Integer impulsesPerKwh;
    private Control control;

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", applianceId);
        if(measurementInterval == null) {
            logger.error("{}: measurement interval not set", applianceId);
            throw new ConfigurationException();
        }
        if(impulsesPerKwh == null) {
            logger.error("{}: impulses per kwh not set", applianceId);
            throw new ConfigurationException();
        }
        logger.debug("{}: configured: impulsesPerKwh={} measurementInterval={}s", applianceId, impulsesPerKwh, measurementInterval);
    }

    void setImpulsesPerKwh(Integer impulsesPerKwh) {
        this.impulsesPerKwh = impulsesPerKwh;
    }

    void setMeasurementInterval(Integer measurementInterval) {
        this.measurementInterval = measurementInterval;
    }

    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    protected void addTimestamp(long timestampMillis) {
        impulseTimestamps.add(timestampMillis);
    }

    private void maintainTimestamps(long timestampNow) {
        List<Long> impulseTimestampsForRemoval = new ArrayList<Long>();
        this.impulseTimestamps.forEach(timestamp -> {
            if(timestampNow - timestamp > this.measurementInterval * 1000) {
                impulseTimestampsForRemoval.add(timestamp);
            }
        });

        // remove expired timestamps but keep the most recent timestamp
        Long mostRecentTimestamp = getMostRecentTimestamp();
        this.impulseTimestamps.removeAll(impulseTimestampsForRemoval);
        if(this.impulseTimestamps.size() == 0 && mostRecentTimestamp != null) {
            this.impulseTimestamps.add(mostRecentTimestamp);
        }
    }

    private Long getMostRecentTimestamp() {
        return this.impulseTimestamps.size() > 0
                ? this.impulseTimestamps.get(this.impulseTimestamps.size() - 1) : null;
    }

    protected double calculatePower(long timestamp1, long timestamp2) {
        return 3600.0 * this.impulsesPerKwh / (timestamp2 - timestamp1);
    }

    public int getAveragePower() {
        return getAveragePower(System.currentTimeMillis());
    }

    int getAveragePower(long timestampNow) {
        this.maintainTimestamps(timestampNow);
        if(this.impulseTimestamps.size() == 0) {
            return 0;
        }
        List<Long> timestampsToAnalyze = new ArrayList<>(this.impulseTimestamps);
        if(timestampsToAnalyze.size() == 1) {
            long ageMillis = timestampNow - this.impulseTimestamps.get(0);
            if(ageMillis > TimeframeIntervalHandler.UPDATE_QUEUE_INTERVAL_SECONDS * 1000) {
                // interpret request time as a second timestamp, but only if it is not very recent
                timestampsToAnalyze.add(timestampNow);
            }
            else {
                return 0;
            }
        }

        Double powerValuesSum = 0.0;
        for(int i=0; i<timestampsToAnalyze.size() - 1; i ++) {
            powerValuesSum += calculatePower(timestampsToAnalyze.get(i), timestampsToAnalyze.get(i + 1));
        }
        return Double.valueOf(powerValuesSum / (timestampsToAnalyze.size() - 1)).intValue();
    }

    public int getMinPower() {
        return getMinPower(System.currentTimeMillis());
    }

    int getMinPower(long timestampNow) {
        this.maintainTimestamps(timestampNow);
        if(this.impulseTimestamps.size() == 0) {
            return 0;
        }
        if(this.impulseTimestamps.size() == 1) {
            return getAveragePower(timestampNow);
        }

        double minPower = Double.MAX_VALUE;
        for(int i=0; i<this.impulseTimestamps.size() - 1; i ++) {
            double power = calculatePower(this.impulseTimestamps.get(i), this.impulseTimestamps.get(i + 1));
            if(power < minPower) {
                minPower = power;
            }
        }
        return Double.valueOf(minPower).intValue();
    }

    public int getMaxPower() {
        return getMaxPower(System.currentTimeMillis());
    }

    int getMaxPower(long timestampNow) {
        this.maintainTimestamps(timestampNow);
        if(this.impulseTimestamps.size() == 0) {
            return 0;
        }
        if(this.impulseTimestamps.size() == 1) {
            return getAveragePower(timestampNow);
        }

        double maxPower = Double.MIN_VALUE;
        for(int i=0; i<this.impulseTimestamps.size() - 1; i ++) {
            double power = calculatePower(this.impulseTimestamps.get(i), this.impulseTimestamps.get(i + 1));
            if(power > maxPower) {
                maxPower = power;
            }
        }
        return Double.valueOf(maxPower).intValue();
    }

    public boolean isOn() {
        return isOn(System.currentTimeMillis());
    }

    boolean isOn(long timestampNow) {
        if(control != null) {
            return control.isOn();
        }
        Long mostRecentTimestamp = getMostRecentTimestamp();
        return mostRecentTimestamp != null && calculatePower(mostRecentTimestamp, timestampNow) >= 1;
    }

}
