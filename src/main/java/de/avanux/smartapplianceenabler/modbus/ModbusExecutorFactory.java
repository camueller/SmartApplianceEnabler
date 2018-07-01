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

public class ModbusExecutorFactory {

    public static ModbusReadTransactionExecutor getReadExecutor(String applianceId, ModbusRegisterType type, String address, int bytes) {
        ModbusReadTransactionExecutor executor;
        switch (type) {
            case Input:
                executor = new ReadInputRegisterExecutor(address, bytes);
                break;
            default:
                return null;
        }
        executor.setApplianceId(applianceId);
        return executor;
    }

    public static ModbusWriteTransactionExecutor getWriteExecutor(String applianceId, ModbusRegisterType type, String address) {
        ModbusWriteTransactionExecutor executor;
        switch (type) {
            case Holding:
                executor = new WriteHoldingRegisterExecutor(address);
                break;
            default:
                return null;
        }
        executor.setApplianceId(applianceId);
        return executor;
    }
}
