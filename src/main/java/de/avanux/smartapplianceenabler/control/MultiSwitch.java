/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.mqtt.VariablePowerConsumerMessage;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class MultiSwitch implements VariablePowerConsumer, ApplianceIdConsumer, Validateable, NotificationProvider {
    private transient Logger logger = LoggerFactory.getLogger(MultiSwitch.class);
    @XmlAttribute
    private String id;
    @XmlElements({
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class)
    })
    private List<Control> controls;
    @XmlElement(name = "PowerLevel", type = PowerLevel.class)
    private List<PowerLevel> powerLevels;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient String applianceId;
    private transient Map<String, Boolean> controlStates = new HashMap<>();
    private transient NotificationHandler notificationHandler;
    private transient MqttClient mqttClient;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttMessage mqttMessageSent;

    @Override
    public String getId() {
        return id;
    }

    private String getWrappedControlTopic(String id) {
        return "Wrapped" + Control.TOPIC + "_" +  id;
    }

    private String getWrappedControlId(String topic) {
        return topic.split("_")[1];
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        if(this.controls != null) {
            this.controls.forEach(control -> {
                if(control instanceof  ApplianceIdConsumer) {
                    ((ApplianceIdConsumer) control).setApplianceId(applianceId);
                }
            });
        }
    }

    public List<PowerLevel> getPowerLevels() {
        return powerLevels;
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    @Override
    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
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
        return this.notifications;
    }

    @Override
    public void validate() throws ConfigurationException {
        if(powerLevels != null) {
            powerLevels.forEach(powerLevel -> logger.debug("{}: configured: {}", this.applianceId, powerLevel.toString()));
        }
    }

    @Override
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
        for(int i=0; i<this.controls.size(); i++) {
            String topic = getWrappedControlTopic(this.controls.get(i).getId());
            controls.get(i).setMqttTopic(topic);
            controls.get(i).setPublishControlStateChangedEvent(false);
            controls.get(i).init();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting ...", this.applianceId);
        this.controls.forEach(control -> control.start(now, timer));
        if(mqttClient != null) {
            for(int i=0; i<this.controls.size(); i++) {
                String topic = getWrappedControlTopic(this.controls.get(i).getId());
                mqttClient.subscribe(topic, true, ControlMessage.class, (receivedTopic, message) -> {
                    if(message instanceof ControlMessage) {
                        controlStates.put(getWrappedControlId(receivedTopic), ((ControlMessage) message).on);
                    }
                });
            }
            mqttClient.subscribe(Control.TOPIC, true, true, VariablePowerConsumerMessage.class, (topic, message) -> {
                if(message instanceof VariablePowerConsumerMessage) {
                    VariablePowerConsumerMessage controlMessage = (VariablePowerConsumerMessage) message;
                    if (controlMessage.on) {
                        if (controlMessage.power != null) {
                            setPower(now, controlMessage.power);
                        }
                    } else {
                        setPower(now, 0);
                    }
                }
            });
            this.mqttPublishTimerTask = new GuardedTimerTask(applianceId, "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        publishControlMessage(now, getPower());
                    }
                    catch(Exception e) {
                        logger.error("{}: Error publishing MQTT message", applianceId, e);
                    }
                }
            };
            timer.schedule(this.mqttPublishTimerTask, 0, this.mqttPublishTimerTask.getPeriod());
        }
    }


    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", this.applianceId);
        this.controls.forEach(control -> control.stop(now));
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    public boolean isOn() {
        return this.controlStates.containsValue(true);
    }

    private void setWrappedControlOn(LocalDateTime now, String controlId, boolean switchOn) {
        logger.debug("{}: Setting wrapped control switch {} to {}", applianceId, controlId, (switchOn ? "on" : "off"));
        String topic = getWrappedControlTopic(controlId);
        mqttClient.publish(topic, new ControlMessage(now, switchOn), true, true);
    }

    @Override
    public void setPower(LocalDateTime now, int power) {
        if(this.powerLevels != null) {
            logger.info("{}: Setting power to {}W", applianceId, power);
            if(power == 0) {
                this.controls.forEach(control -> setWrappedControlOn(now, control.getId(), false));
            }
            else {
                Optional<PowerLevel> powerLevel = this.powerLevels.stream().filter(pl -> pl.getPower() == power).findFirst();
                powerLevel.ifPresent(level -> level.getSwitchStatuses().forEach(switchStatus -> {
                    Control control = controls.stream().filter(c -> c.getId().equals(switchStatus.getIdref())).findFirst().get();
                    setWrappedControlOn(now, control.getId(), switchStatus.getOn());
                }));
            }
            if(this.notificationHandler != null) {
                this.notificationHandler.sendNotification(power > 0 ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
            }
        }
        else {
            logger.error("{}: No power levels configured", applianceId);
        }
    }

    private int getPower() {
        if(this.powerLevels != null) {
            Optional<PowerLevel> powerLevel = this.powerLevels.stream().filter(
                    pl -> pl.getSwitchStatuses().stream().allMatch(
                            switchStatus -> controlStates.get(switchStatus.getIdref()) == switchStatus.getOn())).findFirst();
            if(powerLevel.isPresent()) {
                return powerLevel.get().getPower();
            }
        }
        return 0;
    }

    @Override
    public void setMinPower(Integer minPower) {
    }

    @Override
    public void setMaxPower(int maxPower) {
    }

    private void publishControlMessage(LocalDateTime now, Integer power) {
        MqttMessage message = new VariablePowerConsumerMessage(now, isOn(), power, null);
        if(!message.equals(mqttMessageSent)) {
            mqttClient.publish(Control.TOPIC, message, true);
            mqttMessageSent = message;
        }
    }
}
