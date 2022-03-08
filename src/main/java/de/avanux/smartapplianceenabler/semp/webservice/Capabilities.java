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
package de.avanux.smartapplianceenabler.semp.webservice;

import java.util.ArrayList;
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

    public CurrentPowerMethod getCurrentPowerMethod() {
        if(currentPowerMethod != null && currentPowerMethod.size() > 0) {
            return CurrentPowerMethod.valueOf(currentPowerMethod.get(0));
        }
        return null;
    }

    public void setCurrentPowerMethod(CurrentPowerMethod currentPowerMethod) {
        if(currentPowerMethod != null) {
            if(this.currentPowerMethod == null) {
                this.currentPowerMethod = new ArrayList<String>();
            }
            else {
                this.currentPowerMethod.clear();
            }
            this.currentPowerMethod.add(currentPowerMethod.name());
        }
        else {
            this.currentPowerMethod = null;
        }
    }

    public Boolean getAbsoluteTimestamps() {
        if(absoluteTimestamps != null && absoluteTimestamps.size() > 0) {
            return absoluteTimestamps.get(0);
        }
        return null;
    }

    public void setAbsoluteTimestamps(Boolean absoluteTimestamps) {
        if(absoluteTimestamps != null) {
            if(this.absoluteTimestamps == null) {
                this.absoluteTimestamps = new ArrayList<Boolean>();
            }
            else {
                this.absoluteTimestamps.clear();
            }
            this.absoluteTimestamps.add(absoluteTimestamps);
        }
        else {
            this.absoluteTimestamps = null;
        }
    }

    public Boolean getInterruptionsAllowed() {
        if(interruptionsAllowed != null && interruptionsAllowed.size() > 0) {
            return interruptionsAllowed.get(0);
        }
        return null;
    }

    public void setInterruptionsAllowed(Boolean interruptionsAllowed) {
        if(interruptionsAllowed != null) {
            if(this.interruptionsAllowed == null) {
                this.interruptionsAllowed = new ArrayList<Boolean>();
            }
            else {
                this.interruptionsAllowed.clear();
            }
            this.interruptionsAllowed.add(interruptionsAllowed);
        }
        else {
            this.interruptionsAllowed = null;
        }
    }

    public Boolean getOptionalEnergy() {
        if(optionalEnergy != null && optionalEnergy.size() > 0) {
            return optionalEnergy.get(0);
        }
        return null;
    }

    public void setOptionalEnergy(Boolean optionalEnergy) {
        if(optionalEnergy != null) {
            if(this.optionalEnergy == null) {
                this.optionalEnergy = new ArrayList<Boolean>();
            }
            else {
                this.optionalEnergy.clear();
            }
            this.optionalEnergy.add(optionalEnergy);
        }
        else {
            this.optionalEnergy = null;
        }
    }

    @Override
    public String toString() {
        return "Capabilities{" +
                "currentPowerMethod=" + getCurrentPowerMethod() +
                ", absoluteTimestamps=" + getAbsoluteTimestamps() +
                ", interruptionsAllowed=" + getInterruptionsAllowed() +
                ", optionalEnergy=" + getOptionalEnergy() +
                '}';
    }
}
