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
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.RunningTimeMonitor;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleCharger implements Control, ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(ElectricVehicleCharger.class);
    @XmlAttribute
    private Integer voltage;
    @XmlAttribute
    private Integer phases;
    @XmlAttribute
    private Integer pollInterval; // seconds
    @XmlAttribute
    protected Integer startChargingStateDetectionDelay;
    @XmlAttribute
    protected Boolean forceInitialCharging;
    @XmlElements({
        @XmlElement(name = "EVModbusControl", type = EVModbusControl.class),
    })
    private EVControl control;
    @XmlElements({
            @XmlElement(name = "ElectricVehicle", type = ElectricVehicle.class),
    })
    private List<ElectricVehicle> vehicles;
    private transient Integer connectedVehicleId;
    private transient Integer connectedVehicleSoc;
    private transient Appliance appliance;
    private transient String applianceId;
    private transient Vector<State> stateHistory = new Vector<>();
    private transient boolean useOptionalEnergy = true;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient Long startChargingTimestamp;
    private transient Integer chargeAmount;
    private transient Integer chargePower;

    protected enum State {
        VEHICLE_NOT_CONNECTED,
        VEHICLE_CONNECTED,
        CHARGING,
        CHARGING_COMPLETED,
        ERROR
    }

    public void setAppliance(Appliance appliance) {
        this.appliance = appliance;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        control.setApplianceId(applianceId);
        if(this.vehicles != null) {
            for(ElectricVehicle vehicle: this.vehicles) {
                vehicle.setApplianceId(applianceId);
            }
        }
    }

    public EVControl getControl() {
        return control;
    }

    public void setControl(EVControl control) {
        this.control = control;
    }

    public Integer getVoltage() {
        return voltage != null ? voltage : ElectricVehicleChargerDefaults.getVoltage();
    }

    public Integer getPhases() {
        return phases != null ? phases : ElectricVehicleChargerDefaults.getPhases();
    }

    public Integer getPollInterval() {
        return pollInterval != null ? pollInterval : ElectricVehicleChargerDefaults.getPollInterval();
    }

    public Integer getStartChargingStateDetectionDelay() {
        return startChargingStateDetectionDelay != null ? startChargingStateDetectionDelay :
                ElectricVehicleChargerDefaults.getStartChargingStateDetectionDelay();
    }

    public void setStartChargingStateDetectionDelay(Integer startChargingStateDetectionDelay) {
        this.startChargingStateDetectionDelay = startChargingStateDetectionDelay;
    }

    public Boolean getForceInitialCharging() {
        return forceInitialCharging != null ? forceInitialCharging :
                ElectricVehicleChargerDefaults.getForceInitialCharging();
    }

    public Integer getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(Integer chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public Integer getConnectedVehicleSoc() {
        return connectedVehicleSoc;
    }

    public ElectricVehicle getConnectedVehicle() {
        Integer evId = getConnectedVehicleId();
        if(evId != null) {
            return getVehicle(evId);
        }
        return null;
    }

    public Integer getConnectedVehicleId() {
        return connectedVehicleId;
    }

    public void setConnectedVehicleId(Integer connectedVehicleId) {
        this.connectedVehicleId = connectedVehicleId;
    }

    public ElectricVehicle getVehicle(int evId) {
        if(this.vehicles != null) {
            for(ElectricVehicle electricVehicle : this.vehicles) {
                if(electricVehicle.getId() == evId) {
                    return electricVehicle;
                }
            }
        }
        return null;
    }

    public List<ElectricVehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<ElectricVehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void init() {
        boolean useEvControlMock = Boolean.parseBoolean(System.getProperty("sae.evcontrol.mock", "false"));
        if(useEvControlMock) {
            this.control= new EVControlMock();
            this.appliance.setMeter((Meter) this.control);
        }
        logger.debug("{}: voltage={} phases={} startChargingStateDetectionDelay={}",
                this.applianceId, getVoltage(), getPhases(), getStartChargingStateDetectionDelay());
        if(this.vehicles != null) {
            for(ElectricVehicle vehicle: this.vehicles) {
                logger.debug("{}: {}", this.applianceId, vehicle);
            }
        }
        initStateHistory();
        control.setPollInterval(getPollInterval());
        control.init(true);
    }

    public void start(Timer timer) {
        stopCharging();
        if(timer != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateState();            }
            }, 0, getPollInterval() * 1000);
        }
    }

    /**
     * Returns true, if the state update was performed. This does not necessarily mean that the state has changed!
     * @return
     */
    public boolean updateState() {
        if(isWithinStartChargingStateDetectionDelay()) {
            logger.debug("{}: Skipping state detection for {}s after switched on.", applianceId,
                    getStartChargingStateDetectionDelay());
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

    public boolean wasInStateOneTime(State state) {
        int times = 0;
        for (State historyState: stateHistory) {
            if(historyState == state) {
                times++;
            }
        }
        return times == 1;
    }

    private void initStateHistory() {
        this.stateHistory.clear();
        stateHistory.add(State.VEHICLE_NOT_CONNECTED);
    }

    protected State getNewState(State currenState) {
        State newState = currenState;
        if(control.isInErrorState()) {
            return State.ERROR;
        }
        if(currenState == State.ERROR) {
            if(control.isVehicleConnected()) {
                newState = State.VEHICLE_CONNECTED;
            }
            else if(control.isCharging()) {
                newState = State.CHARGING;
            }
            else if(control.isChargingCompleted()) {
                newState = State.CHARGING_COMPLETED;
            }
            else if(control.isVehicleNotConnected()) {
                newState = State.VEHICLE_NOT_CONNECTED;
            }
        }
        else if(currenState == State.VEHICLE_NOT_CONNECTED) {
            if (control.isVehicleConnected()) {
                newState = State.VEHICLE_CONNECTED;
            }
        }
        else if(currenState == State.VEHICLE_CONNECTED) {
            if(control.isCharging()) {
                newState = State.CHARGING;
            }
            else if(control.isChargingCompleted()) {
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
                else if(control.isVehicleConnected()) {
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
        return isOn(getStartChargingStateDetectionDelay(),
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
            if (this.vehicles != null && this.vehicles.size() > 0) {
                // sadly, we don't know, which ev has been connected, so we will assume the first one if any
                ElectricVehicle firstVehicle = this.vehicles.get(0);
                if (previousState == State.VEHICLE_NOT_CONNECTED) {
                    setConnectedVehicleId(firstVehicle.getId());
                }
                if(previousState == State.VEHICLE_NOT_CONNECTED) {
                    retrieveSoc(firstVehicle);
                }
            }
            if(getForceInitialCharging() && wasInStateOneTime(State.VEHICLE_CONNECTED)) {
                startCharging();
            }
            if(this.appliance != null) {
                this.appliance.activateSchedules();
            }
        }
        if(newState == State.CHARGING) {
            if(getForceInitialCharging() && wasInStateOneTime(State.CHARGING)) {
                stopCharging();
            }
        }
        if(newState == State.CHARGING_COMPLETED) {
            if(this.appliance != null) {
//                this.appliance.deactivateSchedules();
                this.appliance.resetActiveTimeframInterval();
            }
        }
        if(newState == State.VEHICLE_NOT_CONNECTED) {
            if(this.appliance != null) {
                this.appliance.deactivateSchedules();
            }
            stopCharging();
            setConnectedVehicleId(null);
            initStateHistory();
        }
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    protected boolean isWithinStartChargingStateDetectionDelay() {
        return isWithinStartChargingStateDetectionDelay(getStartChargingStateDetectionDelay(),
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

    public boolean isInErrorState() {
        return getState() == State.ERROR;
    }

    public boolean isUseOptionalEnergy() {
        return useOptionalEnergy;
    }

    public  void setEnergyDemand(LocalDateTime now, Integer evId, Integer socCurrent, Integer socRequested,
                                 LocalDateTime chargeEnd) {
        logger.debug("{}: Energy demand: evId={} socCurrent={} socCurrent={} chargeEnd={}",
                applianceId, evId, socCurrent, socRequested, chargeEnd);
        setConnectedVehicleId(evId);

        DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(appliance.getId());
        int maxChargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();

        ElectricVehicle vehicle = getVehicle(evId);
        if(vehicle != null) {
            int batteryCapacity = vehicle.getBatteryCapacity();
            Integer maxVehicleChargePower = vehicle.getMaxChargePower();
            if(maxVehicleChargePower != null && maxVehicleChargePower < maxChargePower) {
                maxChargePower = maxVehicleChargePower;
            }
            int resolvedSocRequested = (socRequested != null ? socRequested : 100);
            int resolvedSocCurrent = (socCurrent != null ? socCurrent : 0);
            int energy = Float.valueOf(((float) resolvedSocRequested - resolvedSocCurrent)/100.0f
                    * (100 + vehicle.getChargeLoss())/100.0f * batteryCapacity).intValue();
            setChargeAmount(energy);
            logger.debug("{}: Calculated energy={}Wh (batteryCapacity={}Wh chargeLoss={}%)", applianceId, energy,
                    batteryCapacity, vehicle.getChargeLoss());

            if(chargeEnd == null) {
                int chargeMinutes = Float.valueOf((float) energy / maxChargePower * 60).intValue();
                chargeEnd = now.plusMinutes(chargeMinutes);
                logger.debug("{}: Calculated charge end={} chargeMinutes={} maxPowerConsumption={} chargeLoss={}",
                        applianceId, chargeEnd, chargeMinutes, maxChargePower, vehicle.getChargeLoss());
            }

            RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
            if (runningTimeMonitor != null) {
                runningTimeMonitor.activateTimeframeInterval(now, energy, chargeEnd);
            }
        }
    }

    public void setChargePower(int power) {
        int phases = getPhases();
        int adjustedPower = power;
        ElectricVehicle chargingVehicle = getConnectedVehicle();
        if(chargingVehicle != null) {
            if(chargingVehicle.getPhases() != null) {
                phases = chargingVehicle.getPhases();
            }
            if(chargingVehicle.getMaxChargePower() != null && power > chargingVehicle.getMaxChargePower()) {
                adjustedPower = chargingVehicle.getMaxChargePower();
                logger.debug("{}: Limiting charge power to vehicle maximum of {}W",
                        applianceId, chargingVehicle.getMaxChargePower());
            }
        }
        int current = Float.valueOf((float) adjustedPower / (getVoltage() * phases)).intValue();
        logger.debug("{}: Set charge power: {}W corresponds to {}A using {} phases",
                applianceId, adjustedPower, current, phases);
        this.chargePower = adjustedPower;
        control.setChargeCurrent(current);
    }

    public Integer getChargePower() {
        return chargePower;
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
        this.chargeAmount = null;
        this.chargePower = null;
    }

    private void retrieveSoc(ElectricVehicle electricVehicle) {
        if(electricVehicle != null) {
            Float soc = electricVehicle.getStateOfCharge();
            if(soc != null) {
                this.connectedVehicleSoc = Float.valueOf(soc).intValue();
                logger.debug("{}: Start charging SoC={}%", applianceId, connectedVehicleSoc);
            }
        }
    }
}
