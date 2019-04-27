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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    private Integer bytes;
    @XmlAttribute
    private String byteOrder;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private Double factorToValue;
    @XmlElement(name = "ModbusRegisterReadValue")
    private List<ModbusRegisterReadValue> registerReadValues;

    public ModbusRegisterRead() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getBytes() {
        if(bytes == null) {
            if(getType() == ModbusReadRegisterType.InputFloat) {
                return 2;
            }
            return 1;
        }
        return bytes;
    }

    public void setBytes(Integer bytes) {
        this.bytes = bytes;
    }

    public ByteOrder getByteOrder() {
        return this.byteOrder != null ? ByteOrder.valueOf(this.byteOrder) : null;
    }

    public void setByteOrder(String byteOrder) {
        this.byteOrder = byteOrder;
    }

    public ModbusReadRegisterType getType() {
        return ModbusReadRegisterType.valueOf(this.type);
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

    public List<ModbusRegisterReadValue> getRegisterReadValues() {
        return registerReadValues;
    }

    public void setRegisterReadValues(List<ModbusRegisterReadValue> registerReadValues) {
        this.registerReadValues = registerReadValues;
    }

    public static ParentWithChild<ModbusRegisterRead, ModbusRegisterReadValue> getFirstRegisterRead(
            String registerName, List<ModbusRegisterRead> registerReads) {
        List<ParentWithChild<ModbusRegisterRead, ModbusRegisterReadValue>> matches = getRegisterReads(
                registerName, registerReads);
        return matches.size() > 0 ? matches.get(0) : null;
    }

    public static List<ParentWithChild<ModbusRegisterRead, ModbusRegisterReadValue>> getRegisterReads(
            String registerName, List<ModbusRegisterRead> registerReads) {
        List<ParentWithChild<ModbusRegisterRead, ModbusRegisterReadValue>> matches = new ArrayList<>();
        if(registerReads != null) {
            for(ModbusRegisterRead registerRead: registerReads) {
                for(ModbusRegisterReadValue registerReadValue: registerRead.getRegisterReadValues()) {
                    if(registerName.equals(registerReadValue.getName())) {
                        matches.add(new ParentWithChild<>(registerRead, registerReadValue));
                    }
                }
            }
        }
        return matches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ModbusRegisterRead that = (ModbusRegisterRead) o;

        return new EqualsBuilder()
                .append(address, that.address)
                .append(bytes, that.bytes)
                .append(byteOrder, that.byteOrder)
                .append(type, that.type)
                .append(factorToValue, that.factorToValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(address)
                .append(bytes)
                .append(byteOrder)
                .append(type)
                .append(factorToValue)
                .toHashCode();
    }
}
