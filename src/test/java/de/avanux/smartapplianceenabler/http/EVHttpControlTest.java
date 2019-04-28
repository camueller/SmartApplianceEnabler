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

package de.avanux.smartapplianceenabler.http;

import de.avanux.smartapplianceenabler.control.ev.EVReadValueName;
import de.avanux.smartapplianceenabler.control.ev.EVWriteValueName;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class EVHttpControlTest {

    private static String BASE_URL = "http://127.0.0.1:8999";
    private EVHttpControl control;
    private HttpRead statusReadSpy;
    private HttpWrite cmdWriteSpy;

    public EVHttpControlTest() {
        this.control = new EVHttpControl();
        this.control.setContentProtocol(ContentProtocolType.json);
        List<HttpRead> reads = new ArrayList<>();
        this.control.setHttpReads(reads);

        statusReadSpy = Mockito.spy(new HttpRead(BASE_URL + "/status"));
        reads.add(statusReadSpy);
        List<HttpReadValue> readValues = new ArrayList<>();
        statusReadSpy.setReadValues(readValues);
        readValues.add(new HttpReadValue(EVReadValueName.VehicleNotConnected.name(), "$.car", "(1)"));
        readValues.add(new HttpReadValue(EVReadValueName.VehicleConnected.name(), "$.car", "(3)"));
        readValues.add(new HttpReadValue(EVReadValueName.Charging.name(), "$.car", "(2)"));
        readValues.add(new HttpReadValue(EVReadValueName.ChargingCompleted.name(), "$.car", "(4)"));
        readValues.add(new HttpReadValue(EVReadValueName.Error.name(), "$.err", "([^0])"));

        List<HttpWrite> writes = new ArrayList<>();
        this.control.setHttpWrites(writes);
        cmdWriteSpy =  Mockito.spy(new HttpWrite(BASE_URL + "/mqtt="));
        writes.add(cmdWriteSpy);
        List<HttpWriteValue> writeValues = new ArrayList<>();
        cmdWriteSpy.setWriteValues(writeValues);
        writeValues.add(new HttpWriteValue(
                EVWriteValueName.ChargingCurrent.name(),
                "amp={0}",
                HttpWriteValueType.QueryParameter,
                HttpMethod.GET));
        writeValues.add(new HttpWriteValue(
                EVWriteValueName.StartCharging.name(),
                "alw=1",
                HttpWriteValueType.QueryParameter,
                HttpMethod.GET));
        writeValues.add(new HttpWriteValue(
                EVWriteValueName.StopCharging.name(),
                "alw=0",
                HttpWriteValueType.QueryParameter,
                HttpMethod.GET));

        this.control.setApplianceId("F-001");
    }

    @Test
    public void isVehicleNotConnected() {
        Mockito.doReturn("{ \"car\": \"1\" }").when(statusReadSpy).executeGet(Mockito.any());
        Assert.assertTrue(this.control.isVehicleNotConnected());
    }

    @Test
    public void isVehicleConnected() {
        Mockito.doReturn("{ \"car\": \"3\" }").when(statusReadSpy).executeGet(Mockito.any());
        Assert.assertTrue(this.control.isVehicleConnected());
    }

    @Test
    public void isCharging() {
        Mockito.doReturn("{ \"car\": \"2\" }").when(statusReadSpy).executeGet(Mockito.any());
        Assert.assertTrue(this.control.isCharging());
    }

    @Test
    public void isChargingCompleted() {
        Mockito.doReturn("{ \"car\": \"4\" }").when(statusReadSpy).executeGet(Mockito.any());
        Assert.assertTrue(this.control.isChargingCompleted());
    }

    @Test
    public void isInErrorState_True() {
        Mockito.doReturn("{ \"err\": \"1\" }").when(statusReadSpy).executeGet(Mockito.any());
        Assert.assertTrue(this.control.isInErrorState());
    }

    @Test
    public void isInErrorState_False() {
        Mockito.doReturn("{ \"err\": \"0\" }").when(statusReadSpy).executeGet(Mockito.any());
        Assert.assertFalse(this.control.isInErrorState());
    }

    @Test
    public void startCharging() {
        Mockito.doReturn("this is the START CHARGING response").when(cmdWriteSpy).executeGet(Mockito.any());
        this.control.startCharging();
        Mockito.verify(cmdWriteSpy).executeGet(BASE_URL + "/mqtt=alw=1");
    }

    @Test
    public void stopCharging() {
        Mockito.doReturn("this is the STOP CHARGING response").when(cmdWriteSpy).executeGet(Mockito.any());
        this.control.stopCharging();
        Mockito.verify(cmdWriteSpy).executeGet(BASE_URL + "/mqtt=alw=0");
    }

    @Test
    public void setChargeCurrent() {
        Mockito.doReturn("this is the SET CHARGE CURRENT response").when(cmdWriteSpy).executeGet(Mockito.any());
        this.control.setChargeCurrent(6);;
        Mockito.verify(cmdWriteSpy).executeGet(BASE_URL + "/mqtt=amp=6");
    }
}
