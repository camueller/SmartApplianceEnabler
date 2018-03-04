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

package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.Application;
import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.meter.HttpElectricityMeter;
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

    @Ignore
    @Test
    public void getPower_KeContactP30() {
        meter.setUrl("http://localhost:8080/test.html");
        meter.setPowerValueExtractionRegex(".*RealPower.*\\n(\\d+\\,\\d{2}) kW.*Energy \\(present session\\).*");
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

    @Test
    public void extractPowerValueFromResponse_KeContactP30() {
        Assert.assertEquals("12,34", meter.extractPowerValueFromResponse("<html><head>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8869-1\">\n" +
                        "\n" +
                        "<link href=\"./styles.css\" rel=\"stylesheet\" media=\"screen\"><title>KeContact P30</title>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "</head>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<body>\n" +
                        "\n" +
                        "<h2>Status</h2>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<table width=\"90%\" border=\"2\" cellspacing=\"5\" cellpadding=\"5\">\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Product-ID\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# SerNo -->KC-P30-EC240422-E00-SN:17968426 ML:17883345\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "MAC Address\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# MAC -->00:60:b5:37:92:c8\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Software\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# VERSION -->P30 v 3.08.4 (170307-132509) : 16189 : 318.0 : 2030006\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Service Info\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# TempC -->0 : 0<br>1 : 1 : 0 : 0 : 264<br>0 : 13 : 24\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "State / Seconds\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# State --><b>plugged</b> : seconds : 467307\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Current limit (PWM | hardware setup)\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# Avail -->32,00A (53,3% duty cycle | 32A)\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "</table>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<h2>Energy Monitor</h2>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<body>\n" +
                        "\n" +
                        "<table width=\"90%\" border=\"2\" cellspacing=\"5\" cellpadding=\"5\">\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Voltage\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# Volt -->224 | 223 | 221 V\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Current\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# Curr -->0,00 | 0,00 | 0,00 A\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "RealPower | PowerFactor\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "12,34 kW | 0,0 %</b> \n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Energy (present session)\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# Energy --><b>2,36</b> kWh\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Energy (total)\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# EnTotal --><b>50,33</b> kWh\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Energy (housegrid meter) in | out\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# EnHouse -->-,--\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<tr>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "Energy (solar meter) in | out\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "<td align=\"left\"  valign=\"top\">\n" +
                        "\n" +
                        "<!--# EnSolar -->-,--\n" +
                        "\n" +
                        "</td>\n" +
                        "\n" +
                        "</tr>\n" +
                        "\n" +
                        "</table>\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "<br>\n" +
                        "\n" +
                        "<span style=\"font-size:80%\">Voltage and current show present values for L1 | L2 | L3<br>\n" +
                        "\n" +
                        "Values displayed may not be used for billing purposes<br>\n" +
                        "\n" +
                        "Energy monitor values shown as -,-- indicate : not available with this product</span>\n" +
                        "\n" +
                        "</body></html>",
                ".*RealPower.*\\n(\\d+\\,\\d{2}) kW.*Energy \\(present session\\).*"));
    }
}
