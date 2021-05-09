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

import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.http.EVHttpControl;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.modbus.EVModbusControl;
import de.avanux.smartapplianceenabler.notification.*;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleCharger implements Control, ApplianceLifeCycle, Validateable, ApplianceIdConsumer,
        TimeframeIntervalChangedListener, NotificationProvider {

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
    @XmlElement(name = "Notifications")
    private Notifications notifications;
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
    private transient SocValues socValues = new SocValues();;
    private transient SocValues socValuesSentToListeners;
    private transient LocalDateTime socTimestamp;
    private transient LocalDateTime socInitialTimestamp;
    private transient boolean socScriptAsync = true;
    private transient boolean socScriptRunning;
    private transient boolean socCalculationRequired;
    private transient float socRetrievalEnergyMeterValue = 0.0f;
    private transient boolean socRetrievalForChargingAlmostCompleted;
    private transient double chargeLoss = 0.0;
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
    private transient boolean firstInvocationAfterSkip;
    private transient Integer minPowerConsumption;
    private transient NotificationHandler notificationHandler;
    private transient DecimalFormat percentageFormat;

    public ElectricVehicleCharger() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        percentageFormat = (DecimalFormat) nf;
        percentageFormat.applyPattern("#'%'");
    }

    public void setAppliance(Appliance appliance) {
        this.appliance = appliance;
        TimeframeIntervalHandler timeframeIntervalHandler = this.appliance.getTimeframeIntervalHandler();
        if(timeframeIntervalHandler != null) {
            timeframeIntervalHandler.addTimeframeIntervalChangedListener(this);
        }
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

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(this.notificationHandler != null) {
            this.notificationHandler.setRequestedNotifications(notifications);
            this.control.setNotificationHandler(notificationHandler);
        }
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
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

    public void setMinPowerConsumption(Integer minPowerConsumption) {
        this.minPowerConsumption = minPowerConsumption;
    }

    public void setSocScriptAsync(boolean socScriptAsync) {
        this.socScriptAsync = socScriptAsync;
    }

    public Integer getSocInitial() {
        return this.socValues.initial != null ? this.socValues.initial : 0;
    }

    public void setSocInitial(Integer socInitial) {
        this.socValues.initial = socInitial;
    }

    public Integer getSocCurrent() {
        return this.socValues.current != null ? this.socValues.current : 0;
    }

    public SocValues getSocValues() {
        return socValues;
    }

    public void updateSocOnControlStateChangedListeners(LocalDateTime now, SocValues socValues) {
        if(socValuesSentToListeners == null || !socValuesSentToListeners.equals(socValues)) {
            for(ControlStateChangedListener listener : controlStateChangedListeners) {
                logger.debug("{}: Notifying {} {} {}", applianceId, ControlStateChangedListener.class.getSimpleName(),
                        listener.getClass().getSimpleName(), listener);
                listener.onEVChargerSocChanged(now, socValues);
            }
            socValuesSentToListeners = new SocValues(socValues);
        }
    }

    private int calculateCurrentSoc() {
        ElectricVehicle vehicle = getConnectedVehicle();
        if (vehicle != null) {
            int energyMeteredSinceLastSocScriptExecution = getEnergyMeteredSinceLastSocScriptExecution();
            int socRetrievedOrInitial = this.socValues.retrieved != null ? this.socValues.retrieved : getSocInitial();
            int soc = Long.valueOf(Math.round(
                    socRetrievedOrInitial + energyMeteredSinceLastSocScriptExecution / (vehicle.getBatteryCapacity() *  (1 + chargeLoss/100)) * 100
            )).intValue();
            int socCurrent = Math.min(soc, 100);
            logger.debug("{}: SOC calculation: socCurrent={} socRetrievedOrInitial={} batteryCapacity={}Wh energyMeteredSinceLastSocScriptExecution={}Wh chargeLoss={}",
                    applianceId, percentageFormat.format(socCurrent), percentageFormat.format(socRetrievedOrInitial),
                    vehicle.getBatteryCapacity(),  energyMeteredSinceLastSocScriptExecution, percentageFormat.format(chargeLoss));
            return socCurrent;
        }
        return 0;
    }

    public double getChargeLoss() {
        return chargeLoss;
    }

    private Double calculateChargeLoss(int energyMeteredSinceLastSocScriptExecution, int socCurrent, int socLastRetrieval) {
        ElectricVehicle vehicle = getConnectedVehicle();
        if (vehicle != null && energyMeteredSinceLastSocScriptExecution > 0) {
            double energyReceivedByEv = (socCurrent - socLastRetrieval)/100.0 * vehicle.getBatteryCapacity();
            double chargeLoss = energyMeteredSinceLastSocScriptExecution * 100.0 / energyReceivedByEv - 100.0;
            logger.debug("{}: charge loss calculation: chargeLoss={} socCurrent={} socLastRetrieval={} batteryCapacity={}Wh energyMeteredSinceLastSocScriptExecution={}Wh energyReceivedByEv={}Wh",
                    applianceId, percentageFormat.format(chargeLoss), percentageFormat.format(socCurrent), percentageFormat.format(socLastRetrieval),
                    vehicle.getBatteryCapacity(), energyMeteredSinceLastSocScriptExecution, (int) energyReceivedByEv);
            return chargeLoss;
        }
        return null;
    }

    public Long getSocInitialTimestamp() {
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        return socInitialTimestamp != null ? socInitialTimestamp.toEpochSecond(zoneOffset) * 1000 : null;
    }

    public void setSocInitialTimestamp(LocalDateTime socInitialTimestamp) {
        this.socInitialTimestamp = socInitialTimestamp;
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
    public void validate() throws ConfigurationException {
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
                    // don't add code here since it is not used by integration tests
                    // add it in updateStateTimerTaskImpl()
                    updateStateTimerTaskImpl(LocalDateTime.now());
                }
            };
            timer.schedule(this.updateStateTimerTask, 0, this.updateStateTimerTask.getPeriod());
        }
    }

    public void updateStateTimerTaskImpl(LocalDateTime now) {
        updateState(now);
        updateActiveTimeframeIntervalFromRequest(now);
        updateSoc(now);
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
    public synchronized boolean updateState(LocalDateTime now) {
        if(isWithinSwitchChargingStateDetectionDelay(false)) {
            logger.debug("{}: Skipping state detection for {}s", applianceId, getStartChargingStateDetectionDelay());
            this.firstInvocationAfterSkip = true;
            return false;
        }
        this.switchChargingStateTimestamp = null;
        EVChargerState previousState = getState();
        EVChargerState currentState = getNewState(now, previousState, firstInvocationAfterSkip);
        setState(now, currentState);
        this.firstInvocationAfterSkip = false;
        return true;
    }

    public EVChargerState getState() {
        return stateHistory.lastElement();
    }

    protected void setState(LocalDateTime now, EVChargerState currentState) {
        EVChargerState previousState = getState();
        if(currentState != previousState) {
            logger.debug("{}: Vehicle state changed: previousState={} newState={}", applianceId, previousState, currentState);
            stateHistory.add(currentState);
            stateLastChangedTimestamp = now;
            socCalculationRequired = true;
            onEVChargerStateChanged(now, previousState, currentState);
        }
        else {
            logger.debug("{}: Vehicle state={}", applianceId, currentState);
        }
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

    protected EVChargerState getNewState(LocalDateTime now, EVChargerState currenState, boolean firstInvocationAfterSkip) {
        boolean vehicleNotConnected = control.isVehicleNotConnected();
        boolean vehicleConnected = control.isVehicleConnected();
        boolean charging = control.isCharging();
        boolean errorState = control.isInErrorState();
        boolean hasOnlyEmptyRequestsBeforeTimeGap = hasOnlyEmptyRequestsBeforeTimeGap(now);
        boolean wasInStateVehicleConnected = wasInState(EVChargerState.VEHICLE_CONNECTED);
        logger.debug("{}: currentState={} startChargingRequested={} stopChargingRequested={} vehicleNotConnected={} " +
                        "vehicleConnected={} charging={} errorState={} wasInStateVehicleConnected={} " +
                        "firstInvocationAfterSkip={} hasOnlyEmptyRequestsBeforeTimeGap={}",
                applianceId, currenState, startChargingRequested, stopChargingRequested, vehicleNotConnected,
                vehicleConnected, charging, errorState, wasInStateVehicleConnected, firstInvocationAfterSkip,
                hasOnlyEmptyRequestsBeforeTimeGap);

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
//        else if(currenState == EVChargerState.CHARGING_COMPLETED) {
//            return EVChargerState.CHARGING_COMPLETED;
//        }
        else if(this.stopChargingRequested && vehicleConnected) {
            if(hasOnlyEmptyRequestsBeforeTimeGap && wasInStateVehicleConnected) {
                return EVChargerState.CHARGING_COMPLETED;
            }
            return EVChargerState.VEHICLE_CONNECTED;
        }
        else if(charging) {
            return EVChargerState.CHARGING;
        }
        else if(this.startChargingRequested) {
            if(!charging && firstInvocationAfterSkip) {
                return EVChargerState.CHARGING_COMPLETED;
            }
        }
        else if(vehicleConnected && !charging) {
            return EVChargerState.VEHICLE_CONNECTED;
        }
        return currenState;
    }

    public void resetChargingCompletedToVehicleConnected(LocalDateTime now) {
        if(isChargingCompleted()) {
            logger.debug("{}: Enforcing state {}", applianceId, EVChargerState.VEHICLE_CONNECTED.name());
            setState(now, EVChargerState.VEHICLE_CONNECTED);
        }
    }

    protected boolean hasOnlyEmptyRequestsBeforeTimeGap(LocalDateTime now) {
        // aktueller Request hat fast keine Energie mehr und es gibt keinen direkten Folge-Request
        List<TimeframeInterval> timeframeIntervalsUntilFirstGap
                = this.appliance.getTimeframeIntervalHandler().findTimeframeIntervalsUntilFirstGap();
        int maxEnergieUntilFirstGap = timeframeIntervalsUntilFirstGap.stream()
                .mapToInt(interval -> {
                    Integer max = interval.getRequest().getMax(now);
                    return max != null ? max : 0;
                }).sum();
        return maxEnergieUntilFirstGap < 100; // Wh
    }

    @Override
    public boolean isControllable() {
        return true;
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
            logger.debug("{}: Requested state already set.", applianceId);
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
        if(isWithinSwitchChargingStateDetectionDelay(true, startChargingStateDetectionDelay, currentMillis,
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
        NotificationType notificationType = NotificationType.valueOf("EVCHARGER_" + newState);
        if(this.notificationHandler != null) {
            this.notificationHandler.sendNotification(notificationType);
        }
        if(newState == EVChargerState.VEHICLE_CONNECTED) {
            if (this.vehicles != null && this.vehicles.size() > 0) {
                // sadly, we don't know, which ev has been connected, so we will assume the first one if any
                ElectricVehicle firstVehicle = this.vehicles.get(0);
                if (getConnectedVehicleId() == null) {
                    setConnectedVehicleId(firstVehicle.getId());
                }
                socValues.batteryCapacity = firstVehicle.getBatteryCapacity();
            }
            if(getForceInitialCharging() && wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED)) {
                if(this.minPowerConsumption != null && this.minPowerConsumption > 0) {
                    setChargePower(this.minPowerConsumption);
                }
                startCharging();
            }
        }
        if(newState == EVChargerState.CHARGING) {
            if(previousState == EVChargerState.VEHICLE_NOT_CONNECTED) {
                stopCharging();
            }
            if(getForceInitialCharging() && wasInStateOneTime(EVChargerState.CHARGING)) {
                logger.debug("{}: Stopping forced initial charging", applianceId);
                stopCharging();
            }
        }
        if(newState == EVChargerState.CHARGING_COMPLETED) {
            stopCharging();
            this.socRetrievalForChargingAlmostCompleted = false;
        }
        if(newState == EVChargerState.VEHICLE_NOT_CONNECTED) {
            if(isOn()) {
                on(now, false);
            }
            Meter meter = appliance.getMeter();
            if(meter != null) {
                meter.resetEnergyMeter();
            }
            setConnectedVehicleId(null);
            this.socValues = new SocValues();
            this.socInitialTimestamp = null;
            this.socRetrievalForChargingAlmostCompleted = false;
            initStateHistory();
        }

        for(ControlStateChangedListener listener : new ArrayList<>(controlStateChangedListeners)) {
            logger.debug("{}: Notifying {} {}", applianceId, ControlStateChangedListener.class.getSimpleName(),
                    listener.getClass().getSimpleName());
            listener.onEVChargerStateChanged(now, previousState, newState, getConnectedVehicle());
        }

        // SOC has to be retrieved after listener notification in order to allow for new listeners interested in SOC
        if(previousState == EVChargerState.VEHICLE_NOT_CONNECTED && newState == EVChargerState.VEHICLE_CONNECTED) {
            this.socValues.batteryCapacity = getConnectedVehicle().getBatteryCapacity();
            this.chargeLoss = getConnectedVehicle().getChargeLoss() != null
                    ? getConnectedVehicle().getChargeLoss().floatValue() : 0.0f;
            updateSoc(now);
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

    protected boolean isWithinSwitchChargingStateDetectionDelay(boolean addPollInterval) {
        return isWithinSwitchChargingStateDetectionDelay(addPollInterval, getStartChargingStateDetectionDelay(),
                System.currentTimeMillis(), this.switchChargingStateTimestamp);
    }

    protected boolean isWithinSwitchChargingStateDetectionDelay(boolean addPollInterval, Integer switchChargingStateDetectionDelay,
                                                                long currentMillis, Long switchChargingStateTimestamp) {
        int consideredPollInterval = addPollInterval && getPollInterval() != null ? getPollInterval() * 2 * 1000 : 0;
        return (switchChargingStateTimestamp != null
                && currentMillis - switchChargingStateTimestamp - consideredPollInterval < switchChargingStateDetectionDelay * 1000);
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
        ElectricVehicle vehicle = getVehicle(evId);

        SocRequest request = new SocRequest();
        request.setApplianceId(applianceId);
        request.setEvId(evId);
        request.setBatteryCapacity(vehicle.getBatteryCapacity());
        request.setSoc(socRequested);
        request.setSocInitial(socCurrent);
        request.setSocCurrent(socCurrent);
        request.setEnabled(true);
        request.updateForced();

        if(chargeEnd == null) {
            DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(applianceId);
            int maxChargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();
            chargeEnd = now.plusSeconds(calculateChargeSeconds(vehicle, request.getMax(now), maxChargePower));
            request.setAcceptControlRecommendations(false);
        }

        Interval interval = new Interval(now, chargeEnd);

        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);

        return timeframeInterval;
    }

    private void updateActiveTimeframeIntervalFromRequest(LocalDateTime now) {
        if(isCharging()) {
            TimeframeInterval activeTimeframeInterval = this.appliance.getTimeframeIntervalHandler().getActiveTimeframeInterval();
            if(activeTimeframeInterval != null) {
                Request request = activeTimeframeInterval.getRequest();
                int chargePower = this.appliance.getMeter().getAveragePower();
                if(!(request instanceof OptionalEnergySocRequest) && chargePower > 0) {
                    int chargeSeconds = calculateChargeSeconds(
                            getConnectedVehicle(),
                            activeTimeframeInterval.getRequest().getMax(now),
                            chargePower);
                    LocalDateTime chargeEnd = now.plusSeconds(chargeSeconds);
                    Interval interval = activeTimeframeInterval.getInterval();
                    if(!interval.getEnd().equals(chargeEnd)) {
                        logger.debug("{}: Adjust timeframe interval end to: {}", applianceId, chargeEnd);
                        interval.setEnd(chargeEnd);
                        this.appliance.getTimeframeIntervalHandler().adjustOptionalEnergyTimeframeIntervalStart();
                    }
                }
            }
        }
    }

    public int calculateChargeSeconds(ElectricVehicle vehicle, Integer energy, Integer chargePower) {
        if(vehicle != null) {
            Integer maxVehicleChargePower = vehicle.getMaxChargePower();
            if(maxVehicleChargePower != null && maxVehicleChargePower < chargePower) {
                chargePower = maxVehicleChargePower;
            }
        }
        else {
            logger.warn("{}: evId not set - using defaults", applianceId);
        }
        int chargeSeconds = Float.valueOf((float) energy / chargePower * 3600).intValue();
        logger.debug("{}: Calculated duration: {}s energy={} chargePower={}",
                applianceId, chargeSeconds, energy, chargePower);

        return chargeSeconds;
    }

    private int getEnergyMeteredSinceLastSocScriptExecution() {
        if(appliance.getMeter() != null) {
            // in Wh
            return Float.valueOf((appliance.getMeter().getEnergy() - socRetrievalEnergyMeterValue) * 1000.0f).intValue();
        }
        return 0;
    }

    public synchronized void setChargePower(int power) {
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

    public synchronized void startCharging() {
        if(!startChargingRequested) {
            logger.debug("{}: Start charging process", applianceId);
            this.startChargingRequested = true;
            this.stopChargingRequested = false;
            control.startCharging();
            this.switchChargingStateTimestamp = System.currentTimeMillis();
        }
    }

    public void setStartChargingRequested(boolean startChargingRequested) {
        this.startChargingRequested = startChargingRequested;
    }

    public synchronized void stopCharging() {
        if(!stopChargingRequested) {
            logger.debug("{}: Stop charging process", applianceId);
            this.startChargingRequested = false;
            this.stopChargingRequested = true;
            control.stopCharging();
            boolean wasInChargingAfterLastVehicleConnected = wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED);
            this.switchChargingStateTimestamp = wasInChargingAfterLastVehicleConnected ? System.currentTimeMillis() : null;
            this.chargePower = null;
        }
    }

    public void setStopChargingRequested(boolean stopChargingRequested) {
        this.stopChargingRequested = stopChargingRequested;
    }

    @Override
    public void timeframeIntervalCreated(LocalDateTime now, TimeframeInterval timeframeInterval) {
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval, TimeframeInterval activatedInterval, boolean wasRunning) {
        if(activatedInterval != null && activatedInterval.getState() == TimeframeIntervalState.ACTIVE) {
            updateSoc(now);
        }
        if(deactivatedInterval != null && deactivatedInterval.getRequest().isFinished(now)) {
            setState(now, EVChargerState.CHARGING_COMPLETED);
        }
    }

    public synchronized void updateSoc(LocalDateTime now) {
        if(! isVehicleNotConnected()) {
            boolean chargingAlmostCompleted = false;
            boolean socChanged = false;
            if(this.socCalculationRequired || isCharging()) {
                int calculatedCurrentSoc = calculateCurrentSoc();
                socChanged = this.socValues.current != null && this.socValues.current != calculatedCurrentSoc;
                this.socValues.current = calculatedCurrentSoc;
                if(socChanged && appliance.getTimeframeIntervalHandler() != null
                        && appliance.getTimeframeIntervalHandler().getActiveTimeframeInterval() != null) {
                    Integer max = appliance.getTimeframeIntervalHandler().getActiveTimeframeInterval().getRequest().getMax(now);
                    if(max < 1000) {
                        chargingAlmostCompleted = true;
                    }
                }
                this.socCalculationRequired = false;
            }
            logger.debug( "{}: SOC retrieval: socCalculationRequired={} socChanged={} chargingAlmostCompleted={} socRetrievalForChargingAlmostCompleted={}",
                    applianceId, socCalculationRequired, socChanged, chargingAlmostCompleted, socRetrievalForChargingAlmostCompleted);
            ElectricVehicle electricVehicle = getConnectedVehicle();
            if(electricVehicle != null && electricVehicle.getSocScript() != null) {
                Integer updateAfterIncrease = electricVehicle.getSocScript().getUpdateAfterIncrease();
                if(updateAfterIncrease == null) {
                    updateAfterIncrease = ElectricVehicleChargerDefaults.getUpdateSocAfterIncrease();
                }
                Integer updateAfterSeconds = electricVehicle.getSocScript().getUpdateAfterSeconds();
                if(this.socValues.initial == null
                        || this.socValues.retrieved == null
                        || (chargingAlmostCompleted && !socRetrievalForChargingAlmostCompleted)
                        || ((this.socValues.retrieved + updateAfterIncrease <= this.socValues.current)
                            && (updateAfterSeconds == null || now.minusSeconds(updateAfterSeconds).isAfter(this.socTimestamp)))
                ) {
                    if(!this.socScriptRunning) {
                        logger.debug( "{}: SOC retrieval is required: {}", applianceId, this.socValues);
                        this.socScriptRunning = true;
                        SocRetriever socRetriever = new SocRetriever(now, electricVehicle, chargingAlmostCompleted);
                        if(socScriptAsync) {
                            Thread managerThread = new Thread(socRetriever);
                            managerThread.start();
                        }
                        else {
                            // for unit tests
                            socRetriever.run();
                        }
                    }
                    else {
                        logger.debug("{}: SOC retrieval already running: {}", applianceId, this.socValues);
                    }
                }
                else {
                    logger.debug("{}: SOC retrieval is NOT required: {}", applianceId, this.socValues);
                }
            }
            updateSocOnControlStateChangedListeners(now, this.socValues);
        }
    }

    private class SocRetriever implements Runnable {
        private LocalDateTime now;
        private ElectricVehicle electricVehicle;
        private boolean chargingAlmostCompleted;

        public SocRetriever(LocalDateTime now, ElectricVehicle electricVehicle, boolean chargingAlmostCompleted) {
            this.now = now;
            this.electricVehicle = electricVehicle;
            this.chargingAlmostCompleted = chargingAlmostCompleted;
        }

        @Override
        public void run() {
            Double soc = getStateOfCharge(now, electricVehicle);
            if(soc != null) {
                logger.debug("{}: Retrieved SOC={}", applianceId, percentageFormat.format(soc));
                Integer socLastRetrieved = socValues.retrieved != null ? socValues.retrieved : socValues.initial;
                if(socValues.initial == null) {
                    socValues.initial = soc.intValue();
                    socValues.current = soc.intValue();
                }
                socValues.retrieved = soc.intValue();
                if(socLastRetrieved != null) {
                    Double chargeLossCalculated = calculateChargeLoss(getEnergyMeteredSinceLastSocScriptExecution(),
                            socValues.retrieved, socLastRetrieved);
                    if(chargeLossCalculated != null) {
                        chargeLoss = chargeLossCalculated > 0 ? chargeLossCalculated : 0.0 ;
                    }
                }
                socValues.current = soc.intValue();
                socRetrievalEnergyMeterValue = appliance.getMeter().getEnergy();
                if(this.chargingAlmostCompleted) {
                    socRetrievalForChargingAlmostCompleted = true;
                }
            }
            socScriptRunning = false;
        }
    }

    /**
     * This method is extracted only for mocking which should also disable any time limits.
     * @param electricVehicle
     * @return
     */
    public Double getStateOfCharge(LocalDateTime now, ElectricVehicle electricVehicle) {
        Double soc = electricVehicle.getStateOfCharge();
        this.socTimestamp = now;
        if(this.socInitialTimestamp == null) {
            this.socInitialTimestamp = this.socTimestamp;
        }
        return soc;
    }
}
