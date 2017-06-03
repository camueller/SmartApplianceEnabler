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

package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.junit.Ignore;
import org.junit.Test;

public class HttpSwitchTest extends TestBase {

    private HttpSwitch zwitch = new HttpSwitch();

    @Ignore
    @Test
    public void on_EdimaxSP2101W() {
        zwitch.setOnUrl("http://192.168.69.74:10000/smartplug.cgi");
        zwitch.setUsername("admin");
        zwitch.setPassword("12345678");
        zwitch.setContentType("application/xml");
        zwitch.setOnData("<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"setup\"><Device.System.Power.State>ON</Device.System.Power.State></CMD></SMARTPLUG>");
        zwitch.on(true);
    }

    @Ignore
    @Test
    public void off_EdimaxSP2101W() {
        zwitch.setOffUrl("http://192.168.69.74:10000/smartplug.cgi");
        zwitch.setUsername("admin");
        zwitch.setPassword("12345678");
        zwitch.setContentType("application/xml");
        zwitch.setOffData("<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"setup\"><Device.System.Power.State>OFF</Device.System.Power.State></CMD></SMARTPLUG>");
        zwitch.on(false);
    }

    @Ignore
    @Test
    public void on_SonoffPow() {
        zwitch.setOnUrl("http://192.168.69.62/cm?cmnd=Power%20On");
        zwitch.on(true);
    }

    @Ignore
    @Test
    public void off_SonoffPow() {
        zwitch.setOffUrl("http://192.168.69.62/cm?cmnd=Power%20Off");
        zwitch.on(false);
    }
}
