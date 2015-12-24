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
package de.avanux.smartapplianceenabler.semp.webservice;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class Capabilities {
    @XmlElementWrapper(name="CurrentPower")
    @XmlElement(name = "Method")
    private List<String> currentPowerMethod;
    @XmlElementWrapper(name="Timestamps")
    @XmlElement(name = "AbsoluteTimestamps")
    private List<Boolean> absoluteTimestamps;
    @XmlElementWrapper(name="Interruptions")
    @XmlElement(name = "InterruptionsAllowed")
    private List<Boolean> interruptionsAllowed;
    @XmlElementWrapper(name="Requests")
    @XmlElement(name = "OptionalEnergy")
    private List<Boolean> optionalEnergy;

    public List<String> getCurrentPowerMethod() {
        return currentPowerMethod;
    }

    public void setCurrentPowerMethod(List<String> currentPowerMethod) {
        this.currentPowerMethod = currentPowerMethod;
    }

    public List<Boolean> getAbsoluteTimestamps() {
        return absoluteTimestamps;
    }

    public void setAbsoluteTimestamps(List<Boolean> absoluteTimestamps) {
        this.absoluteTimestamps = absoluteTimestamps;
    }

    public List<Boolean> getInterruptionsAllowed() {
        return interruptionsAllowed;
    }

    public void setInterruptionsAllowed(List<Boolean> interruptionsAllowed) {
        this.interruptionsAllowed = interruptionsAllowed;
    }

    public List<Boolean> getOptionalEnergy() {
        return optionalEnergy;
    }

    public void setOptionalEnergy(List<Boolean> optionalEnergy) {
        this.optionalEnergy = optionalEnergy;
    }
}
