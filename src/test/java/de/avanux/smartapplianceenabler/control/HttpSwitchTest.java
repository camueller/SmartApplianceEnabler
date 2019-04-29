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
import de.avanux.smartapplianceenabler.http.HttpMethod;
import de.avanux.smartapplianceenabler.http.HttpWrite;
import de.avanux.smartapplianceenabler.http.HttpWriteValue;
import de.avanux.smartapplianceenabler.http.HttpWriteValueType;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class HttpSwitchTest extends TestBase {

    private HttpSwitch httpSwitch;
    private CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class);
    private StatusLine statusLineMock = Mockito.mock(StatusLine.class);

    public HttpSwitchTest() {
        this.httpSwitch = new HttpSwitch();
        this.httpSwitch.setApplianceId("F-001");
        // this.httpSwitch.init();

        Mockito.when(responseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(statusLineMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
    }

    @Test
    public void on_EdimaxSP2101W() {
        String url = "http://192.168.69.74:10000/smartplug.cgi";
        HttpWrite writeSpy = Mockito.spy(new HttpWrite(url, "application/xml", "admin", "12345678"));
        String data = "<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"setup\"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>";
        HttpWriteValue writeValue = new HttpWriteValue(ControlValueName.On.name(), data, HttpWriteValueType.Body, HttpMethod.POST);
        writeSpy.setWriteValues(Collections.singletonList(writeValue));
        this.httpSwitch.setHttpWrites(Collections.singletonList(writeSpy));
        Mockito.doReturn(responseMock).when(writeSpy).executeLeaveOpen(Mockito.any(), Mockito.any(), Mockito.any());

        Assert.assertFalse(this.httpSwitch.isOn());
        this.httpSwitch.on(new LocalDateTime(), true);
        Assert.assertTrue(this.httpSwitch.isOn());
        Mockito.verify(writeSpy).executeLeaveOpen(HttpMethod.POST, url, data);
    }

//    @Ignore
//    @Test
//    public void off_EdimaxSP2101W() {
//        zwitch.setOffUrl("http://192.168.69.74:10000/smartplug.cgi");
//        zwitch.setUsername("admin");
//        zwitch.setPassword("12345678");
//        zwitch.setContentType("application/xml");
//        zwitch.setOffData("<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"setup\"><Device.System.Power.State>OFF</Device.System.Power.State></CMD></SMARTPLUG>");
//        zwitch.on(new LocalDateTime(),false);
//    }
//
//    @Ignore
//    @Test
//    public void on_SonoffPow() {
//        zwitch.setOnUrl("http://192.168.69.62/cm?cmnd=Power%20On");
//        zwitch.on(new LocalDateTime(),true);
//    }
//
//    @Ignore
//    @Test
//    public void off_SonoffPow() {
//        zwitch.setOffUrl("http://192.168.69.62/cm?cmnd=Power%20Off");
//        zwitch.on(new LocalDateTime(),false);
//    }
}
