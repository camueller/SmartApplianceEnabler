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
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.RunningTimeMonitor;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.MockSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitchDefaults;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import de.avanux.smartapplianceenabler.test.TestBuilder;
import de.avanux.smartapplianceenabler.util.DateTimeProvider;
import de.avanux.smartapplianceenabler.webservice.ApplianceStatus;
import de.avanux.smartapplianceenabler.webservice.SaeController;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IntegrationTest extends TestBase {

    private Logger logger = LoggerFactory.getLogger(SaeController.class);
    private DateTimeProvider dateTimeProvider = Mockito.mock(DateTimeProvider.class);
    private SaeController saeController = new SaeController();
    private SempController sempController = new SempController();
    String applianceId = "F-001";

    public IntegrationTest() {
        ApplianceManager.getInstanceWithoutTimer();
    }

    @Test
    public void testSwitchOnAndOff() {
        LocalDateTime timeInitial = toToday(10, 0, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(false)
                .withSchedule(10, 0, 18, 0, 7200, null)
                .init();
        Appliance appliance = builder.getAppliance();
        Control control = appliance.getControl();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();

        log("Check initial values");
        Assert.assertFalse(control.isOn());
        assertPlanningRequest(timeInitial,
                new Timeframe(applianceId,0, 28800,7199, 7200),
                new Timeframe(applianceId, add24h(0), add24h(28800),7199, 7200));

        logger.debug("########## First switching cycle");

        log("Update timeframe intervals right before switch on");
        LocalDateTime timeSwitchOn = toToday(11, 0, 0);
        runningTimeMonitor.updateActiveTimeframeInterval(timeSwitchOn);
        Assert.assertNotNull(runningTimeMonitor.getActiveTimeframeInterval());
        assertPlanningRequest(timeSwitchOn,
                new Timeframe(applianceId,0, 25200,7199, 7200),
                new Timeframe(applianceId, add24h(-3600), add24h(25200),7199, 7200),
                new Timeframe(applianceId, add48h(-3600), add48h(25200),7199, 7200)
                );

        log("Switch on");
        sempController.em2Device(timeSwitchOn, createEM2Device(applianceId,true));

        log("Check values after switch on");
        assertRunningTime(timeSwitchOn, control, runningTimeMonitor, true, true, false,
                0, 7200, null);
        ApplianceStatus applianceStatusAfterSwitchOn = getApplianceStatus(timeSwitchOn);
        Assert.assertTrue(applianceStatusAfterSwitchOn.isOn());
        assertPlanningRequest(timeSwitchOn,
                new Timeframe(applianceId,0, 25200,7199, 7200),
                new Timeframe(applianceId, add24h(-3600), add24h(25200),7199, 7200),
                new Timeframe(applianceId, add48h(-3600), add48h(25200),7199, 7200)
        );

        log("Switch off");
        LocalDateTime timeSwitchOff = toToday(12, 0, 0);
        sempController.em2Device(timeSwitchOff, createEM2Device(applianceId,false));

        log("Update timeframe intervals right after switch off");
        runningTimeMonitor.updateActiveTimeframeInterval(timeSwitchOff);

        log("Check values after switch off");
        assertRunningTime(timeSwitchOff, control, runningTimeMonitor, false,false, true,
                3600, 3600, null);
        assertPlanningRequest(timeSwitchOff,
                new Timeframe(applianceId,0, 21600,3599, 3600),
                new Timeframe(applianceId, add24h(-7200), add24h(21600),7199, 7200),
                new Timeframe(applianceId, add48h(-7200), add48h(21600),7199, 7200)
        );

        ApplianceStatus applianceStatusAfterSwitchOff = getApplianceStatus(timeSwitchOff);
        Assert.assertFalse(applianceStatusAfterSwitchOff.isOn());

        logger.debug("########## Second switching cycle");

        log("Switch on");
        timeSwitchOn = toToday(16, 0, 0);
        sempController.em2Device(timeSwitchOn, createEM2Device(applianceId,true));

        log("Check values after switch on");
        assertRunningTime(timeSwitchOn, control, runningTimeMonitor, true, true, false,
                3600, 3600, null);
        assertPlanningRequest(timeSwitchOn,
                new Timeframe(applianceId,0, 7200,3599, 3600),
                new Timeframe(applianceId, add24h(-21600), add24h(7200),7199, 7200),
                new Timeframe(applianceId, add48h(-21600), add48h(7200),7199, 7200)
        );
        applianceStatusAfterSwitchOn = getApplianceStatus(timeSwitchOn);
        Assert.assertTrue(applianceStatusAfterSwitchOn.isOn());

        log("Switch off");
        timeSwitchOff = toToday(17, 0, 0);
        sempController.em2Device(timeSwitchOff, createEM2Device(applianceId,false));

        log("Update timeframe intervals right after switch off");
        runningTimeMonitor.updateActiveTimeframeInterval(timeSwitchOff);

        log("Check values after switch off");
        assertRunningTime(timeSwitchOff, control, runningTimeMonitor, false,false, true,
                7200, 0, null);
        assertPlanningRequest(timeSwitchOff,
                new Timeframe(applianceId, 0, 3600,0, 0),
                new Timeframe(applianceId, add24h(-25200), add24h(3600),7199, 7200),
                new Timeframe(applianceId, add48h(-25200), add48h(3600),7199, 7200)
        );
        applianceStatusAfterSwitchOff = getApplianceStatus(timeSwitchOff);
        Assert.assertFalse(applianceStatusAfterSwitchOff.isOn());
    }

    @Test
    public void testClickGoLight() {
        LocalDateTime timeInitial = toToday(11, 0, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(false)
                .withSchedule(10, 0, 18, 0, 3600, null)
                .init();
        Appliance appliance = builder.getAppliance();
        Control control = appliance.getControl();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();

        log("Check initial values");
        Assert.assertFalse(control.isOn());

        log("Set runtime creates timeframe to be set");
        Assert.assertEquals(3600, saeController.suggestRuntime(applianceId).intValue());
        saeController.setRuntimeDemand(timeInitial, applianceId, 1800);
        Assert.assertNotNull(runningTimeMonitor.getActiveTimeframeInterval());

        log("Switch on");
        sempController.em2Device(timeInitial, createEM2Device(applianceId,true));

        log("Check values after switch on");
        assertRunningTime(timeInitial, control, runningTimeMonitor, true, true, false,
                0, 1800, null);
        assertPlanningRequest(timeInitial,
                new Timeframe(applianceId,0, 1800,1799, 1800),
                new Timeframe(applianceId, add24h(-3600), add24h(25200),3599, 3600),
                new Timeframe(applianceId, add48h(-3600), add48h(25200),3599, 3600)
        );
        ApplianceStatus applianceStatusAfterSwitchOn = getApplianceStatus(timeInitial);
        Assert.assertTrue(applianceStatusAfterSwitchOn.isOn());

        log("Update timeframe intervals right after min running time is reached");
        log("Timeframe interval created is deactivated and timeframe interval of schedule is activated");
        LocalDateTime timeAfterExpiration = toToday(11, 30, 1);
        runningTimeMonitor.updateActiveTimeframeInterval(timeAfterExpiration);

        log("Check values after expiration");
        assertRunningTime(timeAfterExpiration, control, runningTimeMonitor, false,false, false,
                0, 3600, null);
        assertPlanningRequest(timeAfterExpiration,
                new Timeframe(applianceId,0, 23399,3599, 3600),
                new Timeframe(applianceId, add24h(-5401), add24h(23399),3599, 3600),
                new Timeframe(applianceId, add48h(-5401), add48h(23399),3599, 3600)
        );
        ApplianceStatus applianceStatusAfterSwitchOff = getApplianceStatus(timeAfterExpiration);
        Assert.assertFalse(applianceStatusAfterSwitchOff.isOn());
    }

    @Test
    public void testSwitchOnAndOff_startingCurrentDetectedDuringTimeframeInterval() {
        LocalDateTime timeInitial = toToday(11, 29, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(true)
                .withMockMeter()
                .withSchedule(10, 0, 18, 0, 3600, null)
                .init();

        Appliance appliance = builder.getAppliance();
        StartingCurrentSwitch control = (StartingCurrentSwitch) appliance.getControl();
        Meter meter = appliance.getMeter();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();

        log("Check initial values");
        assertRunningTime(null, control, runningTimeMonitor, false, true, false, false,
                false, null, null, null);

        log("Detect starting current");
        Mockito.when(meter.getAveragePower()).thenReturn(StartingCurrentSwitchDefaults.getPowerThreshold() + 1);
        control.detectStartingCurrent(timeInitial, meter);
        assertRunningTime(timeInitial, control, runningTimeMonitor, false, true, false, false,
                false, null, null, null);
        Assert.assertEquals(0, sempController.createDevice2EM(timeInitial).getPlanningRequest().size());

        LocalDateTime timeStartingCurrent = toToday(11, 30, 0);
        control.detectStartingCurrent(toToday(11, 30, 0), meter);
        assertRunningTime(timeStartingCurrent, control, runningTimeMonitor, false, false, false, false,
                true, 0, 3600, null);
        assertPlanningRequest(timeStartingCurrent, new Timeframe(applianceId,0, 23400,3599, 3600));

        log("Switch on");
        LocalDateTime timeSwitchOn = toToday(12, 0, 0);
        sempController.em2Device(timeSwitchOn, createEM2Device(applianceId,true));

        log("Check values after switch on");
        assertRunningTime(timeSwitchOn, control, runningTimeMonitor, true, true, true, false,
                true, 0, 3600, null);
        assertPlanningRequest(timeSwitchOn, new Timeframe(applianceId,0, 21600,3599, 3600));
        ApplianceStatus applianceStatusAfterSwitchOn = getApplianceStatus(timeSwitchOn);
        Assert.assertTrue(applianceStatusAfterSwitchOn.isOn());

        log("Switch off");
        LocalDateTime timeSwitchOff = toToday(13, 0, 0);
        sempController.em2Device(timeSwitchOff, createEM2Device(applianceId,false));

        log("Check values after switch off");
        assertRunningTime(timeSwitchOff, control, runningTimeMonitor, false, true,false, true,
                true, 3600, 0, null);
        assertPlanningRequest(timeStartingCurrent, new Timeframe(applianceId,0, 23400,0, 0));

        // TODO nochmal an/aus schalten
    }

    @Test
    public void testNoSwitchOn_startingCurrentDetectedDuringTimeframeInterval() {
        LocalDateTime timeInitial = toToday(11, 29, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(true)
                .withMockMeter()
                .withSchedule(10, 0, 13, 0, 3600, null)
                .init();

        Appliance appliance = builder.getAppliance();
        StartingCurrentSwitch control = (StartingCurrentSwitch) appliance.getControl();
        Meter meter = appliance.getMeter();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();

        log("Check initial values");
        assertRunningTime(null, control, runningTimeMonitor, false, true, false, false,
                false, null, null, null);

        log("Detect starting current");
        Mockito.when(meter.getAveragePower()).thenReturn(StartingCurrentSwitchDefaults.getPowerThreshold() + 1);
        control.detectStartingCurrent(timeInitial, meter);
        assertRunningTime(timeInitial, control, runningTimeMonitor, false, true, false, false,
                false, null, null, null);
        Assert.assertEquals(0, sempController.createDevice2EM(timeInitial).getPlanningRequest().size());

        LocalDateTime timeStartingCurrent = toToday(11, 30, 0);
        control.detectStartingCurrent(toToday(11, 30, 0), meter);
        assertRunningTime(timeStartingCurrent, control, runningTimeMonitor, false, false, false, false,
                true, 0, 3600, null);
        assertPlanningRequest(timeStartingCurrent, new Timeframe(applianceId,0, 5400,3599, 3600));

        log("If not switched on during timeframe a new timeframe should exist after timeframe end");
        LocalDateTime timeAfterTimeframeEnd = toToday(13, 1, 0);
        runningTimeMonitor.updateActiveTimeframeInterval(timeAfterTimeframeEnd);
        assertPlanningRequest(timeStartingCurrent, new Timeframe(applianceId,81000, 91800,3599, 3600));
    }

    // @Test
    public void testSwitchOnAndWaitForTimeframeEnd() {
    }

    @Test
    public void testSwitchOnBeforeTimeframeIntervalStart() {
        LocalDateTime timeInitial = toToday(9, 59, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(false)
                .withMockMeter()
                .withSchedule(10, 0, 18, 0, 3600, null)
                .init();
        Appliance appliance = builder.getAppliance();
        Control control = (MockSwitch) appliance.getControl();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();

        log("Switch on");
        sempController.em2Device(timeInitial, createEM2Device(applianceId,true));
        Assert.assertTrue("It should be possible to set appliance control state to running before " +
                "the timeframe interval started", runningTimeMonitor.isRunning());

        log("Check values right after switch on before interval start");
        assertRunningTime(timeInitial, control, runningTimeMonitor, true, true, false,
                false, null, null, null);

        log("Check values after switch on right before interval start");
        LocalDateTime timeBeforeIntervalStart = toToday(9, 59, 59);
        runningTimeMonitor.updateActiveTimeframeInterval(timeBeforeIntervalStart);
        assertRunningTime(timeBeforeIntervalStart, control, runningTimeMonitor, true, true, false,
                false, null, null, null);

        log("Check values after switch on right after interval start");
        LocalDateTime timeIntervalStart = toToday(10, 0, 0);
        runningTimeMonitor.updateActiveTimeframeInterval(timeIntervalStart);
        assertRunningTime(timeIntervalStart, control, runningTimeMonitor, true, true, false,
                true, 60, 3540, null);
    }

    private void log(String message) {
        logger.debug("*********** " + message);
    }

    private ApplianceStatus getApplianceStatus(LocalDateTime now) {
        List<ApplianceStatus> applianceStatuses = saeController.getApplianceStatus(now);
        return applianceStatuses.get(0);
    }


    private EM2Device createEM2Device(String applianceId, boolean on) {
        DeviceControl deviceControl = new DeviceControl();
        deviceControl.setDeviceId(applianceId);
        deviceControl.setOn(on);

        List<DeviceControl> deviceControls = new ArrayList<>();
        deviceControls.add(deviceControl);

        EM2Device em2Device = new EM2Device();
        em2Device.setDeviceControl(deviceControls);
        return em2Device;
    }

    private void assertRunningTime(LocalDateTime now, Control control, RunningTimeMonitor runningTimeMonitor,
                                    boolean on, boolean running, boolean interrupted,
                                    Integer runningTime,
                                    Integer remainingMinRunningTime, Integer remainingMaxRunningTime) {
        assertRunningTime(now, control, runningTimeMonitor, on, running, interrupted, true,
                runningTime, remainingMinRunningTime, remainingMaxRunningTime);
    }

    private void assertRunningTime(LocalDateTime now, Control control, RunningTimeMonitor runningTimeMonitor,
                                   boolean on, boolean applianceOn, boolean running, boolean interrupted,
                                   boolean activateTimeframeIntervalNotNull,  Integer runningTime,
                                   Integer remainingMinRunningTime, Integer remainingMaxRunningTime) {
        assertRunningTime(now, control, runningTimeMonitor, on, running, interrupted, activateTimeframeIntervalNotNull,
                runningTime, remainingMinRunningTime, remainingMaxRunningTime);
        Assert.assertEquals(applianceOn, ((StartingCurrentSwitch) control).isApplianceOn());
        Assert.assertNull(runningTimeMonitor.getSchedules());
    }

    private void assertRunningTime(LocalDateTime now, Control control, RunningTimeMonitor runningTimeMonitor,
                                   boolean on, boolean running, boolean interrupted,
                                   boolean activateTimeframeIntervalNotNull, Integer runningTime,
                                   Integer remainingMinRunningTime, Integer remainingMaxRunningTime) {
        Assert.assertEquals(on, control.isOn());
        Assert.assertEquals(running, runningTimeMonitor.isRunning());
        Assert.assertEquals(interrupted, runningTimeMonitor.isInterrupted());
        if (activateTimeframeIntervalNotNull) {
            Assert.assertNotNull(runningTimeMonitor.getActiveTimeframeInterval());
        }
        Assert.assertEquals(runningTime, runningTimeMonitor.getRunningTimeOfCurrentTimeFrame(now));
        Assert.assertEquals(remainingMinRunningTime, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now));
        Assert.assertEquals(remainingMaxRunningTime, runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now));
    }

    private void assertPlanningRequest(LocalDateTime now, Timeframe... expectedTimeframes) {
        List<PlanningRequest> planningRequests = sempController.createDevice2EM(now).getPlanningRequest();
        Assert.assertEquals(1, planningRequests.size());
        List<Timeframe> timeframes = planningRequests.get(0).getTimeframes();
        Assert.assertEquals(expectedTimeframes.length, timeframes.size());
        for(int i=0; i<expectedTimeframes.length; i++) {
            Assert.assertEquals("Timeframes not equals for index=" + i, expectedTimeframes[i], timeframes.get(i));
        }
    }

    private int add24h(int seconds) {
        return seconds + 86400;
    }

    private int add48h(int seconds) {
        return seconds + 2 * 86400;
    }
}
