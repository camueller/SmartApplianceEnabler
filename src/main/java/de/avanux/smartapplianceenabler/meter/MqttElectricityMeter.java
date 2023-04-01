/*
 * Copyright (C) 2023 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import de.avanux.smartapplianceenabler.protocol.JsonContentProtocolHandler;
import de.avanux.smartapplianceenabler.util.ValueExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Timer;

public class MqttElectricityMeter implements Meter, ApplianceLifeCycle, Validateable, PollPowerExecutor, PollEnergyExecutor, MeterUpdateListener, ApplianceIdConsumer, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(MqttElectricityMeter.class);

    @XmlAttribute
    private String topic;

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String path;
    @XmlAttribute
    private Double factorToValue;

    @XmlAttribute
    private String timePath;

    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient String applianceId;
    private transient MqttClient mqttClient;
    private transient String mqttPublishTopic = Meter.TOPIC;
    private transient ContentProtocolHandler contentContentProtocolHandler;
    private transient PollPowerMeter pollPowerMeter;
    private transient PollEnergyMeter pollEnergyMeter;
    private transient ValueExtractor valueExtractor = new ValueExtractor();
    private transient Double value;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.valueExtractor.setApplianceId(applianceId);
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttPublishTopic = mqttTopic;
    }

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        if(notificationHandler != null) {
            notificationHandler.setRequestedNotifications(notifications);
        }
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", applianceId);
        logger.debug("{}: configured: topic={}", applianceId, topic);
        logger.debug("{}: {} configured: path={} factorToValue={} timePath={}",
                applianceId,
                name,
                path,
                factorToValue,
                timePath);

        if(topic == null) {
            logger.error("{}: Missing 'topic' property", applianceId);
            throw new ConfigurationException();
        }
        if(! (MeterValueName.Power.name().equals(name) || MeterValueName.Energy.name().equals(name))) {
            logger.error("{}: Configuration name must be either {} or {}",
                    applianceId, MeterValueName.Power.name(), MeterValueName.Energy.name());
            throw new ConfigurationException();
        }
        if(path == null) {
            logger.error("{}: Missing 'path' property", applianceId);
            throw new ConfigurationException();
        }
    }

    @Override
    public void init() {
        logger.debug("{}: Initializing ...", this.applianceId);
        mqttClient = new MqttClient(applianceId, getClass());
        if(MeterValueName.Power.name().equals(name)) {
            pollPowerMeter = new PollPowerMeter();
            pollPowerMeter.setApplianceId(applianceId);
            pollPowerMeter.addMeterUpateListener(this);
        }
        if(MeterValueName.Energy.name().equals(name)) {
            pollEnergyMeter = new PollEnergyMeter();
            pollEnergyMeter.setApplianceId(applianceId);
            pollEnergyMeter.addMeterUpateListener(this);
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting ...", applianceId);
        if(pollPowerMeter != null) {
            pollPowerMeter.start(null, null, this);
        }
        if(pollEnergyMeter != null) {
            pollEnergyMeter.start(null, this);
        }
        if(mqttClient != null) {
            mqttClient.subscribe(topic, (topic, message) -> {
                var messageString = new String(message, StandardCharsets.UTF_8);
                logger.debug("{}: MQTT message received: {}", applianceId, messageString);

                var contentHandler = getContentContentProtocolHandler();
                contentHandler.parse(messageString);

                var timeString = contentHandler.readValue(timePath);
                var time = timeString != null ? LocalDateTime.parse(timeString) : now;

                var inputValue = contentHandler.readValue(path);
                value = valueExtractor.getDoubleValue(inputValue, null, factorToValue, 0.0);
                if(MeterValueName.Power.name().equals(name)) {
                    pollPowerMeter.pollPower(time);
                }
                if(MeterValueName.Energy.name().equals(name)) {
                    pollEnergyMeter.pollEnergy(time);
                }
            });
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", applianceId);
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    @Override
    public void startEnergyMeter() {
        logger.debug("{}: Start energy meter ...", applianceId);
        Double energy = 0.0;
        if(pollPowerMeter != null) {
            energy = pollPowerMeter.startEnergyCounter();
        }
        if(pollEnergyMeter != null) {
            energy = pollEnergyMeter.startEnergyCounter();
        }
        logger.debug("{}: Current energy meter value: {}kWh", applianceId, energy);
    }

    @Override
    public void stopEnergyMeter() {
        logger.debug("{}: Stop energy meter ...", applianceId);
        Double energy = null;
        if(pollPowerMeter != null) {
            energy = pollPowerMeter.stopEnergyCounter();
        }
        if(pollEnergyMeter != null) {
            energy = pollEnergyMeter.stopEnergyCounter();
        }
        logger.debug("{}: Current energy meter value: {}kWh", applianceId, energy != null ? energy : 0.0);
    }

    @Override
    public void resetEnergyMeter() {
        logger.debug("{}: Reset energy meter ...", applianceId);
        if(pollPowerMeter != null) {
            pollPowerMeter.reset();
        }
        if(pollEnergyMeter != null) {
            pollEnergyMeter.reset();
        }
    }

    public ContentProtocolHandler getContentContentProtocolHandler() {
        if(this.contentContentProtocolHandler == null) {
            this.contentContentProtocolHandler = new JsonContentProtocolHandler();
        }
        return this.contentContentProtocolHandler;
    }

    @Override
    public Double pollPower() {
        return value;
    }

    @Override
    public Double pollEnergy(LocalDateTime now) {
        return value;
    }

    @Override
    public void onMeterUpdate(LocalDateTime now, int averagePower, Double energy) {
        MqttMessage message = new MeterMessage(now, averagePower, energy != null ? energy : 0.0);
        mqttClient.publish(mqttPublishTopic, message, false);
    }
}
