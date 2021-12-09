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
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttClient {

    private transient Logger logger = LoggerFactory.getLogger(MqttClient.class);
    private String loggerId;
    private String applianceId;
    private static String topicPrefix = "sae";
    public final static int MQTT_PUBLISH_PERIOD = 20;
    private IMqttClient client;
    private Genson genson;


    public MqttClient(String applianceId, Class clazz) {
        this.applianceId = applianceId;
        loggerId = applianceId.length() > 0 ? applianceId + "-MQTT-" + clazz.getSimpleName() : clazz.getSimpleName();

        this.genson = new GensonBuilder()
                .useFields(true, VisibilityFilter.PRIVATE)
                .useMethods(false)
                .useClassMetadata(false)
                .useRuntimeType(true)
                .create();

        String clientId = applianceId.length() > 0 ? applianceId + "-" + clazz.getSimpleName() : clazz.getSimpleName();
        try {
            client = new org.eclipse.paho.client.mqttv3.MqttClient(
                    "tcp://localhost:1883",
                    clientId,
                    new MemoryPersistence()
            );
            logger.debug("{}: Created MQTT client {}", loggerId, clientId);
        }
        catch (Exception e) {
            logger.error("{}: Error creating MQTT client {}", loggerId, clientId, e);
        }
    }

    private MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(1000);
        return options;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public static String getApplianceTopic(String applianceId, String subLevels) {
        return topicPrefix + "/" + applianceId + "/" + subLevels;
    }

    private boolean connect() {
        try {
            if(! client.isConnected()) {
                client.connect(getOptions());
            }
            boolean connectResult = client.isConnected();
            if(! connectResult) {
                logger.error("{}: Connection to MQTT broker failed", loggerId);
            }
            return connectResult;
        }
        catch (Exception e) {
            logger.error("{}: Error connecting to MQTT broker", loggerId, e);
        }
        return false;
    }

    public void send(String topic, MqttMessage message, boolean retained) {
        try {
            if(connect()) {
                String serializedMessage = this.genson.serialize(message);
                org.eclipse.paho.client.mqttv3.MqttMessage pahoMessage
                        = new org.eclipse.paho.client.mqttv3.MqttMessage(serializedMessage.getBytes());
                pahoMessage.setQos(0);
                pahoMessage.setRetained(retained);
                String fullTopic = getApplianceTopic(applianceId, topic);
                logger.debug("{}: Publish message: topic={} payload={}", loggerId, fullTopic, serializedMessage);
                client.publish(fullTopic, pahoMessage);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error sending message", loggerId, e);
        }
    }

    public void subscribe(String topic, boolean expandTopic, Class toType, MqttMessageHandler messageHandler) {
        String fullTopic = expandTopic ? getApplianceTopic(applianceId, topic) : topic;
        try {
            if(connect()) {
                client.subscribe(fullTopic, (receivedTopic, receivedMessage) -> {
                    try {
                        logger.debug("{}: Message received: topic={} payload={}", loggerId, receivedTopic, receivedMessage);
                        MqttMessage message = (MqttMessage) this.genson.deserialize(receivedMessage.getPayload(), toType);
                        messageHandler.messageArrived(receivedTopic, message);
                    }
                    catch(Exception e) {
                        logger.error("{}: Error receiving message", loggerId, e);
                    }
                });
                logger.debug("{}: Messages subscribed: topic={}", loggerId, fullTopic);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error subscribing to messages topic={}", loggerId, fullTopic, e);
        }
    }
}
