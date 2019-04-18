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

package de.avanux.smartapplianceenabler.control.ev.http;

import de.avanux.smartapplianceenabler.control.ev.EVModbusReadRegisterName;
import de.avanux.smartapplianceenabler.control.ev.EVModbusWriteRegisterName;
import de.avanux.smartapplianceenabler.protocol.JsonProtocol;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EVHttpControlTest {

    private EVHttpControl control;

    public EVHttpControlTest() {
        this.control = new EVHttpControl(new JsonProtocol());
        List<HttpRead> reads = new ArrayList<>();
        this.control.setReads(reads);

        HttpRead statusRead = new HttpRead("http://192.168.1.1/status");
        reads.add(statusRead);
        List<HttpReadValue> readValues = new ArrayList<>();
        statusRead.setReadValues(readValues);
        readValues.add(new HttpReadValue(EVModbusReadRegisterName.VehicleNotConnected.name(), "$.car", "(1)"));
        readValues.add(new HttpReadValue(EVModbusReadRegisterName.VehicleConnected.name(), "$.car", "(3)"));
        readValues.add(new HttpReadValue(EVModbusReadRegisterName.Charging.name(), "$.car", "(2)"));
        readValues.add(new HttpReadValue(EVModbusReadRegisterName.ChargingCompleted.name(), "$.car", "(4)"));
        readValues.add(new HttpReadValue(EVModbusReadRegisterName.Error.name(), "$.err", "([^0])"));

        List<HttpWrite> writes = new ArrayList<>();
        this.control.setWrites(writes);
        HttpWrite cmdWrite = new HttpWrite("http://192.168.1.1/mqtt=");
        writes.add(cmdWrite);
        List<HttpWriteValue> writeValues = new ArrayList<>();
        cmdWrite.setWriteValues(writeValues);
        writeValues.add(new HttpWriteValue(
                EVModbusWriteRegisterName.ChargingCurrent.name(),
                "amp={0}",
                HttpWriteValueType.QueryParameter,
                HttpMethod.GET));
        writeValues.add(new HttpWriteValue(
                EVModbusWriteRegisterName.StartCharging.name(),
                "alw=1",
                HttpWriteValueType.QueryParameter,
                HttpMethod.GET));
        writeValues.add(new HttpWriteValue(
                EVModbusWriteRegisterName.StopCharging.name(),
                "alw=0",
                HttpWriteValueType.QueryParameter,
                HttpMethod.GET));
    }

    @Test
    public void isVehicleNotConnected() {
        this.control.parse("{ \"car\": \"1\" }");
        Assert.assertTrue(this.control.isVehicleNotConnected());
    }

    @Test
    public void isVehicleConnected() {
        this.control.parse("{ \"car\": \"3\" }");
        Assert.assertTrue(this.control.isVehicleConnected());
    }

    @Test
    public void isCharging() {
        this.control.parse("{ \"car\": \"2\" }");
        Assert.assertTrue(this.control.isCharging());
    }

    @Test
    public void isChargingCompleted() {
        this.control.parse("{ \"car\": \"4\" }");
        Assert.assertTrue(this.control.isChargingCompleted());
    }

    @Test
    public void isInErrorState_True() {
        this.control.parse("{ \"err\": \"1\" }");
        Assert.assertTrue(this.control.isInErrorState());
    }

    @Test
    public void isInErrorState_False() {
        this.control.parse("{ \"err\": \"0\" }");
        Assert.assertFalse(this.control.isInErrorState());
    }

    @Test
    public void startCharging() {
        this.control.startCharging();
        Assert.assertEquals(HttpMethod.GET, this.control.httpMethod);
        Assert.assertEquals("http://192.168.1.1/mqtt=alw=1", this.control.url);
    }

    @Test
    public void stopCharging() {
        this.control.stopCharging();
        Assert.assertEquals(HttpMethod.GET, this.control.httpMethod);
        Assert.assertEquals("http://192.168.1.1/mqtt=alw=0", this.control.url);
    }

    @Test
    public void setChargeCurrent() {
        this.control.setChargeCurrent(6);;
        Assert.assertEquals(HttpMethod.GET, this.control.httpMethod);
        Assert.assertEquals("http://192.168.1.1/mqtt=amp=6", this.control.url);
    }
}
