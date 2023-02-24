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
package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.gpio.GpioControllable;
import de.avanux.smartapplianceenabler.gpio.PinEdge;
import de.avanux.smartapplianceenabler.gpio.PinMode;
import de.avanux.smartapplianceenabler.gpio.PinPullResistance;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.pigpioj.PigpioCallback;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;

public class S0ElectricityMeter extends GpioControllable implements Meter, NotificationProvider, PigpioCallback {

    private transient Logger logger = LoggerFactory.getLogger(S0ElectricityMeter.class);
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer minPulseDuration; // milliseconds
    @XmlAttribute
    private String pinPullResistance;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient LocalDateTime pulseTimestamp;
    private transient PulsePowerMeter pulsePowerMeter = new PulsePowerMeter();
    private transient PulseEnergyMeter pulseEnergyMeter = new PulseEnergyMeter();
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient NotificationHandler notificationHandler;
    private transient MqttClient mqttClient;
    private transient String mqttPublishTopic = Meter.TOPIC;

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

    public Integer getImpulsesPerKwh() {
        return impulsesPerKwh;
    }

    public Integer getMinPulseDuration() {
        return minPulseDuration != null ? minPulseDuration : S0ElectricityMeterDefaults.getMinPulseDuration();
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.pulsePowerMeter.setApplianceId(applianceId);
        this.pulseEnergyMeter.setApplianceId(applianceId);
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttPublishTopic = mqttTopic;
    }

    protected void setPulsePowerMeter(PulsePowerMeter pulsePowerMeter) {
        this.pulsePowerMeter = pulsePowerMeter;
    }

    protected void setPulseEnergyMeter(PulseEnergyMeter pulseEnergyMeter) {
        this.pulseEnergyMeter = pulseEnergyMeter;
    }

    @Override
    public void startEnergyMeter() {
        this.pulseEnergyMeter.startEnergyCounter();
    }

    @Override
    public void stopEnergyMeter() {
        this.pulseEnergyMeter.stopEnergyCounter();
    }

    @Override
    public void resetEnergyMeter() {
        this.pulseEnergyMeter.resetEnergyCounter();
    }

    @Override
    public void init() {
        logger.debug("{}: Initializing ...", getApplianceId());
        mqttClient = new MqttClient(getApplianceId(), getClass());
        pulsePowerMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulseEnergyMeter.setImpulsesPerKwh(impulsesPerKwh);
        logger.debug("{}: configured: GPIO={} impulsesPerKwh={} minPulseDuration={} pinPullResistance={}",
                getApplianceId(), getPin() != null ? getPin() : null, getImpulsesPerKwh(), getMinPulseDuration(),
                getPinPullResistance());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting {}", getApplianceId(), getClass().getSimpleName());
        if(isPigpioInterfaceAvailable()) {
            try {
                setMode(PinMode.INPUT);
                setPinPullResistance(getPinPullResistance());
                enableListener(this, PinEdge.EITHER);
            }
            catch(Exception e) {
                logger.error("{}: Error start metering using GPIO {}", getApplianceId(), getPin(), e);
                if(this.notificationHandler != null) {
                    this.notificationHandler.sendNotification(NotificationType.COMMUNICATION_ERROR);
                }
            }
        }
        else {
            logGpioAccessDisabled(logger);
        }
        if(mqttClient != null) {
            this.mqttPublishTimerTask = new GuardedTimerTask(getApplianceId(), "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        MqttMessage message = new MeterMessage(LocalDateTime.now(),
                                pulsePowerMeter != null ? pulsePowerMeter.getAveragePower() : 0,
                                pulseEnergyMeter != null ? pulseEnergyMeter.getEnergy() : 0
                        );
                        mqttClient.publish(mqttPublishTopic, message, false);
                    }
                    catch(Exception e) {
                        logger.error("{}: Error publishing MQTT message", getApplianceId(), e);
                    }
                }
            };
            timer.schedule(this.mqttPublishTimerTask, 0, this.mqttPublishTimerTask.getPeriod());
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping {}", getApplianceId(), getClass().getSimpleName());
        if(this.mqttPublishTimerTask != null) {
            this.mqttPublishTimerTask.cancel();
        }
        if(isPigpioInterfaceAvailable()) {
            try {
                disableListener();
            }
            catch(Exception e) {
                logger.error("{}: Error stop metering using GPIO {}", getApplianceId(), getPin(), e);
            }
        }
        else {
            logGpioAccessDisabled(logger);
        }
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    @Override
    public void callback(int pin, boolean value, long epochTime, long nanoTime) {
        LocalDateTime now = LocalDateTime.now();
        if((getPinPullResistance() == PinPullResistance.PULL_DOWN && value)
                || (getPinPullResistance() == PinPullResistance.PULL_UP && !value)) {
            pulseTimestamp = now;
        }
        else if (pulseTimestamp != null && Duration.between(pulseTimestamp, now).toMillis() > getMinPulseDuration()) {
            logger.debug("{}: S0 impulse detected on GPIO {}", getApplianceId(), getPin());
            pulsePowerMeter.addTimestamp(pulseTimestamp);
            pulseEnergyMeter.increasePulseCounter();
            int averagePower = pulsePowerMeter.getAveragePower();
            logger.debug("{}: power: {}W", getApplianceId(), averagePower);
            pulseTimestamp = null;
        }
    }

    protected PinPullResistance getPinPullResistance() {
        if(pinPullResistance != null) {
            if (pinPullResistance.equals("PULL_DOWN")) {
                return PinPullResistance.PULL_DOWN;
            }
            if (pinPullResistance.equals("PULL_UP")) {
                return PinPullResistance.PULL_UP;
            }
        }
        return PinPullResistance.OFF;
    }
}
