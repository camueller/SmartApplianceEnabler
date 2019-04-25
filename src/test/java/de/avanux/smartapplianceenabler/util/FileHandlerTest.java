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
import de.avanux.smartapplianceenabler.control.ev.EVModbusReadRegisterName;
import de.avanux.smartapplianceenabler.control.ev.EVModbusWriteRegisterName;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.control.ev.http.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class FileHandlerTest {

    private FileHandler fileHandler = new FileHandler();

    @Test
    public void load_GoECharger() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("GoECharger.xml");
        Assert.assertTrue(is.available() > 0);
        Appliances appliances = fileHandler.load(Appliances.class, is);
        Assert.assertNotNull(appliances);
        Appliance appliance = appliances.getAppliances().get(0);
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        EVHttpControl httpControl = (EVHttpControl) evCharger.getControl();

        List<HttpRead> reads = httpControl.getReads();
        Assert.assertEquals(1, reads.size());
        HttpRead read = reads.get(0);
        assertRead(read, "http://127.0.0.1:8999/status");
        List<HttpReadValue> readValues = read.getReadValues();
        Assert.assertEquals(5, readValues.size());
        assertReadValue(readValues.get(0), EVModbusReadRegisterName.VehicleNotConnected, "$.car", "(1)");
        assertReadValue(readValues.get(1), EVModbusReadRegisterName.VehicleConnected, "$.car", "(3)");
        assertReadValue(readValues.get(2), EVModbusReadRegisterName.Charging, "$.car", "(2)");
        assertReadValue(readValues.get(3), EVModbusReadRegisterName.ChargingCompleted, "$.car", "(4)");
        assertReadValue(readValues.get(4), EVModbusReadRegisterName.Error, "$.err", "([^0])");

        List<HttpWrite> writes = httpControl.getWrites();
        Assert.assertEquals(1, writes.size());
        HttpWrite write = writes.get(0);
        assertWrite(write, "http://127.0.0.1:8999/mqtt=");
        List<HttpWriteValue> writeValues = write.getWriteValues();
        Assert.assertEquals(3, writeValues.size());
        assertWriteValue(writeValues.get(0), EVModbusWriteRegisterName.ChargingCurrent.name(),"amp={}",
                HttpWriteValueType.QueryParameter, HttpMethod.GET);
        assertWriteValue(writeValues.get(1), EVModbusWriteRegisterName.StartCharging.name(),"alw=1",
                HttpWriteValueType.QueryParameter, HttpMethod.GET);
        assertWriteValue(writeValues.get(2), EVModbusWriteRegisterName.StopCharging.name(),"alw=0",
                HttpWriteValueType.QueryParameter, HttpMethod.GET);
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

}
