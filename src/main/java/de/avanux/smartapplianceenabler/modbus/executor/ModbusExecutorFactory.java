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

package de.avanux.smartapplianceenabler.modbus.executor;

import de.avanux.smartapplianceenabler.modbus.ByteOrder;
import de.avanux.smartapplianceenabler.modbus.ReadRegisterType;
import de.avanux.smartapplianceenabler.modbus.RegisterValueType;
import de.avanux.smartapplianceenabler.modbus.WriteRegisterType;
import de.avanux.smartapplianceenabler.modbus.transformer.*;

public class ModbusExecutorFactory {

    private static ModbusReadTransactionExecutor readCoilExecutor;
    private static ModbusReadTransactionExecutor readDiscreteInputExecutor;
    private static ModbusReadTransactionExecutor readHoldingExecutor;
    private static ModbusReadTransactionExecutor readInputExecutor;
    private static ModbusWriteTransactionExecutor<Boolean> writeCoilExecutor;
    private static ModbusWriteTransactionExecutor<Integer> writeHoldingExecutor;

    public static void setReadCoilExecutor(ModbusReadTransactionExecutor testingReadCoilExecutor) {
        ModbusExecutorFactory.readCoilExecutor = testingReadCoilExecutor;
    }

    public static void setReadDiscreteInputExecutor(ModbusReadTransactionExecutor readDiscreteInputExecutor) {
        ModbusExecutorFactory.readDiscreteInputExecutor = readDiscreteInputExecutor;
    }

    public static void setReadHoldingExecutor(ModbusReadTransactionExecutor readHoldingExecutor) {
        ModbusExecutorFactory.readHoldingExecutor = readHoldingExecutor;
    }

    public static void setReadInputExecutor(ModbusReadTransactionExecutor readInputExecutor) {
        ModbusExecutorFactory.readInputExecutor = readInputExecutor;
    }

    public static void setWriteCoilExecutor(ModbusWriteTransactionExecutor<Boolean> writeCoilExecutor) {
        ModbusExecutorFactory.writeCoilExecutor = writeCoilExecutor;
    }

    public static void setWriteHoldingExecutor(ModbusWriteTransactionExecutor<Integer> writeHoldingExecutor) {
        ModbusExecutorFactory.writeHoldingExecutor = writeHoldingExecutor;
    }


    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, String address, ReadRegisterType type,
                                                                RegisterValueType valueType) {
        return getReadExecutor(applianceId, address, type, valueType, 1, ByteOrder.BigEndian, 1.0);
    }

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, String address, ReadRegisterType type,
                                                                RegisterValueType valueType,
                                                                int requestWords) {
        return getReadExecutor(applianceId, address, type, valueType, requestWords, ByteOrder.BigEndian, 1.0);
    }

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId,
                                                                String address, ReadRegisterType type,
                                                                RegisterValueType valueType,
                                                                int requestWords, ByteOrder byteOrder,
                                                                Double factorToValue) {
        ModbusReadTransactionExecutor executor;
        switch (type) {
            case Coil:
                executor = readCoilExecutor != null ? readCoilExecutor :
                        new ReadCoilExecutorImpl(address);
                break;
            case Discrete:
                executor = readDiscreteInputExecutor != null ? readDiscreteInputExecutor :
                        new ReadDiscreteInputExecutorImpl(address);
                break;
            case Holding:
                executor = readHoldingExecutor != null ? readHoldingExecutor :
                        new ReadHoldingRegisterExecutor(address, requestWords,
                            getValueTransformer(applianceId, valueType, byteOrder, factorToValue));
                break;
            case Input:
                executor = readInputExecutor != null ? readInputExecutor :
                        new ReadInputRegisterExecutor(address, requestWords,
                                getValueTransformer(applianceId, valueType, byteOrder, factorToValue));
                break;
            default:
                throw new RuntimeException("Unsupported register type: " + type.name());
        }
        executor.setApplianceId(applianceId);
        return executor;
    }

    public static ModbusWriteTransactionExecutor<?> getWriteExecutor(String applianceId, WriteRegisterType type,
                                                                  String address, Double factorToValue) {
        ModbusWriteTransactionExecutor<?> executor;
        switch (type) {
            case Holding:
                executor = writeHoldingExecutor != null ? writeHoldingExecutor :
                        new WriteHoldingRegisterExecutorImpl(address, factorToValue);
                break;
            case Coil:
                executor = writeCoilExecutor != null ? writeCoilExecutor :
                        new WriteCoilExecutorImpl(address);
                break;
            default:
                throw new RuntimeException("Unsupported register type: " + type.name());
        }
        executor.setApplianceId(applianceId);
        return executor;
    }

    private static ValueTransformer<?> getValueTransformer(String applianceId, RegisterValueType registerValueType,
                                                           ByteOrder byteOrder, Double factorToValue) {
        ValueTransformer<?> transformer = null;
        switch (registerValueType) {
            case Float:
                transformer = new FloatValueTransformer(factorToValue);
                break;
            case Integer2Float:
                transformer = new Integer2FloatValueTransformer(byteOrder, factorToValue);
                break;
            case Integer:
                transformer = new IntegerValueTransformer();
                break;
            case String:
                transformer = new StringValueTransformer();
                break;
            default:
                throw new RuntimeException("Unsupported register value type: " + registerValueType.name());
        }
        transformer.setApplianceId(applianceId);
        return transformer;
    }
}
