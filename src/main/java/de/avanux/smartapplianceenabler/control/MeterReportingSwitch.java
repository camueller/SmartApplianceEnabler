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
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient MqttClient mqttClient;
    private transient MeterMessage meterMessage;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
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
            mqttClient.subscribe(Meter.TOPIC, true, MeterMessage.class, (topic, message) -> {
                meterMessage = (MeterMessage) message;
            });
        }
    }

    @Override
    public void stop(LocalDateTime now) {
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        return false;
    }

    @Override
    public boolean isControllable() {
        return false;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        controlStateChangedListeners.add(listener);
    }

    @Override
    public void removeControlStateChangedListener(ControlStateChangedListener listener) {
        controlStateChangedListeners.remove(listener);
    }

    @Override
    public boolean isOn() {
        return this.isOn(LocalDateTime.now());
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
            logger.warn("{}: Meter not set.", applianceId);
        }
        return false;
    }

    protected void onControlStateChanged(LocalDateTime now, boolean on) {
        if(onBefore != null && on != onBefore) {
            logger.info("{}: Switch {} detected.", applianceId, (on ? "on" : "off"));
            controlStateChangedListeners.forEach(listener -> listener.controlStateChanged(now, on));
            if(this.notificationHandler != null) {
                this.notificationHandler.sendNotification(on ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
            }
        }
    }
}
