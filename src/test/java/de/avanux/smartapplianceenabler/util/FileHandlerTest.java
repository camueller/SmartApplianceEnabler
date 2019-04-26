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

package de.avanux.smartapplianceenabler.util;

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.Appliances;
import de.avanux.smartapplianceenabler.control.ev.*;
import de.avanux.smartapplianceenabler.control.ev.http.*;
import de.avanux.smartapplianceenabler.modbus.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class FileHandlerTest {

    private FileHandler fileHandler = new FileHandler();

    @Test
    public void load_PhoenixContact() throws Exception {
        Appliances appliances = loadAppliances("PhoenixContact.xml");
        Assert.assertNotNull(appliances);
        Appliance appliance = appliances.getAppliances().get(0);
        Assert.assertNotNull(appliance);
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        Assert.assertNotNull(evCharger);
//        EVModbusControl modbusControl = (EVModbusControl) evCharger.getControl();
//        Assert.assertNotNull(modbusControl);
//
//        List<ModbusRegisterRead> registerReads = modbusControl.getRegisterReads();
//        Assert.assertEquals(2, registerReads.size());
//
//        ModbusRegisterRead r100 = registerReads.get(0);
//        assertModbusRegisterRead(r100, "100", ModbusReadRegisterType.InputString);
//        List<ModbusRegisterReadValue> r100Values = r100.getRegisterReadValues();
//        Assert.assertEquals(5, r100Values.size());
//        assertModbusRegisterReadValue(r100Values.get(0), EVModbusReadRegisterName.VehicleNotConnected, "(A)");
//        assertModbusRegisterReadValue(r100Values.get(1), EVModbusReadRegisterName.VehicleConnected, "(B)");
//        assertModbusRegisterReadValue(r100Values.get(2), EVModbusReadRegisterName.Charging, "(C|D)");
//        assertModbusRegisterReadValue(r100Values.get(3), EVModbusReadRegisterName.ChargingCompleted, "(B)");
//        assertModbusRegisterReadValue(r100Values.get(4), EVModbusReadRegisterName.Error, "(E|F)");
//
//        ModbusRegisterRead r204 = registerReads.get(1);
//        assertModbusRegisterRead(r204, "204", ModbusReadRegisterType.Discrete);
//        List<ModbusRegisterReadValue> r204Values = r204.getRegisterReadValues();
//        Assert.assertEquals(1, r204Values.size());
//        assertModbusRegisterReadValue(r204Values.get(0), EVModbusReadRegisterName.ChargingCompleted, null);
//
//        List<ModbusRegisterWrite> registerWrites = modbusControl.getRegisterWrites();
//        ModbusRegisterWrite r400 = registerWrites.get(0);
//        assertModbusRegisterWrite(r400, "400", ModbusWriteRegisterType.Coil);
//        List<ModbusRegisterWriteValue> r400Values = r400.getRegisterWriteValues();
//        Assert.assertEquals(2, r400Values.size());
//        assertModbusRegisterWriteValue(r400Values.get(0), EVModbusWriteRegisterName.StartCharging, "1");
//        assertModbusRegisterWriteValue(r400Values.get(1), EVModbusWriteRegisterName.StopCharging, "0");
//
//        ModbusRegisterWrite r300 = registerWrites.get(1);
//        assertModbusRegisterWrite(r300, "300", ModbusWriteRegisterType.Holding);
//        List<ModbusRegisterWriteValue> r300Values = r300.getRegisterWriteValues();
//        Assert.assertEquals(1, r300Values.size());
//        assertModbusRegisterWriteValue(r300Values.get(0), EVModbusWriteRegisterName.ChargingCurrent, "0");

        List<ElectricVehicle> vehicles = evCharger.getVehicles();
        assertElectricVehicle(vehicles.get(0), 1, "Nissan Leaf", 40000, 1,
                10000, 20, 100, 90,
                "/home/axel/IdeaProjects/SmartApplianceEnabler/src/test/soc.sh",
                ".*is (\\d*.{0,1}\\d+).*");
        assertElectricVehicle(vehicles.get(1), 2, "Tesla Model S", 80000, null,
                null, 10, null, null,
                null,
                null);
    }

    @Test
    public void load_GoECharger() throws Exception {
        Appliances appliances = loadAppliances("GoECharger.xml");
        Assert.assertNotNull(appliances);
        Appliance appliance = appliances.getAppliances().get(0);
        Assert.assertNotNull(appliance);
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        Assert.assertNotNull(evCharger);
//        EVHttpControl httpControl = (EVHttpControl) evCharger.getControl();
//        Assert.assertNotNull(httpControl);
//
//        List<HttpRead> reads = httpControl.getReads();
//        Assert.assertEquals(1, reads.size());
//        HttpRead read = reads.get(0);
//        assertRead(read, "http://127.0.0.1:8999/status");
//        List<HttpReadValue> readValues = read.getReadValues();
//        Assert.assertEquals(5, readValues.size());
//        assertReadValue(readValues.get(0), EVModbusReadRegisterName.VehicleNotConnected, "$.car", "(1)");
//        assertReadValue(readValues.get(1), EVModbusReadRegisterName.VehicleConnected, "$.car", "(3)");
//        assertReadValue(readValues.get(2), EVModbusReadRegisterName.Charging, "$.car", "(2)");
//        assertReadValue(readValues.get(3), EVModbusReadRegisterName.ChargingCompleted, "$.car", "(4)");
//        assertReadValue(readValues.get(4), EVModbusReadRegisterName.Error, "$.err", "([^0])");
//
//        List<HttpWrite> writes = httpControl.getWrites();
//        Assert.assertEquals(1, writes.size());
//        HttpWrite write = writes.get(0);
//        assertWrite(write, "http://127.0.0.1:8999/mqtt=");
//        List<HttpWriteValue> writeValues = write.getWriteValues();
//        Assert.assertEquals(3, writeValues.size());
//        assertWriteValue(writeValues.get(0), EVModbusWriteRegisterName.ChargingCurrent.name(),"amp={}",
//                HttpWriteValueType.QueryParameter, HttpMethod.GET);
//        assertWriteValue(writeValues.get(1), EVModbusWriteRegisterName.StartCharging.name(),"alw=1",
//                HttpWriteValueType.QueryParameter, HttpMethod.GET);
//        assertWriteValue(writeValues.get(2), EVModbusWriteRegisterName.StopCharging.name(),"alw=0",
//                HttpWriteValueType.QueryParameter, HttpMethod.GET);

        List<ElectricVehicle> vehicles = evCharger.getVehicles();
        assertElectricVehicle(vehicles.get(0), 1, "Nissan Leaf", 40000, 1,
                10000, 20, 100, 90,
                "/home/axel/IdeaProjects/SmartApplianceEnabler/src/test/soc.sh",
                ".*is (\\d*.{0,1}\\d+).*");
        assertElectricVehicle(vehicles.get(1), 2, "Tesla Model S", 80000, null,
                null, 10, null, null,
                null,
                null);
    }

    private Appliances loadAppliances(String filename) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        Assert.assertTrue(is.available() > 0);
        Appliances appliances = fileHandler.load(Appliances.class, is);
        is.close();
        return appliances;
    }

    private void assertRead(HttpRead read, String url) {
        Assert.assertEquals(url, read.getUrl());
    }

    private void assertReadValue(HttpReadValue readValue, EVModbusReadRegisterName name, String path, String extractionRegex) {
        Assert.assertEquals(name.name(), readValue.getName());
        Assert.assertEquals(path, readValue.getPath());
        Assert.assertEquals(extractionRegex, readValue.getExtractionRegex());
    }

    private void assertWrite(HttpWrite write, String url) {
        Assert.assertEquals(url, write.getUrl());
    }

    private void assertWriteValue(HttpWriteValue writeValue, String name, String value, HttpWriteValueType type, HttpMethod method) {
        Assert.assertEquals(name, writeValue.getName());
        Assert.assertEquals(value, writeValue.getValue());
        Assert.assertEquals(type, writeValue.getType());
        Assert.assertEquals(method, writeValue.getMethod());
    }

    private void assertModbusRegisterRead(ModbusRegisterRead registerRead, String address, ModbusReadRegisterType registerType) {
        Assert.assertEquals(address, registerRead.getAddress());
        Assert.assertEquals(registerType, registerRead.getType());
    }

    private void assertModbusRegisterReadValue(ModbusRegisterReadValue registerReadValue, EVModbusReadRegisterName name, String extractionRegex) {
        Assert.assertEquals(name.name(), registerReadValue.getName());
        if(extractionRegex != null) {
            Assert.assertEquals(extractionRegex, registerReadValue.getExtractionRegex());
        }
    }

    private void assertModbusRegisterWrite(ModbusRegisterWrite registerWrite, String address, ModbusWriteRegisterType registerType) {
        Assert.assertEquals(address, registerWrite.getAddress());
        Assert.assertEquals(registerType, registerWrite.getType());
    }

    private void assertModbusRegisterWriteValue(ModbusRegisterWriteValue registerWriteValue, EVModbusWriteRegisterName name, String value) {
        Assert.assertEquals(name.name(), registerWriteValue.getName());
        if(value != null) {
            Assert.assertEquals(value, registerWriteValue.getValue());
        }
    }

    private void assertElectricVehicle(ElectricVehicle ev, Integer id, String name, Integer batteryCapacity,
                                       Integer phases, Integer maxChargePower, Integer chargeLoss,
                                       Integer defaultSocManual, Integer defaultSocOptionalEnergy,
                                       String socScript, String socExtractionRegex) {
        Assert.assertEquals(id, ev.getId());
        Assert.assertEquals(name, ev.getName());
        Assert.assertEquals(batteryCapacity, ev.getBatteryCapacity());
        Assert.assertEquals(phases, ev.getPhases());
        Assert.assertEquals(maxChargePower, ev.getMaxChargePower());
        Assert.assertEquals(chargeLoss, ev.getChargeLoss());
        Assert.assertEquals(defaultSocManual, ev.getDefaultSocManual());
        Assert.assertEquals(defaultSocOptionalEnergy, ev.getDefaultSocOptionalEnergy());
        if(socScript != null) {
            Assert.assertEquals(socScript, ev.getSocScript().getScript());
        }
        if(socExtractionRegex != null) {
            Assert.assertEquals(socExtractionRegex, ev.getSocScript().getExtractionRegex());
        }
    }
}
