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

import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.modbus.*;
import de.avanux.smartapplianceenabler.modbus.executor.*;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.Environment;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class ModbusSwitch extends ModbusSlave implements Control, Validateable, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(ModbusSwitch.class);
    @XmlAttribute
    private String id;
    @XmlElement(name = "ModbusWrite")
    private List<ModbusWrite> modbusWrites;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;
    private transient String mqttTopic = Control.TOPIC;
    private transient boolean publishControlStateChangedEvent = true;

    @Override
    public String getId() {
        return id;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
        this.publishControlStateChangedEvent = publishControlStateChangedEvent;
    }

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(this.notificationHandler != null) {
            this.notificationHandler.setRequestedNotifications(notifications);
        }
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    @Override
    public void init() {
        logger.debug("{}: Initializing ...", getApplianceId());
        mqttClient = new MqttClient(getApplianceId(), getClass());
    }

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", getApplianceId());
        boolean valid = true;
        ModbusValidator validator = new ModbusValidator(getApplianceId());
        for(ControlValueName valueName: ControlValueName.values()) {
            ParentWithChild<ModbusWrite, ModbusWriteValue> write
                    = ModbusWrite.getFirstRegisterWrite(valueName.name(), this.modbusWrites);
            valid = validator.validateWrites(valueName.name(), Collections.singletonList(write));
        }
        if(! valid) {
            throw new ConfigurationException();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        if(mqttClient != null) {
            this.mqttPublishTimerTask = new GuardedTimerTask(getApplianceId(), "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        publishControlMessage(isOn());
                    }
                    catch(Exception e) {
                        logger.error("{}: Error publishing MQTT message", getApplianceId(), e);
                    }
                }
            };
            // initial publishControlMessage() is triggered by initial "switch off" in on(false)
            timer.schedule(this.mqttPublishTimerTask, this.mqttPublishTimerTask.getPeriod(), this.mqttPublishTimerTask.getPeriod());
            mqttClient.subscribe(mqttTopic, true, true, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    ControlMessage controlMessage = (ControlMessage) message;
                    this.on(controlMessage.getTime(), controlMessage.on);
                }
            });
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        if(this.mqttPublishTimerTask != null) {
            this.mqttPublishTimerTask.cancel();
        }
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {}", getApplianceId(), (switchOn ? "on" : "off"));
        if(Environment.isModbusDisabled()) {
            return true;
        }
        boolean result = false;
        ParentWithChild<ModbusWrite, ModbusWriteValue> write
                = ModbusWrite.getFirstRegisterWrite(getValueName(switchOn).name(), this.modbusWrites);
        if (write != null) {
            ModbusWrite registerWrite = write.parent();
            try {
                boolean on = false;
                if(this.notificationHandler != null) {
                    on = isOn();
                }
                ModbusWriteTransactionExecutor executor = ModbusExecutorFactory.getWriteExecutor(getApplianceId(),
                        registerWrite.getType(), registerWrite.getValueType(), registerWrite.getAddress(),registerWrite.getFactorToValue());
                if(executor instanceof WriteCoilExecutor) {
                    executor.setValue(1 == Integer.valueOf(write.child().getValue()));
                    executeTransaction(executor, true);
                    result = switchOn == ((WriteCoilExecutor) executor).getResult();
                }
                else if(executor instanceof WriteHoldingRegisterExecutor) {
                    executor.setValue(Integer.valueOf(write.child().getValue()));
                    executeTransaction(executor, true);
                    result = Integer.valueOf(write.child().getValue()).equals(((WriteHoldingRegisterExecutor) executor).getResult());
                }
                publishControlMessage(switchOn);
                if(this.notificationHandler != null && switchOn != on) {
                    publishControlMessage(switchOn);
                    this.notificationHandler.sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
                }
            }
            catch (Exception e) {
                logger.error("{}: Error switching {} using register {}", getApplianceId(),  (switchOn ? "on" : "off"),
                        registerWrite.getAddress(), e);
                if(this.notificationHandler != null) {
                    this.notificationHandler.sendNotification(NotificationType.COMMUNICATION_ERROR);
                }
            }
        }
        return result;
    }

    public boolean isOn() {
        if(Environment.isModbusDisabled()) {
            return false;
        }
        boolean on = false;
        ParentWithChild<ModbusWrite, ModbusWriteValue> write
                = ModbusWrite.getFirstRegisterWrite(ControlValueName.On.name(), this.modbusWrites);
        if(write != null) {
            ModbusWrite registerWrite = write.parent();
            try {
                RegisterValueType registerValueType = getRegisterValueType(
                        registerWrite.getReadRegisterType(), registerWrite.getValueType());
                ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                        registerWrite.getAddress(), registerWrite.getReadRegisterType(), registerValueType);
                executeTransaction(executor, true);
                if(executor instanceof ReadCoilExecutorImpl) {
                    on = ((ReadCoilExecutorImpl) executor).getValue();
                }
                else if(executor instanceof ReadHoldingRegisterExecutor) {
                    Object registerValue = ((ReadHoldingRegisterExecutor) executor).getValueTransformer().getValue();
                    if(registerValue instanceof Integer) {
                        on = Integer.valueOf(write.child().getValue()).equals((Integer) registerValue);
                    }
                }
            }
            catch (Exception e) {
                logger.error("{}: Error reading {} register {}", getApplianceId(), registerWrite.getReadRegisterType(),
                        registerWrite.getAddress(), e);
            }
        }
        return on;
    }

    private void publishControlMessage(boolean on) {
        MqttMessage message = new ControlMessage(LocalDateTime.now(), on);
        mqttClient.publish(mqttTopic, message, false);
    }

    private RegisterValueType getRegisterValueType(ReadRegisterType registerType, RegisterValueType defaultRegisterValueType) {
        if(registerType == ReadRegisterType.Coil) {
            return RegisterValueType.Integer;
        }
        return defaultRegisterValueType;
    }

    private ControlValueName getValueName(boolean switchOn) {
        return switchOn ? ControlValueName.On : ControlValueName.Off;
    }
}
