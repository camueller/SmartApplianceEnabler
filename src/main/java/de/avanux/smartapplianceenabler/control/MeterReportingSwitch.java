/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class MeterReportingSwitch implements Control, ApplianceIdConsumer, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(MeterReportingSwitch.class);
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer offDetectionDelay; // seconds
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient String applianceId;
    private transient NotificationHandler notificationHandler;
    private transient LocalDateTime lastOn;
    private transient Boolean onBefore;
    private transient MqttClient mqttClient;
    private transient MqttMessage mqttMessageSent;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MeterMessage meterMessage;
    private transient String mqttPublishTopic = Control.TOPIC;
    private transient boolean publishControlStateChangedEvent = true;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttPublishTopic = mqttTopic;
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

    public Integer getPowerThreshold() {
        return powerThreshold != null ? powerThreshold : MeterReportingSwitchDefaults.getPowerThreshold();
    }

    public int getOffDetectionDelay() {
        return offDetectionDelay != null ? offDetectionDelay : MeterReportingSwitchDefaults.getOffDetectionDelay();
    }

    public void setOffDetectionDelay(Integer offDetectionDelay) {
        this.offDetectionDelay = offDetectionDelay;
    }

    protected void setLastOn(LocalDateTime lastOn) {
        this.lastOn = lastOn;
    }

    protected void setOnBefore(Boolean onBefore) {
        this.onBefore = onBefore;
    }

    @Override
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.info("{}: Starting: powerThreshold={} offDetectionDelay={} notificationHandlerSet={}",
                applianceId, getPowerThreshold(), getOffDetectionDelay(), this.notificationHandler != null);
        if(mqttClient != null) {
            mqttClient.subscribe(Meter.TOPIC, true, (topic, message) -> {
                meterMessage = (MeterMessage) message;
            });
            this.mqttPublishTimerTask = new GuardedTimerTask(applianceId, "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        LocalDateTime now = LocalDateTime.now();
                        publishControlMessage(now, isOn(now));
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
        if(this.mqttPublishTimerTask != null) {
            this.mqttPublishTimerTask.cancel();
        }
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    @Override
    public boolean isControllable() {
        return false;
    }

    public boolean isOn(LocalDateTime now) {
        if(meterMessage != null) {
            int power = meterMessage.power;
            boolean powerReachesThreshold = power >= getPowerThreshold();
            if(powerReachesThreshold) {
                lastOn = now;
            }
            boolean onWithinCheckInterval = lastOn != null && lastOn.plusSeconds(getOffDetectionDelay()).isAfter(now);
            boolean on = powerReachesThreshold;
            if(onWithinCheckInterval) {
                on = true;
            }
            onControlStateChanged(now, on);
            onBefore = on;
            return on;
        }
        else {
            logger.warn("{}: No meter values received (yet).", applianceId);
        }
        return false;
    }

    protected void onControlStateChanged(LocalDateTime now, boolean on) {
        if(onBefore != null && on != onBefore) {
            logger.info("{}: Switch {} detected.", applianceId, (on ? "on" : "off"));
            publishControlMessage(now, on);
            if(this.notificationHandler != null) {
                this.notificationHandler.sendNotification(on ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
            }
        }
    }

    private void publishControlMessage(LocalDateTime now, boolean on) {
        MqttMessage message = new ControlMessage(now, on);
        if(!message.equals(mqttMessageSent)) {
            mqttClient.publish(mqttPublishTopic, message, false);
            mqttMessageSent = message;
        }
    }
}
