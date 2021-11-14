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

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.PullResistance;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
abstract public class GpioControllable implements ApplianceIdConsumer, Validateable {
    private transient Logger logger = LoggerFactory.getLogger(GpioControllable.class);
    @XmlAttribute
    private Integer gpio;
    @XmlAttribute
    private String pinPullResistance;
    private transient Context gpioController;
    private transient String applianceId;


    protected Context getGpioContext() {
        return gpioController;
    }

    public void setGpioContext(Context gpioController) {
        this.gpioController = gpioController;
    }

    protected Integer getPin() {
       return gpio;
    }

    protected PullResistance getPinPullResistance() {
        if(pinPullResistance != null) {
            if (pinPullResistance.equals("PULL_DOWN")) {
                return PullResistance.PULL_DOWN;
            }
            if (pinPullResistance.equals("PULL_UP")) {
                return PullResistance.PULL_UP;
            }
        }
        return PullResistance.OFF;
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
