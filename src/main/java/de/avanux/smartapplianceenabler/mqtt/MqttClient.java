/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.mqtt;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttClient {

    private transient Logger logger = LoggerFactory.getLogger(MqttClient.class);
    private String applianceId;
    private String prefix = "sae";
    private IMqttClient client;
    private Genson genson;


    public MqttClient(String applianceId, Class clazz) {
        this.applianceId = applianceId;

        this.genson = new GensonBuilder()
                .useFields(true, VisibilityFilter.PRIVATE)
                .useMethods(false)
                .useClassMetadata(false)
                .useRuntimeType(true)
                .create();

        String clientId = applianceId + "-" + clazz.getSimpleName();
        try {
            client = new org.eclipse.paho.client.mqttv3.MqttClient("tcp://localhost:1883", clientId);
            logger.debug("{}: Created MQTT client {}", applianceId, clientId);
        }
        catch (Exception e) {
            logger.error("{}: Error creating MQTT client {}", applianceId, clientId, e);
        }
    }

    private MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(1000);
        return options;
    }

    public String getFullTopic(String topic) {
        return prefix + "/" + applianceId + "/" + topic;
    }

    public boolean connect() {
        try {
            if(! client.isConnected()) {
                client.connect(getOptions());
            }
            boolean connectResult = client.isConnected();
            if(! connectResult) {
                logger.error("{}: Connection to MQTT server failed", applianceId);
            }
            return connectResult;
        }
        catch (Exception e) {
            logger.error("{}: Error connecting to MQTT server", applianceId, e);
        }
        return false;
    }

    public synchronized void send(String topic, MqttMessage message) {
        try {
            if(connect()) {
                String serializedMessage = this.genson.serialize(message);
                org.eclipse.paho.client.mqttv3.MqttMessage pahoMessage
                        = new org.eclipse.paho.client.mqttv3.MqttMessage(serializedMessage.getBytes());
                pahoMessage.setQos(0);
//                pahoMessage.setRetained(true);
                String fullTopic = getFullTopic(topic);
                logger.debug("{}: Publish MQTT message: topic={} payload={}", applianceId, fullTopic, serializedMessage);
                client.publish(fullTopic, pahoMessage);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error sending MQTT message", applianceId, e);
        }
    }

    public synchronized void subscribe(String topic, Class toType, MqttMessageHandler messageHandler) {
        String fullTopic = getFullTopic(topic);
        try {
            if(connect()) {
                client.subscribe(fullTopic, (receivedTopic, receivedMessage) -> {
                    try {
                        logger.debug("{}: MQTT message received: topic={} payload={}", applianceId, receivedTopic, receivedMessage);
                        MqttMessage message = (MqttMessage) this.genson.deserialize(receivedMessage.getPayload(), toType);
                        messageHandler.messageArrived(receivedTopic, message);
                    }
                    catch(Exception e) {
                        logger.error("{}: Error receiving MQTT message", applianceId, e);
                    }
                });
                logger.debug("{}: MQTT messages subscribed: topic={}", applianceId, fullTopic);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error subscribing to MQTT messages topic={}", applianceId, fullTopic, e);
        }
    }
}
