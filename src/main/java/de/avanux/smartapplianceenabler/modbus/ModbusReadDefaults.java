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

import java.util.Map;
import java.util.TreeMap;

public class ModbusReadDefaults {
    // static members won't be serialized but we need those valus on the client
    private Map<ReadRegisterType, Integer> wordsForRegisterType = new TreeMap<>();
    private static ModbusReadDefaults instance = new ModbusReadDefaults();

    public ModbusReadDefaults() {
        this.wordsForRegisterType.put(ReadRegisterType.InputFloat, 2);
        this.wordsForRegisterType.put(ReadRegisterType.InputDecimal, 1);
        this.wordsForRegisterType.put(ReadRegisterType.InputString, 1);
        this.wordsForRegisterType.put(ReadRegisterType.Holding, 1);
        this.wordsForRegisterType.put(ReadRegisterType.Coil, 1);
        this.wordsForRegisterType.put(ReadRegisterType.Discrete, 1);
    }

    public static Integer getWords(ReadRegisterType registerType) {
        return instance.wordsForRegisterType.get(registerType);
    }
}
