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

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.http.HttpConfiguration;
import de.avanux.smartapplianceenabler.http.HttpTransactionExecutor;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import de.avanux.smartapplianceenabler.http.HttpRead;
import de.avanux.smartapplianceenabler.http.HttpReadValue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpElectricityMeterTest extends TestBase {

    private HttpElectricityMeter meter;
    private HttpTransactionExecutor executorMock = Mockito.mock(HttpTransactionExecutor.class);

    public HttpElectricityMeterTest() {
        PollEnergyMeter pollEnergyMeter = new PollEnergyMeter();
        this.meter = new HttpElectricityMeter();
        pollEnergyMeter.setPollEnergyExecutor(this.meter);
        this.meter.setPollEnergyMeter(pollEnergyMeter);
        this.meter.setApplianceId("F-001");
        this.meter.setHttpTransactionExecutor(this.executorMock);
        this.meter.init();
    }

    @Test
    public void pollPower_EdimaxSP2101W() {
        HttpRead read = new HttpRead("http://192.168.69.74:10000/smartplug.cgi");
        this.meter.setHttpConfiguration(new HttpConfiguration("application/xml", "admin", "12345678"));
        HttpReadValue powerReadValue = new HttpReadValue(MeterValueName.Power.name(), null, "<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"get\"><NOW_POWER><Device.System.Power.NowCurrent></Device.System.Power.NowCurrent><Device.System.Power.NowPower></Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>", ".*NowPower.(\\d+).*", null);
        read.setReadValues(Collections.singletonList(powerReadValue));
        meter.setHttpReads(Collections.singletonList(read));
        Mockito.doReturn(edimaxSP2101WResponse).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertEquals(52.0, meter.pollPower(), 0.01);
    }

    @Test
    public void pollPower_SonoffPow() {
        HttpRead powerReadSpy = Mockito.spy(new HttpRead("http://192.168.69.62/cm?cmnd=Status%208"));
        HttpReadValue powerReadValue = new HttpReadValue(MeterValueName.Power.name(), null, null, ".*Power.:(\\d+).*", null);
        powerReadSpy.setReadValues(Collections.singletonList(powerReadValue));
        meter.setHttpReads(Collections.singletonList(powerReadSpy));
        Mockito.doReturn(sonoffPowResponse).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertEquals(26.0, meter.pollPower(), 0.01);
    }

    @Test
    public void pollPower_KeContactP30() {
        HttpRead powerReadSpy = Mockito.spy(new HttpRead("http://localhost:8080/test.html"));
        HttpReadValue powerReadValue = new HttpReadValue(MeterValueName.Power.name(), null, null, ".*RealPower.*\\n(\\d+\\,\\d{2}) kW.*Energy \\(present session\\).*", null);
        powerReadSpy.setReadValues(Collections.singletonList(powerReadValue));
        meter.setHttpReads(Collections.singletonList(powerReadSpy));
        Mockito.doReturn(keContactP30Response).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        assertEquals(12.34, meter.pollPower(), 0.01);
    }

    @Test
    public void getPower_goECharger() {
        meter.setContentProtocol(ContentProtocolType.JSON);

        HttpRead energyReadSpy = Mockito.spy(new HttpRead("http://127.0.0.1:8999"));
        HttpReadValue powerReadValue = new HttpReadValue(MeterValueName.Energy.name(), "$.dws", null, null, 0.0000027778);
        energyReadSpy.setReadValues(Collections.singletonList(powerReadValue));
        meter.setHttpReads(Collections.singletonList(energyReadSpy));

        LocalDateTime now = LocalDateTime.now();

        String response = goEChargerStatus.replace((CharSequence) "dws\":\"0", (CharSequence) "dws\":\"360000"); // 1.0 kWh
        Mockito.doReturn(response).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        meter.getPollEnergyMeter().addValue(now);

        response = goEChargerStatus.replace((CharSequence) "dws\":\"0", (CharSequence) "dws\":\"366000");  // (1 + 1/60) * 1.0 kWh
        Mockito.doReturn(response).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        meter.getPollEnergyMeter().addValue(now.plusSeconds(60));

//        assertEquals(1000, this.meter.getAveragePower());
//        assertEquals(1000, this.meter.getMaxPower());
//        assertEquals(1000, this.meter.getMinPower());
    }

    @Test
    public void getEnergy_Initial() {
//        assertEquals(0.0f, this.meter.getEnergy(), 0.01);
    }

    @Test
    public void getEnergy_goECharger() {
        this.meter.setContentProtocol(ContentProtocolType.JSON);
        HttpRead energyReadSpy = Mockito.spy(new HttpRead("http://127.0.0.1:8999"));
        HttpReadValue powerReadValue = new HttpReadValue(MeterValueName.Energy.name(), "$.dws", null, null, 0.0000027778);
        energyReadSpy.setReadValues(Collections.singletonList(powerReadValue));
        meter.setHttpReads(Collections.singletonList(energyReadSpy));

        String startResponse = goEChargerStatus.replace((CharSequence) "dws\":\"0", (CharSequence) "dws\":\"360000"); // 1.0 kWh
        Mockito.doReturn(startResponse).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());

        this.meter.startEnergyMeter();
        String stopResponse = goEChargerStatus.replace((CharSequence) "dws\":\"0", (CharSequence) "dws\":\"720000"); // 2.0 kWh
        Mockito.doReturn(stopResponse).when(executorMock).execute(Mockito.any(), Mockito.any(), Mockito.any());
        this.meter.stopEnergyMeter();

//        assertEquals(1, this.meter.getEnergy(), 0.01);
    }

    private final static String goEChargerStatus = "{\"version\":\"B\",\"rbc\":\"251\",\"rbt\":\"2208867\",\"car\":\"1\",\"amp\":\"10\",\"err\":\"0\",\"ast\"\n" +
            ":\"0\",\"alw\":\"1\",\"stp\":\"0\",\"cbl\":\"0\",\"pha\":\"8\",\"tmp\":\"30\",\"dws\":\"0\",\"dwo\":\"0\",\"ad\n" +
            "i\":\"1\",\"uby\":\"0\",\"eto\":\"120\",\"wst\":\"3\",\"nrg\":[2,0,0,235,0,0,0,0,0,0,0,0,0,0,0,0\n" +
            "],\"fwv\":\"020-rc1\",\"sse\":\"000000\",\"wss\":\"goe\",\"wke\":\"\",\"wen\":\"1\",\"tof\":\"101\",\"td\n" +
            "s\":\"1\",\"lbr\":\"255\",\"aho\":\"2\",\"afi\":\"8\",\"ama\":\"32\",\"al1\":\"11\",\"al2\":\"12\",\"al3\":\"\n" +
            "15\",\"al4\":\"24\",\"al5\":\"31\",\"cid\":\"255\",\"cch\":\"65535\",\"cfi\":\"65280\",\"lse\":\"0\",\"us\n" +
            "t\":\"0\",\"wak\":\"\",\"r1x\":\"2\",\"dto\":\"0\",\"nmo\":\"0\",\"eca\":\"0\",\"ecr\":\"0\",\"ecd\":\"0\",\"ec\n" +
            "4\":\"0\",\"ec5\":\"0\",\"ec6\":\"0\",\"ec7\":\"0\",\"ec8\":\"0\",\"ec9\":\"0\",\"ec1\":\"0\",\"rca\":\"\",\"rc\n" +
            "r\":\"\",\"rcd\":\"\",\"rc4\":\"\",\"rc5\":\"\",\"rc6\":\"\",\"rc7\":\"\",\"rc8\":\"\",\"rc9\":\"\",\"rc1\":\"\",\"\n" +
            "rna\":\"\",\"rnm\":\"\",\"rne\":\"\",\"rn4\":\"\",\"rn5\":\"\",\"rn6\":\"\",\"rn7\":\"\",\"rn8\":\"\",\"rn9\":\"\"\n" +
            ",\"rn1\":\"\"}";

    private final static String edimaxSP2101WResponse = "<?xml version=\"1.0\" encoding=\"UTF8\"?><SMARTPLUG id=\"edimax\"><CMD id=\"get\"><NOW_POWER><Device.System.Power.NowCurrent>0.2871</Device.System.Power.NowCurrent><Device.System.Power.NowPower>52.49</Device.System.Power.NowPower></NOW_POWER></CMD></SMARTPLUG>";

    private final static String sonoffPowResponse =  "STATUS8 = {\"StatusPWR\":{\"Total\":0.000, \"Yesterday\":0.000, \"Today\":0.000, \"Power\":26, \"Factor\":0.94, \"Voltage\":234, \"Current\":0.122}}";

    private final static String keContactP30Response = "<html><head>\n" +
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
            "</body></html>";
}
