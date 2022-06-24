/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElectricVehicleHandlerTest implements SocValuesChangedListener {
    private ElectricVehicleHandler sut;
    private SocValues socValues;

    @BeforeEach
    public void beforeEach() throws Exception {
        sut = new ElectricVehicleHandler();
        sut.setApplianceId("F-001-01");
        sut.setSocScriptAsync(false);
        sut.setSocValuesChangedListener(this);

        socValues = null;
    }

    @Test
    public void triggerSocScriptExecution_1ev_onlySoc() {
        var soc = 45;
        sut.setVehicles(Arrays.asList(configureEV(1, soc, null, null, null)));

        sut.triggerSocScriptExecution();

        assertEquals(soc, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_onlyEv1HasSocScript() {
        var soc = 45;
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc, true, null, null),
                configureEV(2, null, null, null, null)
        ));

        sut.triggerSocScriptExecution();

        assertEquals(soc, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_onlyEv2HasSocScript() {
        var soc = 45;
        sut.setVehicles(Arrays.asList(
                configureEV(1, null, true, null, null),
                configureEV(2, soc, null, null, null)
        ));

        sut.triggerSocScriptExecution();

        assertEquals(soc, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_pluggedIn1Better() {
        var soc1 = 45;
        var soc2 = 17;
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc1, true, null, null),
                configureEV(2, soc2, null, null, null)
        ));

        sut.triggerSocScriptExecution();

        assertEquals(soc1, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_pluggedIn2Better() {
        var soc1 = 45;
        var soc2 = 17;
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc1, null, null, null),
                configureEV(2, soc2, true, null, null)
        ));

        sut.triggerSocScriptExecution();

        assertEquals(soc2, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_pluginTime1Better() {
        var soc1 = 45;
        var soc2 = 17;
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc1, true, "14:01", null),
                configureEV(2, soc2, true, null, null)
        ));
        var now = LocalDateTime.now().withHour(14).withMinute(3);
        sut.getEvIdWithSocScriptExecutor().get(1).setNowForTesting(now);

        sut.triggerSocScriptExecution();

        assertEquals(soc1, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_pluginTime2Better() {
        var soc1 = 45;
        var soc2 = 17;
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc1, true, null, null),
                configureEV(2, soc2, true, "14:01", null)
        ));
        var now = LocalDateTime.now().withHour(14).withMinute(3);
        sut.getEvIdWithSocScriptExecutor().get(2).setNowForTesting(now);

        sut.triggerSocScriptExecution();

        assertEquals(soc2, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_location1Better() {
        var soc1 = 45;
        var soc2 = 17;
        var evChargerLocation = new ImmutablePair<>(50.293448956647715, 8.982373236258528);
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc1, true, null, evChargerLocation),
                configureEV(2, soc2, true, null, null)
        ));
        sut.setEvChargerLocation(evChargerLocation);

        sut.triggerSocScriptExecution();

        assertEquals(soc1, socValues.current);
    }

    @Test
    public void triggerSocScriptExecution_2ev_location2Better() {
        var soc1 = 45;
        var soc2 = 17;
        var evChargerLocation = new ImmutablePair<>(50.293448956647715, 8.982373236258528);
        sut.setVehicles(Arrays.asList(
                configureEV(1, soc1, true, null, null),
                configureEV(2, soc2, true, null, evChargerLocation)
        ));
        sut.setEvChargerLocation(evChargerLocation);

        sut.triggerSocScriptExecution();

        assertEquals(soc2, socValues.current);
    }

    private ElectricVehicle configureEV(int id, Integer soc, Boolean pluggedIn, String pluginTime, Pair<Double, Double> location) {
        var socScript = mock(SocScript.class);
        when(socScript.getScript()).thenReturn("this is a script for id " + id);
        when(socScript.getResult()).thenReturn(new SocScriptExecutionResult(soc, pluggedIn, pluginTime, location));

        var ev = new ElectricVehicle();
        ev.setId(id);
        if(soc != null) {
            ev.setSocScript(socScript);
        }
        return ev;
    }


    @Override
    public void onSocValuesChanged(SocValues socValues) {
        this.socValues = socValues;
    }
}
