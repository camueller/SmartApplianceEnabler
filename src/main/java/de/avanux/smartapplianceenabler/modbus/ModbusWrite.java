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
public class ModbusWrite {
    @XmlAttribute
    private String address;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String valueType;
    @XmlAttribute
    private Double factorToValue;
    @XmlElement(name = "ModbusWriteValue")
    private List<ModbusWriteValue> writeValues;

    public ModbusWrite() {
    }

    public String getAddress() {
        return address;
    }

    public WriteRegisterType getType() {
        return WriteRegisterType.valueOf(this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public ReadRegisterType getReadRegisterType() {
        return ReadRegisterType.valueOf(this.type);
    }

    public RegisterValueType getValueType() {
        return this.valueType != null ? RegisterValueType.valueOf(this.valueType) : null;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Double getFactorToValue() {
        return factorToValue;
    }

    public void setFactorToValue(Double factorToValue) {
        this.factorToValue = factorToValue;
    }

    public List<ModbusWriteValue> getWriteValues() {
        return writeValues;
    }

    public void setWriteValues(List<ModbusWriteValue> writeValues) {
        this.writeValues = writeValues;
    }

    public static ParentWithChild<ModbusWrite, ModbusWriteValue> getFirstRegisterWrite(
            String registerName, List<ModbusWrite> registerWrites) {
        List<ParentWithChild<ModbusWrite, ModbusWriteValue>> matches
                = getRegisterWrites(registerName, registerWrites);
        return matches.size() > 0 ? matches.get(0) : null;
    }

    public static List<ParentWithChild<ModbusWrite, ModbusWriteValue>> getRegisterWrites(
            String registerName, List<ModbusWrite> registerWrites) {
        List<ParentWithChild<ModbusWrite, ModbusWriteValue>> matches = new ArrayList<>();
        if(registerWrites != null) {
            for(ModbusWrite registerWrite: registerWrites) {
                for(ModbusWriteValue registerWriteValue: registerWrite.getWriteValues()) {
                    if(registerName.equals(registerWriteValue.getName())) {
                        matches.add(new ParentWithChild<>(registerWrite, registerWriteValue));
                    }
                }
            }
        }
        return matches;
    }
}
