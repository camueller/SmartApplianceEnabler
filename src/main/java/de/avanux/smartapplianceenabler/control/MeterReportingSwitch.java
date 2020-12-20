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
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class MeterReportingSwitch implements Control, ApplianceIdConsumer, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(MeterReportingSwitch.class);
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient String applianceId;
    private transient NotificationHandler notificationHandler;
    private transient Meter meter;
    private transient Boolean onBefore;

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

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    @Override
    public void init() {
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
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
    }

    @Override
    public void removeControlStateChangedListener(ControlStateChangedListener listener) {
    }

    @Override
    public boolean isOn() {
        if(this.meter != null) {
            // FIXME konfigurierbar machen
            int power = this.meter.getAveragePower();
            boolean on = power > 5;
            logger.debug("{}: power={} on={} onBefore={} notificationHandlerSet={}", applianceId, power, on, onBefore,
                    this.notificationHandler != null);
            if(onBefore != null && this.notificationHandler != null && on != onBefore) {
                logger.info("{}: Switch {} detected.", applianceId, (on ? "on" : "off"));
                this.notificationHandler.sendNotification(on ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
            }
            onBefore = on;
            return on;
        }
        else {
            logger.error("{}: Meter not set.", applianceId);
        }
        return false;
    }
}
