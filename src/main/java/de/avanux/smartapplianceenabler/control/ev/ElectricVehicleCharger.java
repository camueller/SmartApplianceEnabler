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
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.VariablePowerConsumer;
import de.avanux.smartapplianceenabler.http.EVHttpControl;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.modbus.EVModbusControl;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusSlaveUser;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
public class ElectricVehicleCharger implements VariablePowerConsumer, ApplianceLifeCycle, Validateable, ApplianceIdConsumer,
        TimeframeIntervalChangedListener, SocValuesChangedListener, NotificationProvider, ModbusSlaveUser {

    private transient Logger logger = LoggerFactory.getLogger(ElectricVehicleCharger.class);
    @XmlAttribute
    private String id;
    @XmlAttribute
    private Integer voltage;
    @XmlAttribute
    private Integer phases;
    @XmlAttribute
    private Integer pollInterval; // seconds
    @XmlAttribute
    protected Integer startChargingStateDetectionDelay;
    @XmlAttribute
    protected Integer chargePowerRepetition;
    @XmlAttribute
    protected Boolean forceInitialCharging;
    @XmlAttribute
    private Double latitude;
    @XmlAttribute
    private Double longitude;
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
    private transient ElectricVehicleHandler evHandler;
    private transient Appliance appliance;
    private transient String applianceId;
    private transient Vector<EVChargerState> stateHistory = new Vector<>();
    private transient LocalDateTime stateLastChangedTimestamp;
    private transient boolean useOptionalEnergy = true;
    private transient Long switchChargingStateTimestamp;
    private transient Integer chargePower;
    private transient Timer timer;
    private transient GuardedTimerTask updateStateTimerTask;
    private transient GuardedTimerTask chargePowerRepetitionTimerTask;
    private transient boolean startChargingRequested;
    private transient boolean stopChargingRequested;
    private transient boolean firstInvocationAfterSkip;
    private transient Integer minPowerConsumption;
    private transient NotificationHandler notificationHandler;
    private transient DecimalFormat percentageFormat;
    private transient MqttClient mqttClient;
    private transient MeterMessage meterMessage;

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
    public String getId() {
        return id;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        control.setApplianceId(applianceId);
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
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

    @Override
    public Set<ModbusSlave> getModbusSlaves() {
        return control instanceof EVModbusControl ? Collections.singleton((EVModbusControl) control) : new HashSet<>();
    }

    public ElectricVehicleHandler getElectricVehicleHandler() {
        return evHandler;
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

    public void setMinPower(Integer minPower) {
        this.minPowerConsumption = minPower;
    }

    public Integer getMinPower() {
        return this.minPowerConsumption;
    }

    public void setMaxPower(int maxPower) {
    }

    public List<ElectricVehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<ElectricVehicle> vehicles) {
        this.vehicles = vehicles;
    }

    @Override
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
        boolean useEvControlMock = Boolean.parseBoolean(System.getProperty("sae.evcontrol.mock", "false"));
        if(useEvControlMock) {
            this.control= new EVChargerControlMock();
            this.appliance.setMeter((Meter) this.control);
        }
        logger.debug("{}: voltage={} phases={} startChargingStateDetectionDelay={} chargePowerRepetition={}",
                this.applianceId, getVoltage(), getPhases(), getStartChargingStateDetectionDelay(), this.chargePowerRepetition);

        this.evHandler = new ElectricVehicleHandler();
        this.evHandler.setSocValuesChangedListener(this);
        this.evHandler.setApplianceId(applianceId);
        this.evHandler.setVehicles(vehicles);
        if(this.latitude != null && this.longitude != null) {
            this.evHandler.setEvChargerLocation(new ImmutablePair<Double, Double>(latitude, longitude));
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
        if(mqttClient != null) {
            mqttClient.subscribe(Meter.TOPIC, true, (topic, message) -> {
                meterMessage = (MeterMessage) message;
                evHandler.setMeterMessage(meterMessage);
            });
        }
        mqttClient.subscribe(Control.TOPIC,true, true, (topic, message) -> {
            if(message instanceof VariablePowerConsumerMessage) {
                VariablePowerConsumerMessage controlMessage = (VariablePowerConsumerMessage) message;
                if(controlMessage.on) {
                    if(controlMessage.power != null) {
                        setPower(now, controlMessage.power);
                    }
                }
                this.on(controlMessage.getTime(), controlMessage.on);
            }
        });
        this.timer = timer;
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
            // the initial delay is needed in order to have regular timeframe intervals created by TimeframeIntervalHandler
            // before OptionalEnergyInterval is created by onEVChargerStateChanged
            timer.schedule(this.updateStateTimerTask, this.updateStateTimerTask.getPeriod(), this.updateStateTimerTask.getPeriod());
        }
        publishControlMessage(isOn());
    }

    public void updateStateTimerTaskImpl(LocalDateTime now) {
        updateState(now);
        updateActiveTimeframeIntervalRequest(now);
        updateSoc(now);
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", this.applianceId);
        if(this.updateStateTimerTask != null) {
            this.updateStateTimerTask.cancel();
        }
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
        cancelChargePowerRepetitionTimerTask();
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
        return stateHistory != null ? stateHistory.lastElement() : EVChargerState.VEHICLE_NOT_CONNECTED;
    }

    protected void setState(LocalDateTime now, EVChargerState currentState) {
        EVChargerState previousState = getState();
        if(currentState != previousState) {
            logger.debug("{}: Vehicle state changed: previousState={} newState={}", applianceId, previousState, currentState);
            stateHistory.add(currentState);
            stateLastChangedTimestamp = now;
            this.evHandler.setSocCalculationRequired(true);
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
        boolean allTimeframeIntervalRequestsAreFinished = appliance.getTimeframeIntervalHandler().getQueue().stream().allMatch(interval -> interval.getRequest().isFinished(now));
        logger.debug("{}: currentState={} startChargingRequested={} stopChargingRequested={} vehicleNotConnected={} vehicleConnected={} charging={} errorState={} wasInStateVehicleConnected={} firstInvocationAfterSkip={} hasOnlyEmptyRequestsBeforeTimeGap={} allTimeframeIntervalRequestsAreFinished={}", applianceId, currenState, startChargingRequested, stopChargingRequested, vehicleNotConnected, vehicleConnected, charging, errorState, wasInStateVehicleConnected, firstInvocationAfterSkip, hasOnlyEmptyRequestsBeforeTimeGap, allTimeframeIntervalRequestsAreFinished);

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
        else if(currenState == EVChargerState.CHARGING_COMPLETED && allTimeframeIntervalRequestsAreFinished) {
            return EVChargerState.CHARGING_COMPLETED;
        }
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
            if(currenState == EVChargerState.CHARGING && allTimeframeIntervalRequestsAreFinished) {
                return EVChargerState.CHARGING_COMPLETED;
            }
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
        }
        else {
            logger.debug("{}: Requested state already set.", applianceId);
        }
        return true;
    }

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
            if(getForceInitialCharging() && wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED)) {
                setPowerToMinimum();
                this.on(now, true);
            }
        }
        if(newState == EVChargerState.CHARGING) {
            if(previousState == EVChargerState.VEHICLE_NOT_CONNECTED) {
                this.on(now, false);
            }
            if(getForceInitialCharging() && wasInStateOneTime(EVChargerState.CHARGING)) {
                logger.debug("{}: Stopping forced initial charging", applianceId);
                this.on(now, false);
            }
        }
        if(newState == EVChargerState.CHARGING_COMPLETED) {
            this.on(now, false);
            this.evHandler.onChargingCompleted();
        }
        if(newState == EVChargerState.VEHICLE_NOT_CONNECTED) {
            if(isOn()) {
                on(now, false);
            }
            Meter meter = appliance.getMeter();
            if(meter != null) {
                meter.resetEnergyMeter();
            }
            this.evHandler.onVehicleDisconnected();
            this.appliance.getTimeframeIntervalHandler().clearQueue();
            initStateHistory();
        }

        publishEVChargerSocChangedEvent(now, this.evHandler.getSocValues());
        publishEVChargerStateChangedEvent(now, previousState, newState, this.evHandler.getConnectedOrFirstVehicleId());
        publishControlMessage(isOn());

        // SOC has to be retrieved after listener notification in order to allow for new listeners interested in SOC
        if(previousState == EVChargerState.VEHICLE_NOT_CONNECTED
                && (newState == EVChargerState.VEHICLE_CONNECTED || newState == EVChargerState.CHARGING)) {
            this.evHandler.terminateSocScriptExecution();
            this.evHandler.triggerSocScriptExecution();
        }
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

    public TimeframeInterval createTimeframeInterval(LocalDateTime now, Integer evId, Integer socCurrent, Integer socTarget,
                                             LocalDateTime chargeEnd) {
        ElectricVehicle vehicle = this.evHandler.getVehicle(evId);

        SocRequest request = new SocRequest();
        request.setApplianceId(applianceId);
        request.setEvId(evId);
        request.setBatteryCapacity(vehicle.getBatteryCapacity());
        request.setSoc(socTarget);
        request.setSocInitial(socCurrent);
        request.setSocCurrent(socCurrent);
        request.setEnabled(true);
        request.setUpdateTimeframeIntervalEnd(chargeEnd == null);
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

    private void updateActiveTimeframeIntervalRequest(LocalDateTime now) {
        if(isCharging()) {
            TimeframeInterval activeTimeframeInterval = this.appliance.getTimeframeIntervalHandler().getActiveTimeframeInterval();
            if(activeTimeframeInterval != null && activeTimeframeInterval.getRequest() instanceof AbstractEnergyRequest) {
                AbstractEnergyRequest request = (AbstractEnergyRequest) activeTimeframeInterval.getRequest();
                int chargePower = meterMessage != null ? meterMessage.power : 0;
                if(request.isUpdateTimeframeIntervalEnd() && chargePower > 0) {
                    int chargeSeconds = calculateChargeSeconds(
                            this.evHandler.getConnectedVehicle(),
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

    private GuardedTimerTask createChargePowerRepetitionTimerTask(int chargeCurrent) {
        return new GuardedTimerTask(this.applianceId,"ChargePowerRepetition",
                this.chargePowerRepetition * 1000) {
            @Override
            public void runTask() {
                control.setChargeCurrent(chargeCurrent);
            }
        };
    }

    private void cancelChargePowerRepetitionTimerTask() {
        if(this.chargePowerRepetitionTimerTask != null) {
            this.chargePowerRepetitionTimerTask.cancel();
        }
    }

    public void setPowerToMinimum() {
        logger.debug("{}: Set minimum charge power", applianceId);
        if(this.minPowerConsumption != null && this.minPowerConsumption > 0) {
            setPower(LocalDateTime.now(), this.minPowerConsumption);
        }
    }

    public synchronized void setPower(LocalDateTime now, int power) {
        int phases = getPhases();
        int adjustedPower = power;
        ElectricVehicle chargingVehicle = this.evHandler.getConnectedVehicle();
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
        if(this.chargePowerRepetition != null && this.timer != null) {
            logger.debug("{}: Scheduling charge power repetition ...", this.applianceId);
            if(this.chargePowerRepetitionTimerTask != null) {
                cancelChargePowerRepetitionTimerTask();
            }
            this.chargePowerRepetitionTimerTask = createChargePowerRepetitionTimerTask(current);
            this.timer.schedule(this.chargePowerRepetitionTimerTask, 0, this.chargePowerRepetitionTimerTask.getPeriod());
        } else {
            control.setChargeCurrent(current);
        }
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
            publishControlMessage(true);
        }
    }

    public void setStartChargingRequested(boolean startChargingRequested) {
        this.startChargingRequested = startChargingRequested;
    }

    public synchronized void stopCharging() {
        if(!stopChargingRequested) {
            logger.debug("{}: Stop charging process", applianceId);
            publishControlMessage(false);
            this.startChargingRequested = false;
            this.stopChargingRequested = true;
            control.stopCharging();
            boolean wasInChargingAfterLastVehicleConnected = wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED);
            this.switchChargingStateTimestamp = wasInChargingAfterLastVehicleConnected ? System.currentTimeMillis() : null;
            this.chargePower = null;
            if(this.chargePowerRepetitionTimerTask != null) {
                this.chargePowerRepetitionTimerTask.cancel();
            }
        }
    }

    public void setStopChargingRequested(boolean stopChargingRequested) {
        this.stopChargingRequested = stopChargingRequested;
    }

    @Override
    public void timeframeIntervalCreated(LocalDateTime now, TimeframeInterval timeframeInterval) {
        if(this.evHandler.getConnectedVehicle().getSocScript() != null && timeframeInterval.getRequest() instanceof SocRequest) {
            timeframeInterval.getRequest().setEnabled(false);
        }
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
            var request = appliance.getTimeframeIntervalHandler() != null
                    && appliance.getTimeframeIntervalHandler().getActiveTimeframeInterval() != null
                    ? appliance.getTimeframeIntervalHandler().getActiveTimeframeInterval().getRequest()
                    : null;
            this.evHandler.updateSoc(now, request, isCharging());
        }
    }

    @Override
    public void onSocValuesChanged(SocValues socValues) {
        publishEVChargerSocChangedEvent(LocalDateTime.now(), socValues);
    }

    private void publishControlMessage(boolean on) {
        VariablePowerConsumerMessage message = new VariablePowerConsumerMessage(
                LocalDateTime.now(),
                on,
                chargePower,
                useOptionalEnergy
        );
        mqttClient.publish(Control.TOPIC, message, false);
    }

    private void publishEVChargerStateChangedEvent(LocalDateTime now, EVChargerState previousState,
                                                   EVChargerState newState, Integer evId) {
        EVChargerStateChangedEvent event = new EVChargerStateChangedEvent(now, previousState, newState, evId);
        mqttClient.publish(MqttEventName.EVChargerStateChanged, event, false);
    }

    private void publishEVChargerSocChangedEvent(LocalDateTime now, SocValues socValues) {
        EVChargerSocChangedEvent event = new EVChargerSocChangedEvent(now, socValues, this.evHandler.getChargeLoss());
        mqttClient.publish(MqttEventName.EVChargerSocChanged, event, false);
    }
}
