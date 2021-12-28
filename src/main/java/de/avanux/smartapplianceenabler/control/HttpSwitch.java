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
import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.http.*;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import de.avanux.smartapplianceenabler.protocol.JsonContentProtocolHandler;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Changes the on/off state of an appliance by sending an HTTP request.
 *
 * IMPORTANT: The URLs in Appliance.xml have to be escaped (e.g. use "&amp;" instead of "&")
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpSwitch implements Control, ApplianceLifeCycle, Validateable, ApplianceIdConsumer, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(HttpSwitch.class);
    @XmlElement(name = "HttpConfiguration")
    private HttpConfiguration httpConfiguration;
    @XmlElement(name = "HttpWrite")
    private List<HttpWrite> httpWrites;
    @XmlElement(name = "HttpRead")
    private HttpRead httpRead;
    @XmlAttribute
    private String contentProtocol;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient String applianceId;
    private transient HttpHandler httpHandler = new HttpHandler();
    private transient HttpTransactionExecutor httpTransactionExecutor = new HttpTransactionExecutor();
    private transient ContentProtocolHandler contentContentProtocolHandler;
    protected transient boolean on;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;
    private transient String mqttTopic = Control.TOPIC;
    private transient boolean publishControlStateChangedEvent = true;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.httpTransactionExecutor.setApplianceId(applianceId);
        this.httpHandler.setApplianceId(applianceId);
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    @Override
    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
        this.publishControlStateChangedEvent = publishControlStateChangedEvent;
    }

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(this.notificationHandler != null) {
            this.notificationHandler.setRequestedNotifications(notifications);
            this.httpTransactionExecutor.setNotificationHandler(notificationHandler);
        }
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    public void setHttpConfiguration(HttpConfiguration httpConfiguration) {
        this.httpConfiguration = httpConfiguration;
    }

    public void setHttpTransactionExecutor(HttpTransactionExecutor httpTransactionExecutor) {
        this.httpTransactionExecutor = httpTransactionExecutor;
    }

    public void setHttpWrites(List<HttpWrite> httpWrites) {
        this.httpWrites = httpWrites;
    }

    @Override
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
        if(this.httpConfiguration != null) {
            this.httpTransactionExecutor.setConfiguration(this.httpConfiguration);
        }
        this.httpHandler.setHttpTransactionExecutor(httpTransactionExecutor);
    }


    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", applianceId);
        HttpValidator validator = new HttpValidator(applianceId);

        List<String> writeValueNames = Arrays.stream(ControlValueName.values())
                .map(valueName -> valueName.name()).collect(Collectors.toList());
        boolean writesValid = validator.validateWrites(writeValueNames, this.httpWrites);
        boolean readValid = this.httpRead == null || validator.validateReads(
                Collections.singletonList(ControlValueName.On.name()),
                Collections.singletonList(this.httpRead), true);
        if(! (writesValid && readValid)) {
            throw new ConfigurationException();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        this.mqttPublishTimerTask = new GuardedTimerTask(applianceId, "MqttPublish-" + getClass().getSimpleName(),
                MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
            @Override
            public void runTask() {
                try {
                    publishControlMessage(LocalDateTime.now(), isOn());
                }
                catch(Exception e) {
                    logger.error("{}: Error publishing MQTT message", applianceId, e);
                }
            }
        };
        timer.schedule(this.mqttPublishTimerTask, 0, this.mqttPublishTimerTask.getPeriod());

        mqttClient.subscribe(mqttTopic, true, true, ControlMessage.class, (topic, message) -> {
            if(message instanceof ControlMessage) {
                ControlMessage controlMessage = (ControlMessage) message;
                this.on(controlMessage.getTime(), controlMessage.on);
            }
        });
    }

    @Override
    public void stop(LocalDateTime now) {
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    private boolean isOn() {
        if(this.httpRead != null) {
            ParentWithChild<HttpRead, HttpReadValue> onRead = HttpRead.getFirstHttpRead(ControlValueName.On.name(),
                    Collections.singletonList(this.httpRead));
            if(onRead != null) {
                return this.httpHandler.getBooleanValue(onRead, getContentContentProtocolHandler());
            }
        }
        // fall back to internal state if no HttpRead is configured
        return on;
    }

    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.info("{}: Switching {}", applianceId, (switchOn ? "on" : "off"));
        ParentWithChild<HttpWrite, HttpWriteValue> write
                = HttpWrite.getFirstHttpWrite(getValueName(switchOn).name(), this.httpWrites);
        if(write != null) {
            HttpMethod httpMethod = write.child().getMethod();
            String data = httpMethod == HttpMethod.POST ? write.child().getValue() : null;
            CloseableHttpResponse response = this.httpTransactionExecutor.executeLeaveOpen(write.child().getMethod(),
                    write.parent().getUrl(), data);
            if(response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                this.httpTransactionExecutor.closeResponse(response);
                if(statusCode == HttpStatus.SC_OK) {
                    on = switchOn;
                    publishControlMessage(now, switchOn);
                    if(this.notificationHandler != null && switchOn != on) {
                        this.notificationHandler.sendNotification(switchOn ? NotificationType.CONTROL_ON : NotificationType.CONTROL_OFF);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void publishControlMessage(LocalDateTime now, boolean on) {
        MqttMessage message = new ControlMessage(now, on);
        mqttClient.publish(mqttTopic, message, true);
    }

    public ContentProtocolHandler getContentContentProtocolHandler() {
        if(this.contentContentProtocolHandler == null) {
            if(ContentProtocolType.JSON.name().equals(this.contentProtocol)) {
                this.contentContentProtocolHandler = new JsonContentProtocolHandler();
            }
        }
        return this.contentContentProtocolHandler;
    }

    private ControlValueName getValueName(boolean switchOn) {
        return switchOn ? ControlValueName.On : ControlValueName.Off;
    }
}
