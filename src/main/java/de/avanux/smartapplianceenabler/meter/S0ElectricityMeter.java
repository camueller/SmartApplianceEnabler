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
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Timer;

@XmlType(propOrder={"gpio", "pinPullResistance", "impulsesPerKwh", "measurementInterval"})
public class S0ElectricityMeter extends GpioControllable implements Meter {

    private transient Logger logger = LoggerFactory.getLogger(S0ElectricityMeter.class);
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    private transient GpioPinDigitalInput inputPin;
    private transient PulsePowerMeter pulsePowerMeter = new PulsePowerMeter();
    private transient PulseEnergyMeter pulseEnergyMeter = new PulseEnergyMeter();


    public Integer getImpulsesPerKwh() {
        return impulsesPerKwh;
    }

    public Integer getMeasurementInterval() {
        return measurementInterval != null ? measurementInterval : S0ElectricityMeterDefaults.getMeasurementInterval();
    }

    public void setControl(Control control) {
        this.pulsePowerMeter.setControl(control);
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.pulsePowerMeter.setApplianceId(applianceId);
        this.pulseEnergyMeter.setApplianceId(applianceId);
    }

    @Override
    public int getAveragePower() {
        return pulsePowerMeter.getAveragePower();
    }

    @Override
    public int getMinPower() {
        return pulsePowerMeter.getMinPower();
    }

    @Override
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
    public void init() {
        pulsePowerMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulsePowerMeter.setMeasurementInterval(getMeasurementInterval());
        pulseEnergyMeter.setImpulsesPerKwh(impulsesPerKwh);
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting {} for {}", getApplianceId(), getClass().getSimpleName(), getGpio());
        GpioController gpioController = getGpioController();
        if(gpioController != null) {
            try {
                inputPin = gpioController.provisionDigitalInputPin(getGpio(), getPinPullResistance());
                inputPin.addListener(new GpioPinListenerDigital() {
                    @Override
                    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                        logger.debug("{}: GPIO {} changed to {}", getApplianceId(), event.getPin(), event.getState());
                        if(event.getState() == PinState.HIGH) {
                            pulsePowerMeter.addTimestampAndMaintain(System.currentTimeMillis());
                            pulseEnergyMeter.increasePulseCounter();
                        }
                    }
                });
                logger.debug("{}: {} uses {}", getApplianceId(), getClass().getSimpleName(), getGpio());
            }
            catch(Exception e) {
                logger.error("{}: Error start metering using {}", getApplianceId(), getGpio(), e);
            }
        }
        else {
            logGpioAccessDisabled(logger);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping {} for {}", getApplianceId(), getClass().getSimpleName(), getGpio());
        GpioController gpioController = getGpioController();
        if(gpioController != null) {
            gpioController.unprovisionPin(inputPin);
        }
        else {
            logGpioAccessDisabled(logger);
        }
    }
}
