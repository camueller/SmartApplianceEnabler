/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.meter.Meter;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
abstract public class AbstractRequest implements Request {
    private transient Logger logger = LoggerFactory.getLogger(AbstractRequest.class);
    private transient String applianceId;
    private transient Meter meter;
    private transient Control control;
    private transient boolean enabled;
    private transient boolean enabledBefore;
    private transient boolean active;
    private transient int runtimeUntilLastStatusChange;
    private transient LocalDateTime controlStatusChangedAt;


    public AbstractRequest() {
    }

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    protected String getApplianceId() {
        return applianceId;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    protected Meter getMeter() {
        return meter;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    protected Control getControl() {
        return control;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isEnabledBefore() {
        return enabledBefore;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isActive() {
        return active;
    }

    public void onTimeframeIntervalStateChanged(LocalDateTime now, TimeframeIntervalState previousState,
                                                TimeframeIntervalState newState) {
        this.active = false;
        if(newState == TimeframeIntervalState.ACTIVE) {
            this.active = true;
        }
    }

    @Override
    public Integer getRuntime(LocalDateTime now) {
        return runtimeUntilLastStatusChange + getSecondsSinceStatusChange(now);
    }

    @Override
    public LocalDateTime getControlStatusChangedAt() {
        return controlStatusChangedAt;
    }

    protected int getSecondsSinceStatusChange(LocalDateTime now) {
        try {
            if(controlStatusChangedAt != null && now != null) {
                Interval runtimeSinceStatusChange = new Interval(controlStatusChangedAt.toDateTime(), now.toDateTime());
                return Double.valueOf(runtimeSinceStatusChange.toDuration().getMillis() / 1000.0).intValue();
            }
        }
        catch(IllegalArgumentException e) {
            logger.warn("{} Invalid interval: start={} end={}", getApplianceId(), controlStatusChangedAt.toDateTime(),
                    now.toDateTime());
        }
        return 0;
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
        if (isActive()) {
            if (switchOn) {
                enabledBefore = true;
                if (meter != null) {
                    meter.startEnergyMeter();
                }
            } else {
                if (meter != null) {
                    meter.stopEnergyMeter();
                }
                runtimeUntilLastStatusChange += getSecondsSinceStatusChange(now);
            }
            controlStatusChangedAt = now;
        }
    }

    @Override
    public void onEVChargerStateChanged(LocalDateTime now, EVChargerState previousState, EVChargerState newState,
                                        ElectricVehicle ev) {
    }

    @Override
    public void onEVChargerSocChanged(LocalDateTime now, Float soc) {
    }

    @Override
    public String toString() {
        return isEnabled() ? "ENABLED" : "DISABLED";
    }
}
