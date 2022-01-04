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

package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class MasterElectricityMeter implements ApplianceIdConsumer, Validateable, Meter {
    private transient Logger logger = LoggerFactory.getLogger(MasterElectricityMeter.class);
    @XmlAttribute
    private Boolean masterSwitchOn;
    @XmlAttribute
    private Boolean slaveSwitchOn;
    @XmlElements({
            @XmlElement(name = "S0ElectricityMeter", type = S0ElectricityMeter.class),
            @XmlElement(name = "ModbusElectricityMeter", type = ModbusElectricityMeter.class),
            @XmlElement(name = "HttpElectricityMeter", type = HttpElectricityMeter.class),
    })
    private Meter meter;
    private transient SlaveElectricityMeter slaveMeter;
    private transient String applianceId;
    private transient boolean isMasterControlOn;
    private transient boolean isSlaveControlOn;
    private transient MqttClient mqttClient;
    final static public String WRAPPED_METER_TOPIC = "Wrapped" + Meter.TOPIC;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        if(this.meter instanceof ApplianceIdConsumer) {
            ((ApplianceIdConsumer) this.meter).setApplianceId(applianceId);
        }
    }

    public Meter getWrappedMeter() {
        return meter;
    }

    public void setSlaveElectricityMeter(SlaveElectricityMeter slaveMeter) {
        this.slaveMeter = slaveMeter;
    }

    @Override
    public void setMqttTopic(String mqttTopic) {
    }

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: configured: masterSwitchOn={} slaveSwitchOn={}", applianceId, masterSwitchOn, slaveSwitchOn);
    }

    @Override
    public void init() {
        mqttClient = new MqttClient(applianceId, getClass());
        if(this.meter != null) {
            this.meter.init();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        if (mqttClient != null) {
            var masterControlTopic = MqttClient.getApplianceTopic(applianceId, Control.TOPIC);
            mqttClient.subscribe(masterControlTopic, false, ControlMessage.class, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    isMasterControlOn = ((ControlMessage) message).on;
                }
            });

            var slaveControlTopic = MqttClient.getApplianceTopic(slaveMeter.getApplianceId(), Control.TOPIC);
            mqttClient.subscribe(slaveControlTopic, false, ControlMessage.class, (topic, message) -> {
                if(message instanceof ControlMessage) {
                    isSlaveControlOn = ((ControlMessage) message).on;
                }
            });

            mqttClient.subscribe(WRAPPED_METER_TOPIC, true, MeterMessage.class, (topic, message) -> {
                publishMeterMessage((MeterMessage) message);
            });
        }
        if(this.meter != null) {
            this.meter.start(now, timer);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        meter.stop(now);
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    private boolean isMeteringForSlave() {
        boolean meteringForSlave = false;
        if(masterSwitchOn != null && slaveSwitchOn != null) {
            meteringForSlave = masterSwitchOn == this.isMasterControlOn && slaveSwitchOn == this.isSlaveControlOn;
        }
        else if(masterSwitchOn != null) {
            meteringForSlave = masterSwitchOn == this.isMasterControlOn;
        }
        else if(slaveSwitchOn != null) {
            meteringForSlave = slaveSwitchOn == this.isSlaveControlOn;
        }
        logger.debug("{}: isMeteringForSlave={} masterSwitchOn={} isMasterControlOn={} slaveSwitchOn={} isSlaveControlOn={}",
                applianceId, meteringForSlave, masterSwitchOn, isMasterControlOn, slaveSwitchOn, isSlaveControlOn);
        return meteringForSlave;
    }

    @Override
    public void startEnergyMeter() {
        meter.startEnergyMeter();
    }

    @Override
    public void stopEnergyMeter() {
        meter.stopEnergyMeter();
    }

    @Override
    public void resetEnergyMeter() {
        meter.resetEnergyMeter();
    }

    @Override
    public void startAveragingInterval(LocalDateTime now, Timer timer, int nextPollCompletedSecondsFromNow) {
        meter.startAveragingInterval(now, timer, nextPollCompletedSecondsFromNow);
    }

    private void publishMeterMessage(MeterMessage message) {
        MeterMessage masterMeterMessage = null;
        MeterMessage slaveMeterMessage = null;
        if(isMeteringForSlave()) {
            masterMeterMessage = new MeterMessage(message.getTime(), 0, 0);
            slaveMeterMessage = message;
        }
        else {
            masterMeterMessage = message;
            slaveMeterMessage = new MeterMessage(message.getTime(), 0, 0);
        }

        var masterMeterPublishTopic = MqttClient.getApplianceTopic(applianceId, Meter.TOPIC);
        mqttClient.publish(masterMeterPublishTopic, false, masterMeterMessage, false,false);

        var slaveMeterPublishTopic = MqttClient.getApplianceTopic(slaveMeter.getApplianceId(), Meter.TOPIC);
        mqttClient.publish(slaveMeterPublishTopic, false, slaveMeterMessage, false,false);
    }
}
