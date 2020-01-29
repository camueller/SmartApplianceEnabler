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
public class RuntimeRequest extends AbstractRequest implements Request, ControlStateChangedListener, StartingCurrentSwitchListener {
    @XmlAttribute
    private Integer min;
    @XmlAttribute
    private int max;
    private transient Logger logger = LoggerFactory.getLogger(RuntimeRequest.class);
    private transient boolean wasRunning;
    private transient LocalDateTime controlStatusChangedAt;

    public RuntimeRequest() {
    }

    public RuntimeRequest(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    public Integer getMin(LocalDateTime now) {
        if(min != null && isActive() && getControl().isOn()) {
            return min - getSecondsSinceStatusChange(now);
        }
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax(LocalDateTime now) {
        if(isActive() && getControl().isOn()) {
            return max - getSecondsSinceStatusChange(now);
        }
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
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
        if(isActive()) {
            if(switchOn) {
                wasRunning = true;
            }
            else {
                if(controlStatusChangedAt != null) {
                    int secondsSinceStatusChange = getSecondsSinceStatusChange(now);
                    int newMax = this.max - secondsSinceStatusChange;
                    this.max = newMax > 0 ? newMax : 0;
                    if(this.min != null) {
                        int newMin = this.min - secondsSinceStatusChange;
                        this.min = newMin > 0 ? newMin : 0;
                    }
                }
            }
            controlStatusChangedAt = now;
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

    protected int getSecondsSinceStatusChange(LocalDateTime now) {
        try {
            if(this.controlStatusChangedAt != null && now != null) {
                Interval runtimeSinceStatusChange = new Interval(this.controlStatusChangedAt.toDateTime(), now.toDateTime());
                return Double.valueOf(runtimeSinceStatusChange.toDuration().getMillis() / 1000.0).intValue();
            }
        }
        catch(IllegalArgumentException e) {
            logger.warn("{} Invalid interval: start={} end={}", getApplianceId(), this.controlStatusChangedAt.toDateTime(),
                    now.toDateTime());
        }
        return 0;
    }

    @Override
    public String toString() {
        String text = isEnabled() ? "ENABLED" : "DISABLED";
        if(min != null) {
            text += min.toString();
        }
        else {
            text += "?";
        }
        text += "s/";
        text += max;
        text += "s";
        return text;
    }
}
