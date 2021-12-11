/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.http.*;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpSwitchTest extends TestBase {

    private HttpSwitch httpSwitch;
    private CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class);
    private HttpTransactionExecutor executorMock = Mockito.mock(HttpTransactionExecutor.class);
    private StatusLine statusLineMock = Mockito.mock(StatusLine.class);

    public HttpSwitchTest() {
        this.httpSwitch = new HttpSwitch();
        this.httpSwitch.setApplianceId("F-001");
        // this.httpSwitch.init();
        this.httpSwitch.setHttpTransactionExecutor(this.executorMock);

        Mockito.when(responseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    }

    @Test
    public void on_EdimaxSP2101W() {
//        String url = "http://192.168.69.74:10000/smartplug.cgi";
//        this.httpSwitch.setHttpConfiguration(new HttpConfiguration("application/xml", "admin", "12345678"));
//        HttpMethod httpMethod = HttpMethod.POST;
//        HttpWrite write= new HttpWrite(url);
//        String data = "<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"setup\"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>";
//        HttpWriteValue writeValue = new HttpWriteValue(ControlValueName.On.name(), data, httpMethod);
//        write.setWriteValues(Collections.singletonList(writeValue));
//        this.httpSwitch.setHttpWrites(Collections.singletonList(write));
//        Mockito.doReturn(responseMock).when(executorMock).executeLeaveOpen(Mockito.any(), Mockito.any(), Mockito.any());
//
//        assertFalse(this.httpSwitch.isOn());
//        this.httpSwitch.on(LocalDateTime.now(), true);
//        assertTrue(this.httpSwitch.isOn());
//        Mockito.verify(executorMock).executeLeaveOpen(httpMethod, url, data);
    }

//    @Test
//    public void off_EdimaxSP2101W() {
//        String url = "http://192.168.69.74:10000/smartplug.cgi";
//        this.httpSwitch.setHttpConfiguration(new HttpConfiguration("application/xml", "admin", "12345678"));
//        HttpMethod httpMethod = HttpMethod.POST;
//        HttpWrite write = new HttpWrite(url);
//        String data = "<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"setup\"><Device.System.Power.State>OFF</Device.System.Power.State></CMD></SMARTPLUG>";
//        HttpWriteValue writeValue = new HttpWriteValue(ControlValueName.Off.name(), data, httpMethod);
//        write.setWriteValues(Collections.singletonList(writeValue));
//        this.httpSwitch.setHttpWrites(Collections.singletonList(write));
//        Mockito.doReturn(responseMock).when(executorMock).executeLeaveOpen(Mockito.any(), Mockito.any(), Mockito.any());
//        this.httpSwitch.on = true;
//        assertTrue(this.httpSwitch.isOn());
//        this.httpSwitch.on(LocalDateTime.now(), false);
//        assertFalse(this.httpSwitch.isOn());
//        Mockito.verify(executorMock).executeLeaveOpen(httpMethod, url, data);
//    }
//
//    @Test
//    public void on_SonoffPow() {
//        String url = "http://192.168.69.62/cm?cmnd=Power%20On";
//        HttpMethod httpMethod = HttpMethod.GET;
//        HttpWrite write = new HttpWrite(url);
//        HttpWriteValue writeValue = new HttpWriteValue(ControlValueName.On.name(), null, httpMethod);
//        write.setWriteValues(Collections.singletonList(writeValue));
//        this.httpSwitch.setHttpWrites(Collections.singletonList(write));
//        Mockito.doReturn(responseMock).when(executorMock).executeLeaveOpen(Mockito.any(), Mockito.any(), Mockito.any());
//
//        assertFalse(this.httpSwitch.isOn());
//        this.httpSwitch.on(LocalDateTime.now(), true);
//        assertTrue(this.httpSwitch.isOn());
//        Mockito.verify(executorMock).executeLeaveOpen(httpMethod, url, null);
//    }
//
//    @Test
//    public void off_SonoffPow() {
//        String url = "http://192.168.69.62/cm?cmnd=Power%20Off";
//        HttpMethod httpMethod = HttpMethod.GET;
//        HttpWrite write = new HttpWrite(url);
//        HttpWriteValue writeValue = new HttpWriteValue(ControlValueName.Off.name(), null, httpMethod);
//        write.setWriteValues(Collections.singletonList(writeValue));
//        this.httpSwitch.setHttpWrites(Collections.singletonList(write));
//        Mockito.doReturn(responseMock).when(executorMock).executeLeaveOpen(Mockito.any(), Mockito.any(), Mockito.any());
//        this.httpSwitch.on = true;
//        assertTrue(this.httpSwitch.isOn());
//        this.httpSwitch.on(LocalDateTime.now(), false);
//        assertFalse(this.httpSwitch.isOn());
//        Mockito.verify(executorMock).executeLeaveOpen(httpMethod, url, null);
//    }
}
