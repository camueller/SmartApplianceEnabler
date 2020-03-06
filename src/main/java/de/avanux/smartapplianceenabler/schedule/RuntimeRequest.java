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
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "min", "max" })
public class RuntimeRequest extends AbstractRequest implements StartingCurrentSwitchListener {
    @XmlAttribute
    private Integer min;
    @XmlAttribute
    private int max;

    public RuntimeRequest() {
    }

    public RuntimeRequest(Integer min, int max) {
        setMin(min);
        setMax(max);
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(RuntimeRequest.class);
    }

    public Integer getMin(LocalDateTime now) {
        if(min != null && isActive()) {
            return min - getRuntime(now);
        }
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax(LocalDateTime now) {
        if(isActive()) {
            return max - getRuntime(now);
        }
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public boolean isUsingOptionalEnergy() {
        return (this.min != null && this.min == 0 && this.max > 0);
    }

    @Override
    public Boolean isAcceptControlRecommendations() {
        return super.isAcceptControlRecommendations() != null ? super.isAcceptControlRecommendations() : true;
    }

    public boolean hasStartingCurrentSwitch() {
        return getControl() instanceof StartingCurrentSwitch;
    }

    @Override
    public void setControl(Control control) {
        super.setControl(control);
        if (control instanceof StartingCurrentSwitch) {
            ((StartingCurrentSwitch) control).addStartingCurrentSwitchListener(this);
        }
    }

    @Override
    public void startingCurrentDetected(LocalDateTime now) {
        if (isActive()) {
            setEnabled(true);
        }
    }

    @Override
    public void finishedCurrentDetected() {
        setEnabled(false);
        resetEnabledBefore();
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return getMax(now) <= 0;
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval, TimeframeInterval activatedInterval, boolean wasRunning) {
        super.activeIntervalChanged(now, applianceId, deactivatedInterval, activatedInterval, wasRunning);
        if(deactivatedInterval != null && deactivatedInterval.getRequest() == this) {
            setEnabled(false);
        }
    }

    @Override
    public String toString() {
        return toString(LocalDateTime.now());
    }

    @Override
    public String toString(LocalDateTime now) {
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
