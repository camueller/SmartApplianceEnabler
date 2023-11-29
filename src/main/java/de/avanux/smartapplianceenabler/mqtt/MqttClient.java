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
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttClient {

    private static transient Logger logger = LoggerFactory.getLogger(MqttClient.class);
    private static String topicPrefix = "sae";
    private static MqttBroker mqttBroker = new MqttBroker();
    private String loggerId;
    private String applianceId;
    private static Map<String, Integer> counterForClientId = new HashMap();
    private Map<String, IMessageHandler> messageHandlerForSubscribedTopic = new HashMap();
    public final static int MQTT_PUBLISH_PERIOD = 20;
    private IMqttClient client;
    private static org.eclipse.paho.client.mqttv3.MqttClient instance;
    private Genson genson;
    private static ExecutorService executor;
    private boolean shutdownInProgress = false;

    public MqttClient(String applianceId, Class clazz) {
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

        if(applianceId.length() > 0) {
            var counter = counterForClientId.get(clientIdBuilder.toString());
            if (counter != null) {
                counter++;
            } else {
                counter = 0;
            }
            counterForClientId.put(clientIdBuilder.toString(), counter);
            clientIdBuilder.append("-").append(counter);
        } else {
            clientIdBuilder.append("-").append(this.hashCode());
        }

        var brokerUri = buildBrokerUri(mqttBroker.getResolvedHost(), mqttBroker.getResolvedPort());
        logger.info("Using MQTT broker " + brokerUri);
        client = createClient(clientIdBuilder.toString(), brokerUri, loggerId);
    }

    protected String getApplianceId() {
        return applianceId;
    }

    public static void setMqttBroker(MqttBroker mqttBroker) {
        MqttClient.mqttBroker = mqttBroker;
    }

    private static String buildBrokerUri(String host, Integer port) {
        return "tcp://" + host + ":" + port;
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

    private static MqttConnectOptions getOptions(String username, String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        if(username != null) {
            options.setUserName(username);
        }
        if(password != null) {
            options.setPassword(password.toCharArray());
        }
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
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

    public static String getEventTopic(MqttEventName event) {
        return topicPrefix + "/" + MqttEventName.TOPIC + "/" + event.getName();
    }

    private static org.eclipse.paho.client.mqttv3.MqttClient createClient(String clientId, String brokerUri, String loggerId) {
        org.eclipse.paho.client.mqttv3.MqttClient client = null;
        try {
            client = new org.eclipse.paho.client.mqttv3.MqttClient(
                    brokerUri,
                    clientId,
                    new MemoryPersistence()
            );
            logger.debug("{}: Created MQTT client {}", loggerId, clientId);
        }
        catch (Exception e) {
            logger.error("{}: Error creating MQTT client {}", loggerId, clientId, e);
        }
        return client;
    }

    private synchronized boolean connect() {
        if(! shutdownInProgress) {
            try {
                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        logger.trace("{}: MQTT connecton completed. reconnect={}", loggerId, reconnect);
                        if(reconnect) {
                            messageHandlerForSubscribedTopic.keySet().forEach(fullTopic -> {
                                subscribeCachedMessageHandler(fullTopic);
                            });
                        }
                    }

                    @Override
                    public void connectionLost(Throwable throwable) {
                        logger.error("{}: MQTT connecton lost {}", loggerId, throwable);
                    }

                    @Override
                    public void messageArrived(String s, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage) throws Exception {
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    }
                });
                synchronized (this) {
                    if(! client.isConnected()) {
                        client.connect(getOptions(mqttBroker.getUsername(), mqttBroker.getPassword()));
                    }
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
        }
        return false;
    }


    public synchronized void disconnect() {
        shutdownInProgress = true;
        try {
            var connected = client.isConnected();
            if(connected) {
                client.disconnect();
            }
            logger.debug("{}: disconnected from MQTT broker - was connected={}", loggerId, connected);
        }
        catch(Exception e) {
            logger.error("{}: Error disconnecting from MQTT broker", loggerId, e);
        }
    }

    public static boolean isMqttBrokerAvailable(String host, Integer port, String username, String password) {
        var brokerUri = buildBrokerUri(host, port);
        var loggerId = MqttClient.class.getSimpleName();
        try {
            if(instance == null) {
                instance = createClient(MqttClient.class.getSimpleName(), brokerUri, loggerId);
            }
            if(! instance.isConnected()) {
                instance.connect(getOptions(username, password));
            }
            var connected = instance.isConnected();
            logger.debug("{}: MQTT connection available: {}", loggerId, connected);
            return connected;
        }
        catch(Exception e) {
            logger.error("{}: Error testing connection to MQTT broker", loggerId, e);
        }
        return false;
    }

    public static void removeMqttBrokerInstanceForAvailabilityTest() {
        instance = null;
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
        publish(event, message, false);
    }

    public void publish(MqttEventName event, MqttMessage message, boolean retained) {
        String fullTopic = applianceId.length() > 0 ? getEventTopic(applianceId, event) : getEventTopic(event);
        publishMessage(fullTopic, message, retained);
    }

    public void publishMessage(String fullTopic, MqttMessage message, boolean retained) {
        message.setType(message.getClass().getSimpleName());
        String serializedMessage = genson.serialize(message);
        publishMessage(fullTopic, serializedMessage.getBytes(), retained, null);
    }

    public void publishMessage(String fullTopic, byte[] message, boolean retained, OnErrorCallback onErrorCallback) {
        if(executor != null) {
            executor.submit(() -> {
                try {
                    if(connect()) {
                        logger.trace("{}: Publish message: topic={} payload={} retained={}", loggerId, fullTopic, new String(message, StandardCharsets.UTF_8), retained);
                        client.publish(fullTopic, createMessage(message, retained));
                    }
                }
                catch (Exception e) {
                    logger.error("{}: Error sending message", loggerId, e);
                    if(onErrorCallback != null) {
                        onErrorCallback.onError(e);
                    }
                }
            });
        }
    }

    private org.eclipse.paho.client.mqttv3.MqttMessage createMessage(byte[] payload, boolean retained) {
        org.eclipse.paho.client.mqttv3.MqttMessage message = new org.eclipse.paho.client.mqttv3.MqttMessage(payload);
        message.setQos(0);
        message.setRetained(retained);
        return message;
    }

    public void subscribe(MqttEventName event, MqttMessageHandler messageHandler) {
        String fullTopic = getEventTopic(applianceId, event);
        subscribe(fullTopic, false, messageHandler);
    }

    public void subscribe(String topic, boolean expandTopic, MqttMessageHandler messageHandler) {
        subscribe(topic, expandTopic, false, messageHandler);
    }

    public void subscribe(String topic, boolean expandTopic, boolean set, MqttMessageHandler messageHandler) {
        String fullTopic = expandTopic
                ? (set ? getApplianceTopicForSet(applianceId, topic) : getApplianceTopic(applianceId, topic))
                : topic;
        messageHandlerForSubscribedTopic.put(fullTopic, messageHandler);
        if(connect()) {
            subscribeCachedMessageHandler(fullTopic);
        }
    }

    public void subscribe(String topic, RawMqttMessageHandler messageHandler) {
        messageHandlerForSubscribedTopic.put(topic, messageHandler);
        if(connect()) {
            subscribeCachedMessageHandler(topic);
        }
    }

    private void subscribeCachedMessageHandler(String fullTopic) {
        try {
            client.subscribe(fullTopic, (receivedTopic, receivedMessage) -> {
                var messageHandler = messageHandlerForSubscribedTopic.get(fullTopic);
                if(messageHandler instanceof RawMqttMessageHandler) {
                    ((RawMqttMessageHandler) messageHandler).messageArrived(receivedTopic, receivedMessage.getPayload());
                }
                else if(messageHandler instanceof MqttMessageHandler) {
                    receiveMessage(receivedTopic, receivedMessage, (MqttMessageHandler) messageHandlerForSubscribedTopic.get(fullTopic));
                }
            });
            logger.debug("{}: Messages subscribed: topic={}", loggerId, fullTopic);
        }
        catch (Exception e) {
            logger.error("{}: Error subscribing to messages topic={}", loggerId, fullTopic, e);
        }
    }

    private synchronized void receiveMessage(String receivedTopic, org.eclipse.paho.client.mqttv3.MqttMessage receivedMessage,
                                             MqttMessageHandler messageHandler) {
        String messageType = null;
        try {
            logger.trace("{}: Message received: topic={} payload={}", loggerId, receivedTopic, receivedMessage);
            MqttMessage messageWithType = this.genson.deserialize(receivedMessage.getPayload(), MqttMessage.class);
            messageType = messageWithType.getType();
            String className = "de.avanux.smartapplianceenabler.mqtt." + messageType;
            Class toType = Class.forName(className);
            MqttMessage message = (MqttMessage) this.genson.deserialize(receivedMessage.getPayload(), toType);
            messageHandler.messageArrived(receivedTopic, message);
        }
        catch (ClassNotFoundException e) {
            if(messageType == null) {
                logger.error("{}: Message type missing", loggerId, e);
            }
            else {
                logger.error("{}: Unknown message type {}", loggerId, messageType, e);
            }
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

    protected void unsubscribeMessage(String fullTopic) {
        try {
            messageHandlerForSubscribedTopic.remove(fullTopic);
            if(connect()) {
                client.unsubscribe(fullTopic);
                logger.debug("{}: Messages unsubscribed: topic={}", loggerId, fullTopic);
            }
        }
        catch (Exception e) {
            logger.error("{}: Error unsubscribing to messages topic={}", loggerId, fullTopic, e);
        }
    }

    public interface OnErrorCallback {
        void onError(Throwable t);
    }
}
