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

import de.avanux.smartapplianceenabler.modbus.*;
import de.avanux.smartapplianceenabler.modbus.executor.*;
import de.avanux.smartapplianceenabler.modbus.transformer.StringValueTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EVModbusControlPhoenixContactTest {

    private EVModbusControl evModbusControl;
    private StringValueTransformer strinValueTransformer;
    private ModbusReadTransactionExecutor readInputExecutor;
    private ModbusWriteCoilTestingExecutor writeCoilExecutor;
    private ModbusWriteHoldingTestingExecutor writeHoldingExecutor;

    @BeforeEach
    public void beforeEach() throws Exception {
        this.evModbusControl = new EVModbusControl();
        this.evModbusControl.setApplianceId("F-001-01");
        this.evModbusControl.setPollInterval(10);

        ModbusRead register100 = new ModbusRead();
        register100.setType(ReadRegisterType.Input.name());
        register100.setValueType(RegisterValueType.String.name());
        List<ModbusReadValue> register100ReadValues = new ArrayList<>();
        register100ReadValues.add(new ModbusReadValue("VehicleNotConnected", "(A)"));
        register100ReadValues.add(new ModbusReadValue("VehicleConnected", "(B)"));
        register100ReadValues.add(new ModbusReadValue("Charging", "(C|D)"));
        register100ReadValues.add(new ModbusReadValue("ChargingCompleted", "(B)"));
        register100ReadValues.add(new ModbusReadValue("Error", "(E|F)"));
        register100.setReadValues(register100ReadValues);

        ModbusRead register204 = new ModbusRead();
        register204.setType(ReadRegisterType.Discrete.name());
        register204.setReadValues(Collections.singletonList(
                new ModbusReadValue("ChargingCompleted", null)));

        ModbusWrite register400 = new ModbusWrite();
        register400.setType(WriteRegisterType.Coil.name());
        List<ModbusWriteValue> register400WriteValues = new ArrayList<>();
        register400WriteValues.add(new ModbusWriteValue("StartCharging", "1"));
        register400WriteValues.add(new ModbusWriteValue("StopCharging", "0"));
        register400.setWriteValues(register400WriteValues);

        ModbusWrite register300 = new ModbusWrite();
        register300.setType(WriteRegisterType.Holding.name());
        register300.setWriteValues(Collections.singletonList(
                new ModbusWriteValue("ChargingCurrent", "0")));

        this.evModbusControl.setModbusReads(Arrays.asList(register100, register204));
        this.evModbusControl.setModbusWrites(Arrays.asList(register400, register300));
        this.evModbusControl.init();
        this.evModbusControl.validate();

        strinValueTransformer = new StringValueTransformer();

        this.readInputExecutor = mock(ModbusReadInputTestingExecutor.class);
        when(readInputExecutor.getValueTransformer()).thenReturn(strinValueTransformer);
        ModbusExecutorFactory.setReadInputExecutor(this.readInputExecutor);

        this.writeCoilExecutor = new ModbusWriteCoilTestingExecutor();
        ModbusExecutorFactory.setWriteCoilExecutor(this.writeCoilExecutor);

        this.writeHoldingExecutor = new ModbusWriteHoldingTestingExecutor();
        ModbusExecutorFactory.setWriteHoldingExecutor(this.writeHoldingExecutor);
    }

    @Test
    public void isVehicleNotConnected_A() {
        strinValueTransformer.setByteValues(toIntegerArray("A"));
        assertTrue(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleNotConnected_B() {
        strinValueTransformer.setByteValues(toIntegerArray("B"));
        assertFalse(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleNotConnected_C() {
        strinValueTransformer.setByteValues(toIntegerArray("C"));
        assertFalse(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleNotConnected_D() {
        strinValueTransformer.setByteValues(toIntegerArray("D"));
        assertFalse(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleConnected_A() {
        strinValueTransformer.setByteValues(toIntegerArray("A"));
        assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_B() {
        strinValueTransformer.setByteValues(toIntegerArray("B"));
        assertTrue(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_C() {
        strinValueTransformer.setByteValues(toIntegerArray("C"));
        assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_D() {
        strinValueTransformer.setByteValues(toIntegerArray("D"));
        assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isCharging_C() {
        strinValueTransformer.setByteValues(toIntegerArray("C"));
        assertTrue(this.evModbusControl.isCharging());
    }

    @Test
    public void isCharging_D() {
        strinValueTransformer.setByteValues(toIntegerArray("D"));
        assertTrue(this.evModbusControl.isCharging());
    }

    @Test
    public void isCharging_A() {
        strinValueTransformer.setByteValues(toIntegerArray("A"));
        assertFalse(this.evModbusControl.isCharging());
    }

    @Test
    public void isCharging_B() {
        strinValueTransformer.setByteValues(toIntegerArray("B"));
        assertFalse(this.evModbusControl.isCharging());
    }

    @Test
    public void setChargeCurrent() {
        int chargeCurrent = 13;
        this.evModbusControl.setChargeCurrent(chargeCurrent);
        assertEquals(chargeCurrent, this.writeHoldingExecutor.getValue().intValue());
    }

    @Test
    public void startCharging() {
        this.evModbusControl.startCharging();
        assertTrue(this.writeCoilExecutor.getValue());
    }

    @Test
    public void stopCharging() {
        this.evModbusControl.startCharging();
        this.evModbusControl.stopCharging();
        assertFalse(this.writeCoilExecutor.getValue());
    }

    private Integer[] toIntegerArray(String input) {
        byte[] byteArray = input.getBytes();
        Integer[] intArray = new Integer[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = (int) byteArray[i];
        }
        return intArray;
    }
}
