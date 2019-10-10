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
import de.avanux.smartapplianceenabler.http.EVHttpControl;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.modbus.EVModbusControl;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;
import de.avanux.smartapplianceenabler.util.Validateable;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleCharger implements Control, ApplianceLifeCycle, Validateable, ApplianceIdConsumer {

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
        @XmlElement(name = "EVHttpControl", type = EVHttpControl.class),
    })
    private EVControl control;
    @XmlElements({
            @XmlElement(name = "ElectricVehicle", type = ElectricVehicle.class),
    })
    private List<ElectricVehicle> vehicles;
    private transient Integer connectedVehicleId;
    private transient Integer connectedVehicleSoc;
    private transient Long connectedVehicleSocTimestamp;
    private transient Appliance appliance;
    private transient String applianceId;
    private transient Vector<State> stateHistory = new Vector<>();
    private transient Long stateLastChangedTimestamp;
    private transient boolean useOptionalEnergy = true;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient Long switchChargingStateTimestamp;
    private transient Integer chargeAmount;
    private transient Integer chargePower;
    private transient GuardedTimerTask updateStateTimerTask;
    private transient boolean startChargingRequested;
    private transient boolean stopChargingRequested;

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

    // TODO until we retrieve SOC periodically we calculate the current SOC here
    public Integer getCurrentSoc(Meter meter) {
        int whAlreadyCharged = 0;
        if (meter != null) {
            whAlreadyCharged = Float.valueOf(meter.getEnergy() * 1000.0f).intValue();
        }

        ElectricVehicle vehicle = getConnectedVehicle();
        if (vehicle != null) {
            Integer initialSoc = connectedVehicleSoc != null ? connectedVehicleSoc : 0;
            Integer chargeLoss = vehicle.getChargeLoss() != null ? vehicle.getChargeLoss() : 0;
            Integer currentSoc = Float.valueOf(initialSoc
                    + whAlreadyCharged / Float.valueOf(vehicle.getBatteryCapacity())
                    * (100 - chargeLoss)).intValue();
            return currentSoc > 100 ? 100 : currentSoc;
        }
        return 0;
    }

    public Long getConnectedVehicleSocTimestamp() {
        return connectedVehicleSocTimestamp;
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

    @Override
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
        control.init();
    }

    @Override
    public void validate() {
        control.validate();
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting ...", this.applianceId);
        if(timer != null) {
            this.updateStateTimerTask = new GuardedTimerTask(this.applianceId,"UpdateState",
                    getPollInterval() * 1000) {
                @Override
                public void runTask() {
                    updateState(new LocalDateTime());
                }
            };
            timer.schedule(this.updateStateTimerTask, 0, this.updateStateTimerTask.getPeriod());
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", this.applianceId);
        if(this.updateStateTimerTask != null) {
            this.updateStateTimerTask.cancel();
        }
    }

    /**
     * Returns true, if the state update was performed. This does not necessarily mean that the state has changed!
     * @return
     */
    public boolean updateState(LocalDateTime now) {
        if(isWithinSwitchChargingStateDetectionDelay()) {
            logger.debug("{}: Skipping state detection for {}s", applianceId, getStartChargingStateDetectionDelay());
            return false;
        }
        this.switchChargingStateTimestamp = null;
        State previousState = getState();
        State currentState = getNewState(previousState);
        if(currentState != previousState) {
            logger.debug("{}: Vehicle state changed: previousState={} newState={}", applianceId, previousState, currentState);
            stateHistory.add(currentState);
            stateLastChangedTimestamp = now.toDateTime().getMillis();
            onStateChanged(now, previousState, currentState);
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

    /**
     * Returns true, if inState occurs after last occurance of afterLastState in state history.
     * @param inState
     * @param afterLastState
     * @return
     */
    public boolean wasInStateAfterLastState(State inState, State afterLastState) {
        int indexLastAfterState = stateHistory.lastIndexOf(afterLastState);
        if(indexLastAfterState > -1) {
            List<State> afterStates = stateHistory.subList(indexLastAfterState, stateHistory.size() - 1);
            return afterStates.contains(afterLastState);
        }
        return false;
    }

    public Long getStateLastChangedTimestamp() {
        return stateLastChangedTimestamp;
    }

    private void initStateHistory() {
        this.stateHistory.clear();
        stateHistory.add(State.VEHICLE_NOT_CONNECTED);
    }

    protected State getNewState(State currenState) {
        boolean vehicleNotConnected = control.isVehicleNotConnected();
        boolean vehicleConnected = control.isVehicleConnected();
        boolean charging = control.isCharging();
        boolean errorState = control.isInErrorState();
        boolean wasInChargingAfterLastVehicleConnected = wasInStateAfterLastState(State.CHARGING, State.VEHICLE_CONNECTED);
        logger.debug("{}: currenState={} startChargingRequested={} stopChargingRequested={} vehicleNotConnected={} " +
                        "vehicleConnected={} charging={} errorState={} wasInChargingAfterLastVehicleConnected={}",
                applianceId, currenState, startChargingRequested, stopChargingRequested, vehicleNotConnected,
                vehicleConnected, charging, errorState, wasInChargingAfterLastVehicleConnected);

        // only use variables logged above
        State newState = currenState;
        if(errorState) {
            return State.ERROR;
        }
        if(vehicleNotConnected) {
            newState = State.VEHICLE_NOT_CONNECTED;
        }
        else if(currenState == State.CHARGING_COMPLETED) {
            newState = State.CHARGING_COMPLETED;
        }
        else if(this.startChargingRequested && vehicleConnected) {
            if(charging) {
                newState = State.CHARGING;
            }
            else {
                newState = State.CHARGING_COMPLETED;
            }
        }
        else if(this.stopChargingRequested && vehicleConnected) {
            newState = State.VEHICLE_CONNECTED;
        }
        else if(charging) {
            newState = State.CHARGING;
        }
        else if(vehicleConnected) {
            if(wasInChargingAfterLastVehicleConnected) {
                newState = State.CHARGING_COMPLETED;
            }
            else {
                newState = State.VEHICLE_CONNECTED;
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
                System.currentTimeMillis(), this.switchChargingStateTimestamp);
    }

    protected boolean isOn(Integer startChargingStateDetectionDelay,
                        long currentMillis, Long startChargingTimestamp) {
        if(isWithinSwitchChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis,
                startChargingTimestamp)) {
            if(this.startChargingRequested) {
                return true;
            }
            if(this.stopChargingRequested) {
                return false;
            }
        }
        return isCharging();
    }

    private void onStateChanged(LocalDateTime now, State previousState, State newState) {
        this.startChargingRequested = false;
        this.stopChargingRequested = false;
        if(newState == State.VEHICLE_CONNECTED) {
            if (this.vehicles != null && this.vehicles.size() > 0) {
                // sadly, we don't know, which ev has been connected, so we will assume the first one if any
                ElectricVehicle firstVehicle = this.vehicles.get(0);
                if (getConnectedVehicleId() == null) {
                    setConnectedVehicleId(firstVehicle.getId());
                }
                if(previousState == State.VEHICLE_NOT_CONNECTED) {
                    retrieveSoc(firstVehicle);
                    if(this.appliance != null) {
                        this.appliance.activateSchedules();
                    }
                }
            }
            if(getForceInitialCharging() && wasInStateOneTime(State.VEHICLE_CONNECTED)) {
                startCharging();
            }
        }
        if(newState == State.CHARGING) {
            if(getForceInitialCharging() && wasInStateOneTime(State.CHARGING)) {
                logger.debug("{}: Stopping forced initial charging", applianceId);
                stopCharging();
            }
        }
        if(newState == State.CHARGING_COMPLETED) {
            stopCharging();
        }
        if(newState == State.VEHICLE_NOT_CONNECTED) {
            on(now, false);
            if(this.appliance != null) {
                this.appliance.deactivateSchedules();
                Meter meter = this.appliance.getMeter();
                if(meter != null) {
                    meter.resetEnergyMeter();
                }
            }
            setConnectedVehicleId(null);
            initStateHistory();
        }
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    protected boolean isWithinSwitchChargingStateDetectionDelay() {
        return isWithinSwitchChargingStateDetectionDelay(getStartChargingStateDetectionDelay(),
                System.currentTimeMillis(), this.switchChargingStateTimestamp);
    }

    protected boolean isWithinSwitchChargingStateDetectionDelay(Integer switchChargingStateDetectionDelay,
                                                                long currentMillis, Long switchChargingStateTimestamp) {
        return (switchChargingStateTimestamp != null
                && currentMillis - switchChargingStateTimestamp < switchChargingStateDetectionDelay * 1000);
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
            updateSoc(Integer.valueOf(socCurrent != null ? socCurrent : 0).floatValue());
            int energy = Float.valueOf(((float) resolvedSocRequested - getConnectedVehicleSoc())/100.0f
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
        this.startChargingRequested = true;
        control.startCharging();
        this.switchChargingStateTimestamp = System.currentTimeMillis();
    }

    public void setStartChargingRequested(boolean startChargingRequested) {
        this.startChargingRequested = startChargingRequested;
    }

    public void stopCharging() {
        logger.debug("{}: Stop charging process", applianceId);
        control.stopCharging();
        this.switchChargingStateTimestamp = System.currentTimeMillis();
        this.chargeAmount = null;
        this.chargePower = null;
        this.startChargingRequested = false;
        this.stopChargingRequested = true;
    }

    public void setStopChargingRequested(boolean stopChargingRequested) {
        this.stopChargingRequested = stopChargingRequested;
    }

    private void retrieveSoc(ElectricVehicle electricVehicle) {
        if(electricVehicle != null) {
            Float soc = electricVehicle.getStateOfCharge();
            if(soc != null) {
                updateSoc(soc);
            }
        }
    }

    private void updateSoc(Float soc) {
        this.connectedVehicleSoc = Float.valueOf(soc).intValue();
        this.connectedVehicleSocTimestamp = System.currentTimeMillis();
        logger.debug("{}: Current SoC={}%", applianceId, connectedVehicleSoc);
    }
}
