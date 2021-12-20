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

import com.pi4j.Pi4J;
import com.pi4j.common.Descriptor;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.raspberrypi.RaspberryPi;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
    private transient DigitalOutput output;
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
//        Context gpioContext = getGpioContext();
//        if (gpioContext != null) {
            try {
                if(output == null) {
                    logger.debug("{}: ****** Switch on", getApplianceId());
                    int pin = 17;
                    Context context = Pi4J.newAutoContext();
                    DigitalOutputConfig config = DigitalOutput.newConfigBuilder(context)
                            .id("BCM" + pin)
                            .name("Tester")
                            .address(pin)
                            .provider(RaspberryPi.DIGITAL_OUTPUT_PROVIDER_ID)
                            .build();
                    DigitalOutput output = context.create(config);
                    output.state(DigitalState.HIGH);
                    logger.debug("{}: ****** Switched on", getApplianceId());

                    logger.debug("{}: ****** Platforms", print(context.platforms().describe()));
                    logger.debug("{}: ****** Platform", print(context.platform().describe()));
                    logger.debug("{}: ****** Providers", print(context.providers().describe()));
                }
                logger.debug("{}: {} uses {} reverseStates={}", getApplianceId(), getClass().getSimpleName(),
                        getPin(), reverseStates);
            } catch (Exception e) {
                logger.error("{}: Error starting {} for {}", getApplianceId(), getClass().getSimpleName(), getPin(), e);
            }
//        } else {
//            logGpioAccessDisabled(logger);
//        }
    }

    private String print(Descriptor descriptor) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        descriptor.print(ps);
        return os.toString();
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
        if (output != null) {
            output.state(switchOn ? DigitalState.HIGH : DigitalState.LOW);
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
        if (output != null) {
            return adjustState(output.state()) == DigitalState.HIGH;
        }
        logGpioAccessDisabled(logger);
        return false;
    }

    private DigitalState adjustState(DigitalState pinState) {
        if (reverseStates) {
            if (pinState == DigitalState.HIGH) {
                return DigitalState.LOW;
            }
            return DigitalState.HIGH;
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