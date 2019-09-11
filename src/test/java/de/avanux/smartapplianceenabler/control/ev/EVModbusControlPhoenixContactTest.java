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
        this.evModbusControl.setPollInterval(10);

        ModbusRead register100 = new ModbusRead();
        register100.setType(ReadRegisterType.InputString.name());
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
    public void isVehicleNotConnected_A() {
        this.readStringTestingExecutor.setValue("A");
        Assert.assertTrue(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleNotConnected_B() {
        this.readStringTestingExecutor.setValue("B");
        Assert.assertFalse(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleNotConnected_C() {
        this.readStringTestingExecutor.setValue("C");
        Assert.assertFalse(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleNotConnected_D() {
        this.readStringTestingExecutor.setValue("D");
        Assert.assertFalse(this.evModbusControl.isVehicleNotConnected());
    }

    @Test
    public void isVehicleConnected_A() {
        this.readStringTestingExecutor.setValue("A");
        Assert.assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_B() {
        this.readStringTestingExecutor.setValue("B");
        Assert.assertTrue(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_C() {
        this.readStringTestingExecutor.setValue("C");
        Assert.assertFalse(this.evModbusControl.isVehicleConnected());
    }

    @Test
    public void isVehicleConnected_D() {
        this.readStringTestingExecutor.setValue("D");
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
