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
package de.avanux.smartapplianceenabler.configuration;

import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.mqtt.MqttBroker;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Connectivity")
@XmlAccessorType(XmlAccessType.FIELD)
public class Connectivity {
    @XmlElement(name = "MqttBroker")
    private MqttBroker mqttBroker;
    @XmlElement(name = "ModbusTCP")
    private List<ModbusTcp> modbusTCPs;

    public MqttBroker getMqttBroker() {
        return mqttBroker;
    }

    public void setMqttBroker(MqttBroker mqttBroker) {
        this.mqttBroker = mqttBroker;
    }

    public List<ModbusTcp> getModbusTCPs() {
        return modbusTCPs;
    }

    public void setModbusTCPs(List<ModbusTcp> modbusTCPs) {
        this.modbusTCPs = modbusTCPs;
    }

}
