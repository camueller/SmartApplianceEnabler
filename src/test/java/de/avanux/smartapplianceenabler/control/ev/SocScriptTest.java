/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

public class SocScriptTest {

    private SocScript socScript = new SocScript();
    private String output = "Prepare Session\n" +
            "Login...\n" +
            "get_latest_battery_status from servers\n" +
            "request an update from the car itself\n" +
            "{'answer': {'status': 200, 'VoltLabel': {'HighVolt': '240', 'LowVolt': '120'}, 'BatteryStatusRecords': {'OperationResult': 'START', 'OperationDateAndTime': '09.Jun 2022 16:05', 'ADDED_FOR_TEST_Latitude': '50.29371451885252', 'ADDED_FOR_TEST_Longitude': '8.981674925489573', 'BatteryStatus': {'BatteryChargingStatus': 'NOT_CHARGING', 'BatteryCapacity': '240', 'BatteryRemainingAmount': '168', 'BatteryRemainingAmountWH': '25680', 'BatteryRemainingAmountkWH': '', 'SOC': {'Value': '70'}}, 'PluginState': 'CONNECTED', 'CruisingRangeAcOn': '168000', 'CruisingRangeAcOff': '171000', 'TimeRequiredToFull': {'HourRequiredToFull': '13', 'MinutesRequiredToFull': '30'}, 'TimeRequiredToFull200': {'HourRequiredToFull': '9', 'MinutesRequiredToFull': '30'}, 'TimeRequiredToFull200_6kW': {'HourRequiredToFull': '4', 'MinutesRequiredToFull': '0'}, 'NotificationDateAndTime': '2022/06/09 14:05', 'TargetDate': '2022/06/09 14:05'}}, 'battery_capacity': '240', 'battery_remaining_amount': '168', 'charging_status': 'NOT_CHARGING', 'is_charging': False, 'is_quick_charging': False, 'plugin_state': 'CONNECTED', 'is_connected': True, 'is_connected_to_quick_charger': False, 'cruising_range_ac_off_km': 171.0, 'cruising_range_ac_on_km': 168.0, 'time_to_full_trickle': datetime.timedelta(seconds=48600), 'time_to_full_l2': datetime.timedelta(seconds=34200), 'time_to_full_l2_6kw': datetime.timedelta(seconds=14400), 'battery_percent': 70.0, 'state_of_charge': '70'}\n";

//    @Test
//    public void getStateOfCharge() throws Exception {
//        File socScriptFile = writeSocScriptFile("#!/bin/sh\necho \"Car SOC is 42.7 percent.\"\n");
//        socScript.setScript(socScriptFile.getAbsolutePath());
//        socScript.setExtractionRegex(".*is (\\d*.{0,1}\\d+).*");
//        assertEquals(42.7f, socScript.getStateOfCharge(), 0.01f);
//    }
//
//    @Test
//    public void getStateOfCharge_negativeReturnCode() throws Exception {
//        File socScriptFile = writeSocScriptFile("#!/bin/sh\necho \"Car SOC is 42.7 percent.\"\nexit 1\\n");
//        socScript.setScript(socScriptFile.getAbsolutePath());
//        socScript.setExtractionRegex(".*is (\\d*.{0,1}\\d+).*");
//        assertNull(socScript.getStateOfCharge());
//    }

//    private File writeSocScriptFile(String content) throws Exception {
//        File socScriptFile = File.createTempFile("soc", ".sh");
//        socScriptFile.setExecutable(true);
//        PrintWriter out = new PrintWriter(socScriptFile);
//        out.print(content);
//        out.close();
//        return socScriptFile;
//    }


    @Test
    public void extractValue_soc() {
        assertEquals("70", socScript.extractValue(output, ".*state_of_charge': '(\\d+).*"));
    }

    @Test
    public void extractValue_plugInTime() {
        assertEquals("14:05", socScript.extractValue(output, ".*TargetDate': '[0-9/]+ ([0-9:]+).*"));
    }

    @Test
    public void matchValue_pluggedIn() {
        assertTrue(socScript.matchValue(output, ".*PluginState': '(CONNECTED).*"));
    }

    @Test
    public void extractValue_latitude() {
        assertEquals("50.29371451885252", socScript.extractValue(output, ".*ADDED_FOR_TEST_Latitude': '([0-9.]+)"));
    }

    @Test
    public void extractValue_longitude() {
        assertEquals("8.981674925489573", socScript.extractValue(output, ".*ADDED_FOR_TEST_Longitude': '([0-9.]+)"));
    }
}
