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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.pigpioj.PigpioCallback;
import uk.pigpioj.PigpioInterface;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
abstract public class GpioControllable implements ApplianceIdConsumer, Validateable, GpioControllableUser {
    private transient Logger logger = LoggerFactory.getLogger(GpioControllable.class);
    // FIXME rename to pin
    @XmlAttribute
    private Integer gpio;
    private transient PigpioInterface pigpioInterface;
    private transient String applianceId;

    @Override
    public Set<GpioControllable> getGpioControllables() {
        return Collections.singleton(this);
    }

    protected PigpioInterface getPigpioInterface() {
        return pigpioInterface;
    }

    public void setPigpioInterface(PigpioInterface piGpio) {
        this.pigpioInterface = piGpio;
    }

    protected boolean isPigpioInterfaceAvailable() {
        return this.pigpioInterface != null;
    }

    protected Integer getPin() {
       return gpio;
    }

    protected void setMode(PinMode mode) throws IOException {
        int rc = pigpioInterface.setMode(getPin(), mode.getNumVal());
        if (rc < 0) {
            throw new IOException("pigpioInterface.setMode returned " + rc);
        }
    }

    protected void setPinPullResistance(PinPullResistance pinPullResistance) throws IOException {
        int rc = pigpioInterface.setPullUpDown(getPin(), pinPullResistance.getNumVal());
        if (rc < 0) {
            throw new IOException("pigpioInterface.setPinPullResistance returned " + rc);
        }
    }

    protected void enableListener(PigpioCallback listener, PinEdge edge) throws IOException {
        int rc = pigpioInterface.enableListener(getPin(), edge.getNumVal(), listener);
        if (rc < 0) {
            throw new IOException("pigpioInterface.enableListener returned " + rc);
        }
    }

    protected void disableListener() throws IOException {
        int rc = pigpioInterface.disableListener(getPin());
        if (rc < 0) {
            throw new IOException("pigpioInterface.disableListener returned " + rc);
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
