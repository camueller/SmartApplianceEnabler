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

package de.avanux.smartapplianceenabler.integration;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.RunningTimeMonitor;
import de.avanux.smartapplianceenabler.appliance.RuntimeInterval;
import de.avanux.smartapplianceenabler.control.ev.EVControl;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.test.TestBuilder;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EVChargerTest extends TestBase {

    private Logger logger = LoggerFactory.getLogger(EVChargerTest.class);
    private EVControl evControl = Mockito.mock(EVControl.class);

    @Test
    public void optionalEnergyRequest() {
        String applianceId = "F-001";
        Integer evId = 1;
        Integer batteryCapacity = 40000;
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId)
                .withEvCharger(evControl)
                .withElectricVehicle(evId, batteryCapacity)
                .withMockMeter()
                .init();
        Appliance appliance = builder.getAppliance();
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        LocalDateTime now = toToday(10, 0, 0);

        log("Check initial values");
        List<RuntimeInterval> runtimeIntervalsNotConnected = appliance.getRuntimeIntervals(now, false);
        Assert.assertEquals(0, runtimeIntervalsNotConnected.size());

        log("After vehicle has been connected");
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        List<RuntimeInterval> runtimeIntervalsConnected = appliance.getRuntimeIntervals(now, false);
        Assert.assertEquals(
                Collections.singletonList(
                    new RuntimeInterval(0, 172800, 0, 44000, true)
                ),
                runtimeIntervalsConnected);
    }

    @Test
    public void daytimeframeSocRequest() {
        String applianceId = "F-001";
        Integer evId = 1;
        Integer batteryCapacity = 40000;
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId)
                .withEvCharger(evControl)
                .withElectricVehicle(evId, batteryCapacity)
                .withMockMeter()
                .withSchedule(10,0, 16, 0)
                .withSocRequest(evId, 50)
                .init();
        Appliance appliance = builder.getAppliance();
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();

        log("Vehicle not connected");
        LocalDateTime timeInitial = toToday(9, 50, 0);
        List<RuntimeInterval> runtimeIntervalsNotConnected = appliance.getRuntimeIntervals(timeInitial, false);
        Assert.assertEquals(0, runtimeIntervalsNotConnected.size());

        log("After vehicle has been connected");
        LocalDateTime timeVehicleConnected = toToday(9, 55, 0);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        List<RuntimeInterval> runtimeIntervalsConnected = appliance.getRuntimeIntervals(timeVehicleConnected, false);
        Assert.assertEquals(
            Arrays.asList(
                    new RuntimeInterval(0, 299, 0, 44000, true),
                    new RuntimeInterval(300, 21900, 22000, 22000, true),
                    new RuntimeInterval(86700, 108300, 22000, 22000, true)
            ),
            runtimeIntervalsConnected);
    }

    @Test
    public void manualStartFollowedByDaytimeframeRuntimeRequest() {
        String applianceId = "F-001";
        Integer evId = 1;
        Integer batteryCapacity = 40000;
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId)
                .withEvCharger(evControl)
                .withElectricVehicle(evId, batteryCapacity)
                .withMockMeter()
                .withSchedule(16,0, 22, 0)
                .withRuntimeRequest(5000, 5000)
                .init();
        Appliance appliance = builder.getAppliance();
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();

        log("Vehicle not connected");
        LocalDateTime timeInitial = toToday(9, 50, 0);
        List<RuntimeInterval> runtimeIntervalsNotConnected = appliance.getRuntimeIntervals(timeInitial, false);
        Assert.assertEquals(0, runtimeIntervalsNotConnected.size());

        log("After vehicle has been connected");
        LocalDateTime timeVehicleConnected = toToday(10, 0, 0);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        tick(appliance, timeVehicleConnected);
        List<RuntimeInterval> runtimeIntervalsConnected = appliance.getRuntimeIntervals(timeVehicleConnected, false);
        Assert.assertEquals(
                Arrays.asList(
                        new RuntimeInterval(0, 21599, 0, 44000, true),
                        new RuntimeInterval(21600, 43200, 5000, 5000),
                        new RuntimeInterval(108000, 129600, 5000, 5000)
                ),
                runtimeIntervalsConnected);

        log("Manual start");
        LocalDateTime timeManualStart = toToday(11, 0, 0);
        appliance.setEnergyDemand(timeManualStart, evId, 40, 50, null);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        tick(appliance, timeManualStart);
        LocalDateTime timeAfterManualStart = toToday(12, 0, 0);
        tick(appliance, timeAfterManualStart);
        List<RuntimeInterval> runtimeIntervalsManualStart = appliance.getRuntimeIntervals(timeAfterManualStart, false);
        Assert.assertEquals(
                Arrays.asList(
                        new RuntimeInterval(0, 12240, 4400, 4400, true),
                        new RuntimeInterval(14400, 36000, 5000, 5000),
                        new RuntimeInterval(100800, 122400, 5000, 5000)
                ),
                runtimeIntervalsManualStart);

        log("Manual start - charging completed");
        LocalDateTime timeManualStartChargingCompleted = toToday(13, 0, 0);
        Mockito.when(evControl.isCharging()).thenReturn(false);
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        tick(appliance, timeManualStartChargingCompleted);
        Assert.assertTrue(evCharger.isChargingCompleted());
        List<RuntimeInterval> runtimeIntervalsManualStartChargingCompleted
                = appliance.getRuntimeIntervals(timeManualStartChargingCompleted, false);
        Assert.assertEquals(0, runtimeIntervalsManualStartChargingCompleted.size());
    }

    private void tick(Appliance appliance, LocalDateTime now) {
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        evCharger.updateState();

        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
        runningTimeMonitor.updateActiveTimeframeInterval(now);
    }

    private void log(String message) {
        logger.debug("*********** " + message);
    }
}
