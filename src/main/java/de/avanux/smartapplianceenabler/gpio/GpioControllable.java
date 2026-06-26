/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.gpio;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmType;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collections;
import java.util.Set;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
abstract public class GpioControllable implements ApplianceIdConsumer, Validateable, GpioControllableUser {
    private transient Logger logger = LoggerFactory.getLogger(GpioControllable.class);
    // FIXME rename to pin
    @XmlAttribute
    private Integer gpio;
    private transient Context pi4jContext;
    private transient DigitalInput digitalInput;
    private transient DigitalOutput digitalOutput;
    private transient Pwm pwm;
    private transient String applianceId;

    @Override
    public Set<GpioControllable> getGpioControllables() {
        return Collections.singleton(this);
    }

    public void setPi4jContext(Context context) {
        this.pi4jContext = context;
    }

    protected boolean isGpioAvailable() {
        return this.pi4jContext != null;
    }

    protected Integer getPin() {
       return gpio;
    }

    protected void initializeOutput() {
        digitalOutput = pi4jContext.create(DigitalOutput.newConfigBuilder(pi4jContext)
                .id("output-" + gpio)
                .address(gpio)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("ffm-digital-output")
                .build());
    }

    protected void initializeInput(PinPullResistance pinPullResistance) {
        digitalInput = pi4jContext.create(DigitalInput.newConfigBuilder(pi4jContext)
                .id("input-" + gpio)
                .address(gpio)
                .pull(pinPullResistance.toPi4jPullResistance())
                .provider("ffm-digital-input")
                .build());
    }

    protected void initializePwm(int chip, int channel, int frequency) {
        pwm = pi4jContext.create(Pwm.newConfigBuilder(pi4jContext)
                .id("pwm-" + gpio)
                .chip(chip)
                .channel(channel)
                .pwmType(PwmType.HARDWARE)
                .frequency(frequency)
                .dutyCycle(0)
                .provider("ffm-pwm")
                .build());
    }

    protected DigitalInput getDigitalInput() {
        return digitalInput;
    }

    protected DigitalOutput getDigitalOutput() {
        return digitalOutput;
    }

    protected Pwm getPwm() {
        return pwm;
    }

    protected void shutdownInput() {
        if (digitalInput != null) {
            try {
                digitalInput.close();
            } catch (Exception e) {
                logger.error("{}: Error shutting down digital input on GPIO {}", applianceId, gpio, e);
            }
        }
    }

    protected void logGpioAccessDisabled(Logger logger) {
        logger.warn("{}: Configured for {}, but GPIO access disabled.", applianceId, getPin());
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    protected String getApplianceId() {
        return applianceId;
    }

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", applianceId);
        if(gpio == null) {
            logger.error("{}: Missing 'gpio' property", applianceId);
            throw new ConfigurationException();
        }
    }
}
