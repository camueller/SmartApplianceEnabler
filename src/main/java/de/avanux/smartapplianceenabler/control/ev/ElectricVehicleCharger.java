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

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleCharger implements Control, ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(ElectricVehicleCharger.class);
    @XmlAttribute
    private Integer voltage = 230;
    @XmlAttribute
    private Integer phases = 1;
    @XmlAttribute
    private Integer pollInterval = 10; // seconds
    @XmlAttribute
    protected Integer startChargingStateDetectionDelay = 300;
    @XmlElements({
            @XmlElement(name = "EVModbusControl", type = EVModbusControl.class),
    })
    private EVControl control;
    private transient Appliance appliance;
    private transient String applianceId;
    private transient Vector<State> stateHistory = new Vector<>();
    private transient boolean useOptionalEnergy = true;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient Long startChargingTimestamp;

    protected enum State {
        VEHICLE_NOT_CONNECTED,
        VEHICLE_CONNECTED,
        CHARGING,
        CHARGING_COMPLETED
    }

    public void setAppliance(Appliance appliance) {
        this.appliance = appliance;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        control.setApplianceId(applianceId);
    }

    public EVControl getControl() {
        return control;
    }

    protected void setControl(EVControl control) {
        this.control = control;
    }

    public void init() {
        logger.debug("{}: voltage={} phases={} startChargingStateDetectionDelay={}",
                this.applianceId, this.voltage, this.phases, this.startChargingStateDetectionDelay);
        initStateHistory();
        control.validate();
    }

    public void start(Timer timer) {
        stopCharging();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateState();            }
        }, 0, control.getVehicleStatusPollInterval() * 1000);
    }

    /**
     * Returns true, if the state update was performed. This does not necessarily mean that the state has changed!
     * @return
     */
    protected boolean updateState() {
        if(isWithinStartChargingStateDetectionDelay()) {
            logger.debug("{}: Skipping state detection for {}s after switched on.", applianceId, this.startChargingStateDetectionDelay);
            return false;
        }
        State previousState = getState();
        State currentState = getNewState(previousState);
        if(currentState != previousState) {
            logger.debug("{}: Vehicle state changed: previousState={} newState={}", applianceId, previousState, currentState);
            stateHistory.add(currentState);
            onStateChanged(previousState, currentState);
        }
        else {
            logger.debug("{}: Vehicle state={}", applianceId, currentState);
        }
        return true;
    }

    public State getState() {
        return stateHistory.lastElement();
    }

    protected void setState(State state) {
        this.stateHistory.add(state);
    }

    public boolean wasInState(State state) {
        return stateHistory.contains(state);
    }

    private void initStateHistory() {
        this.stateHistory.clear();
        stateHistory.add(State.VEHICLE_NOT_CONNECTED);
    }

    protected State getNewState(State currenState) {
        State newState = currenState;
        if(currenState == State.VEHICLE_NOT_CONNECTED) {
            if (control.isVehicleConnected()) {
                newState = State.VEHICLE_CONNECTED;
            }
        }
        else if(currenState == State.VEHICLE_CONNECTED) {
            if(control.isCharging()) {
                newState = State.CHARGING;
            }
            else if(wasInState(State.CHARGING) && control.isChargingCompleted()) {
                newState = State.CHARGING_COMPLETED;
            }
            else if(control.isVehicleNotConnected()) {
                newState = State.VEHICLE_NOT_CONNECTED;
            }
        }
        else if(currenState == State.CHARGING) {
            if(! control.isCharging()) {
                if(control.isChargingCompleted()) {
                    newState = State.CHARGING_COMPLETED;
                }
                if(control.isVehicleConnected()) {
                    newState = State.VEHICLE_CONNECTED;
                }
                else if(control.isVehicleNotConnected()) {
                    newState = State.VEHICLE_NOT_CONNECTED;
                }
            }
        }
        else if(currenState == State.CHARGING_COMPLETED) {
            if (control.isVehicleNotConnected()) {
                newState = State.VEHICLE_NOT_CONNECTED;
            }
        }
        return newState;
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
        return isOn(this.startChargingStateDetectionDelay,
                System.currentTimeMillis(), this.startChargingTimestamp);
    }

    protected boolean isOn(Integer startChargingStateDetectionDelay,
                        long currentMillis, Long startChargingTimestamp) {
        if(isWithinStartChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis,
                startChargingTimestamp)) {
            return true;
        }
        return isCharging();
    }

    private void onStateChanged(State previousState, State newState) {
        if(newState == State.VEHICLE_CONNECTED) {
            if(this.appliance != null) {
                this.appliance.activateSchedules();
            }
        }
        if(newState == State.VEHICLE_NOT_CONNECTED) {
            if(this.appliance != null) {
                this.appliance.deactivateSchedules();
            }
            stopCharging();
            initStateHistory();
        }
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    protected boolean isWithinStartChargingStateDetectionDelay() {
        return isWithinStartChargingStateDetectionDelay(this.startChargingStateDetectionDelay,
                System.currentTimeMillis(), this.startChargingTimestamp);
    }

    protected boolean isWithinStartChargingStateDetectionDelay(Integer startChargingStateDetectionDelay,
                                                               long currentMillis, Long startChargingTimestamp) {
        return (startChargingTimestamp != null
                && currentMillis - startChargingTimestamp < startChargingStateDetectionDelay * 1000);
    }

    public boolean isVehicleConnected() {
        return getState() == State.VEHICLE_CONNECTED;
    }

    public boolean isCharging() {
        return getState() == State.CHARGING;
    }

    public boolean isChargingCompleted() {
        return getState() == State.CHARGING_COMPLETED;
    }

    public boolean isUseOptionalEnergy() {
        return useOptionalEnergy;
    }

    public void setChargePower(int power) {
        int current = Float.valueOf(power / (this.voltage * this.phases)).intValue();
        logger.debug("{}: Set charge power: {}W corresponds to {}A", applianceId, power, current);
        control.setChargeCurrent(current);
    }

    public void startCharging() {
        logger.debug("{}: Start charging process", applianceId);
        control.startCharging();
        this.startChargingTimestamp = System.currentTimeMillis();
    }

    public void stopCharging() {
        logger.debug("{}: Stop charging process", applianceId);
        control.stopCharging();
        this.startChargingTimestamp = null;
    }

}
