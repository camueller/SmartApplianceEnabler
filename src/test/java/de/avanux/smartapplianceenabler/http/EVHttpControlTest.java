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

import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.control.ev.EVReadValueName;
import de.avanux.smartapplianceenabler.control.ev.EVWriteValueName;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EVHttpControlTest {

    private static String BASE_URL = "http://127.0.0.1:8999";
    private EVHttpControl control;
    private HttpTransactionExecutor executorMock;

    @BeforeEach
    public void setUp() throws ConfigurationException {
        this.executorMock = Mockito.mock(HttpTransactionExecutor.class);

        this.control = new EVHttpControl();
        this.control.setHttpTransactionExecutor(executorMock);
        this.control.setContentProtocol(ContentProtocolType.JSON);
        List<HttpRead> reads = new ArrayList<>();
        this.control.setHttpReads(reads);

        HttpRead read = new HttpRead(BASE_URL + "/status");
        reads.add(read);
        List<HttpReadValue> readValues = new ArrayList<>();
        read.setReadValues(readValues);
        readValues.add(new HttpReadValue(EVReadValueName.VehicleNotConnected.name(), "$.car", "(1)"));
        readValues.add(new HttpReadValue(EVReadValueName.VehicleConnected.name(), "$.car", "(3|4)"));
        readValues.add(new HttpReadValue(EVReadValueName.Charging.name(), "$.car", "(2)"));
        readValues.add(new HttpReadValue(EVReadValueName.Error.name(), "$.err", "([^0])"));

        List<HttpWrite> writes = new ArrayList<>();
        this.control.setHttpWrites(writes);
        HttpWrite write =  new HttpWrite(BASE_URL + "/mqtt=");
        writes.add(write);
        List<HttpWriteValue> writeValues = new ArrayList<>();
        write.setWriteValues(writeValues);
        writeValues.add(new HttpWriteValue(
                EVWriteValueName.ChargingCurrent.name(),
                "amp={0}",
                HttpMethod.GET));
        writeValues.add(new HttpWriteValue(
                EVWriteValueName.StartCharging.name(),
                "alw=1",
                HttpMethod.GET));
        writeValues.add(new HttpWriteValue(
                EVWriteValueName.StopCharging.name(),
                "alw=0",
                HttpMethod.GET));

        this.control.setApplianceId("F-001");
        this.control.setPollInterval(10);
        this.control.init();
        this.control.validate();
    }

    @Test
    public void isVehicleNotConnected() {
        Mockito.doReturn("{ \"car\": \"1\" }").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(this.control.isVehicleNotConnected());
    }

    @Test
    public void isVehicleConnected() {
        Mockito.doReturn("{ \"car\": \"3\" }").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(this.control.isVehicleConnected());
    }

    @Test
    public void isCharging() {
        Mockito.doReturn("{ \"car\": \"2\" }").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(this.control.isCharging());
    }

    @Test
    public void isInErrorState_True() {
        Mockito.doReturn("{ \"err\": \"1\" }").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertTrue(this.control.isInErrorState());
    }

    @Test
    public void isInErrorState_False() {
        Mockito.doReturn("{ \"err\": \"0\" }").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertFalse(this.control.isInErrorState());
    }

    @Test
    public void startCharging() {
        Mockito.doReturn("this is the START CHARGING response").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        this.control.startCharging();
        Mockito.verify(executorMock).execute(HttpMethod.GET,BASE_URL + "/mqtt=alw=1", "alw=1");
    }

    @Test
    public void stopCharging() {
        Mockito.doReturn("this is the STOP CHARGING response").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        this.control.stopCharging();
        Mockito.verify(executorMock).execute(HttpMethod.GET, BASE_URL + "/mqtt=alw=0", "alw=0");
    }

    @Test
    public void setChargeCurrent() {
        Mockito.doReturn("this is the SET CHARGE CURRENT response").when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        this.control.setChargeCurrent(6);;
        Mockito.verify(executorMock).execute(HttpMethod.GET,BASE_URL + "/mqtt=amp=6", "amp={0}");
    }
}
