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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.gpio.PinMode;
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
public class Switch extends GpioControllable implements Control, ApplianceIdConsumer, NotificationProvider {
    private transient Logger logger = LoggerFactory.getLogger(Switch.class);
    @XmlAttribute
    private boolean reverseStates;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient NotificationHandler notificationHandler;

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
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting {} for {}", getApplianceId(), getClass().getSimpleName(), getPin());
        if(isPigpioInterfaceAvailable()) {
            try {
                setMode(PinMode.OUTPUT);
                logger.debug("{}: {} uses {} reverseStates={}", getApplianceId(), getClass().getSimpleName(),
                        getPin(), reverseStates);
            } catch (Exception e) {
                logger.error("{}: Error starting {} for {}", getApplianceId(), getClass().getSimpleName(), getPin(), e);
            }
        } else {
            logGpioAccessDisabled(logger);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping {} for {}", getApplianceId(), getClass().getSimpleName(), getPin());
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {} {}", getApplianceId(), (switchOn ? "on" : "off"), getPin());
        var pigpioInterface = getPigpioInterface();
        if (pigpioInterface != null) {
            pigpioInterface.write(getPin(), switchOn);
        } else {
            logGpioAccessDisabled(logger);
        }
        if(this.notificationHandler != null) {
            this.notificationHandler.sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
        for (ControlStateChangedListener listener : new ArrayList<>(controlStateChangedListeners)) {
            listener.controlStateChanged(now, switchOn);
        }
        return true;
    }

    @Override
    public boolean isOn() {
        var pigpioInterface = getPigpioInterface();
        if (pigpioInterface != null) {
            return adjustState(pigpioInterface.read(getPin()) == 1);
        }
        logGpioAccessDisabled(logger);
        return false;
    }

    private boolean adjustState(boolean pinState) {
        if (reverseStates) {
            if (pinState) {
                return false;
            }
            return true;
        }
        return pinState;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void removeControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.remove(listener);
    }
}