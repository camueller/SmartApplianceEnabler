/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.TreeMap;

public class ModbusReadDefaults {
    private static ModbusReadDefaults instance = new ModbusReadDefaults();

    // static members won't be serialized but we need those valus on the client
    private ByteOrder[] byteOrders = ByteOrder.values();
    private Map<Pair<ReadRegisterType, RegisterValueType>, Integer> wordsForRegisterType = new TreeMap<>();

    public ModbusReadDefaults() {
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Input, RegisterValueType.Float), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Input, RegisterValueType.Integer), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Input, RegisterValueType.Integer2Float), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Input, RegisterValueType.String), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Holding, RegisterValueType.Float), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Holding, RegisterValueType.Integer), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Holding, RegisterValueType.Integer2Float), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Holding, RegisterValueType.String), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Coil, null), 1);
        this.wordsForRegisterType.put(Pair.of(ReadRegisterType.Discrete, null), 1);
    }

    public static Integer getWords(ReadRegisterType registerType, RegisterValueType valueType) {
        return instance.wordsForRegisterType.get(Pair.of(registerType, valueType));
    }

    public ByteOrder[] getByteOrders() {
        return byteOrders;
    }
}
