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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttClient {

    private static transient Logger logger = LoggerFactory.getLogger(MqttClient.class);
    private static String topicPrefix = "sae";
    private String loggerId;
    private String applianceId;
    private static int counter;
    public final static int MQTT_PUBLISH_PERIOD = 20;
    private IMqttClient client;
    private Genson genson;
    private static ExecutorService executor;

    public MqttClient(String applianceId, Class clazz) {
        this(applianceId, clazz, false);
    }

    public MqttClient(String applianceId, Class clazz, boolean clientIdWithCounter) {
        this.applianceId = applianceId;
        loggerId = applianceId.length() > 0 ? applianceId + "-MQTT-" + clazz.getSimpleName() : clazz.getSimpleName();

        this.genson = new GensonBuilder()
                .useFields(true, VisibilityFilter.PRIVATE)
                .useMethods(false)
                .useClassMetadata(false)
                .useRuntimeType(true)
                .create();


        StringBuilder clientIdBuilder = new StringBuilder();
        if(applianceId.length() > 0) {
            clientIdBuilder.append(applianceId + "-");
        }
        clientIdBuilder.append(clazz.getSimpleName());
        if(clientIdWithCounter) {
            clientIdBuilder.append("-" + counter++);
        }
        String clientId = clientIdBuilder.toString();

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

    public static void start() {
        executor = Executors.newFixedThreadPool(1);
        logger.debug("MQTT client started");
    }

    public static void stop() {
        if(executor != null) {
            executor.shutdown();
            logger.debug("MQTT client stopped");
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

    public static String getApplianceTopicForSet(String applianceId, String subLevels) {
        return getApplianceTopic(applianceId, subLevels) + "/set";
    }

    public static String getEventTopic(String applianceId, MqttEventName event) {
        return topicPrefix + "/" + applianceId + "/" + MqttEventName.TOPIC + "/" + event.getName();
    }

    private synchronized boolean connect() {
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

    public void publish(String topic, MqttMessage message, boolean retained) {
        publish(topic, true, message, false, retained);
    }

    public void publish(String topic, MqttMessage message, boolean set, boolean retained) {
        publish(topic, true, message, set, retained);
    }

    public void publish(String topic, boolean expandTopic, MqttMessage message, boolean set, boolean retained) {
        String fullTopic = expandTopic
                ? (set ? getApplianceTopicForSet(applianceId, topic) : getApplianceTopic(applianceId, topic))
                : topic;
        publishMessage(fullTopic, message, retained);
    }

    public void publish(MqttEventName event, MqttMessage message) {
        String fullTopic = getEventTopic(applianceId, event);
        publishMessage(fullTopic, message, false);
    }

    private void publishMessage(String fullTopic, MqttMessage message, boolean retained) {
        if(executor != null) {
            executor.submit(() -> {
                try {
                    if(connect()) {
                        String serializedMessage = genson.serialize(message);
                        org.eclipse.paho.client.mqttv3.MqttMessage pahoMessage
                                = new org.eclipse.paho.client.mqttv3.MqttMessage(serializedMessage.getBytes());
                        pahoMessage.setQos(0);
                        pahoMessage.setRetained(retained);
                        logger.trace("{}: Publish message: topic={} payload={}", loggerId, fullTopic, serializedMessage);
                        client.publish(fullTopic, pahoMessage);
                    }
                }
                catch (Exception e) {
                    logger.error("{}: Error sending message", loggerId, e);
                }
            });
        }
    }

    public void subscribe(MqttEventName event, Class toType, MqttMessageHandler messageHandler) {
        String fullTopic = getEventTopic(applianceId, event);
        subscribe(fullTopic, false, toType, messageHandler);
    }

    public void subscribe(String topic, boolean expandTopic, Class toType, MqttMessageHandler messageHandler) {
        subscribe(topic, expandTopic, false, toType, messageHandler);
    }

    public void subscribe(String topic, boolean expandTopic, boolean set, Class toType, MqttMessageHandler messageHandler) {
        String fullTopic = expandTopic
                ? (set ? getApplianceTopicForSet(applianceId, topic) : getApplianceTopic(applianceId, topic))
                : topic;
        try {
            if(connect()) {
                client.subscribe(fullTopic, (receivedTopic, receivedMessage) -> {
                    receiveMessage(receivedTopic, receivedMessage, messageHandler, toType);
                });
                logger.debug("{}: Messages subscribed: topic={}", loggerId, fullTopic);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error subscribing to messages topic={}", loggerId, fullTopic, e);
        }
    }

    private synchronized void receiveMessage(String receivedTopic, org.eclipse.paho.client.mqttv3.MqttMessage receivedMessage,
                                             MqttMessageHandler messageHandler, Class toType) {
        try {
            logger.trace("{}: Message received: topic={} payload={}", loggerId, receivedTopic, receivedMessage);
            MqttMessage message = (MqttMessage) this.genson.deserialize(receivedMessage.getPayload(), toType);
            messageHandler.messageArrived(receivedTopic, message);
        }
        catch(Exception e) {
            logger.error("{}: Error receiving message", loggerId, e);
        }
    }

    public void unsubscribe(String topic) {
        String fullTopic = getApplianceTopic(applianceId, topic);
        unsubscribeMessage(fullTopic);
    }

    public void unsubscribe(MqttEventName event) {
        String fullTopic = getEventTopic(applianceId, event);
        unsubscribeMessage(fullTopic);
    }

    private void unsubscribeMessage(String fullTopic) {
        try {
            if(connect()) {
                client.unsubscribe(fullTopic);
                logger.debug("{}: Messages unsubscribed: topic={}", loggerId, fullTopic);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error unsubscribing to messages topic={}", loggerId, fullTopic, e);
        }
    }
}
