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
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

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
    public void createRuntimeRequest() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getRequest().getMin(),
                null, now);
        Assert.assertEquals(1800, (int) runtimeRequest.getEarliestStart());
        Assert.assertEquals(30600, (int) runtimeRequest.getLatestEnd());
        Assert.assertEquals(7200, (int) runtimeRequest.getMinRunningTime());
        Assert.assertNull(runtimeRequest.getMaxRunningTime());
    }

    @Test
    public void createRuntimeRequest_MaxRunningTime() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(3600, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getRequest().getMin(),
                schedule.getRequest().getMax(), now);
        Assert.assertEquals(1800, (int) runtimeRequest.getEarliestStart());
        Assert.assertEquals(30600, (int) runtimeRequest.getLatestEnd());
        Assert.assertEquals(3600, (int) runtimeRequest.getMinRunningTime());
        Assert.assertEquals(7200, (int) runtimeRequest.getMaxRunningTime());
    }

    @Test
    public void createRuntimeRequest_MaxRunningTimeExceedsLatestEnd() {
        LocalDateTime now = toToday(8, 0, 0);
        Schedule schedule = new Schedule(3600, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getRequest().getMin(),
                schedule.getRequest().getMax(), now);
        Assert.assertEquals(0, (int) runtimeRequest.getEarliestStart());
        Assert.assertEquals(3600, (int) runtimeRequest.getLatestEnd());
        Assert.assertEquals(3600, (int) runtimeRequest.getMinRunningTime());
        Assert.assertEquals(3600, (int) runtimeRequest.getMaxRunningTime());
    }

    @Test
    public void createRuntimeRequest_TimeFrameOverMidnight_BeforeMidnight() {
        LocalDateTime now = toToday(23, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getRequest().getMin(),
                null, now);
        Assert.assertEquals(0, (int) runtimeRequest.getEarliestStart());
        Assert.assertEquals(16200, (int) runtimeRequest.getLatestEnd());
        Assert.assertEquals(7200, (int) runtimeRequest.getMinRunningTime());
        Assert.assertNull(runtimeRequest.getMaxRunningTime());
    }

    @Test
    public void createRuntimeRequest_TimeFrameOverMidnight_AfterMidnight() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, null,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getRequest().getMin(),
                null, now);
        Assert.assertEquals(0, (int) runtimeRequest.getEarliestStart());
        Assert.assertEquals(12600, (int) runtimeRequest.getLatestEnd());
        Assert.assertEquals(7200, (int) runtimeRequest.getMinRunningTime());
        Assert.assertNull(runtimeRequest.getMaxRunningTime());
    }

    @Test
    public void getRuntimeRequest_TimeFrameAlreadyStartedButNotYetActive() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(8, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));

        List<RuntimeRequest> runtimeRequests = this.appliance.getRuntimeRequests(now,
                Collections.singletonList(schedule), null, true,
                3600 - nowSeconds, null);

        Assert.assertEquals(3, runtimeRequests.size());
        Assert.assertEquals(new RuntimeRequest(0, 14400-nowSeconds,
                        3600-nowSeconds, null), runtimeRequests.get(0));
        Assert.assertEquals(new RuntimeRequest(86400-nowSeconds, 100800-nowSeconds,
                        3600, null), runtimeRequests.get(1));
        Assert.assertEquals(new RuntimeRequest(172800-nowSeconds, 187200-nowSeconds,
                3600, null), runtimeRequests.get(2));
    }

    @Test
    public void getRuntimeRequest_TimeFrameAlreadyStartedAndActive_Sufficient() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(8, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));
        TimeframeInterval activeTimeframeInterval = schedule.getTimeframe().getIntervals(now).get(0);

        List<RuntimeRequest> runtimeRequests = this.appliance.getRuntimeRequests(now,
                Collections.singletonList(schedule), activeTimeframeInterval, true,
                3600 - nowSeconds, null);

        Assert.assertEquals(3, runtimeRequests.size());
        Assert.assertEquals(new RuntimeRequest(0, 14400-nowSeconds,
                        3600-nowSeconds, null), runtimeRequests.get(0));
        Assert.assertEquals(new RuntimeRequest(86400-nowSeconds, 100800-nowSeconds,
                        3600, null), runtimeRequests.get(1));
        Assert.assertEquals(new RuntimeRequest(172800-nowSeconds, 187200-nowSeconds,
                        3600, null), runtimeRequests.get(2));
    }

    @Test
    public void getRuntimeRequest_TimeFrameAlreadyStartedAndActive_NotSufficient() {
        int nowSeconds = 10;
        LocalDateTime now = toToday(11, 0, nowSeconds);
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(8, 0, 0), new TimeOfDay(12, 0, 0));
        TimeframeInterval activeTimeframeInterval = schedule.getTimeframe().getIntervals(now).get(0);

        List<RuntimeRequest> runtimeRequests = this.appliance.getRuntimeRequests(now,
                Collections.singletonList(schedule), activeTimeframeInterval, true,
                3600 - nowSeconds, null);

        Assert.assertEquals(3, runtimeRequests.size());
        Assert.assertEquals(new RuntimeRequest(0, 3600-nowSeconds,
                3600-nowSeconds, null), runtimeRequests.get(0));
        Assert.assertEquals(new RuntimeRequest(75600-nowSeconds, 90000-nowSeconds,
                3600, null), runtimeRequests.get(1));
        Assert.assertEquals(new RuntimeRequest(162000-nowSeconds, 176400-nowSeconds,
                3600, null), runtimeRequests.get(2));
    }
}
