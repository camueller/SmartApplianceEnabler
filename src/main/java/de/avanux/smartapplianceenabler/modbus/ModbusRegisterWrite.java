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

import de.avanux.smartapplianceenabler.util.ParentWithChild;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ModbusRegisterWrite {
    @XmlAttribute
    private String address;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private Double factorToValue;
    @XmlElement(name = "ModbusRegisterWriteValue")
    private List<ModbusRegisterWriteValue> registerWriteValues;

    public ModbusRegisterWrite() {
    }

    public String getAddress() {
        return address;
    }

    public ModbusWriteRegisterType getType() {
        return ModbusWriteRegisterType.valueOf(this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getFactorToValue() {
        return factorToValue;
    }

    public void setFactorToValue(Double factorToValue) {
        this.factorToValue = factorToValue;
    }

    public ModbusReadRegisterType getReadRegisterType() {
        return ModbusReadRegisterType.valueOf(this.type);
    }

    public List<ModbusRegisterWriteValue> getRegisterWriteValues() {
        return registerWriteValues;
    }

    public void setRegisterWriteValues(List<ModbusRegisterWriteValue> registerWriteValues) {
        this.registerWriteValues = registerWriteValues;
    }

    public static ParentWithChild<ModbusRegisterWrite, ModbusRegisterWriteValue> getFirstRegisterWrite(
            String registerName, List<ModbusRegisterWrite> registerWrites) {
        List<ParentWithChild<ModbusRegisterWrite, ModbusRegisterWriteValue>> matches
                = getRegisterWrites(registerName, registerWrites);
        return matches.size() > 0 ? matches.get(0) : null;
    }

    public static List<ParentWithChild<ModbusRegisterWrite, ModbusRegisterWriteValue>> getRegisterWrites(
            String registerName, List<ModbusRegisterWrite> registerWrites) {
        List<ParentWithChild<ModbusRegisterWrite, ModbusRegisterWriteValue>> matches = new ArrayList<>();
        if(registerWrites != null) {
            for(ModbusRegisterWrite registerWrite: registerWrites) {
                for(ModbusRegisterWriteValue registerWriteValue: registerWrite.getRegisterWriteValues()) {
                    if(registerName.equals(registerWriteValue.getName())) {
                        matches.add(new ParentWithChild<>(registerWrite, registerWriteValue));
                    }
                }
            }
        }
        return matches;
    }
}
