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
package de.avanux.smartapplianceenabler.control;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Switch extends GpioControllable implements Control, ApplianceIdConsumer {
    private transient ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Switch.class));
    @XmlAttribute
    private boolean reverseStates;
    private transient GpioPinDigitalOutput outputPin;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();


    @Override
    public void start() {
        GpioController gpioController = getGpioController();
        if(gpioController != null) {
            try {
                outputPin = gpioController.provisionDigitalOutputPin(getGpio(), adjustState(PinState.LOW));
                logger.info("Switch uses " + getGpio() + (reverseStates ? " (reversed states)" : ""));
            }
            catch(Exception e) {
                logger.error("Error starting " + getClass().getSimpleName() + " for " + getGpio(), e);
            }
        }
        else {
            logGpioAccessDisabled(logger);
        }
    }

    public void stop() {
        super.stop();
        GpioController gpioController = getGpioController();
        if(gpioController != null) {
            gpioController.unprovisionPin(outputPin);
        }
    }

    @Override
    public boolean on(boolean switchOn) {
        logger.info("Switching " + (switchOn ? "on" : "off") + getGpio());
        if(outputPin != null) {
            outputPin.setState(adjustState(switchOn ? PinState.HIGH : PinState.LOW));
        }
        else {
            logGpioAccessDisabled(logger);
        }
        for(ControlStateChangedListener listener : controlStateChangedListeners) {
            listener.controlStateChanged(switchOn);
        }
        return true;
    }

    @Override
    public boolean isOn() {
        if(outputPin != null) {
            return adjustState(outputPin.getState()) == PinState.HIGH;
        }
        logGpioAccessDisabled(logger);
        return false;
    }
    
    private PinState adjustState(PinState pinState) {
        if(reverseStates) {
            if(pinState == PinState.HIGH) {
                return PinState.LOW;
            }
            return PinState.HIGH;
        }
        return pinState;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.logger.setApplianceId(applianceId);
    }
}
