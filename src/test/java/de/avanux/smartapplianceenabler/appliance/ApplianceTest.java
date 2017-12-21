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
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

public class ApplianceTest extends TestBase {
    private Logger logger;
    private Appliance appliance;

    public ApplianceTest() {
        String applianceId = "F-001";
        this.appliance = new Appliance();
        this.appliance.setId(applianceId);
    }

    @Test
    public void createSempTimeFrame() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, 7200,
                new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();
        schedule.getTimeframe().setSchedule(schedule);

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getMinRunningTime(),
                null, now);
        Assert.assertEquals(1800, (long) runtimeRequest.getEarliestStart());
        Assert.assertEquals(30600, (long) runtimeRequest.getLatestEnd());
    }

    @Test
    public void createSempTimeFrame_TimeFrameOverMidnight_BeforeMidnight() {
        LocalDateTime now = toToday(23, 30, 0);
        Schedule schedule = new Schedule(7200, 7200,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getMinRunningTime(),
                null, now);
        Assert.assertEquals(0, (long) runtimeRequest.getEarliestStart());
        Assert.assertEquals(16200, (long) runtimeRequest.getLatestEnd());
    }

    @Test
    public void createSempTimeFrame_TimeFrameOverMidnight_AfterMidnight() {
        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, 7200,
                new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        RuntimeRequest runtimeRequest = this.appliance.createRuntimeRequest(interval, schedule.getMinRunningTime(),
                null, now);
        Assert.assertEquals(0, (long) runtimeRequest.getEarliestStart());
        Assert.assertEquals(12600, (long) runtimeRequest.getLatestEnd());
    }
}
