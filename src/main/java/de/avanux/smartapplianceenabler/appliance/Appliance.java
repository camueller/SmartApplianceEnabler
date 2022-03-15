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
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.pigpioj.PigpioInterface;

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
            @XmlElement(name = "StartingCurrentSwitch", type = StartingCurrentSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class),
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

    public void init(PigpioInterface pigpioInterface, Map<String, ModbusTcp> modbusIdWithModbusTcp, String notificationCommand) {
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
        if(isEvCharger()) {
            ElectricVehicleCharger evCharger = ((ElectricVehicleCharger) control);
            evCharger.setAppliance(this);

            DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(this.id);
            if(deviceInfo.getCharacteristics() != null && deviceInfo.getCharacteristics().getMinPowerConsumption() != null) {
                evCharger.setMinPowerConsumption(deviceInfo.getCharacteristics().getMinPowerConsumption());
            }
        }
        if(control instanceof StartingCurrentSwitch) {
            Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
            ((ApplianceIdConsumer) wrappedControl).setApplianceId(id);
            wrappedControl.setMqttTopic(StartingCurrentSwitch.WRAPPED_CONTROL_TOPIC);
            wrappedControl.setPublishControlStateChangedEvent(false);
        }
        if(!(control instanceof StartingCurrentSwitch)) {
            control.init();
        }

        if(control instanceof StartingCurrentSwitch) {
            StartingCurrentSwitch startingCurrentSwitch = (StartingCurrentSwitch) control;
            startingCurrentSwitch.setTimeframeIntervalHandler(timeframeIntervalHandler);
            startingCurrentSwitch.init();
            logger.debug("{}: {} uses {}", id, control.getClass().getSimpleName(), meter.getClass().getSimpleName());
        }

        if(getGpioControllables().size() > 0) {
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
            mqttClient.subscribe(Meter.TOPIC, true, MeterMessage.class, (topic, message) -> {
                meterMessage = (MeterMessage) message;
            });
        }
        if(meter != null) {
            logger.info("{}: Starting {}", id, meter.getClass().getSimpleName());
            meter.start(now, timer);
            if(control instanceof AlwaysOnSwitch || control instanceof MeterReportingSwitch) {
                meter.startEnergyMeter();
            }
        }
        if(control != null) {
            logger.info("{}: Starting {}", id, control.getClass().getSimpleName());
            control.start(now, timer);
            logger.info("{}: Switch off appliance initially", id);
            publishControlMessage(now, false);
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

    private void publishControlMessage(LocalDateTime now, boolean on) {
        mqttClient.publish(Control.TOPIC, new ControlMessage(now, on), true, true);
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
            else if(control instanceof StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                if(wrappedControl instanceof GpioControllable) {
                    controllables.add((GpioControllable) wrappedControl);
                }
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
                || isEvCharger()
            );
    }

    private boolean isEvCharger() {
        return this.control instanceof ElectricVehicleCharger;
    }

    public void setApplianceState(LocalDateTime now, boolean switchOn, Integer chargePower, String logMessage) {
        if(control != null) {
            logger.debug("{}: {}", id, logMessage);
            if(switchOn && isEvCharger()) {
                ElectricVehicleCharger evCharger = (ElectricVehicleCharger) control;
                if(chargePower != null) {
                    evCharger.setChargePower(chargePower);
                }
                else if(!evCharger.isOn()) {
                    evCharger.setChargePowerToMinimum();
                }
            }
            publishControlMessage(now, switchOn);
        }
        else {
            logger.warn("{}: Appliance configuration does not contain control.", id);
        }
    }

    public void setEnergyDemand(LocalDateTime now, Integer evId, Integer socCurrent, Integer socRequested, LocalDateTime chargeEnd) {
        if (isEvCharger()) {
            ElectricVehicleCharger evCharger = (ElectricVehicleCharger) this.control;
            if(meterMessage != null && meterMessage.energy > 0.1) {
                logger.debug("{}: skipping ev charger configuration to continue charging process already started", id);
            }
            else {
                logger.debug("{}: will do ev charger configuration", id);
                evCharger.setConnectedVehicleId(evId);
                evCharger.setSocInitial(socCurrent);
                evCharger.setSocCurrent(socCurrent);
                evCharger.setSocInitialTimestamp(now);
            }

            TimeframeInterval timeframeInterval =
                    evCharger.createTimeframeInterval(now, evId, socCurrent, socRequested, chargeEnd);
            timeframeIntervalHandler.addTimeframeInterval(now, timeframeInterval, true, true);

            if(chargeEnd == null) {
                // if no charge end is provided we switch on immediately with full power
                DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(this.id);
                int chargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();
                setApplianceState(now, true, chargePower,"Switching on ev charger with maximum power");
            }
        }
        else {
            logger.warn("{}: Energy demand can only be set for ev charger!", id);
        }
    }

    public void updateSoc(LocalDateTime now,  Integer socCurrent, Integer socRequested) {
        if (isEvCharger()) {
            ElectricVehicleCharger evCharger = (ElectricVehicleCharger) this.control;
            ElectricVehicle ev = evCharger.getConnectedVehicle();
            if(ev != null) {
                if(!evCharger.isOn() && !isAcceptControlRecommendations()) {
                    logger.debug("{}: Removing timeframe interval of stopped charging process", id);
                    TimeframeInterval activeTimeframeInterval = timeframeIntervalHandler.getActiveTimeframeInterval();
                    if(activeTimeframeInterval != null) {
                        timeframeIntervalHandler.removeTimeframeInterval(now, activeTimeframeInterval);
                    }
                }
                timeframeIntervalHandler.updateSocOfOptionalEnergyTimeframeIntervalForEVCharger(now,
                        ev.getId(), ev.getBatteryCapacity(), socCurrent, socRequested);
                evCharger.setSocInitial(socCurrent);
                evCharger.setSocInitialTimestamp(now);
                evCharger.setSocCurrent(socCurrent);
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
        if(meter != null && meter instanceof  ModbusElectricityMeter) {
            slaves.add((ModbusElectricityMeter) meter);
        }
        if(control != null) {
            if(control instanceof  ModbusSwitch) {
                slaves.add((ModbusSwitch) control);
            }
            else if(control instanceof  StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                if(wrappedControl instanceof ModbusSwitch) {
                    slaves.add((ModbusSwitch) wrappedControl);
                }
            }
            else if(isEvCharger()) {
                EVChargerControl evChargerControl = ((ElectricVehicleCharger) control).getControl();
                if(evChargerControl instanceof EVModbusControl) {
                    slaves.add((EVModbusControl) evChargerControl);
                }
            }
        }
        return slaves;
    }

    public boolean canConsumeOptionalEnergy(LocalDateTime now) {
        if(isEvCharger()) {
            return ((ElectricVehicleCharger) this.control).isUseOptionalEnergy();
        }
        TimeframeInterval activeTimeframeInterval = timeframeIntervalHandler.getActiveTimeframeInterval();
        if(activeTimeframeInterval != null) {
            Request request = activeTimeframeInterval.getRequest();
            return request.getMin(now) != null && request.getMax(now) > request.getMin(now);
        }
        return false;
    }

    private boolean hasStartingCurrentDetection() {
        return control != null && control instanceof StartingCurrentSwitch;
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval,
                                      TimeframeInterval activatedInterval, boolean wasRunning) {
        if(deactivatedInterval != null) {
            setApplianceState(now, false, null, "Switching off since timeframe interval was deactivated");
            if(meter != null && ! isEvCharger()) {
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
