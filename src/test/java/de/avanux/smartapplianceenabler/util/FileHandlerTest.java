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
import de.avanux.smartapplianceenabler.control.ev.EVReadValueName;
import de.avanux.smartapplianceenabler.control.ev.EVWriteValueName;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.http.*;
import de.avanux.smartapplianceenabler.modbus.*;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileHandlerTest {

    private FileHandler fileHandler = new FileHandler();

    @Test
    public void load_PhoenixContact() throws Exception {
        Appliances appliances = loadAppliances("PhoenixContact.xml");
        assertNotNull(appliances);
        Appliance appliance = appliances.getAppliances().get(0);
        assertNotNull(appliance);
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        assertNotNull(evCharger);
        assertEquals(300, evCharger.getStartChargingStateDetectionDelay());

        EVModbusControl modbusControl = (EVModbusControl) evCharger.getControl();
        assertNotNull(modbusControl);

        List<ModbusRead> registerReads = modbusControl.getModbusReads();
        assertEquals(1, registerReads.size());

        ModbusRead r100 = registerReads.get(0);
        assertModbusRead(r100, "100", ReadRegisterType.Input, RegisterValueType.String);
        List<ModbusReadValue> r100Values = r100.getReadValues();
        assertEquals(4, r100Values.size());
        assertModbusReadValue(r100Values.get(0), EVReadValueName.VehicleNotConnected, "(A)");
        assertModbusReadValue(r100Values.get(1), EVReadValueName.VehicleConnected, "(B)");
        assertModbusReadValue(r100Values.get(2), EVReadValueName.Charging, "(C|D)");
        assertModbusReadValue(r100Values.get(3), EVReadValueName.Error, "(E|F)");

        List<ModbusWrite> registerWrites = modbusControl.getModbusWrites();
        ModbusWrite r400 = registerWrites.get(0);
        assertModbusRegisterWrite(r400, "400", WriteRegisterType.Coil);
        List<ModbusWriteValue> r400Values = r400.getWriteValues();
        assertEquals(2, r400Values.size());
        assertModbusRegisterWriteValue(r400Values.get(0), EVWriteValueName.StartCharging, "1");
        assertModbusRegisterWriteValue(r400Values.get(1), EVWriteValueName.StopCharging, "0");

        ModbusWrite r300 = registerWrites.get(1);
        assertModbusRegisterWrite(r300, "300", WriteRegisterType.Holding);
        List<ModbusWriteValue> r300Values = r300.getWriteValues();
        assertEquals(1, r300Values.size());
        assertModbusRegisterWriteValue(r300Values.get(0), EVWriteValueName.ChargingCurrent, "0");

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
        assertNotNull(appliances);
        Appliance appliance = appliances.getAppliances().get(0);
        assertNotNull(appliance);
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        assertNotNull(evCharger);

        EVHttpControl httpControl = (EVHttpControl) evCharger.getControl();
        assertNotNull(httpControl);

        List<HttpRead> reads = httpControl.getHttpReads();
        assertEquals(1, reads.size());
        HttpRead read = reads.get(0);
        assertRead(read, "http://192.168.1.1/status");
        List<HttpReadValue> readValues = read.getReadValues();
        assertEquals(4, readValues.size());
        assertReadValue(readValues.get(0), EVReadValueName.VehicleNotConnected, "$.car", "(1)");
        assertReadValue(readValues.get(1), EVReadValueName.VehicleConnected, "$.car", "(3|4)");
        assertReadValue(readValues.get(2), EVReadValueName.Charging, "$.car", "(2)");
        assertReadValue(readValues.get(3), EVReadValueName.Error, "$.err", "([^0])");

        List<HttpWrite> writes = httpControl.getHttpWrites();
        assertEquals(1, writes.size());
        HttpWrite write = writes.get(0);
        assertWrite(write, "http://192.168.1.1/mqtt?payload=");
        List<HttpWriteValue> writeValues = write.getWriteValues();
        assertEquals(3, writeValues.size());
        assertWriteValue(writeValues.get(0), EVWriteValueName.ChargingCurrent.name(),"amp={0}", HttpMethod.GET);
        assertWriteValue(writeValues.get(1), EVWriteValueName.StartCharging.name(),"alw=1", HttpMethod.GET);
        assertWriteValue(writeValues.get(2), EVWriteValueName.StopCharging.name(),"alw=0", HttpMethod.GET);

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
        assertTrue(is.available() > 0);
        Appliances appliances = fileHandler.load(Appliances.class, is, null);
        is.close();
        return appliances;
    }

    private void assertRead(HttpRead read, String url) {
        assertEquals(url, read.getUrl());
    }

    private void assertReadValue(HttpReadValue readValue, EVReadValueName name, String path, String extractionRegex) {
        assertEquals(name.name(), readValue.getName());
        assertEquals(path, readValue.getPath());
        assertEquals(extractionRegex, readValue.getExtractionRegex());
    }

    private void assertWrite(HttpWrite write, String url) {
        assertEquals(url, write.getUrl());
    }

    private void assertWriteValue(HttpWriteValue writeValue, String name, String value, HttpMethod method) {
        assertEquals(name, writeValue.getName());
        assertEquals(value, writeValue.getValue());
        assertEquals(method, writeValue.getMethod());
    }

    private void assertModbusRead(ModbusRead registerRead, String address, ReadRegisterType registerType, RegisterValueType registerValueType) {
        assertEquals(address, registerRead.getAddress());
        assertEquals(registerType, registerRead.getType());
        assertEquals(registerValueType, registerRead.getValueType());
    }

    private void assertModbusReadValue(ModbusReadValue registerReadValue, EVReadValueName name, String extractionRegex) {
        assertEquals(name.name(), registerReadValue.getName());
        if(extractionRegex != null) {
            assertEquals(extractionRegex, registerReadValue.getExtractionRegex());
        }
    }

    private void assertModbusRegisterWrite(ModbusWrite registerWrite, String address, WriteRegisterType registerType) {
        assertEquals(address, registerWrite.getAddress());
        assertEquals(registerType, registerWrite.getType());
    }

    private void assertModbusRegisterWriteValue(ModbusWriteValue registerWriteValue, EVWriteValueName name, String value) {
        assertEquals(name.name(), registerWriteValue.getName());
        if(value != null) {
            assertEquals(value, registerWriteValue.getValue());
        }
    }

    private void assertElectricVehicle(ElectricVehicle ev, Integer id, String name, Integer batteryCapacity,
                                       Integer phases, Integer maxChargePower, Integer chargeLoss,
                                       Integer defaultSocManual, Integer defaultSocOptionalEnergy,
                                       String socScript, String socExtractionRegex) {
        assertEquals(id, ev.getId());
        assertEquals(name, ev.getName());
        assertEquals(batteryCapacity, ev.getBatteryCapacity());
        assertEquals(phases, ev.getPhases());
        assertEquals(maxChargePower, ev.getMaxChargePower());
        assertEquals(chargeLoss, ev.getChargeLoss());
        assertEquals(defaultSocManual, ev.getDefaultSocManual());
        assertEquals(defaultSocOptionalEnergy, ev.getDefaultSocOptionalEnergy());
        if(socScript != null) {
            assertEquals(socScript, ev.getSocScript().getScript());
        }
        if(socExtractionRegex != null) {
            assertEquals(socExtractionRegex, ev.getSocScript().getExtractionRegex());
        }
    }
}
