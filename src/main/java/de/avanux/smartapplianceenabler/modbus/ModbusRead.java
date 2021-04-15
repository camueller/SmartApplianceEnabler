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
public class ModbusRead {
    @XmlAttribute
    private String address;
    @XmlAttribute
    private Integer words;
    @XmlAttribute
    private String byteOrder;
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String valueType;
    @XmlAttribute
    private Double factorToValue;
    @XmlElement(name = "ModbusReadValue")
    private List<ModbusReadValue> readValues;

    public ModbusRead() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getWords() {
        if(words == null) {
            return ModbusReadDefaults.getWords(getType(), getValueType());
        }
        return words;
    }

    public void setWords(Integer words) {
        this.words = words;
    }

    public ByteOrder getByteOrder() {
        return this.byteOrder != null ? ByteOrder.valueOf(this.byteOrder) : null;
    }

    public void setByteOrder(String byteOrder) {
        this.byteOrder = byteOrder;
    }

    public ReadRegisterType getType() {
        return ReadRegisterType.valueOf(this.type);
    }

    public void setType(String type) {
        this.type = type;
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

    public List<ModbusReadValue> getReadValues() {
        return readValues;
    }

    public void setReadValues(List<ModbusReadValue> readValues) {
        this.readValues = readValues;
    }

    public static ParentWithChild<ModbusRead, ModbusReadValue> getFirstRegisterRead(
            String registerName, List<ModbusRead> registerReads) {
        List<ParentWithChild<ModbusRead, ModbusReadValue>> matches = getRegisterReads(
                registerName, registerReads);
        return matches.size() > 0 ? matches.get(0) : null;
    }

    public static List<ParentWithChild<ModbusRead, ModbusReadValue>> getRegisterReads(
            String registerName, List<ModbusRead> registerReads) {
        List<ParentWithChild<ModbusRead, ModbusReadValue>> matches = new ArrayList<>();
        if(registerReads != null) {
            for(ModbusRead registerRead: registerReads) {
                for(ModbusReadValue registerReadValue: registerRead.getReadValues()) {
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

        ModbusRead that = (ModbusRead) o;

        return new EqualsBuilder()
                .append(address, that.address)
                .append(words, that.words)
                .append(byteOrder, that.byteOrder)
                .append(type, that.type)
                .append(factorToValue, that.factorToValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(address)
                .append(words)
                .append(byteOrder)
                .append(type)
                .append(factorToValue)
                .toHashCode();
    }
}
