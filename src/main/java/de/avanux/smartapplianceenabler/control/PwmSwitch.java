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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
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
    private int pwmChip = 0;
    @XmlAttribute
    private Double minDutyCycle;
    @XmlAttribute
    private Double maxDutyCycle;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient Integer minPowerConsumption;
    private transient Integer maxPowerConsumption;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;

    public PwmSwitch() {
    }

    public PwmSwitch(String id, int pwmFrequency, Double minDutyCycle, Double maxDutyCycle) {
        this.id = id;
        this.pwmFrequency = pwmFrequency;
        this.minDutyCycle = minDutyCycle;
        this.maxDutyCycle = maxDutyCycle;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
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

    private double resolveMinDutyCycle() {
        return this.maxDutyCycle != null ? this.minDutyCycle : 0;
    }

    private double resolveMaxDutyCycle() {
        return this.maxDutyCycle != null ? this.maxDutyCycle : 100;
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
        if(mqttClient == null) {
            mqttClient = new MqttClient(getApplianceId(), getClass());
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting for GPIO {}", getApplianceId(), getPin());
        if(isGpioAvailable()) {
            try {
                initializePwm(pwmChip, pwmFrequency);
                on(now, false, null);
                logger.debug("{}: using GPIO {} with pwmFrequency={} minDutyCycle={}({} ms) maxDutyCycle={}({} ms)",
                        getApplianceId(), getPin(), pwmFrequency, resolveMinDutyCycle(),
                        toImpulsWidthMilliseconds(resolveMinDutyCycle()), resolveMaxDutyCycle(), toImpulsWidthMilliseconds(resolveMaxDutyCycle()));
            } catch (Exception e) {
                logger.error("{}: Error starting {} for GPIO {}", getApplianceId(), getClass().getSimpleName(), getPin(), e);
            }
            if(mqttClient != null) {
                this.mqttPublishTimerTask = new GuardedTimerTask(getApplianceId(), "MqttPublish-" + getClass().getSimpleName(),
                        MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                    @Override
                    public void runTask(LocalDateTime now) {
                        try {
                            publishControlMessage(now, getPower());
                        }
                        catch(Exception e) {
                            logger.error("{}: Error publishing MQTT message", getApplianceId(), e);
                        }
                    }
                };
                // initial publishControlMessage() is triggered by initial "switch off" in on(false)
                timer.schedule(this.mqttPublishTimerTask, this.mqttPublishTimerTask.getPeriod(), this.mqttPublishTimerTask.getPeriod());
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
        setDutyCyclePercent(now, calculateDutyCyclePercent(power));
        publishControlMessage(now, power);
        if(this.notificationHandler != null) {
            this.notificationHandler.sendNotification(isOn() ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
        }
    }

    private int getPower() {
        double dutyCycle = getDutyCyclePercent();
        double range = resolveMaxDutyCycle() - resolveMinDutyCycle();
        if (range == 0) return 0;
        return (int) (dutyCycle * maxPowerConsumption / range);
    }

    protected double calculateDutyCyclePercent(int power) {
        var minPowerConsumptionOrZero = this.minPowerConsumption != null ? this.minPowerConsumption : 0;
        var powerRatio = Integer.valueOf(power - minPowerConsumptionOrZero).doubleValue() / Integer.valueOf(this.maxPowerConsumption - minPowerConsumptionOrZero).doubleValue();
        var dutyCyclePercent = (resolveMaxDutyCycle() - resolveMinDutyCycle()) * powerRatio + resolveMinDutyCycle();
        logger.debug("{}: Calculated duty cycle={}% from power ratio={}", getApplianceId(), dutyCyclePercent, powerRatio);
        return dutyCyclePercent;
    }

    private void setDutyCyclePercent(LocalDateTime now, double dutyCyclePercent) {
        logger.info("{}: Setting GPIO {} duty cycle to {}% ({} ms)", getApplianceId(), getPin(), dutyCyclePercent,
                toImpulsWidthMilliseconds(dutyCyclePercent));
        var pwm = getPwm();
        if (pwm != null) {
            try {
                if (dutyCyclePercent <= 0) {
                    pwm.off();
                } else {
                    pwm.on((int) dutyCyclePercent);
                }
            } catch (Exception e) {
                logger.error("{}: Error setting PWM duty cycle", getApplianceId(), e);
            }
        } else {
            logGpioAccessDisabled(logger);
        }
    }

    private double getDutyCyclePercent() {
        var pwm = getPwm();
        if (pwm != null) {
            return pwm.dutyCycle();
        }
        return 0;
    }

    private int toImpulsWidthMilliseconds(double dutyCycle) {
        return Double.valueOf(1.0d / this.pwmFrequency * 1000 * (dutyCycle / 100)).intValue();
    }

    public void on(LocalDateTime now, boolean switchOn, Integer power) {
        if(!switchOn || power == 0) {
            setDutyCyclePercent(now, 0);
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
