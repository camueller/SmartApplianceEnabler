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

package de.avanux.smartapplianceenabler.control.ev;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleCharger implements Control, ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(ElectricVehicleCharger.class);
    @XmlAttribute
    private Integer voltage = 230;
    @XmlAttribute
    private Integer phases = 1;
    @XmlElements({
            @XmlElement(name = "EVModbusControl", type = EVModbusControl.class),
    })
    private EVControl evControl;
    private transient String applianceId;
    private transient boolean vehicleConnected = false;
    private transient boolean useOptionalEnergy = true;
    transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        evControl.setApplianceId(applianceId);
    }

    public EVControl getEvControl() {
        return evControl;
    }

    public void init() {
        logger.debug("{}: voltage={} phases={}", this.applianceId, this.voltage, this.phases);
        evControl.validate();
    }

    public void start(Timer timer) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean currentVehicleConnectedState = evControl.isVehicleConnected();
                if (vehicleConnected != currentVehicleConnectedState) {
                    logger.debug("{}: State changed: vehicleConnected = {}", applianceId, currentVehicleConnectedState);
                }
                else {
                    logger.debug("{}: vehicleConnected = {}", applianceId, currentVehicleConnectedState);
                }
                vehicleConnected = currentVehicleConnectedState;
            }
        }, 0, evControl.getVehicleStatusPollInterval() * 1000);
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        if(switchOn) {
            logger.info("{}: Switching on", applianceId);
            startCharging();
        }
        else {
            logger.info("{}: Switching off", applianceId);
            stopCharging();
        }
        for(ControlStateChangedListener listener : controlStateChangedListeners) {
            listener.controlStateChanged(now, switchOn);
        }
        return true;
    }

    @Override
    public boolean isOn() {
        return evControl.isCharging();
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    public boolean isVehicleConnected() {
        return vehicleConnected;
    }

    public boolean isUseOptionalEnergy() {
        return useOptionalEnergy;
    }

    public void setChargePower(int power) {
        int current = Float.valueOf(power / (this.voltage * this.phases)).intValue();
        logger.debug("{}: Set charge power: {}W corresponds to {}A", applianceId, power, current);
        evControl.setChargeCurrent(current);
    }

    public void startCharging() {
        logger.debug("{}: Start charging process", applianceId);
        evControl.startCharging();
    }

    public void stopCharging() {
        logger.debug("{}: Stop charging process", applianceId);
        evControl.stopCharging();
    }

}
