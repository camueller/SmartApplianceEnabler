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
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class WrappedControl implements Control, ApplianceIdConsumer, NotificationProvider {
    private transient Logger logger = LoggerFactory.getLogger(WrappedControl.class);
    @XmlAttribute
    private String id;
    @XmlElements({
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "MqttSwitch", type = MqttSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class)
    })
    private Control control;
    private transient String applianceId;
    private transient Map<LocalDateTime, Integer> powerUpdates = new TreeMap<>();
    private transient boolean applianceOn;
    private transient NotificationHandler notificationHandler;
    private transient GuardedTimerTask mqttPublishTimerTask;
    private transient MqttClient mqttClient;
    final static public String WRAPPED_CONTROL_TOPIC = "Wrapped" + Control.TOPIC;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        if(this.control instanceof ApplianceIdConsumer) {
            ((ApplianceIdConsumer) this.control).setApplianceId(applianceId);
        }
    }

    protected String getApplianceId() {
        return applianceId;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public Control getControl() {
        return control;
    }

    protected MqttClient getMqttClient() {
        return mqttClient;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    @Override
    public void setPublishControlStateChangedEvent(boolean publishControlStateChangedEvent) {
    }

    protected NotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(control instanceof NotificationProvider && notificationHandler != null) {
            this.notificationHandler.setRequestedNotifications(((NotificationProvider) control).getNotifications());
        }
    }

    @Override
    public Notifications getNotifications() {
        return control instanceof NotificationProvider ? ((NotificationProvider) control).getNotifications() : null;
    }

    @Override
    public boolean isControllable() {
        return true;
    }

    @Override
    public void init() {
        logger.debug("{}: Initializing ...", applianceId);
        mqttClient = new MqttClient(applianceId, getClass());
        if(this.control != null) {
            this.control.setMqttTopic(StartingCurrentSwitch.WRAPPED_CONTROL_TOPIC);
            this.control.setPublishControlStateChangedEvent(false);
            this.control.init();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        if(this.control != null) {
            this.control.start(now, timer);
        }
        if(mqttClient != null) {
            mqttClient.subscribe(Meter.TOPIC, true, (topic, message) -> {
                if(message instanceof MeterMessage) {
                    onMeterUpdate(message.getTime(), ((MeterMessage) message).power, ((MeterMessage) message).energy);
                }
            });
            mqttClient.subscribe(WRAPPED_CONTROL_TOPIC, true, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    applianceOn = ((ControlMessage) message).on;
                }
            });
            mqttClient.subscribe(Control.TOPIC, true, true, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    ControlMessage controlMessage = (ControlMessage) message;
                    on(controlMessage.getTime(), controlMessage.on);
                }
            });
            this.mqttPublishTimerTask = new GuardedTimerTask(applianceId, "MqttPublish-" + getClass().getSimpleName(),
                    MqttClient.MQTT_PUBLISH_PERIOD * 1000) {
                @Override
                public void runTask() {
                    try {
                        publishControlMessage(isOn());
                    }
                    catch(Exception e) {
                        logger.error("{}: Error publishing MQTT message", applianceId, e);
                    }
                }
            };
            // initial publishControlMessage() is triggered by initial "switch off" in on(false)
            timer.schedule(this.mqttPublishTimerTask, this.mqttPublishTimerTask.getPeriod(), this.mqttPublishTimerTask.getPeriod());
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", this.applianceId);
        if(this.mqttPublishTimerTask != null) {
            this.mqttPublishTimerTask.cancel();
        }
        if(this.control != null) {
            this.control.stop(now);
        }
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    abstract protected void onMeterUpdate(LocalDateTime now, int averagePower, Double energy);

    protected void addPowerUpdate(LocalDateTime now, int averagePower, int maxAgeSeconds) {
        this.powerUpdates.put(now, averagePower);
        Set<LocalDateTime> expiredPowerUpdates = new HashSet<>();
        this.powerUpdates.keySet().forEach(timestamp -> {
            if(now.minusSeconds(maxAgeSeconds).isAfter(timestamp)) {
                expiredPowerUpdates.add(timestamp);
            }
        });
        for(LocalDateTime expiredPowerUpdate: expiredPowerUpdates) {
            this.powerUpdates.remove(expiredPowerUpdate);
        }
        Integer min = this.powerUpdates.values().stream().mapToInt(v -> v).min().orElseThrow(NoSuchElementException::new);
        Integer max = this.powerUpdates.values().stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new);
        logger.debug("{}: power value cache: min={}W max={}W values={} maxAge={}s",
                applianceId, min, max, this.powerUpdates.size(), maxAgeSeconds);
    }

    protected Map<LocalDateTime, Integer> getPowerUpdates() {
        return powerUpdates;
    }

    protected void clearPowerUpdates() {
        this.powerUpdates.clear();
    }

    abstract protected void on(LocalDateTime now, boolean switchOn);

    abstract public boolean isOn();

    protected boolean isApplianceOn() {
        return applianceOn;
    }

    protected void setApplianceOn(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting wrapped appliance switch to {}", applianceId, (switchOn ? "on" : "off"));
        mqttClient.publish(WRAPPED_CONTROL_TOPIC, new ControlMessage(now, switchOn), true, false);
    }

    protected void publishControlMessage(boolean on) {
        MqttMessage message = buildControlMessage(on);
        mqttClient.publish(Control.TOPIC, message, false);
    }

    abstract protected MqttMessage buildControlMessage(boolean on);
}
