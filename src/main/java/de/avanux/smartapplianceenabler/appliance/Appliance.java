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
package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.control.*;
import de.avanux.smartapplianceenabler.control.ev.*;
import de.avanux.smartapplianceenabler.gpio.GpioControllable;
import de.avanux.smartapplianceenabler.gpio.GpioAccessProvider;
import de.avanux.smartapplianceenabler.meter.*;
import de.avanux.smartapplianceenabler.modbus.EVModbusControl;
import de.avanux.smartapplianceenabler.modbus.ModbusElectricityMeterDefaults;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.Notification;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.Characteristics;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Appliance implements Validateable, TimeframeIntervalChangedListener {

    private transient Logger logger = LoggerFactory.getLogger(Appliance.class);
    @XmlAttribute
    private String id;
    // Mapping interfaces in JAXB:
    // https://jaxb.java.net/guide/Mapping_interfaces.html
    // http://stackoverflow.com/questions/25374375/jaxb-wont-unmarshal-my-previously-marshalled-interface-impls
    @XmlElements({
            @XmlElement(name = "AlwaysOnSwitch", type = AlwaysOnSwitch.class),
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class),
            @XmlElement(name = "MeterReportingSwitch", type = MeterReportingSwitch.class),
            @XmlElement(name = "MockSwitch", type = MockSwitch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "LevelSwitch", type = LevelSwitch.class),
            @XmlElement(name = "StartingCurrentSwitch", type = StartingCurrentSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class),
            @XmlElement(name = "SwitchOption", type = SwitchOption.class),
            @XmlElement(name = "PwmSwitch", type = PwmSwitch.class),
            @XmlElement(name = "ElectricVehicleCharger", type = ElectricVehicleCharger.class),
    })
    private Control control;
    @XmlElements({
            @XmlElement(name = "HttpElectricityMeter", type = HttpElectricityMeter.class),
            @XmlElement(name = "ModbusElectricityMeter", type = ModbusElectricityMeter.class),
            @XmlElement(name = "S0ElectricityMeter", type = S0ElectricityMeter.class),
            @XmlElement(name = "MasterElectricityMeter", type = MasterElectricityMeter.class),
            @XmlElement(name = "SlaveElectricityMeter", type = SlaveElectricityMeter.class),
    })
    private Meter meter;
    @XmlElement(name = "Schedule")
    private List<Schedule> schedules;
    @XmlElement(name = "Notification")
    private Notification notification;
    private transient TimeframeIntervalHandler timeframeIntervalHandler;
    private transient MqttClient mqttClient;
    private transient MeterMessage meterMessage;
    private transient static final int CONSIDERATION_INTERVAL_DAYS = 2;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Meter getMeter() {
        return this.meter;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public void deleteMeter() {
        logger.debug("{}: Delete meter", id);
        if(meter != null) {
            meter.stop(LocalDateTime.now());
        }
        setMeter(null);
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        if(control instanceof StartingCurrentSwitch) {
            if(((StartingCurrentSwitch) control).getControl() != null) {
                // only accept StartingCurrentSwitch with inner control
                this.control = control;
            }
            else {
                this.control = null;
            }
        }
        else {
            this.control = control;
        }
    }

    public void deleteControl() {
        logger.debug("{}: Delete control", id);
        if(control != null) {
            control.stop(LocalDateTime.now());
        }
        setControl(null);
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
        if(timeframeIntervalHandler != null) {
            timeframeIntervalHandler.setSchedules(schedules);
            timeframeIntervalHandler.clearQueue();
            timeframeIntervalHandler.fillQueue(LocalDateTime.now());
        }
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public boolean isAcceptControlRecommendations() {
        if(!this.control.isControllable()) {
            return false;
        }
        if(this.timeframeIntervalHandler != null) {
            TimeframeInterval activeOrQueuedTimeframeInterval = this.timeframeIntervalHandler.getFirstTimeframeInterval(
                    TimeframeIntervalState.ACTIVE, TimeframeIntervalState.QUEUED);
            return activeOrQueuedTimeframeInterval != null
                    && activeOrQueuedTimeframeInterval.getRequest().isAcceptControlRecommendations();
        }
        return false;
    }

    public TimeframeIntervalHandler getTimeframeIntervalHandler() {
        return timeframeIntervalHandler;
    }

    public void setTimeframeIntervalHandler(TimeframeIntervalHandler timeframeIntervalHandler) {
        if(this.timeframeIntervalHandler == null) {
            this.timeframeIntervalHandler = timeframeIntervalHandler;
        }
        this.timeframeIntervalHandler.setApplianceId(id);
        this.timeframeIntervalHandler.setMqttClient(mqttClient);
        this.timeframeIntervalHandler.addTimeframeIntervalChangedListener(this);
    }

    public void init(Map<String, ModbusTcp> modbusIdWithModbusTcp, String notificationCommand) {
        logger.debug("{}: Initializing appliance", id);
        mqttClient = new MqttClient(id, getClass());
        if(getTimeframeIntervalHandler() == null) {
            setTimeframeIntervalHandler(new TimeframeIntervalHandler(this.schedules, this.control));
        }
        Meter meter = getMeter();
        if(meter != null) {
            if(meter instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) meter).setApplianceId(id);
            }
            if(meter instanceof HttpElectricityMeter) {
                ((HttpElectricityMeter) meter).setPollInterval(
                        control instanceof StartingCurrentSwitch
                                ? StartingCurrentSwitchDefaults.getPollInterval()
                                : HttpElectricityMeterDefaults.getPollInterval());
            }
            if(meter instanceof ModbusElectricityMeter) {
                ((ModbusElectricityMeter) meter).setPollInterval(
                        control instanceof StartingCurrentSwitch
                                ? StartingCurrentSwitchDefaults.getPollInterval()
                                : ModbusElectricityMeterDefaults.getPollInterval());
            }
            if(meter instanceof MasterElectricityMeter) {
                MasterElectricityMeter masterMeter = (MasterElectricityMeter) meter;
                var wrappedMeter = masterMeter.getWrappedMeter();
                wrappedMeter.setMqttTopic(MasterElectricityMeter.WRAPPED_METER_TOPIC);
            }
            if(meter instanceof SlaveElectricityMeter) {
                SlaveElectricityMeter slaveMeter = (SlaveElectricityMeter) meter;
                Appliance masterAppliance = ApplianceManager.getInstance().getAppliance(slaveMeter.getMasterElectricityMeterApplianceId());
                MasterElectricityMeter masterMeter = (MasterElectricityMeter) masterAppliance.getMeter();
                masterMeter.setSlaveElectricityMeter((SlaveElectricityMeter) meter);
                slaveMeter.setMasterElectricityMeter(masterMeter);
            }
            if(meter instanceof NotificationProvider && notificationCommand != null) {
                NotificationHandler notificationHandler = new NotificationHandler(
                        id,
                        notificationCommand,
                        this.notification != null ? this.notification.getSenderId() : null,
                        this.notification != null ? this.notification.getMaxCommunicationErrors() : null
                );
                ((NotificationProvider) meter).setNotificationHandler(notificationHandler);
            }
            meter.init();
        }
        if(control == null) {
            control = new MeterReportingSwitch();
        }
        if(control instanceof ApplianceIdConsumer) {
            ((ApplianceIdConsumer) control).setApplianceId(id);
        }
        if(control instanceof NotificationProvider && notificationCommand != null) {
            NotificationHandler notificationHandler = new NotificationHandler(
                    id,
                    notificationCommand,
                    this.notification != null ? this.notification.getSenderId() : null,
                    this.notification != null ? this.notification.getMaxCommunicationErrors() : null
            );
            ((NotificationProvider) control).setNotificationHandler(notificationHandler);
        }
        if(isElectricVehicleCharger()) {
            ElectricVehicleCharger evCharger = ((ElectricVehicleCharger) control);
            evCharger.setAppliance(this);
        }
        if(control instanceof VariablePowerConsumer) {
            var powerConsumer = (VariablePowerConsumer) control;
            DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(this.id);
            Characteristics characteristics = deviceInfo.getCharacteristics();
            if(characteristics != null) {
                if(characteristics.getMinPowerConsumption() != null) {
                    powerConsumer.setMinPower(characteristics.getMinPowerConsumption());
                }
                powerConsumer.setMaxPower(characteristics.getMaxPowerConsumption());
            }
        }

        if(control instanceof TimeframeIntervalHandlerDependency) {
            TimeframeIntervalHandlerDependency timeframeIntervalHandlerDependency = (TimeframeIntervalHandlerDependency) control;
            timeframeIntervalHandlerDependency.setTimeframeIntervalHandler(timeframeIntervalHandler);
        }

        control.init();

        if(getGpioControllables().size() > 0) {
            var pigpioInterface = GpioAccessProvider.getPigpioInterface();
            if(pigpioInterface != null) {
                for(GpioControllable gpioControllable : getGpioControllables()) {
                    logger.info("{}: Configuring GPIO for {}", id, gpioControllable.getClass().getSimpleName());
                    gpioControllable.setPigpioInterface(pigpioInterface);
                }
            }
            else {
                logger.error("pigpioInterface not available.");
            }
        }

        for(ModbusSlave modbusSlave : getModbusSlaves()) {
            logger.info("{}: Configuring {}", id, modbusSlave.getClass().getSimpleName());
            String modbusId = modbusSlave.getIdref();
            ModbusTcp modbusTcp = modbusIdWithModbusTcp.get(modbusId);
            modbusSlave.setModbusTcp(modbusTcp);
        }
    }

    @Override
    public void validate() throws ConfigurationException {
        logger.info("{}: Validating appliance configuration", id);
        if(control instanceof Validateable) {
            ((Validateable) control).validate();
        }
        if(meter instanceof Validateable) {
            ((Validateable) meter).validate();
        }
    }

    public void start(Timer timer) {
        logger.info("{}: Starting appliance", id);
        LocalDateTime now = LocalDateTime.now();
        if(timeframeIntervalHandler != null) {
            timeframeIntervalHandler.setTimer(timer);
        }
        if(mqttClient != null) {
            mqttClient.subscribe(Meter.TOPIC, true, (topic, message) -> {
                meterMessage = (MeterMessage) message;
            });
            mqttClient.subscribe(MqttEventName.WrappedControlSwitchOnDetected, (topic, message) -> {
                logger.debug("{} Handling event WrappedControlSwitchOnDetected", id);
                if(control instanceof StartingCurrentSwitch) {
                    mqttClient.publish(MqttEventName.EnableRuntimeRequest, new MqttMessage(now));
                }
            });
            mqttClient.subscribe(MqttEventName.WrappedControlSwitchOffDetected, (topic, message) -> {
                logger.debug("{} Handling event WrappedControlSwitchOffDetected", id);
                publishControlMessage(now, false, null);
                if(control instanceof StartingCurrentSwitch) {
                    mqttClient.publish(MqttEventName.DisableRuntimeRequest, new MqttMessage(now));
                }
            });
        }
        if(meter != null) {
            logger.info("{}: Starting {}", id, meter.getClass().getSimpleName());
            meter.start(now, timer);
            if(isEnergyMeteredDaily()) {
                meter.startEnergyMeter();
            }
        }
        if(control != null) {
            logger.info("{}: Starting {}", id, control.getClass().getSimpleName());
            control.start(now, timer);
            logger.info("{}: Switch off appliance initially", id);
            publishControlMessage(now, false, null);
        }
    }

    public void stop() {
        logger.info("{}: Stopping appliance ...", id);
        LocalDateTime now = LocalDateTime.now();
        if(control != null) {
            logger.info("{}: Stopping {}", id, control.getClass().getSimpleName());
            control.stop(now);
        }
        if(meter != null) {
            logger.info("{}: Stopping meter {}", id, meter.getClass().getSimpleName());
            meter.stop(now);
        }
        if(timeframeIntervalHandler != null) {
            timeframeIntervalHandler.cancelTimer();
            timeframeIntervalHandler.disconnectMqttClient();
        }
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    public void atMidnight() {
        logger.debug("{}: Running midnight procdure ...", id);
        if(meter != null && isEnergyMeteredDaily()) {
            meter.resetEnergyMeter();
        }
    }

    private void publishControlMessage(LocalDateTime now, boolean on, Integer power) {
        var message = getControl() instanceof VariablePowerConsumer ?
                new VariablePowerConsumerMessage(now, on, power, null) : new ControlMessage(now, on);
        mqttClient.publish(Control.TOPIC, message, true, false);
    }

    public void setHolidays(List<LocalDate> holidays) {
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                final Timeframe timeframe = schedule.getTimeframe();
                if(timeframe instanceof DayTimeframe) {
                    ((DayTimeframe) timeframe).setHolidays(holidays);
                }
            }
        }
    }

    public boolean hasTimeframeForHolidays() {
        if(schedules != null) {
            for (Schedule schedule : schedules) {
                Timeframe timeframe = schedule.getTimeframe();
                if(timeframe instanceof  DayTimeframe) {
                    List<Integer> daysOfWeekValues = ((DayTimeframe) timeframe).getDaysOfWeekValues();
                    if(daysOfWeekValues != null && daysOfWeekValues.contains(DayTimeframe.DOW_HOLIDAYS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasTimeframesWithOptionalEnergyRequests() {
        LocalDateTime now = LocalDateTime.now();
        if(schedules != null) {
            return schedules.stream().anyMatch(schedule -> {
                int min = schedule.getRequest().getMin(now) != null ? schedule.getRequest().getMin(now) : 0;
                return min < schedule.getRequest().getMax(now);
            });
        }
        return false;
    }

    public boolean isEnergyMeteredDaily() {
        var hasSchedules = schedules != null ? this.schedules.size() > 0 : false;
        return !(hasSchedules || isElectricVehicleCharger() || (this.meter instanceof MasterElectricityMeter));
    }

    private Set<GpioControllable> getGpioControllables() {
        Set<GpioControllable> controllables = new HashSet<GpioControllable>();
        if(meter != null) {
            if(meter instanceof S0ElectricityMeter) {
                controllables.add((S0ElectricityMeter) meter);
            } else if(meter instanceof MasterElectricityMeter) {
                Meter wrappedMeter = ((MasterElectricityMeter) meter).getWrappedMeter();
                if(wrappedMeter instanceof GpioControllable) {
                    controllables.add((GpioControllable) wrappedMeter);
                }
            }
        }
        if(control != null) {
            if(control instanceof GpioControllable) {
                controllables.add((GpioControllable) control);
            }
            else if(control instanceof WrappedControl) {
                Control wrappedControl = ((WrappedControl) control).getControl();
                if(wrappedControl instanceof GpioControllable) {
                    controllables.add((GpioControllable) wrappedControl);
                }
            }
            else if(control instanceof LevelSwitch) {
                controllables.addAll(((LevelSwitch) control).getGpioControllables());
            }
        }
        return controllables;
    }

    public boolean isControllable() {
        return control != null &&
            (
                control instanceof Switch
                || control instanceof HttpSwitch
                || control instanceof ModbusSwitch
                || control instanceof MockSwitch
                || control instanceof StartingCurrentSwitch
                || control instanceof VariablePowerConsumer
            );
    }

    public boolean isElectricVehicleCharger() {
        return this.control instanceof ElectricVehicleCharger;
    }

    public void setApplianceState(LocalDateTime now, boolean switchOn, boolean currentlySwitchedOn, Integer power, String logMessage) {
        if(control != null) {
            logger.debug("{}: {}", id, logMessage);
            if(switchOn && power == null && currentlySwitchedOn) {
                logger.debug("{}: Ignoring switch-on command without power value provided while already switched on", id);
            }
            else {
                if(control instanceof VariablePowerConsumer && switchOn && power == null) {
                    var minPower = ((VariablePowerConsumer) control).getMinPower();
                    power = minPower != null ? minPower : 0;
                    logger.debug("{}: power value not provided. Will use configured minimum power: {}W", id, power);
                }
                publishControlMessage(now, switchOn, power);
            }
        }
        else {
            logger.warn("{}: Appliance configuration does not contain control.", id);
        }
    }

    public void setEnergyDemand(LocalDateTime now, Integer evId, Integer socCurrent, Integer socTarget, LocalDateTime chargeEnd) {
        if (isElectricVehicleCharger()) {
            ElectricVehicleCharger evCharger = (ElectricVehicleCharger) this.control;
            if(meterMessage != null && meterMessage.energy > 0.1) {
                logger.debug("{}: skipping ev charger configuration to continue charging process already started", id);
            }
            else {
                logger.debug("{}: will do ev charger configuration", id);
                var evHandler = evCharger.getElectricVehicleHandler();
                evHandler.setConnectedVehicleId(evId);
                evHandler.setSocInitial(socCurrent);
                evHandler.setSocCurrent(socCurrent);
                evHandler.setSocInitialTimestamp(now);
            }

            TimeframeInterval timeframeInterval =
                    evCharger.createTimeframeInterval(now, evId, socCurrent, socTarget, chargeEnd);
            timeframeIntervalHandler.addTimeframeInterval(now, timeframeInterval, true, true);

            if(chargeEnd == null) {
                // if no charge end is provided we switch on immediately with full power
                DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(this.id);
                int chargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();
                setApplianceState(now, true, false, chargePower,"Switching on ev charger with maximum power");
            }
        }
        else {
            logger.warn("{}: Energy demand can only be set for ev charger!", id);
        }
    }

    public void updateSoc(LocalDateTime now,  Integer socCurrent, Integer socTarget) {
        if (isElectricVehicleCharger()) {
            ElectricVehicleCharger evCharger = (ElectricVehicleCharger) this.control;
            ElectricVehicle ev = evCharger.getElectricVehicleHandler().getConnectedVehicle();
            if(ev != null) {
                if(!evCharger.isOn() && !isAcceptControlRecommendations()) {
                    logger.debug("{}: Removing timeframe interval of stopped charging process", id);
                    timeframeIntervalHandler.removeActiveTimeframeInterval(now);
                }
                timeframeIntervalHandler.updateSocOfOptionalEnergyTimeframeIntervalForEVCharger(now,
                        ev.getId(), ev.getBatteryCapacity(), socCurrent, socTarget);
                var evHandler = evCharger.getElectricVehicleHandler();
                evHandler.setSocInitial(socCurrent);
                evHandler.setSocInitialTimestamp(now);
                evHandler.setSocCurrent(socCurrent);
                evCharger.resetChargingCompletedToVehicleConnected(now);
            }
            else {
                logger.warn("{}: no ev connected", id);
            }
        }
        else {
            logger.warn("{}: SOC can only be set for ev charger!", id);
        }
    }

    private Set<ModbusSlave> getModbusSlaves() {
        Set<ModbusSlave> slaves = new HashSet<ModbusSlave>();
        if(meter != null) {
            if(meter instanceof ModbusElectricityMeter) {
                slaves.add((ModbusElectricityMeter) meter);
            }
            else if (meter instanceof MasterElectricityMeter) {
                Meter wrappedMeter = ((MasterElectricityMeter) meter).getWrappedMeter();
                if(wrappedMeter instanceof ModbusElectricityMeter) {
                    slaves.add((ModbusElectricityMeter) wrappedMeter);
                }
            }
        }
        if(control != null) {
            if(control instanceof ModbusSwitch) {
                slaves.add((ModbusSwitch) control);
            }
            else if(control instanceof StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                if(wrappedControl instanceof ModbusSwitch) {
                    slaves.add((ModbusSwitch) wrappedControl);
                }
            }
            else if(isElectricVehicleCharger()) {
                EVChargerControl evChargerControl = ((ElectricVehicleCharger) control).getControl();
                if(evChargerControl instanceof EVModbusControl) {
                    slaves.add((EVModbusControl) evChargerControl);
                }
            }
        }
        return slaves;
    }

    public boolean canConsumeOptionalEnergy(LocalDateTime now) {
        if(this.control instanceof VariablePowerConsumer) {
            return true;
        }
        return hasTimeframesWithOptionalEnergyRequests();
    }

    private boolean hasStartingCurrentDetection() {
        return control != null && control instanceof StartingCurrentSwitch;
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval,
                                      TimeframeInterval activatedInterval, boolean wasRunning) {
        if(deactivatedInterval != null) {
            setApplianceState(now, false, true, null, "Switching off since timeframe interval was deactivated");
        }
        if(activatedInterval != null) {
            if(meter != null && !isElectricVehicleCharger()) {
                meter.resetEnergyMeter();
            }
        }
    }

    @Override
    public void timeframeIntervalCreated(LocalDateTime now, TimeframeInterval timeframeInterval) {
        timeframeInterval.setApplianceId(id);
        timeframeInterval.getRequest().setApplianceId(id);
        timeframeInterval.getRequest().setMeter(meter);
    }

    @Override
    public String toString() {
        return id;
    }
}
