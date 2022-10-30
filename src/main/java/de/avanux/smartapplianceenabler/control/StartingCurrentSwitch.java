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
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandler;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandlerDependency;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Allows to prepare operation of an appliance while only little power is consumed.
 * Once the appliance starts the main work power consumption increases.
 * If it exceeds a threshold for a configured duration the wrapped control will be
 * switched off and energy demand will be posted. When switched on from external
 * the wrapped control of the appliance is switched on again to continue the
 * main work.
 * <p>
 * The StartingCurrentSwitch maintains a power state for the outside perspective which is
 * separate from the power state of the wrapped control.
 * The latter is only powered off after the starting current has been detected until the "on" command is received.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StartingCurrentSwitch extends WrappedControl implements TimeframeIntervalHandlerDependency {
    private transient Logger logger = LoggerFactory.getLogger(StartingCurrentSwitch.class);
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer startingCurrentDetectionDuration; // seconds
    @XmlAttribute
    private Integer finishedCurrentDetectionDuration; // seconds
    @XmlAttribute
    private Integer minRunningTime; // seconds
    private transient TimeframeIntervalHandler timeframeIntervalHandler;
    private transient boolean on;
    private transient boolean startingCurrentDetected;
    private transient LocalDateTime switchOnTime;

    @Override
    public void setTimeframeIntervalHandler(TimeframeIntervalHandler timeframeIntervalHandler) {
        this.timeframeIntervalHandler = timeframeIntervalHandler;
    }

    public Integer getPowerThreshold() {
        return powerThreshold != null ? powerThreshold : StartingCurrentSwitchDefaults.getPowerThreshold();
    }

    protected void setPowerThreshold(Integer powerThreshold) {
        this.powerThreshold = powerThreshold;
    }

    public Integer getStartingCurrentDetectionDuration() {
        return startingCurrentDetectionDuration != null ? startingCurrentDetectionDuration
                : StartingCurrentSwitchDefaults.getStartingCurrentDetectionDuration();
    }

    protected void setStartingCurrentDetectionDuration(Integer startingCurrentDetectionDuration) {
        this.startingCurrentDetectionDuration = startingCurrentDetectionDuration;
    }

    public Integer getFinishedCurrentDetectionDuration() {
        return finishedCurrentDetectionDuration != null ? finishedCurrentDetectionDuration
                : StartingCurrentSwitchDefaults.getFinishedCurrentDetectionDuration();
    }

    protected void setFinishedCurrentDetectionDuration(Integer finishedCurrentDetectionDuration) {
        this.finishedCurrentDetectionDuration = finishedCurrentDetectionDuration;
    }

    public Integer getMinRunningTime() {
        return minRunningTime != null ? minRunningTime : StartingCurrentSwitchDefaults.getMinRunningTime();
    }

    protected void setMinRunningTime(Integer minRunningTime) {
        this.minRunningTime = minRunningTime;
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.info("{}: Starting current switch: powerThreshold={}W startingCurrentDetectionDuration={}s " +
                        "finishedCurrentDetectionDuration={}s minRunningTime={}s",
                getApplianceId(), getPowerThreshold(), getStartingCurrentDetectionDuration(),
                getFinishedCurrentDetectionDuration(), getMinRunningTime());
        super.start(now, timer);
        setApplianceOn(now, true);
    }

    public void on(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting switch state to {}", getApplianceId(), (switchOn ? "on" : "off"));
        if (switchOn) {
            // don't switch off appliance - otherwise it cannot be operated
            setApplianceOn(now, true);
            switchOnTime = now;
            startingCurrentDetected = false;
        }
        on = switchOn;
        publishControlMessage(switchOn);
        if(getNotificationHandler() != null && switchOn != on) {
            getNotificationHandler().sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    public boolean isOn() {
        return on;
    }

    public void onMeterUpdate(LocalDateTime now, int averagePower, Double energy) {
        boolean applianceOn = isApplianceOn();
        logger.debug("{}: on={} applianceOn={} averagePower={}", getApplianceId(), on, applianceOn, averagePower);
        if (applianceOn) {
            detectExternalSwitchOn(now);
            if (on) {
                addPowerUpdate(now, averagePower, getFinishedCurrentDetectionDuration());
                detectFinishedCurrent(now);
            }
            else {
                addPowerUpdate(now, averagePower, getStartingCurrentDetectionDuration());
                detectStartingCurrent(now);
            }
        } else {
            logger.debug("{}: Appliance not switched on.", getApplianceId());
        }
    }

    public void detectStartingCurrent(LocalDateTime now) {
        boolean belowPowerThreshold = getPowerUpdates().values().stream().anyMatch(power -> power < getPowerThreshold());
        if (!on && !belowPowerThreshold) {
            logger.debug("{}: Starting current detected.", getApplianceId());
            startingCurrentDetected = true;
            setApplianceOn(now,false);
            switchOnTime = null;
            clearPowerUpdates();;
            getMqttClient().publish(MqttEventName.WrappedControlSwitchOnDetected, new MqttMessage(now));
        } else {
            logger.debug("{}: Starting current not detected.", getApplianceId());
        }
    }

    public void detectFinishedCurrent(LocalDateTime now) {
        boolean abovePowerThreshold = this.getPowerUpdates().values().stream().anyMatch(power -> power > getPowerThreshold());
        if (isMinRunningTimeExceeded(now) && !abovePowerThreshold) {
            logger.debug("{}: Finished current detected.", getApplianceId());
            clearPowerUpdates();
            getMqttClient().publish(MqttEventName.WrappedControlSwitchOffDetected, new MqttMessage(now));
        } else {
            logger.debug("{}: Finished current not detected.", getApplianceId());
        }
    }

    public void detectExternalSwitchOn(LocalDateTime now) {
        if(startingCurrentDetected && switchOnTime == null) { // && applianceOn ensured by caller
            logger.debug("{}: External switch-on detected.", getApplianceId());
            startingCurrentDetected = false;
            on = true;
            switchOnTime = now;
            Integer runtime = timeframeIntervalHandler.suggestRuntime();
            timeframeIntervalHandler.setRuntimeDemand(now, runtime, null, false);
            publishControlMessage(on);
        }
    }

    /**
     * Returns true, if the minimum running time has been exceeded.
     *
     * @return
     */
    protected boolean isMinRunningTimeExceeded(LocalDateTime now) {
        return switchOnTime != null && switchOnTime.plusSeconds(getMinRunningTime()).isBefore(now);
    }

    @Override
    protected MqttMessage buildControlMessage(boolean on) {
        return new StartingCurrentSwitchMessage(LocalDateTime.now(), on,
                getPowerThreshold(), getStartingCurrentDetectionDuration(), getFinishedCurrentDetectionDuration());
    }
}
