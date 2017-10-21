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
package de.avanux.smartapplianceenabler.appliance;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@XmlType(propOrder={"gpio", "pinPullResistance", "impulsesPerKwh", "measurementInterval"})
public class S0ElectricityMeter extends GpioControllable implements Meter {
    private transient ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(S0ElectricityMeter.class));
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer measurementInterval = 60; // seconds
    @XmlAttribute
    private boolean powerOnAlways;
    private transient PulseElectricityMeter pulseElectricityMeter = new PulseElectricityMeter();

    
    public Integer getImpulsesPerKwh() {
        return impulsesPerKwh;
    }

    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void setControl(Control control) {
        this.pulseElectricityMeter.setControl(control);
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.logger.setApplianceId(applianceId);
        this.pulseElectricityMeter.setApplianceId(applianceId);
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
    public boolean isOn() {
        return pulseElectricityMeter.isOn();
    }

    public void start() {
        pulseElectricityMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulseElectricityMeter.setMeasurementInterval(measurementInterval);
        pulseElectricityMeter.setPowerOnAlways(powerOnAlways);

        if(getGpioController() != null) {
            final GpioPinDigitalInput input = getGpioController().provisionDigitalInputPin(getGpio(), getPinPullResistance());
            input.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    logger.debug("GPIO " + event.getPin() + " changed to " + event.getState());
                    if(event.getState() == PinState.HIGH) {
                        pulseElectricityMeter.addTimestampAndMaintain(System.currentTimeMillis());
                    }
                }
            });
            logger.info("Start metering using " + getGpio());
        }
        else { 
            logGpioAccessDisabled(logger);
        }
    }
}
