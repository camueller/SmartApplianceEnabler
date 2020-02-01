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
import de.avanux.smartapplianceenabler.control.ev.EVChargerControl;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import de.avanux.smartapplianceenabler.test.TestBuilder;
import de.avanux.smartapplianceenabler.util.DateTimeProvider;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ApplianceTest extends TestBase {
    private Appliance appliance;
    private String applianceId = "F-001";
    private Integer evId = 1;
    private Integer batteryCapacity = 40000;
    private DateTimeProvider dateTimeProvider = Mockito.mock(DateTimeProvider.class);
    private ElectricVehicleCharger evChargerSpy = Mockito.spy(new ElectricVehicleCharger());
    private EVChargerControl evChargerControl = Mockito.mock(EVChargerControl.class);
    private Meter meter = Mockito.mock(Meter.class);

    public ApplianceTest() {
        String applianceId = "F-001";
        this.appliance = new Appliance();
        this.appliance.setId(applianceId);
    }

    @Test
    public void createRuntimeInterval() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(now),
                null, now);
        assertEquals(1800, (int) runtimeInterval.getEarliestStart());
        assertEquals(30600, (int) runtimeInterval.getLatestEnd());
        assertEquals(7200, (int) runtimeInterval.getMinRunningTime());
        assertNull(runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_MaxRunningTime() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(3600, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(now),
                schedule.getRequest().getMax(now), now);
        assertEquals(1800, (int) runtimeInterval.getEarliestStart());
        assertEquals(30600, (int) runtimeInterval.getLatestEnd());
        assertEquals(3600, (int) runtimeInterval.getMinRunningTime());
        assertEquals(7200, (int) runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_MaxRunningTimeExceedsLatestEnd() {
        LocalDateTime now = toToday(8, 0, 0);
        Schedule schedule = new Schedule(3600, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(now),
                schedule.getRequest().getMax(now), now);
        assertEquals(0, (int) runtimeInterval.getEarliestStart());
        assertEquals(3600, (int) runtimeInterval.getLatestEnd());
        assertEquals(3600, (int) runtimeInterval.getMinRunningTime());
        assertEquals(3600, (int) runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_TimeFrameOverMidnight_BeforeMidnight() {
        LocalDateTime now = toToday(23, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(now),
                null, now);
        assertEquals(0, (int) runtimeInterval.getEarliestStart());
        assertEquals(16200, (int) runtimeInterval.getLatestEnd());
        assertEquals(7200, (int) runtimeInterval.getMinRunningTime());
        assertNull(runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_TimeFrameOverMidnight_AfterMidnight() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(now),
                null, now);
        assertEquals(0, (int) runtimeInterval.getEarliestStart());
        assertEquals(12600, (int) runtimeInterval.getLatestEnd());
        assertEquals(7200, (int) runtimeInterval.getMinRunningTime());
        assertNull(runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void getRuntimeIntervals_TimeFrameAlreadyStartedButNotYetActive() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(8, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(now,
                Collections.singletonList(schedule), null, true,
                3600 - nowSeconds, null);

        assertEquals(3, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 14400-nowSeconds,
                        3600-nowSeconds, null), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(86400-nowSeconds, 100800-nowSeconds,
                        3600, null), runtimeIntervals.get(1));
        assertEquals(new RuntimeInterval(172800-nowSeconds, 187200-nowSeconds,
                3600, null), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_RuntimeRequest_TimeFrameOfScheduleAlreadyStartedAndActive_Sufficient() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(8, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));
        TimeframeInterval activeTimeframeInterval = schedule.getTimeframe().getIntervals(now).get(0);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(now,
                Collections.singletonList(schedule), activeTimeframeInterval, true,
                3600 - nowSeconds, null);

        assertEquals(3, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 14400-nowSeconds,
                        3600-nowSeconds, null), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(86400-nowSeconds, 100800-nowSeconds,
                        3600, null), runtimeIntervals.get(1));
        assertEquals(new RuntimeInterval(172800-nowSeconds, 187200-nowSeconds,
                        3600, null), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_RuntimeRequest_TimeFrameOfScheduleAlreadyStartedAndActive_NotSufficient() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(11, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));
        TimeframeInterval activeTimeframeInterval = schedule.getTimeframe().getIntervals(now).get(0);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(now,
                Collections.singletonList(schedule), activeTimeframeInterval, true,
                3600 - nowSeconds, null);

        assertEquals(3, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 3600-nowSeconds,
                3600-nowSeconds, null), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(75600-nowSeconds, 90000-nowSeconds,
                3600, null), runtimeIntervals.get(1));
        assertEquals(new RuntimeInterval(162000-nowSeconds, 176400-nowSeconds,
                3600, null), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_RuntimeRequest_TimeFrameAlreadyStarted() {
        LocalDateTime timeInitial = toToday(11, 0, 0);
        Integer runtime = 1200;
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(false)
                .withActivatedTimeframeInterval(timeInitial, runtime)
                .init();
        Appliance appliance = builder.getAppliance();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
        TimeframeInterval activeTimeframeInterval = runningTimeMonitor.getActiveTimeframeInterval();

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(1, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, runtime, runtime, null), runtimeIntervals.get(0));
    }

    @Test
    public void getRuntimeIntervals_EnergyRequest_TimeFrameOfScheduleAlreadyStarted() {
        LocalDateTime timeInitial = toToday(11, 0, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withSchedule(10,0, 16, 0)
                .withEnergyRequest(5000, 5000)
                .init();
        Appliance appliance = builder.getAppliance();
        TimeframeInterval activeTimeframeInterval = appliance.getSchedules().get(0)
                .getTimeframe().getIntervals(timeInitial).get(0);

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(3, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 18000, 5000, 5000,
                true), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(82800, 104400,5000, 5000,
                true), runtimeIntervals.get(1));
        assertEquals(new RuntimeInterval(169200, 190800,5000, 5000,
                true), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_EnergyRequest_TimeFrameAlreadyStarted_NoMeter() {
        LocalDateTime timeInitial = toToday(11, 0, 0);
        LocalDateTime timeChargeEnd = toToday(13, 0, 0);
        Integer energy = 20000;
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMockSwitch(false)
                .withActivatedTimeframeInterval(timeInitial, energy, timeChargeEnd)
                .init();
        Appliance appliance = builder.getAppliance();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
        TimeframeInterval activeTimeframeInterval = runningTimeMonitor.getActiveTimeframeInterval();

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(1, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 7200, energy, energy, true),
                runtimeIntervals.get(0));
    }

    @Test
    public void getRuntimeIntervals_EnergyRequest_TimeFrameAlreadyStarted_Meter() {
        LocalDateTime timeInitial = toToday(11, 0, 0);
        LocalDateTime timeChargeEnd = toToday(13, 0, 0);
        Integer energy = 20000;
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withMeter(meter)
                .withMockSwitch(false)
                .withActivatedTimeframeInterval(timeInitial, energy, timeChargeEnd)
                .init();
        Appliance appliance = builder.getAppliance();
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
        TimeframeInterval activeTimeframeInterval = runningTimeMonitor.getActiveTimeframeInterval();
        Mockito.when(meter.getEnergy()).thenReturn(5.0f);

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(1, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 7200, 15000, 15000, true),
                runtimeIntervals.get(0));
    }

    @Test
    public void getRuntimeIntervals_EnergyRequest_TimeFrameOfScheduleNotYetStarted() {
        LocalDateTime timeInitial = toToday(9, 0, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withSchedule(10,0, 16, 0)
                .withEnergyRequest(5000, 5000)
                .init();
        Appliance appliance = builder.getAppliance();
        TimeframeInterval activeTimeframeInterval = appliance.getSchedules().get(0)
                .getTimeframe().getIntervals(timeInitial).get(0);

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(2, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(3600, 25200, 5000, 5000,
                true), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(90000, 111600,5000, 5000,
                true), runtimeIntervals.get(1));
    }

    @Test
    public void getRuntimeIntervals_SocRequest_TimeFrameOfScheduleAlreadyStarted() {
        LocalDateTime timeInitial = toToday(11, 0, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withEvCharger(evChargerSpy, evChargerControl)
                .withElectricVehicle(evId, batteryCapacity, 80, null)
                .withSchedule(10,0, 16, 0)
                .withSocRequest(evId, 80)
                .init();
        Appliance appliance = builder.getAppliance();
        Mockito.doReturn(60).when(evChargerSpy).getConnectedVehicleSoc();
        TimeframeInterval activeTimeframeInterval = appliance.getSchedules().get(0)
                .getTimeframe().getIntervals(timeInitial).get(0);

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(3, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(0, 18000, 8800, 8800,
                true), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(82800, 104400, 8800, 8800,
                true), runtimeIntervals.get(1));
        assertEquals(new RuntimeInterval(169200, 190800, 8800, 8800,
                true), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_SocRequest_TimeFrameOfScheduleNotYetStarted() {
        LocalDateTime timeInitial = toToday(9, 0, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, timeInitial)
                .withEvCharger(evChargerSpy, evChargerControl)
                .withElectricVehicle(evId, batteryCapacity, 80, null)
                .withSchedule(10,0, 16, 0)
                .withSocRequest(evId, 80)
                .init();
        Appliance appliance = builder.getAppliance();
        Mockito.doReturn(60).when(evChargerSpy).getConnectedVehicleSoc();
        TimeframeInterval activeTimeframeInterval = appliance.getSchedules().get(0)
                .getTimeframe().getIntervals(timeInitial).get(0);

        List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(timeInitial,
                appliance.getSchedules(), activeTimeframeInterval, true,
                3600, null);

        assertEquals(2, runtimeIntervals.size());
        assertEquals(new RuntimeInterval(3600, 25200, 8800, 8800,
                true), runtimeIntervals.get(0));
        assertEquals(new RuntimeInterval(90000, 111600, 8800, 8800,
                true), runtimeIntervals.get(1));
    }

    @Test
    public void getRuntimeIntervals_EV_vehicleConnected() {
        LocalDateTime now = toToday(11, 0, 0);

        RunningTimeMonitor runningTimeMonitor = new RunningTimeMonitor();
        this.appliance.setRunningTimeMonitor(runningTimeMonitor);

        Meter meter = Mockito.mock(Meter.class);
        float energyAlreadyCharged = 10.0f;
        Mockito.when(meter.getEnergy()).thenReturn(energyAlreadyCharged);
        this.appliance.setMeter(meter);

        ElectricVehicleCharger evCharger = Mockito.spy(new ElectricVehicleCharger());
        this.appliance.setControl(evCharger);
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setBatteryCapacity(40000);
        vehicle.setDefaultSocOptionalEnergy(60);
        Mockito.doReturn(true).when(evCharger).isVehicleConnected();
        Mockito.doReturn(false).when(evCharger).isCharging();
        Mockito.doReturn(false).when(evCharger).isInErrorState();
        //Mockito.doReturn(Collections.singletonList(vehicle)).when(evCharger).getVehicles();
        Mockito.doReturn(vehicle).when(evCharger).getConnectedVehicle();
        int connectedVehicleSoc = 10;
        Mockito.doReturn(connectedVehicleSoc).when(evCharger).getConnectedVehicleSoc();

        List<RuntimeInterval> nonEvOptionalEnergyIntervals = new ArrayList<>();
        RuntimeInterval nonEvOptionalEnergyInterval = new RuntimeInterval(3600, 7200,
                3600, null);
        nonEvOptionalEnergyIntervals.add(nonEvOptionalEnergyInterval);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(nonEvOptionalEnergyIntervals);
        assertEquals(2, runtimeIntervals.size());
        RuntimeInterval evOptionalEnergyInterval = new RuntimeInterval(0, 3599,
                0, 12000, true);
        assertEquals(runtimeIntervals.get(0), evOptionalEnergyInterval);
        assertEquals(runtimeIntervals.get(1), nonEvOptionalEnergyInterval);
    }

    @Test
    public void getRuntimeIntervals_EV_error() {
        ElectricVehicleCharger evCharger = Mockito.spy(new ElectricVehicleCharger());
        this.appliance.setControl(evCharger);
        Mockito.doReturn(true).when(evCharger).isInErrorState();

        List<RuntimeInterval> nonEvOptionalEnergyIntervals = new ArrayList<>();
        RuntimeInterval nonEvOptionalEnergyInterval = new RuntimeInterval(3600, 7200,
                3600, null);
        nonEvOptionalEnergyIntervals.add(nonEvOptionalEnergyInterval);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(nonEvOptionalEnergyIntervals);
        assertEquals(0, runtimeIntervals.size());
    }
}
