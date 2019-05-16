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
import de.avanux.smartapplianceenabler.control.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class S0ElectricityMeterNetworked implements Meter, PulseReceiver.PulseListener, ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(S0ElectricityMeterNetworked.class);
    @XmlAttribute
    private String idref;
    @XmlAttribute
    private Integer port;
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    private transient PulsePowerMeter pulsePowerMeter = new PulsePowerMeter();
    private transient PulseEnergyMeter pulseEnergyMeter = new PulseEnergyMeter();
    private transient PulseReceiver pulseReceiver;
    private transient String applianceId;
    private transient Long previousPulseTimestamp;
    private transient Long previousPulseCounter;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.pulsePowerMeter.setApplianceId(applianceId);
        this.pulseEnergyMeter.setApplianceId(applianceId);
    }

    public String getIdref() {
        return idref;
    }

    public void setIdref(String idref) {
        this.idref = idref;
    }

    public void setPulseReceiver(PulseReceiver pulseReceiver) {
        this.pulseReceiver = pulseReceiver;
    }

    public int getAveragePower() {
        return pulsePowerMeter.getAveragePower();
    }

    public int getMinPower() {
        return pulsePowerMeter.getMinPower();
    }

    public int getMaxPower() {
        return pulsePowerMeter.getMaxPower();
    }

    @Override
    public float getEnergy() {
        return this.pulseEnergyMeter.getEnergy();
    }

    @Override
    public void startEnergyMeter() {
        this.pulseEnergyMeter.startEnergyCounter();
    }

    @Override
    public void stopEnergyMeter() {
        this.pulseEnergyMeter.stopEnergyCounter();
    }

    @Override
    public void resetEnergyMeter() {
        this.pulseEnergyMeter.resetEnergyCounter();
    }

    @Override
    public boolean isOn() {
        return pulsePowerMeter.isOn();
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval != null ? measurementInterval : S0ElectricityMeterDefaults.getMeasurementInterval();
    }

    public void setControl(Control control) {
        this.pulsePowerMeter.setControl(control);
    }

    public void start(Timer timer) {
        logger.debug("{}: Appliance start: impulsesPerKwh={} measurementInterval={}", applianceId, impulsesPerKwh,
                getMeasurementInterval());
        pulsePowerMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulsePowerMeter.setMeasurementInterval(getMeasurementInterval());
        if(pulseReceiver != null) {
            pulseReceiver.addListener(applianceId, this);
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void pulseReceived(long counter) {
        long timestamp = System.currentTimeMillis();
        if(previousPulseCounter != null) {
            if(counter < previousPulseCounter) {
                logger.debug("{}: Counter overflow - skipping detection of missing packets", applianceId);
            }
            else {
                long counterDiff = counter - previousPulseCounter;
                if(counterDiff > 1) {
                    long timestampDiff = timestamp - previousPulseTimestamp;
                    logger.warn("{}: Missing packet detected: counterDiff={} timestampDiff={}", applianceId, counterDiff, timestampDiff);
                    long timeDiffPerCounterIncrement = Double.valueOf(timestampDiff / counterDiff).longValue();
                    long assumedTimestamp = previousPulseTimestamp;
                    for (long assumedCounter = previousPulseCounter + 1; assumedCounter < counter; assumedCounter++) {
                        assumedTimestamp += timeDiffPerCounterIncrement;
                        logger.warn("{}: Assuming timestamp for missing packet {}", applianceId, assumedTimestamp);
                        pulsePowerMeter.addTimestampAndMaintain(assumedTimestamp);
                        pulseEnergyMeter.increasePulseCounter();
                    }
                }
            }
        }
        logger.debug("{}: Adding timestamp {} for packet counter {}", applianceId, timestamp, counter);
        pulsePowerMeter.addTimestampAndMaintain(timestamp);
        previousPulseCounter = counter;
        previousPulseTimestamp = timestamp;
    }
}
