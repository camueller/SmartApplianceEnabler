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
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractRequest implements Request {
    private transient Logger logger = LoggerFactory.getLogger(AbstractRequest.class);
    private transient String applianceId;
    private transient Meter meter;
    private transient Control control;
    private transient boolean enabled;
    private transient boolean active;


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

    public boolean isEnabled() {
        return enabled;
    }

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
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
        if (isActive()) {
            if (meter != null) {
                if (switchOn) {
                    meter.startEnergyMeter();
                } else {
                    meter.stopEnergyMeter();
                }
            }
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
