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
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    /**
     * Reference to the cached shared input. When non-null, this indicates that
     * GpioControllable should NOT call digitalInput.close() during shutdown since
     * the instance is shared and managed by GpioAccessProvider's cache.
     */
    private transient GpioAccessProvider.SharedDigitalInput sharedInput;
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
        // Use cached shared input to prevent Pi4J ComponentRegistry accumulation.
        // Each pi4jContext.create(DigitalInput...) registers a new instance that is never unregistered,
        // causing unbounded heap growth over repeated start/stop cycles.
        this.sharedInput = GpioAccessProvider.provideSharedDigitalInput(gpio);
        if (sharedInput != null) {
            this.digitalInput = sharedInput.getDelegate();
            logger.debug("{}: Using shared DigitalInput for GPIO {}", applianceId, gpio);
        } else {
            logGpioAccessDisabled(logger);
        }
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
        if (sharedInput != null) {
            // Use shared input refcount - close only releases when last user decrements
            try {
                sharedInput.close();
            } catch (Exception e) {
                logger.error("{}: Error releasing shared digital input on GPIO {}", applianceId, gpio, e);
            }
            this.sharedInput = null;
            this.digitalInput = null;
        } else if (digitalInput != null) {
            // Legacy path - close directly on non-cached instances
            try {
                digitalInput.close();
            } catch (Exception e) {
                logger.error("{}: Error shutting down digital input on GPIO {}", applianceId, gpio, e);
            }
            this.digitalInput = null;
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
