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

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Switch extends GpioControllable implements Control, ApplianceIdConsumer {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Switch.class));
    @XmlAttribute
    private boolean reverseStates;
    @XmlTransient
    GpioPinDigitalOutput outputPin;
    @XmlTransient
    List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();


    @Override
    public void start() {
        logger.info("Switch uses " + getGpio() + (reverseStates ? " (reversed states)" : ""));
        if(getGpioController() != null) {
            outputPin = getGpioController().provisionDigitalOutputPin(getGpio(), adjustState(PinState.LOW));
        }
        else {
            logGpioAccessDisabled(logger);
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
