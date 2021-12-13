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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import java.time.LocalDateTime;

import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class Switch extends GpioControllable implements Control, ApplianceIdConsumer, NotificationProvider {
    private transient Logger logger = LoggerFactory.getLogger(Switch.class);
    @XmlAttribute
    private boolean reverseStates;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient GpioPinDigitalOutput outputPin;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;
    private transient String mqttTopic = Control.TOPIC;
    private transient boolean publishControlStateChangedEvent = true;

    @Override
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
        mqttClient = new MqttClient(getApplianceId(), getClass());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting {} for {}", getApplianceId(), getClass().getSimpleName(), getGpio());
        GpioController gpioController = getGpioController();
        if (gpioController != null) {
            try {
                outputPin = (GpioPinDigitalOutput) gpioController.getProvisionedPin(getGpio());
                if(outputPin == null) {
                    outputPin = gpioController.provisionDigitalOutputPin(getGpio(), adjustState(PinState.LOW));
                }
                logger.debug("{}: {} uses {} reverseStates={}", getApplianceId(), getClass().getSimpleName(),
                        getGpio(), reverseStates);
            } catch (Exception e) {
                logger.error("{}: Error starting {} for {}", getApplianceId(), getClass().getSimpleName(), getGpio(), e);
            }
            this.mqttPublishTimerTask = new GuardedTimerTask(getApplianceId(), "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        publishControlMessage(LocalDateTime.now(), isOn());
                    }
                    catch(Exception e) {
                        logger.error("{}: Error publishing MQTT message", getApplianceId(), e);
                    }
                }
            };
            timer.schedule(this.mqttPublishTimerTask, 0, this.mqttPublishTimerTask.getPeriod());

            mqttClient.subscribe(mqttTopic, true, true, ControlMessage.class, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    ControlMessage controlMessage = (ControlMessage) message;
                    this.on(controlMessage.getTime(), controlMessage.on);
                }
            });
        } else {
            logGpioAccessDisabled(logger);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping {} for {}", getApplianceId(), getClass().getSimpleName(), getGpio());
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {} {}", getApplianceId(), (switchOn ? "on" : "off"), getGpio());
        if (outputPin != null) {
            outputPin.setState(adjustState(switchOn ? PinState.HIGH : PinState.LOW));
        } else {
            logGpioAccessDisabled(logger);
        }
        publishControlStateChangedEvent(now, switchOn);
        publishControlMessage(now, switchOn);
        if(this.notificationHandler != null) {
            this.notificationHandler.sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
        return true;
    }

    public boolean isOn() {
        if (outputPin != null) {
            return adjustState(outputPin.getState()) == PinState.HIGH;
        }
        logGpioAccessDisabled(logger);
        return false;
    }

    private PinState adjustState(PinState pinState) {
        if (reverseStates) {
            if (pinState == PinState.HIGH) {
                return PinState.LOW;
            }
            return PinState.HIGH;
        }
        return pinState;
    }

    private void publishControlMessage(LocalDateTime now, boolean on) {
        MqttMessage message = new ControlMessage(now, isOn());
        mqttClient.publish(mqttTopic, message, true);
    }

    private void publishControlStateChangedEvent(LocalDateTime now, boolean on) {
        if(publishControlStateChangedEvent) {
            ControlStateChangedEvent event = new ControlStateChangedEvent(now, on);
            mqttClient.publish(MqttEventName.ControlStateChanged, event);
        }
    }
}