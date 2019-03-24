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
import de.avanux.smartapplianceenabler.modbus.ModbusReadRegisterType;
import de.avanux.smartapplianceenabler.modbus.ModbusWriteRegisterType;

public class ModbusExecutorFactory {

    private static ModbusReadTransactionExecutor testingReadStringExecutor;
    private static ModbusReadTransactionExecutor testingReadBooleanExecutor;
    private static ModbusReadTransactionExecutor testingReadFloatExecutor;
    private static ModbusWriteTransactionExecutor testingWriteBooleanExecutor;
    private static ModbusWriteTransactionExecutor testingWriteIntegerExecutor;

    public static void setTestingReadStringExecutor(ModbusReadTransactionExecutor testingReadStringExecutor) {
        ModbusExecutorFactory.testingReadStringExecutor = testingReadStringExecutor;
    }

    public static void setTestingReadBooleanExecutor(ModbusReadTransactionExecutor testingReadBooleanExecutor) {
        ModbusExecutorFactory.testingReadBooleanExecutor = testingReadBooleanExecutor;
    }

    public static void setTestingReadFloatExecutor(ModbusReadTransactionExecutor testingReadFloatExecutor) {
        ModbusExecutorFactory.testingReadFloatExecutor = testingReadFloatExecutor;
    }

    public static void setTestingWriteBooleanExecutor(ModbusWriteTransactionExecutor testingWriteBooleanExecutor) {
        ModbusExecutorFactory.testingWriteBooleanExecutor = testingWriteBooleanExecutor;
    }

    public static void setTestingWriteIntegerExecutor(ModbusWriteTransactionExecutor testingWriteIntegerExecutor) {
        ModbusExecutorFactory.testingWriteIntegerExecutor = testingWriteIntegerExecutor;
    }


    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, ModbusReadRegisterType type,
                                                                String address) {
        return getReadExecutor(applianceId, type, address, 1, ByteOrder.BigEndian, 1.0);
    }

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, ModbusReadRegisterType type,
                                                                String address, int bytes) {
        return getReadExecutor(applianceId, type, address, bytes, ByteOrder.BigEndian, 1.0);
    }

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, ModbusReadRegisterType type,
                                                                String address, int bytes, ByteOrder byteOrder,
                                                                Double factorToValue) {
        ModbusReadTransactionExecutor executor;
        switch (type) {
            case InputString:
                if(testingReadStringExecutor != null) {
                    executor = testingReadStringExecutor;
                }
                else {
                    executor = new ReadStringInputRegisterExecutorImpl(address, bytes);
                }
                break;
            case InputFloat:
                if(testingReadFloatExecutor != null) {
                    executor = testingReadFloatExecutor;
                }
                else {
                    executor = new ReadFloatInputRegisterExecutorImpl(address, bytes);
                }
                break;
            case InputDecimal:
                if(testingReadFloatExecutor != null) {
                    executor = testingReadFloatExecutor;
                }
                else {
                    executor = new ReadDecimalInputRegisterExecutorImpl(address, bytes, byteOrder, factorToValue);
                }
                break;
            case Coil:
                if(testingReadBooleanExecutor != null) {
                    executor = testingReadBooleanExecutor;
                }
                else {
                    executor = new ReadCoilExecutorImpl(address);
                }
                break;
            case Discrete:
                if(testingReadBooleanExecutor != null) {
                    executor = testingReadBooleanExecutor;
                }
                else {
                    executor = new ReadDiscreteInputExecutorImpl(address);
                }
                break;
            default:
                return null;
        }
        executor.setApplianceId(applianceId);
        return executor;
    }

    public static ModbusWriteTransactionExecutor getWriteExecutor(String applianceId, ModbusWriteRegisterType type, String address) {
        ModbusWriteTransactionExecutor executor;
        switch (type) {
            case Holding:
                if(testingWriteIntegerExecutor != null) {
                    executor = testingWriteIntegerExecutor;
                }
                else {
                    executor = new WriteHoldingRegisterExecutorImpl(address);
                }
                break;
            case Coil:
                if(testingWriteBooleanExecutor != null) {
                    executor = testingWriteBooleanExecutor;
                }
                else {
                    executor = new WriteCoilExecutorImpl(address);
                }
                break;
            default:
                return null;
        }
        executor.setApplianceId(applianceId);
        return executor;
    }
}
