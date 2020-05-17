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
import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.http.EVHttpControl;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.modbus.EVModbusControl;
import de.avanux.smartapplianceenabler.schedule.Interval;
import de.avanux.smartapplianceenabler.schedule.SocRequest;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.Vector;

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
    private EVChargerControl control;
    @XmlElements({
            @XmlElement(name = "ElectricVehicle", type = ElectricVehicle.class),
    })
    private List<ElectricVehicle> vehicles;
    private transient Integer connectedVehicleId;
    private transient Integer connectedVehicleSoc;
    private transient boolean socScriptAsync = true;
    private transient Long connectedVehicleSocTimestamp;
    private transient Appliance appliance;
    private transient String applianceId;
    private transient Vector<EVChargerState> stateHistory = new Vector<>();
    private transient LocalDateTime stateLastChangedTimestamp;
    private transient boolean useOptionalEnergy = true;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient Long switchChargingStateTimestamp;
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

    public EVChargerControl getControl() {
        return control;
    }

    public void setControl(EVChargerControl control) {
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

    public void setSocScriptAsync(boolean socScriptAsync) {
        this.socScriptAsync = socScriptAsync;
    }

    public Integer getConnectedVehicleSoc() {
        return connectedVehicleSoc;
    }

    public void setConnectedVehicleSoc(LocalDateTime now, Integer connectedVehicleSoc) {
        this.connectedVehicleSoc = connectedVehicleSoc;
        for(ControlStateChangedListener listener : controlStateChangedListeners) {
            logger.debug("{}: Notifying {} {} {}", applianceId, ControlStateChangedListener.class.getSimpleName(),
                    listener.getClass().getSimpleName(), listener);
            listener.onEVChargerSocChanged(now, Integer.valueOf(connectedVehicleSoc).floatValue());
        }
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
            this.control= new EVChargerControlMock();
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
                    updateState(LocalDateTime.now());
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
        EVChargerState previousState = getState();
        EVChargerState currentState = getNewState(previousState);
        if(currentState != previousState) {
            logger.debug("{}: Vehicle state changed: previousState={} newState={}", applianceId, previousState, currentState);
            stateHistory.add(currentState);
            stateLastChangedTimestamp = now;
            onEVChargerStateChanged(now, previousState, currentState);
        }
        else {
            logger.debug("{}: Vehicle state={}", applianceId, currentState);
        }
        return true;
    }

    public EVChargerState getState() {
        return stateHistory.lastElement();
    }

    protected void setState(EVChargerState state) {
        this.stateHistory.add(state);
    }

    public boolean wasInState(EVChargerState state) {
        return stateHistory.contains(state);
    }

    public boolean wasInStateOneTime(EVChargerState state) {
        int times = 0;
        for (EVChargerState historyState: stateHistory) {
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
    public boolean wasInStateAfterLastState(EVChargerState inState, EVChargerState afterLastState) {
        int indexLastAfterState = stateHistory.lastIndexOf(afterLastState);
        if(indexLastAfterState > -1) {
            List<EVChargerState> afterStates = stateHistory.subList(indexLastAfterState, stateHistory.size() - 1);
            return afterStates.contains(afterLastState);
        }
        return false;
    }

    public LocalDateTime getStateLastChangedTimestamp() {
        return stateLastChangedTimestamp;
    }

    private void initStateHistory() {
        this.stateHistory.clear();
        stateHistory.add(EVChargerState.VEHICLE_NOT_CONNECTED);
    }

    protected EVChargerState getNewState(EVChargerState currenState) {
        boolean vehicleNotConnected = control.isVehicleNotConnected();
        boolean vehicleConnected = control.isVehicleConnected();
        boolean charging = control.isCharging();
        boolean errorState = control.isInErrorState();
        boolean wasInChargingAfterLastVehicleConnected = wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED);
        logger.debug("{}: currenState={} startChargingRequested={} stopChargingRequested={} vehicleNotConnected={} " +
                        "vehicleConnected={} charging={} errorState={} wasInChargingAfterLastVehicleConnected={}",
                applianceId, currenState, startChargingRequested, stopChargingRequested, vehicleNotConnected,
                vehicleConnected, charging, errorState, wasInChargingAfterLastVehicleConnected);

        // only use variables logged above
        if(errorState) {
            return EVChargerState.ERROR;
        }
        if(currenState == EVChargerState.ERROR) {
            if(charging) {
                return EVChargerState.CHARGING;
            }
            if(vehicleConnected) {
                return EVChargerState.VEHICLE_CONNECTED;
            }
        }
        if(vehicleNotConnected) {
            return EVChargerState.VEHICLE_NOT_CONNECTED;
        }
        else if(currenState == EVChargerState.VEHICLE_NOT_CONNECTED && charging) {
            // the charger may start charging right after it has been connected
            return EVChargerState.CHARGING;
        }
        else if(currenState == EVChargerState.CHARGING_COMPLETED) {
            return EVChargerState.CHARGING_COMPLETED;
        }
        else if(this.startChargingRequested && vehicleConnected && !charging) {
            if(charging) {
                return EVChargerState.CHARGING;
            }
            else {
                return EVChargerState.CHARGING_COMPLETED;
            }
        }
        else if(this.startChargingRequested && charging) {
            return EVChargerState.CHARGING;
        }
        else if(this.stopChargingRequested && vehicleConnected) {
            return EVChargerState.VEHICLE_CONNECTED;
        }
        else if(vehicleConnected && !charging) {
            if(wasInChargingAfterLastVehicleConnected) {
                return EVChargerState.CHARGING_COMPLETED;
            }
            else {
                return EVChargerState.VEHICLE_CONNECTED;
            }
        }
        return currenState;
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        // only change state if requested state differs from actual state
        if(isOn() ^ switchOn) {
            if(switchOn) {
                logger.info("{}: Switching on", applianceId);
                startCharging();
            }
            else {
                logger.info("{}: Switching off", applianceId);
                stopCharging();
            }
            for(ControlStateChangedListener listener : new ArrayList<>(controlStateChangedListeners)) {
                logger.debug("{}: Notifying {} {}", applianceId, ControlStateChangedListener.class.getSimpleName(),
                        listener.getClass().getSimpleName());
                listener.controlStateChanged(now, switchOn);
            }
        }
        else {
            logger.debug("{}: Already switched on.", applianceId);
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

    private void onEVChargerStateChanged(LocalDateTime now, EVChargerState previousState, EVChargerState newState) {
        this.startChargingRequested = false;
        this.stopChargingRequested = false;
        if(newState == EVChargerState.VEHICLE_CONNECTED) {
            if (this.vehicles != null && this.vehicles.size() > 0) {
                // sadly, we don't know, which ev has been connected, so we will assume the first one if any
                ElectricVehicle firstVehicle = this.vehicles.get(0);
                if (getConnectedVehicleId() == null) {
                    setConnectedVehicleId(firstVehicle.getId());
                }
            }
            if(getForceInitialCharging() && wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED)) {
                startCharging();
            }
        }
        if(newState == EVChargerState.CHARGING) {
            if(getForceInitialCharging() && wasInStateOneTime(EVChargerState.CHARGING)) {
                logger.debug("{}: Stopping forced initial charging", applianceId);
                stopCharging();
            }
        }
        if(newState == EVChargerState.CHARGING_COMPLETED) {
            stopCharging();
        }
        if(newState == EVChargerState.VEHICLE_NOT_CONNECTED) {
            if(isOn()) {
                on(now, false);
            }
            setConnectedVehicleId(null);
            initStateHistory();
        }

        for(ControlStateChangedListener listener : new ArrayList<>(controlStateChangedListeners)) {
            logger.debug("{}: Notifying {} {}", applianceId, ControlStateChangedListener.class.getSimpleName(),
                    listener.getClass().getSimpleName());
            listener.onEVChargerStateChanged(now, previousState, newState, getConnectedVehicle());
        }

        // SOC has to be retrieved after listener notification in order to allow for new listeners interested in SOC
        if(previousState == EVChargerState.VEHICLE_NOT_CONNECTED && newState == EVChargerState.VEHICLE_CONNECTED) {
            retrieveSoc(now);
        }
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void removeControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.remove(listener);
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

    public boolean isVehicleNotConnected() {
        return getState() == EVChargerState.VEHICLE_NOT_CONNECTED;
    }

    public boolean isVehicleConnected() {
        return getState() == EVChargerState.VEHICLE_CONNECTED;
    }

    public boolean isCharging() {
        return getState() == EVChargerState.CHARGING;
    }

    public boolean isChargingCompleted() {
        return getState() == EVChargerState.CHARGING_COMPLETED;
    }

    public boolean isInErrorState() {
        return getState() == EVChargerState.ERROR;
    }

    public boolean isUseOptionalEnergy() {
        return useOptionalEnergy;
    }

    public TimeframeInterval createTimeframeInterval(LocalDateTime now, Integer evId, Integer socCurrent, Integer socRequested,
                                             LocalDateTime chargeEnd) {
        SocRequest request = new SocRequest();
        request.setEvId(evId);
        request.setSoc(socRequested);
        request.setSocInitial(socCurrent);
        request.setEnabled(true);

        if(chargeEnd == null) {
            Integer energy = request.calculateEnergy(getVehicle(evId));
            chargeEnd = now.plusSeconds(calculateChargeSeconds(getVehicle(evId), energy));
        }

        Interval interval = new Interval(now, chargeEnd);

        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);

        return timeframeInterval;
    }

    public int calculateChargeSeconds(ElectricVehicle vehicle, Integer energy) {
        DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(applianceId);
        int maxChargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();
        if(vehicle != null) {
            Integer maxVehicleChargePower = vehicle.getMaxChargePower();
            if(maxVehicleChargePower != null && maxVehicleChargePower < maxChargePower) {
                maxChargePower = maxVehicleChargePower;
            }
        }
        else {
            logger.warn("{}: evId not set - using defaults", applianceId);
        }
        int chargeSeconds = Float.valueOf((float) energy / maxChargePower * 3600).intValue();
        logger.debug("{}: Calculated duration: {}s energy={} maxChargePower={}",
                applianceId, chargeSeconds, energy, maxChargePower);

        return chargeSeconds;
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
        boolean wasInChargingAfterLastVehicleConnected = wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED);
        this.switchChargingStateTimestamp = wasInChargingAfterLastVehicleConnected ? System.currentTimeMillis() : null;
//        this.chargeAmount = null;
        this.chargePower = null;
        if(!this.startChargingRequested) {
            this.stopChargingRequested = true;
        }
        this.startChargingRequested = false;
    }

    public void setStopChargingRequested(boolean stopChargingRequested) {
        this.stopChargingRequested = stopChargingRequested;
    }

    public void retrieveSoc(LocalDateTime now) {
        retrieveSoc(now, getConnectedVehicle());
    }

    private void retrieveSoc(LocalDateTime now, ElectricVehicle electricVehicle) {
        if(electricVehicle != null) {
            SocRetriever socRetriever = new SocRetriever(now, electricVehicle);
            if(socScriptAsync) {
                Thread managerThread = new Thread(socRetriever);
                managerThread.start();
            }
            else {
                // for unit tests
                socRetriever.run();
            }
        }
    }

    private class SocRetriever implements Runnable {
        private LocalDateTime now;
        private ElectricVehicle electricVehicle;

        public SocRetriever(LocalDateTime now, ElectricVehicle electricVehicle) {
            this.now = now;
            this.electricVehicle = electricVehicle;
        }

        @Override
        public void run() {
            Float soc = getStateOfCharge(electricVehicle);
            if(soc != null) {
                setConnectedVehicleSoc(now, soc.intValue());
                logger.debug("{}: Current SoC={}%", applianceId, connectedVehicleSoc);
            }
        }
    }

    /**
     * This method is extracted only for mocking which should also disable any time limits.
     * @param electricVehicle
     * @return
     */
    public Float getStateOfCharge(ElectricVehicle electricVehicle) {
        if(connectedVehicleSocTimestamp == null
                || System.currentTimeMillis() - this.connectedVehicleSocTimestamp > 1 * 60 * 60) {
            logger.debug("{}: Try to retrieve SoC", applianceId);
            Float soc = electricVehicle.getStateOfCharge();
            this.connectedVehicleSocTimestamp = System.currentTimeMillis();
            return soc;
        }
        logger.debug("{}: Using cached SoC", applianceId);
        return Integer.valueOf(this.connectedVehicleSoc).floatValue();
    }
}
