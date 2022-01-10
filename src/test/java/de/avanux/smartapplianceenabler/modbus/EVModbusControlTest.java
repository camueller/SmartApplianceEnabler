/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.control.ev.EVReadValueName;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusExecutorFactory;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusReadTransactionExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusWriteHoldingTestingExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ReadInputRegisterExecutor;
import de.avanux.smartapplianceenabler.modbus.transformer.StringValueTransformer;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EVModbusControlTest {
    private EVModbusControl evModbusControl;
    private ModbusWriteHoldingTestingExecutor writeHoldingTestingExecutor;

    public EVModbusControlTest() {
        this.evModbusControl = new EVModbusControl();
        this.evModbusControl.setApplianceId("F-001");
        this.evModbusControl.setPollInterval(10);

        this.writeHoldingTestingExecutor = new ModbusWriteHoldingTestingExecutor();
        ModbusExecutorFactory.setWriteHoldingExecutor(this.writeHoldingTestingExecutor);
    }

    @Test
    public void isMatchingVehicleStatus_trueUsingCache() {
        isMatchingVehicleStatusUsingCache(true, EVReadValueName.VehicleConnected,
                "(B)", new Integer[]{66});
    }

    @Test
    public void isMatchingVehicleStatus_falseUsingCache() {
        isMatchingVehicleStatusUsingCache(false, EVReadValueName.VehicleConnected,
                "(B)", new Integer[]{65});
    }

    private void isMatchingVehicleStatusUsingCache(boolean expectedResult, EVReadValueName registerName,
                                                   String extractionRegex, Integer[] byteValues) {
        ModbusRead registerRead = new ModbusRead();
        registerRead.setAddress("42");
        registerRead.setType(ReadRegisterType.Input.name());
        registerRead.setValueType(RegisterValueType.String.name());
        registerRead.setWords(1);

        ModbusReadValue registerReadValue = new ModbusReadValue(registerName.name(), extractionRegex);
        registerRead.setReadValues(Collections.singletonList(registerReadValue));

        this.evModbusControl.setModbusReads(Collections.singletonList(registerRead));
        this.evModbusControl.init();

        ModbusReadTransactionExecutor executor
                = new ReadInputRegisterExecutor(registerRead.getAddress(), byteValues.length, new StringValueTransformer());
        executor.getValueTransformer().setByteValues(byteValues);
        this.evModbusControl.getRequestCache().put(registerRead, executor);

        assertEquals(expectedResult, this.evModbusControl.isMatchingVehicleStatus(registerName, false));
    }
}
