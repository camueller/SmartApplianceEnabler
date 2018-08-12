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
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ModbusRegisterRead {
    @XmlAttribute
    private String address;
    @XmlAttribute
    private Integer bytes = 1;
    @XmlAttribute
    private String type;
    @XmlElement(name = "ModbusRegisterReadValue")
    private List<ModbusRegisterReadValue> registerReadValues;
    private transient ModbusRegisterReadValue selectedRegisterReadValue;

    public ModbusRegisterRead() {
    }

    public ModbusRegisterRead(String address, Integer bytes, ModbusReadRegisterType type,
                              ModbusRegisterReadValue selectedRegisterReadValue) {
        this.address = address;
        this.bytes = bytes;
        this.type = type.name();
        this.selectedRegisterReadValue = selectedRegisterReadValue;
    }

    public String getAddress() {
        return address;
    }

    public Integer getBytes() {
        if(getType() == ModbusReadRegisterType.InputFloat) {
            return 2;
        }
        return bytes;
    }

    public ModbusReadRegisterType getType() {
        return ModbusReadRegisterType.valueOf(this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ModbusRegisterReadValue> getRegisterReadValues() {
        return registerReadValues;
    }

    public void setRegisterReadValues(List<ModbusRegisterReadValue> registerReadValues) {
        this.registerReadValues = registerReadValues;
    }

    public ModbusRegisterReadValue getSelectedRegisterReadValue() {
        return selectedRegisterReadValue;
    }

    public static ModbusRegisterRead getFirstRegisterRead(String registerName, List<ModbusRegisterRead> registerReads) {
        List<ModbusRegisterRead> matches = getRegisterReads(registerName, registerReads);
        return matches.size() > 0 ? matches.get(0) : null;
    }

    public static List<ModbusRegisterRead> getRegisterReads(String registerName, List<ModbusRegisterRead> registerReads) {
        List<ModbusRegisterRead> matches = new ArrayList<>();
        for(ModbusRegisterRead registerRead: registerReads) {
            for(ModbusRegisterReadValue registerReadValue: registerRead.getRegisterReadValues()) {
                if(registerName.equals(registerReadValue.getName())) {
                    matches.add(new ModbusRegisterRead(registerRead.getAddress(), registerRead.getBytes(),
                            registerRead.getType(), registerReadValue));
                }
            }
        }
        return matches;
    }
}
