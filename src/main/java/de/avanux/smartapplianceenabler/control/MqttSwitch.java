/*
 * Copyright (C) 2023 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import de.avanux.smartapplianceenabler.protocol.JsonContentProtocolHandler;
import de.avanux.smartapplianceenabler.util.Environment;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.ValueExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Timer;

public class MqttSwitch implements Control, ApplianceLifeCycle, Validateable, ApplianceIdConsumer, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(MqttSwitch.class);
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String topic;
    @XmlAttribute
    private String onPayload;
    @XmlAttribute
    private String offPayload;
    @XmlAttribute
    private String statusTopic;
    @XmlAttribute
    private String contentProtocol;
    @XmlAttribute
    private String statusExtractionRegex;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient String applianceId;
    protected transient boolean on;
    private transient ContentProtocolHandler contentContentProtocolHandler;
    private transient ValueExtractor valueExtractor = new ValueExtractor();
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;
    private transient String mqttTopic = Control.TOPIC;
    private transient boolean publishControlStateChangedEvent = true;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.valueExtractor.setApplianceId(applianceId);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    @Override
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
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", applianceId);
        logger.debug("{}: configured: topic={} onPayload={} offPayload={}",
                applianceId, topic, onPayload, offPayload);
        logger.debug("{}: status configured: statusTopic={} statusExtractionRegex={}",
                applianceId, statusTopic, statusExtractionRegex);

        if(topic == null) {
            logger.error("{}: Missing 'topic' property", applianceId);
            throw new ConfigurationException();
        }
        if(onPayload == null) {
            logger.error("{}: Missing 'onPayload' property", applianceId);
            throw new ConfigurationException();
        }
        if(offPayload == null) {
            logger.error("{}: Missing 'offPayload' property", applianceId);
            throw new ConfigurationException();
        }
    }

    @Override
    public void init() {
        logger.debug("{}: Initializing ...", applianceId);
        mqttClient = new MqttClient(applianceId, getClass());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting ...", applianceId);
        if(mqttClient != null) {
            this.mqttPublishTimerTask = new GuardedTimerTask(applianceId, "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        publishControlMessage(LocalDateTime.now(), isOn());
                    }
                    catch(Exception e) {
                        logger.error("{}: Error publishing MQTT message", applianceId, e);
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
            if(statusTopic != null) {
                mqttClient.subscribe(statusTopic, (topic, message) -> {
                    var messageString = new String(message, StandardCharsets.UTF_8);
                    logger.trace("{}: MQTT message received: {}", applianceId, messageString);
                    on = valueExtractor.getBooleanValue(messageString, statusExtractionRegex, false);
                });
            }
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", applianceId);
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

    private boolean isOn() {
        return on;
    }

    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {}", applianceId, (switchOn ? "on" : "off"));
        if(Environment.isHttpDisabled()) {
            return true;
        }
        mqttClient.publishMessage(topic, switchOn ? onPayload.getBytes() : offPayload.getBytes(), false, new MqttClient.OnErrorCallback() {
            @Override
            public void onError(Throwable t) {
                if(notificationHandler != null) {
                    notificationHandler.sendNotification(NotificationType.COMMUNICATION_ERROR);
                }
            }
        });
        publishControlMessage(now, switchOn);
        if(this.notificationHandler != null && switchOn != on) {
            this.notificationHandler.sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
        on = switchOn;
        return true;
    }

    private void publishControlMessage(LocalDateTime now, boolean on) {
        MqttMessage message = new ControlMessage(now, on);
        mqttClient.publish(mqttTopic, message, false);
    }

    public ContentProtocolHandler getContentContentProtocolHandler() {
        if(this.contentContentProtocolHandler == null) {
            if(ContentProtocolType.JSON.name().equals(this.contentProtocol)) {
                this.contentContentProtocolHandler = new JsonContentProtocolHandler();
            }
        }
        return this.contentContentProtocolHandler;
    }

}
