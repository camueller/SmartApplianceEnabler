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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.gpio.GpioControllable;
import de.avanux.smartapplianceenabler.gpio.PinMode;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.mqtt.VariablePowerConsumerMessage;
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
public class PwmSwitch extends GpioControllable implements VariablePowerConsumer, Validateable, ApplianceIdConsumer, NotificationProvider {
    private transient Logger logger = LoggerFactory.getLogger(PwmSwitch.class);
    @XmlAttribute
    private String id;
    @XmlAttribute
    private int pwmFrequency;
    @XmlAttribute
    private double minDutyCycle;
    @XmlAttribute
    private double maxDutyCycle;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient int range;
    private transient Integer minPowerConsumption;
    private transient Integer maxPowerConsumption;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;

    public PwmSwitch() {
    }

    public PwmSwitch(String id, int pwmFrequency, double minDutyCycle, double maxDutyCycle, int range) {
        this.id = id;
        this.pwmFrequency = pwmFrequency;
        this.minDutyCycle = minDutyCycle;
        this.maxDutyCycle = maxDutyCycle;
        this.range = range;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setMinPower(Integer minPower) {
        this.minPowerConsumption = minPower;
    }

    public Integer getMinPower() {
        return this.minPowerConsumption;
    }

    public void setMaxPower(int maxPower) {
        this.maxPowerConsumption = maxPower;
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
        logger.debug("{}: configured: minPowerConsumption={} maxPowerConsumption={}", getApplianceId(), minPowerConsumption, maxPowerConsumption);
    }

    @Override
    public void init() {
        logger.debug("{}: Initializing ...", getApplianceId());
        mqttClient = new MqttClient(getApplianceId(), getClass());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting for GPIO {}", getApplianceId(), getPin());
        if(isPigpioInterfaceAvailable()) {
            try {
                setMode(PinMode.OUTPUT);
                getPigpioInterface().setPWMFrequency(getPin(), pwmFrequency);
                range = getPigpioInterface().getPWMRealRange(getPin());
                getPigpioInterface().setPWMRange(getPin(), range);
                on(now, false, null);
                logger.debug("{}: using GPIO {} with pwmFrequency={} minDutyCycle={} maxDutyCycle={} range={}",
                        getApplianceId(), getPin(), pwmFrequency, minDutyCycle, maxDutyCycle, range);
            } catch (Exception e) {
                logger.error("{}: Error starting {} for GPIO {}", getApplianceId(), getClass().getSimpleName(), getPin(), e);
            }
            if(mqttClient != null) {
                this.mqttPublishTimerTask = new GuardedTimerTask(getApplianceId(), "MqttPublish-" + getClass().getSimpleName(),
                        MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                    @Override
                    public void runTask() {
                        try {
                            publishControlMessage(LocalDateTime.now(), getPower());
                        }
                        catch(Exception e) {
                            logger.error("{}: Error publishing MQTT message", getApplianceId(), e);
                        }
                    }
                };
                timer.schedule(this.mqttPublishTimerTask, 0, this.mqttPublishTimerTask.getPeriod());
                mqttClient.subscribe(Control.TOPIC, true, true, (topic, message) -> {
                    if(message instanceof VariablePowerConsumerMessage) {
                        VariablePowerConsumerMessage controlMessage = (VariablePowerConsumerMessage) message;
                        if(controlMessage.on) {
                            this.on(controlMessage.getTime(), true, controlMessage.power);
                        }
                        else {
                            this.on(controlMessage.getTime(), false, null);
                        }
                    }
                });
            }
        } else {
            logGpioAccessDisabled(logger);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", getApplianceId());
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

    public void setPower(LocalDateTime now, int power) {
        logger.info("{}: Setting power to {}W", getApplianceId(), power);
        setDutyCycle(now, calculateDutyCycle(power));
        publishControlMessage(now, power);
        if(this.notificationHandler != null) {
            this.notificationHandler.sendNotification(isOn() ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
    }

    private int getPower() {
        return Double.valueOf(((getDutyCycle() / (double) range - minDutyCycle / 100) / ((this.maxDutyCycle - this.minDutyCycle) / 100)) * maxPowerConsumption).intValue();
    }

    protected int calculateDutyCycle(int power) {
        return Double.valueOf((((double) power / (double) this.maxPowerConsumption) * (this.maxDutyCycle - this.minDutyCycle) / 100.0) * range).intValue();
    }

    private void setDutyCycle(LocalDateTime now, int dutyCycle) {
        logger.info("{}: Setting GPIO {} duty cycle to {}", getApplianceId(), getPin(), dutyCycle);
        var pigpioInterface = getPigpioInterface();
        if (pigpioInterface != null) {
            pigpioInterface.setPWMDutyCycle(getPin(), dutyCycle);
        } else {
            logGpioAccessDisabled(logger);
        }
    }

    private int getDutyCycle() {
        if(isPigpioInterfaceAvailable()) {
            return getPigpioInterface().getPWMDutyCycle(getPin());
        }
        return 0;
    }

    public void on(LocalDateTime now, boolean switchOn, Integer power) {
        if(!switchOn || power == 0) {
            setDutyCycle(now, 0);
        } else {
            setPower(now, power != null ? power : this.minPowerConsumption);
        }
    }

    public boolean isOn() {
        return getPower() > 0;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
        // only need for switches embeddable in other switches (i.e. StartingCurrentSwitch)
    }

    @Override
    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
        // only need for switches embeddable in other switches (i.e. StartingCurrentSwitch)
    }

    private void publishControlMessage(LocalDateTime now, Integer power) {
        MqttMessage message = new VariablePowerConsumerMessage(now, isOn(), power, null);
        mqttClient.publish(Control.TOPIC, message, false);
    }
}
