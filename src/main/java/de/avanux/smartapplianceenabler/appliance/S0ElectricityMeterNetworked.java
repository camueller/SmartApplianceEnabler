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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by axel on 25.03.16.
 */
public class S0ElectricityMeterNetworked implements Meter, PulseReceiver.PulseListener {
    @XmlTransient
    private Logger logger = LoggerFactory.getLogger(S0ElectricityMeterNetworked.class);
    @XmlAttribute
    private String idref;
    @XmlAttribute
    private Integer port;
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    @XmlTransient
    private PulseElectricityMeter pulseElectricityMeter = new PulseElectricityMeter();
    @XmlTransient
    private PulseReceiver pulseReceiver;
    @XmlTransient
    private String applianceId;
    @XmlTransient
    private Long previousPulseTimestamp;
    @XmlTransient
    private Long previousPulseCounter;


    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public String getIdref() {
        return idref;
    }

    public void setPulseReceiver(PulseReceiver pulseReceiver) {
        this.pulseReceiver = pulseReceiver;
    }

    public int getAveragePower() {
        return pulseElectricityMeter.getAveragePower();
    }

    public int getMinPower() {
        return pulseElectricityMeter.getMinPower();
    }

    public int getMaxPower() {
        return pulseElectricityMeter.getMaxPower();
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void start() {
        pulseElectricityMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulseElectricityMeter.setMeasurementInterval(measurementInterval);
        if(pulseReceiver != null) {
            pulseReceiver.addListener(applianceId, this);
        }
    }

    @Override
    public void pulseReceived(long counter) {
        long timestamp = System.currentTimeMillis();
        if(previousPulseCounter != null) {
            if(counter < previousPulseCounter) {
                logger.debug("Counter overflow - skipping detection of missing packets");
            }
            else {
                long counterDiff = counter - previousPulseCounter;
                if(counterDiff > 1) {
                    long timestampDiff = timestamp - previousPulseTimestamp;
                    logger.warn("Missing packet detected: counterDiff=" + counterDiff + " timestampDiff=" + timestampDiff);
                    long timeDiffPerCounterIncrement = Double.valueOf(timestampDiff / counterDiff).longValue();
                    long assumedTimestamp = previousPulseTimestamp;
                    for (long assumedCounter = previousPulseCounter + 1; assumedCounter < counter; assumedCounter++) {
                        assumedTimestamp += timeDiffPerCounterIncrement;
                        logger.warn("Assuming timestamp for missing packet " + assumedTimestamp);
                        pulseElectricityMeter.addTimestampAndMaintain(assumedTimestamp);
                    }
                }
            }
        }
        logger.debug("Adding timestamp " + timestamp + " for packet counter " + counter);
        pulseElectricityMeter.addTimestampAndMaintain(timestamp);
        previousPulseCounter = counter;
        previousPulseTimestamp = timestamp;
    }
}
