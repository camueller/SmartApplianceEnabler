/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.modbus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ModbusRegisterRead {
    @XmlAttribute
    private String address;
    @XmlAttribute
    private Integer bytes;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private Integer pollInterval = 10; // seconds
    @XmlElement(name = "ModbusRegisterReadValue")
    private List<ModbusRegisterReadValue> registerReadValues;
    private transient ModbusRegisterReadValue selectedRegisterValue;

    public ModbusRegisterRead() {
    }

    public ModbusRegisterRead(String address, Integer bytes, ModbusRegisterType type, Integer pollInterval,
                              ModbusRegisterReadValue selectedRegisterValue) {
        this.address = address;
        this.bytes = bytes;
        this.type = type.name();
        this.pollInterval = pollInterval;
        this.selectedRegisterValue = selectedRegisterValue;
    }

    public String getAddress() {
        return address;
    }

    public Integer getBytes() {
        return bytes;
    }

    public ModbusRegisterType getType() {
        return ModbusRegisterType.valueOf(this.type);
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public List<ModbusRegisterReadValue> getRegisterReadValues() {
        return registerReadValues;
    }

    public ModbusRegisterReadValue getSelectedRegisterReadValue() {
        return selectedRegisterValue;
    }

}
