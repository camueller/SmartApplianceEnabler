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

import de.avanux.smartapplianceenabler.control.*;
import de.avanux.smartapplianceenabler.control.ev.EVChargerControl;
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.avanux.smartapplianceenabler.appliance.*;

import java.time.LocalDateTime;

import java.util.Timer;

/**
 * A switch which is always switched on.
 */
public class PartnerSwitch implements Control, ControlStateChangedListener, ApplianceIdConsumer {

    private String partnerId = "F-30078930-000000000001-01";
    private transient Logger  logger = LoggerFactory.getLogger(Appliance.class);
    private transient boolean on;
    private transient String  applianceId;

    @Override
    public void init() 
    {
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {

        Appliance app = ApplianceManager.getInstance().getAppliance(partnerId);
        if (app == null)
        {
            logger.warn("{}: Partner appliance with id {} not found", applianceId, partnerId);
            return;
        }
        Control control = app.getControl();
        if(control instanceof Switch)
        {
            control.addControlStateChangedListener(this);
            // initialize with inverted state of partner appliance
            this.on = !control.isOn();
        }
        else
        {
            logger.warn("{}: Partner Control was not type switch!", applianceId);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        Appliance app = ApplianceManager.getInstance().getAppliance(partnerId);
        if (app == null)
        {
            logger.warn("{}: Partner appliance with id {} not found", applianceId, partnerId);
            return;
        }
        Control control = app.getControl();
        if(control instanceof Switch)
        {
            control.removeControlStateChangedListener(this);
        }
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {}", applianceId, (switchOn ? "on" : "off"));
        on = !switchOn;
        return true;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
    }

    @Override
    public void removeControlStateChangedListener(ControlStateChangedListener listener) {
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn)
    {
        logger.info("{}: Switching to oposite state as partner");
        this.on(now, !switchOn);
    }

    public void onEVChargerStateChanged(LocalDateTime now, EVChargerState previousState, EVChargerState newState,
                                 ElectricVehicle ev)
    {};

    public void onEVChargerSocChanged(LocalDateTime now, Float soc)
    {};
}
