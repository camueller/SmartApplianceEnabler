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
import de.avanux.smartapplianceenabler.meter.PowerUpdateListener;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.schedule.DayTimeframeCondition;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandler;
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
public class StartingCurrentSwitch implements Control, ApplianceIdConsumer, PowerUpdateListener, NotificationProvider {
    private transient Logger logger = LoggerFactory.getLogger(StartingCurrentSwitch.class);
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer startingCurrentDetectionDuration; // seconds
    @XmlAttribute
    private Integer finishedCurrentDetectionDuration; // seconds
    @XmlAttribute
    private Integer minRunningTime; // seconds
    @XmlElements({
            @XmlElement(name = "MeterReportingSwitch", type = MeterReportingSwitch.class),
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class),
            @XmlElement(name = "MockSwitch", type = MockSwitch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class)
    })
    private Control control;
    @XmlElement(name = "ForceSchedule")
    private DayTimeframeCondition dayTimeframeCondition;
    private transient String applianceId;
    private transient TimeframeIntervalHandler timeframeIntervalHandler;
    private transient Map<LocalDateTime, Integer> powerUpdates = new TreeMap<>();
    private transient boolean on;
    private transient boolean applianceOn;
    private transient boolean startingCurrentDetected;
    private transient LocalDateTime switchOnTime;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;
    final static public String WRAPPED_CONTROL_TOPIC = "Wrapped" + Control.TOPIC;


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public Control getControl() {
        return control;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
    }

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(control instanceof NotificationProvider && notificationHandler != null) {
            this.notificationHandler.setRequestedNotifications(((NotificationProvider) control).getNotifications());
        }
    }

    @Override
    public Notifications getNotifications() {
        return control instanceof NotificationProvider ? ((NotificationProvider) control).getNotifications() : null;
    }

    public void setTimeframeIntervalHandler(TimeframeIntervalHandler timeframeIntervalHandler) {
        this.timeframeIntervalHandler = timeframeIntervalHandler;
    }

    public DayTimeframeCondition getDayTimeframeCondition() {
        return dayTimeframeCondition;
    }

    public void setDayTimeframeCondition(DayTimeframeCondition dayTimeframeCondition) {
        this.dayTimeframeCondition = dayTimeframeCondition;
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
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
        if(this.control != null) {
            this.control.init();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.info("{}: Starting current switch: powerThreshold={}W startingCurrentDetectionDuration={}s " +
                        "finishedCurrentDetectionDuration={}s minRunningTime={}s",
                applianceId, getPowerThreshold(), getStartingCurrentDetectionDuration(),
                getFinishedCurrentDetectionDuration(), getMinRunningTime());
        if(this.control != null) {
            this.control.start(now, timer);
        }
        applianceOn(now, true);
        if(mqttClient != null) {
            mqttClient.subscribe(Meter.TOPIC, true, MeterMessage.class, (topic, message) -> {
                if(message instanceof MeterMessage) {
                    onPowerUpdate(message.getTime(), ((MeterMessage) message).power);
                }
            });
            mqttClient.subscribe(WRAPPED_CONTROL_TOPIC, true, ControlMessage.class, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    applianceOn = ((ControlMessage) message).on;
                }
            });
            mqttClient.subscribe(Control.TOPIC, true, true, ControlMessage.class, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    ControlMessage controlMessage = (ControlMessage) message;
                    this.on(controlMessage.getTime(), controlMessage.on);
                }
            });
        }
        this.mqttPublishTimerTask = new GuardedTimerTask(applianceId, "MqttPublish-" + getClass().getSimpleName(),
                MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
            @Override
            public void runTask() {
                try {
                    publishControlMessage(isOn());
                }
                catch(Exception e) {
                    logger.error("{}: Error publishing MQTT message", applianceId, e);
                }
            }
        };
        timer.schedule(this.mqttPublishTimerTask, 0, this.mqttPublishTimerTask.getPeriod());
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", this.applianceId);
        if(this.control != null) {
            this.control.stop(now);
        }
    }

    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting switch state to {}", applianceId, (switchOn ? "on" : "off"));
        if (switchOn) {
            // don't switch off appliance - otherwise it cannot be operated
            applianceOn(now, true);
            switchOnTime = now;
            startingCurrentDetected = false;
        }
        on = switchOn;
        publishControlStateChangedEvent(switchOn);
        publishControlMessage(switchOn);
        if(this.notificationHandler != null && switchOn != on) {
            this.notificationHandler.sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
        return on;
    }

    private void applianceOn(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting wrapped appliance switch to {}", applianceId, (switchOn ? "on" : "off"));
        mqttClient.publish(WRAPPED_CONTROL_TOPIC, new ControlMessage(now, switchOn), true, true);
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    public boolean isOn() {
        return on;
    }

    private void publishControlMessage(boolean on) {
        MqttMessage message = new StartingCurrentSwitchMessage(LocalDateTime.now(), on,
                getPowerThreshold(), getStartingCurrentDetectionDuration(), getFinishedCurrentDetectionDuration());
        mqttClient.publish(Control.TOPIC, message, true);
    }

    private void publishControlStateChangedEvent(boolean on) {
        ControlStateChangedEvent event = new ControlStateChangedEvent(LocalDateTime.now(), on);
        mqttClient.publish(MqttEventName.ControlStateChanged, event);
    }

    @Override
    public void onPowerUpdate(LocalDateTime now, int averagePower) {
        boolean applianceOn = this.applianceOn;
        logger.debug("{}: on={} applianceOn={} averagePower={}", applianceId, on, applianceOn, averagePower);
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
            logger.debug("{}: Appliance not switched on.", applianceId);
        }
    }

    public void addPowerUpdate(LocalDateTime now, int averagePower, int maxAgeSeconds) {
        this.powerUpdates.put(now, averagePower);
        Set<LocalDateTime> expiredPowerUpdates = new HashSet<>();
        this.powerUpdates.keySet().forEach(timestamp -> {
            if(now.minusSeconds(maxAgeSeconds).isAfter(timestamp)) {
                expiredPowerUpdates.add(timestamp);
            }
        });
        for(LocalDateTime expiredPowerUpdate: expiredPowerUpdates) {
            this.powerUpdates.remove(expiredPowerUpdate);
        }
        Integer min = this.powerUpdates.values().stream().mapToInt(v -> v).min().orElseThrow(NoSuchElementException::new);
        Integer max = this.powerUpdates.values().stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new);
        logger.debug("{}: power value cache: min={}W max={}W values={} maxAge={}s",
                applianceId, min, max, this.powerUpdates.size(), maxAgeSeconds);
    }

    public void detectStartingCurrent(LocalDateTime now) {
        boolean belowPowerThreshold = this.powerUpdates.values().stream().anyMatch(power -> power < getPowerThreshold());
        if (!on && !belowPowerThreshold) {
            logger.debug("{}: Starting current detected.", applianceId);
            startingCurrentDetected = true;
            applianceOn(now,false);
            switchOnTime = null;
            this.powerUpdates.clear();
            mqttClient.publish(MqttEventName.StartingCurrentDetected, new MqttMessage(now));
        } else {
            logger.debug("{}: Starting current not detected.", applianceId);
        }
    }

    public void detectFinishedCurrent(LocalDateTime now) {
        boolean abovePowerThreshold = this.powerUpdates.values().stream().anyMatch(power -> power > getPowerThreshold());
        if (isMinRunningTimeExceeded(now) && !abovePowerThreshold) {
            logger.debug("{}: Finished current detected.", applianceId);
            this.powerUpdates.clear();
            mqttClient.publish(MqttEventName.FinishedCurrentDetected, new MqttMessage(now));
            on(now, false);
        } else {
            logger.debug("{}: Finished current not detected.", applianceId);
        }
    }

    public void detectExternalSwitchOn(LocalDateTime now) {
        if(startingCurrentDetected && switchOnTime == null) { // && applianceOn ensured by caller
            logger.debug("{}: External switch-on detected.", applianceId);
            startingCurrentDetected = false;
            on = true;
            switchOnTime = now;
            Integer runtime = timeframeIntervalHandler.suggestRuntime();
            timeframeIntervalHandler.setRuntimeDemand(now, runtime, null, false);
            publishControlStateChangedEvent(on);
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
}
