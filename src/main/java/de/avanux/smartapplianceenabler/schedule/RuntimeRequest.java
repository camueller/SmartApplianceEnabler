/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitchListener;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "min", "max" })
public class RuntimeRequest extends AbstractRequest implements StartingCurrentSwitchListener {
    @XmlAttribute
    private Integer min;
    @XmlAttribute
    private int max;
    private transient Logger logger = LoggerFactory.getLogger(RuntimeRequest.class);
    private transient Integer currentMin;
    private transient Integer currentMax;

    public RuntimeRequest() {
    }

    public RuntimeRequest(Integer min, Integer max) {
        setMin(min);
        setMax(max);
    }

    public Integer getMin(LocalDateTime now) {
        if(currentMin == null) {
            currentMin = min;
        }
        if(currentMin != null && isActive() && getControl().isOn()) {
            return currentMin - getSecondsSinceStatusChange(now);
        }
        return currentMin;
    }

    public void setMin(Integer min) {
        this.min = min;
        this.currentMin = min;
    }

    public Integer getMax(LocalDateTime now) {
        if(currentMax == null) {
            currentMax = max;
        }
        if(isActive() && getControl().isOn()) {
            return currentMax - getSecondsSinceStatusChange(now);
        }
        return currentMax;
    }

    public void setMax(Integer max) {
        this.max = max;
        this.currentMax = max;
    }

    @Override
    public boolean isUsingOptionalEnergy() {
        return (this.min != null && this.min == 0 && this.max > 0);
    }

    @Override
    public void setControl(Control control) {
        super.setControl(control);
        control.addControlStateChangedListener(this);
        if (control instanceof StartingCurrentSwitch) {
            setEnabled(false);
            ((StartingCurrentSwitch) control).addStartingCurrentSwitchListener(this);
        }
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
        super.controlStateChanged(now, switchOn);
        if(isActive()) {
            if(! switchOn) {
                int secondsSinceStatusChange = getSecondsSinceStatusChange(now);
                int newMax = this.currentMax - secondsSinceStatusChange;
                this.currentMax = newMax > 0 ? newMax : 0;
                if(this.currentMin != null) {
                    int newMin = this.currentMin - secondsSinceStatusChange;
                    this.currentMin = newMin > 0 ? newMin : 0;
                }
            }
        }
    }

    @Override
    public void startingCurrentDetected(LocalDateTime now) {
        setEnabled(true);
    }

    @Override
    public void finishedCurrentDetected() {
        setEnabled(false);
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return getMax(now) <= 0;
    }

    @Override
    public String toString() {
        LocalDateTime now = new LocalDateTime();
        Integer min = getMin(now);
        Integer max = getMax(now);
        String text = super.toString();
        text += "/";
        if(min != null) {
            text += min.toString();
        }
        else {
            text += "_";
        }
        text += "s/";
        text += max;
        text += "s";
        return text;
    }
}
