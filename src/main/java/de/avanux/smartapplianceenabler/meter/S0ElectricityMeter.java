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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.GpioControllable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"gpio", "pinPullResistance", "impulsesPerKwh", "measurementInterval"})
public class S0ElectricityMeter extends GpioControllable implements Meter {
    private transient Logger logger = LoggerFactory.getLogger(S0ElectricityMeter.class);
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    private transient GpioPinDigitalInput inputPin;
    private transient PulseElectricityMeter pulseElectricityMeter = new PulseElectricityMeter();

    
    public Integer getImpulsesPerKwh() {
        return impulsesPerKwh;
    }

    public Integer getMeasurementInterval() {
        return measurementInterval != null ? measurementInterval : S0ElectricityMeterDefaults.getMeasurementInterval();
    }

    public void setControl(Control control) {
        this.pulseElectricityMeter.setControl(control);
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
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

    @Override
    public void start() {
        pulseElectricityMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulseElectricityMeter.setMeasurementInterval(getMeasurementInterval());

        GpioController gpioController = getGpioController();
        if(gpioController != null) {
            try {
                inputPin = gpioController.provisionDigitalInputPin(getGpio(), getPinPullResistance());
                inputPin.addListener(new GpioPinListenerDigital() {
                    @Override
                    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                        logger.debug("{}: GPIO {} changed to {}", getApplianceId(), event.getPin(), event.getState());
                        if(event.getState() == PinState.HIGH) {
                            pulseElectricityMeter.addTimestampAndMaintain(System.currentTimeMillis());
                        }
                    }
                });
                logger.debug("{}: Starting {} for {}", getApplianceId(), getClass().getSimpleName(), getGpio());
            }
            catch(Exception e) {
                logger.error("{}: Error start metering using {}", getApplianceId(), getGpio(), e);
            }
        }
        else { 
            logGpioAccessDisabled();
        }
    }

    public void stop() {
        super.stop();
        GpioController gpioController = getGpioController();
        if(gpioController != null) {
            gpioController.unprovisionPin(inputPin);
        }
    }
}
