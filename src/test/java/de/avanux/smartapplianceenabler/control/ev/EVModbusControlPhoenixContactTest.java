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
import de.avanux.smartapplianceenabler.modbus.executor.ModbusExecutorFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EVModbusControlPhoenixContactTest {

    private EVModbusControl evModbusControl;
    private ModbusReadStringTestingExecutor readStringTestingExecutor;
    private ModbusReadBooleanTestingExecutor readBooleanTestingExecutor;
    private ModbusReadFloatTestingExecutor readFloatTestingExecutor;
    private ModbusWriteBooleanTestingExecutor writeBooleanTestingExecutor;
    private ModbusWriteIntegerTestingExecutor writeIntegerTestingExecutor;

    public EVModbusControlPhoenixContactTest() {
        this.evModbusControl = new EVModbusControl();
        this.evModbusControl.setApplianceId("F-001-01");

        ModbusRegisterRead register100 = new ModbusRegisterRead();
        register100.setType(ModbusReadRegisterType.InputString.name());
        List<ModbusRegisterReadValue> register100ReadValues = new ArrayList<>();
        register100ReadValues.add(new ModbusRegisterReadValue("VehicleConnected", "(B)"));
        register100ReadValues.add(new ModbusRegisterReadValue("Charging", "(C|D)"));
        register100ReadValues.add(new ModbusRegisterReadValue("ChargingCompleted", "(B)"));
        register100.setRegisterReadValues(register100ReadValues);

        ModbusRegisterRead register204 = new ModbusRegisterRead();
        register204.setType(ModbusReadRegisterType.Discrete.name());
        register204.setRegisterReadValues(Collections.singletonList(
                new ModbusRegisterReadValue("ChargingCompleted", null)));

        ModbusRegisterWrite register400 = new ModbusRegisterWrite();
        register400.setType(ModbusWriteRegisterType.Coil.name());
        List<ModbusRegisterWriteValue> register400WriteValues = new ArrayList<>();
        register400WriteValues.add(new ModbusRegisterWriteValue("StartCharging", "1"));
        register400WriteValues.add(new ModbusRegisterWriteValue("StopCharging", "0"));
        register400.setRegisterWriteValues(register400WriteValues);

        ModbusRegisterWrite register300 = new ModbusRegisterWrite();
        register300.setType(ModbusWriteRegisterType.Holding.name());
        register300.setRegisterWriteValues(Collections.singletonList(
                new ModbusRegisterWriteValue("ChargingCurrent", "0")));

        evModbusControl.setRegisterReads(Arrays.asList(register100, register204));
        evModbusControl.setRegisterWrites(Arrays.asList(register400, register300));

        this.readStringTestingExecutor = new ModbusReadStringTestingExecutor();
        ModbusExecutorFactory.setTestingReadStringExecutor(this.readStringTestingExecutor);

        this.readBooleanTestingExecutor = new ModbusReadBooleanTestingExecutor();
        ModbusExecutorFactory.setTestingReadBooleanExecutor(this.readBooleanTestingExecutor);

        this.readFloatTestingExecutor = new ModbusReadFloatTestingExecutor();
        ModbusExecutorFactory.setTestingReadFloatExecutor(this.readFloatTestingExecutor);

        this.writeBooleanTestingExecutor = new ModbusWriteBooleanTestingExecutor();
        ModbusExecutorFactory.setTestingWriteBooleanExecutor(this.writeBooleanTestingExecutor);

        this.writeIntegerTestingExecutor = new ModbusWriteIntegerTestingExecutor();
        ModbusExecutorFactory.setTestingWriteIntegerExecutor(this.writeIntegerTestingExecutor);
    }



    @Test
    public void isVehicleConnected_B() {
        this.readStringTestingExecutor.setValue("B");
        Assert.assertTrue(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_A() {
        this.readStringTestingExecutor.setValue("A");
        Assert.assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_C() {
        this.readStringTestingExecutor.setValue("C");
        Assert.assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isCharging_C() {
        this.readStringTestingExecutor.setValue("C");
        Assert.assertTrue(this.evModbusControl.isCharging());
    }

    @Test
    public void isCharging_D() {
        this.readStringTestingExecutor.setValue("D");
        Assert.assertTrue(this.evModbusControl.isCharging());
    }

    @Test
    public void isCharging_A() {
        this.readStringTestingExecutor.setValue("A");
        Assert.assertFalse(this.evModbusControl.isCharging());
    }

    @Test
    public void isCharging_B() {
        this.readStringTestingExecutor.setValue("B");
        Assert.assertFalse(this.evModbusControl.isCharging());
    }

    @Test
    public void isChargingCompleted_B_ChargingCompleted1() {
        this.readStringTestingExecutor.setValue("B");
        this.readBooleanTestingExecutor.setValue(true);
        Assert.assertTrue(this.evModbusControl.isChargingCompleted());
    }

    @Test
    public void isChargingCompleted_C_ChargingCompleted1() {
        this.readStringTestingExecutor.setValue("C");
        this.readBooleanTestingExecutor.setValue(true);
        Assert.assertFalse(this.evModbusControl.isChargingCompleted());
    }

    @Test
    public void isChargingCompleted_A_ChargingCompleted1() {
        this.readStringTestingExecutor.setValue("A");
        this.readBooleanTestingExecutor.setValue(true);
        Assert.assertFalse(this.evModbusControl.isChargingCompleted());
    }

    @Test
    public void isChargingCompleted_B_ChargingCompleted0() {
        this.readStringTestingExecutor.setValue("B");
        this.readBooleanTestingExecutor.setValue(false);
        Assert.assertFalse(this.evModbusControl.isChargingCompleted());
    }

    @Test
    public void setChargeCurrent() {
        int chargeCurrent = 13;
        this.evModbusControl.setChargeCurrent(chargeCurrent);
        Assert.assertEquals(chargeCurrent, this.writeIntegerTestingExecutor.getValue().intValue());
    }

    @Test
    public void startCharging() {
        this.evModbusControl.startCharging();
        Assert.assertTrue(this.writeBooleanTestingExecutor.getValue());
    }

    @Test
    public void stopCharging() {
        this.evModbusControl.startCharging();
        this.evModbusControl.stopCharging();
        Assert.assertFalse(this.writeBooleanTestingExecutor.getValue());
    }
}
