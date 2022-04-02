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
import de.avanux.smartapplianceenabler.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Timer;

/**
 * This switch only maintains its state and listeners.
 * It does not really switch anything.
 */
public class MockSwitch implements Control, ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(MockSwitch.class);
    private transient String applianceId;
    private transient MqttClient mqttClient;
    private transient String mqttTopic = Control.TOPIC;
    private transient boolean publishControlStateChangedEvent = true;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    @Override
    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
        this.publishControlStateChangedEvent = publishControlStateChangedEvent;
    }

    @Override
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        mqttClient.subscribe(mqttTopic, true, true, ControlMessage.class, (topic, message) -> {
            if(message instanceof ControlMessage) {
                ControlMessage controlMessage = (ControlMessage) message;
                this.on(controlMessage.getTime(), controlMessage.on);
            }
        });
    }

    @Override
    public void stop(LocalDateTime now) {
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {}", applianceId, (switchOn ? "on" : "off"));
        publishControlMessage(now, switchOn);
        return true;
    }

    private void publishControlMessage(LocalDateTime now, boolean on) {
        MqttMessage message = new ControlMessage(now, on);
        mqttClient.publish(mqttTopic, message, true);
    }
}
