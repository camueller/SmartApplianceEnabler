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

import de.avanux.smartapplianceenabler.mqtt.MqttEventName;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.mqtt.SwitchOptionMessage;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandler;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandlerDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.time.LocalDateTime;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class SwitchOption extends WrappedControl implements TimeframeIntervalHandlerDependency {
    private transient Logger logger = LoggerFactory.getLogger(SwitchOption.class);
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer switchOnDetectionDuration; // seconds
    @XmlAttribute
    private Integer switchOffDetectionDuration; // seconds
    private transient LocalDateTime switchOnTime;
    private transient boolean detectingSwitchOn;
    private transient boolean switchOnDetected;
    private transient TimeframeIntervalHandler timeframeIntervalHandler;

    @Override
    public void setTimeframeIntervalHandler(TimeframeIntervalHandler timeframeIntervalHandler) {
        this.timeframeIntervalHandler = timeframeIntervalHandler;
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.info("{}: Starting switch option: powerThreshold={}W switchOnDetectionDuration={}s " +
                        "switchOffDetectionDuration={}s",
                getApplianceId(), powerThreshold, switchOnDetectionDuration, switchOffDetectionDuration);
        super.start(now, timer);
    }

    @Override
    public void onMeterUpdate(LocalDateTime now, int averagePower, Double energy) {
        boolean on = isOn();
        logger.debug("{}: on={} averagePower={}", getApplianceId(), on, averagePower);
        if(on) {
            if (!switchOnDetected && detectingSwitchOn) {
                addPowerUpdate(now, averagePower, switchOnDetectionDuration);
                detectSwitchOn(now);
            }
            else if(switchOnDetected) {
                addPowerUpdate(now, averagePower, switchOffDetectionDuration);
                detectSwitchOff(now);
            }
        }
    }

    public void detectSwitchOn(LocalDateTime now) {
        var switchOnDetectionDurationExpired = now.minusSeconds(switchOnDetectionDuration).isAfter(switchOnTime);
        if(switchOnDetectionDurationExpired) {
            logger.debug("{}: No switch on detected within switchOnDetectionDuration={}s. Removing timeframe interval.", getApplianceId(), switchOnDetectionDuration);
            timeframeIntervalHandler.removeActiveTimeframeInterval(now);
            on(now, false);
        }
        else {
            boolean abovePowerThreshold = getPowerUpdates().values().stream().anyMatch(power -> power > powerThreshold);
            if (abovePowerThreshold) {
                logger.debug("{}: Switch on detected.", getApplianceId());
                switchOnDetected = true;
                detectingSwitchOn = false;
                clearPowerUpdates();
                getMqttClient().publish(MqttEventName.WrappedControlSwitchOnDetected, new MqttMessage(now));
            } else {
                logger.debug("{}: Switch on not detected.", getApplianceId());
            }
        }
    }

    public void detectSwitchOff(LocalDateTime now) {
        boolean abovePowerThreshold = getPowerUpdates().values().stream().anyMatch(power -> power > powerThreshold);
        if (!abovePowerThreshold) {
            logger.debug("{}: Switch off detected.", getApplianceId());
            clearPowerUpdates();
            getMqttClient().publish(MqttEventName.WrappedControlSwitchOffDetected, new MqttMessage(now));
            timeframeIntervalHandler.removeActiveTimeframeInterval(now);
        } else {
            logger.debug("{}: Switch off not detected.", getApplianceId());
        }
    }

    @Override
    protected void on(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting switch state to {}", getApplianceId(), (switchOn ? "on" : "off"));
        setApplianceOn(now, switchOn);
        if(switchOn) {
            switchOnTime = now;
            detectingSwitchOn = true;
        }
        else {
            switchOnTime = null;
            detectingSwitchOn = false;
        }
        publishControlMessage(switchOn);
        if(getNotificationHandler() != null) {
            getNotificationHandler().sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
    }

    @Override
    public boolean isOn() {
        return isApplianceOn();
    }

    @Override
    protected MqttMessage buildControlMessage(boolean on) {
        return new SwitchOptionMessage(LocalDateTime.now(), on, powerThreshold, switchOnDetectionDuration, switchOffDetectionDuration);
    }
}
