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

import de.avanux.smartapplianceenabler.modbus.ModbusReadRegisterType;
import de.avanux.smartapplianceenabler.modbus.ModbusWriteRegisterType;

public class ModbusExecutorFactory {

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, ModbusReadRegisterType type, String address) {
        return getReadExecutor(applianceId, type, address, 1);
    }

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, ModbusReadRegisterType type, String address, int bytes) {
        ModbusReadTransactionExecutor executor;
        switch (type) {
            case InputString:
                executor = new ReadStringInputRegisterExecutor(address, bytes);
                break;
            case InputFloat:
                executor = new ReadFloatInputRegisterExecutor(address, bytes);
                break;
            case Coil:
                executor = new ReadCoilExecutor(address);
                break;
            case Discrete:
                executor = new ReadDiscreteInputExecutor(address);
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
                executor = new WriteHoldingRegisterExecutor(address);
                break;
            case Coil:
                executor = new WriteCoilExecutor(address);
                break;
            default:
                return null;
        }
        executor.setApplianceId(applianceId);
        return executor;
    }
}
