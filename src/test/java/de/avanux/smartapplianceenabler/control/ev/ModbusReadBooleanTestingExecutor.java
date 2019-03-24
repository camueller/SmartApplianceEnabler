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

package de.avanux.smartapplianceenabler.control.ev;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusReadTransactionExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusTestingExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ReadCoilExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ReadDiscreteInputExecutor;

public class ModbusReadBooleanTestingExecutor implements ModbusReadTransactionExecutor<Boolean>,
        ReadCoilExecutor, ReadDiscreteInputExecutor, ModbusTestingExecutor {

    private Boolean value;

    @Override
    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {

    }

    @Override
    public void setApplianceId(String applianceId) {

    }
}
