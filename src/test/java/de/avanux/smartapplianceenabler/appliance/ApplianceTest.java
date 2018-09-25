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
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplianceTest extends TestBase {
    private Appliance appliance;

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

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(),
                null, now);
        Assert.assertEquals(1800, (int) runtimeInterval.getEarliestStart());
        Assert.assertEquals(30600, (int) runtimeInterval.getLatestEnd());
        Assert.assertEquals(7200, (int) runtimeInterval.getMinRunningTime());
        Assert.assertNull(runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_MaxRunningTime() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(3600, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(),
                schedule.getRequest().getMax(), now);
        Assert.assertEquals(1800, (int) runtimeInterval.getEarliestStart());
        Assert.assertEquals(30600, (int) runtimeInterval.getLatestEnd());
        Assert.assertEquals(3600, (int) runtimeInterval.getMinRunningTime());
        Assert.assertEquals(7200, (int) runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_MaxRunningTimeExceedsLatestEnd() {
        LocalDateTime now = toToday(8, 0, 0);
        Schedule schedule = new Schedule(3600, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(),
                schedule.getRequest().getMax(), now);
        Assert.assertEquals(0, (int) runtimeInterval.getEarliestStart());
        Assert.assertEquals(3600, (int) runtimeInterval.getLatestEnd());
        Assert.assertEquals(3600, (int) runtimeInterval.getMinRunningTime());
        Assert.assertEquals(3600, (int) runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_TimeFrameOverMidnight_BeforeMidnight() {
        LocalDateTime now = toToday(23, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(),
                null, now);
        Assert.assertEquals(0, (int) runtimeInterval.getEarliestStart());
        Assert.assertEquals(16200, (int) runtimeInterval.getLatestEnd());
        Assert.assertEquals(7200, (int) runtimeInterval.getMinRunningTime());
        Assert.assertNull(runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void createRuntimeInterval_TimeFrameOverMidnight_AfterMidnight() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeInterval runtimeInterval = this.appliance.createRuntimeRequestInterval(interval, schedule.getRequest().getMin(),
                null, now);
        Assert.assertEquals(0, (int) runtimeInterval.getEarliestStart());
        Assert.assertEquals(12600, (int) runtimeInterval.getLatestEnd());
        Assert.assertEquals(7200, (int) runtimeInterval.getMinRunningTime());
        Assert.assertNull(runtimeInterval.getMaxRunningTime());
    }

    @Test
    public void getRuntimeInterval_TimeFrameAlreadyStartedButNotYetActive() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(8, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(now,
                Collections.singletonList(schedule), null, true,
                3600 - nowSeconds, null);

        Assert.assertEquals(3, runtimeIntervals.size());
        Assert.assertEquals(new RuntimeInterval(0, 14400-nowSeconds,
                        3600-nowSeconds, null), runtimeIntervals.get(0));
        Assert.assertEquals(new RuntimeInterval(86400-nowSeconds, 100800-nowSeconds,
                        3600, null), runtimeIntervals.get(1));
        Assert.assertEquals(new RuntimeInterval(172800-nowSeconds, 187200-nowSeconds,
                3600, null), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_TimeFrameAlreadyStartedAndActive_Sufficient() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(8, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));
        TimeframeInterval activeTimeframeInterval = schedule.getTimeframe().getIntervals(now).get(0);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(now,
                Collections.singletonList(schedule), activeTimeframeInterval, true,
                3600 - nowSeconds, null);

        Assert.assertEquals(3, runtimeIntervals.size());
        Assert.assertEquals(new RuntimeInterval(0, 14400-nowSeconds,
                        3600-nowSeconds, null), runtimeIntervals.get(0));
        Assert.assertEquals(new RuntimeInterval(86400-nowSeconds, 100800-nowSeconds,
                        3600, null), runtimeIntervals.get(1));
        Assert.assertEquals(new RuntimeInterval(172800-nowSeconds, 187200-nowSeconds,
                        3600, null), runtimeIntervals.get(2));
    }

    @Test
    public void getRuntimeIntervals_TimeFrameAlreadyStartedAndActive_NotSufficient() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(11, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));
        TimeframeInterval activeTimeframeInterval = schedule.getTimeframe().getIntervals(now).get(0);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(now,
                Collections.singletonList(schedule), activeTimeframeInterval, true,
                3600 - nowSeconds, null);

        Assert.assertEquals(3, runtimeIntervals.size());
        Assert.assertEquals(new RuntimeInterval(0, 3600-nowSeconds,
                3600-nowSeconds, null), runtimeIntervals.get(0));
        Assert.assertEquals(new RuntimeInterval(75600-nowSeconds, 90000-nowSeconds,
                3600, null), runtimeIntervals.get(1));
        Assert.assertEquals(new RuntimeInterval(162000-nowSeconds, 176400-nowSeconds,
                3600, null), runtimeIntervals.get(2));
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
        Mockito.doReturn(true).when(evCharger).isVehicleConnected();
        Mockito.doReturn(false).when(evCharger).isInErrorState();

        List<RuntimeInterval> nonEvOptionalEnergyIntervals = new ArrayList<>();
        RuntimeInterval nonEvOptionalEnergyInterval = new RuntimeInterval(3600, 7200,
                3600, null);
        nonEvOptionalEnergyIntervals.add(nonEvOptionalEnergyInterval);

        List<RuntimeInterval> runtimeIntervals = this.appliance.getRuntimeIntervals(nonEvOptionalEnergyIntervals);
        Assert.assertEquals(2, runtimeIntervals.size());
        RuntimeInterval evOptionalEnergyInterval = new RuntimeInterval(0, 3599,
                0, 40000 - Float.valueOf(energyAlreadyCharged).intValue() * 1000, true);
        Assert.assertEquals(runtimeIntervals.get(0), evOptionalEnergyInterval);
        Assert.assertEquals(runtimeIntervals.get(1), nonEvOptionalEnergyInterval);
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
        Assert.assertEquals(0, runtimeIntervals.size());
    }
}
