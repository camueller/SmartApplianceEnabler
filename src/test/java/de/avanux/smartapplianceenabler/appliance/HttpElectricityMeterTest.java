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

import de.avanux.smartapplianceenabler.Application;
import de.avanux.smartapplianceenabler.TestBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpElectricityMeterTest extends TestBase {

    private static Logger logger = LoggerFactory.getLogger(Application.class);
    private HttpElectricityMeter meter = new HttpElectricityMeter();

    @Ignore
    @Test
    public void getPower_EdimaxSP2101W() {
        meter.setUrl("http://192.168.69.74:10000/smartplug.cgi");
        meter.setUsername("admin");
        meter.setPassword("12345678");
        meter.setContentType("application/xml");
        meter.setData("<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"get\"><NOW_POWER><Device.System.Power.NowCurrent></Device.System.Power.NowCurrent><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>");
        meter.setPowerValueExtractionRegex(".*NowPower.(\\d+).*");
        float power = meter.getPower();
        logger.debug("Power=" + power);
        Assert.assertTrue(power > 0);
    }

    @Ignore
    @Test
    public void getPower_SonoffPow() {
        meter.setUrl("http://192.168.69.62/cm?cmnd=Status%208");
        meter.setPowerValueExtractionRegex(".*Power.:(\\d+).*");
        float power = meter.getPower();
        logger.debug("Power=" + power);
        Assert.assertTrue(power > 0);
    }

    @Test
    public void extractPowerValueFromResponse_SonffPow() {
        Assert.assertEquals("27", meter.extractPowerValueFromResponse("STATUS8 = {\"StatusPWR\":{\"Total\":0.000, \"Yesterday\":0.000, \"Today\":0.000, \"Power\":27, \"Factor\":0.94, \"Voltage\":234, \"Current\":0.122}}",
                ".*Power.:(\\d+).*"));
    }

    @Test
    public void extractPowerValueFromResponse_EdimaxSP2101W() {
        Assert.assertEquals("52.49", meter.extractPowerValueFromResponse("<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"get\"><NOW_POWER><Device.System.Power.NowCurrent>0.2871</Device.System.Power.NowCurrent><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>",
                ".*NowPower>(\\d*.{0,1}\\d+).*"));
    }
}
