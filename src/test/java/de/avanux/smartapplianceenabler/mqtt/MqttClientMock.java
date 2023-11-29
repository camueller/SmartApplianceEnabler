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

package de.avanux.smartapplianceenabler.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttClientMock extends MqttClient {

    private Map<String, List<MqttMessageHandler>> messageHandlerForSubscribedTopic = new HashMap();

    public MqttClientMock(String applianceId, Class clazz) {
        super(applianceId, clazz);
    }

    public void subscribe(String topic, boolean expandTopic, MqttMessageHandler messageHandler) {
        subscribe(topic, expandTopic, false, messageHandler);
    }

    public void subscribe(String topic, boolean expandTopic, boolean set, MqttMessageHandler messageHandler) {
        String fullTopic = expandTopic
                ? (set ? getApplianceTopicForSet(getApplianceId(), topic) : getApplianceTopic(getApplianceId(), topic))
                : topic;
        var messageHandlers = messageHandlerForSubscribedTopic.get(fullTopic);
        if(messageHandlers == null) {
            messageHandlers = new ArrayList<>();
        }
        messageHandlers.add(messageHandler);
        messageHandlerForSubscribedTopic.put(fullTopic, messageHandlers);
    }

    protected void unsubscribeMessage(String fullTopic) {
        // this removes all subscribers of the topic since we cannot identify the message handler to be removed from list of subscribers
        messageHandlerForSubscribedTopic.remove(fullTopic);
    }

    public void publishMessage(String fullTopic, MqttMessage message, boolean retained) {
        var messageHandlers = messageHandlerForSubscribedTopic.get(fullTopic);
        if(messageHandlers != null) {
            List.copyOf(messageHandlers).stream().forEach(messageHandler -> messageHandler.messageArrived(fullTopic, message));
        }
    }

}
