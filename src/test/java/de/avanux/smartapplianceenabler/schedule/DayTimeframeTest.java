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

package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.TestBase;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DayTimeframeTest extends TestBase {

    @Test
    public void getIntervals_AllDayOfWeek_BeforeIntervalStart() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        List<TimeframeInterval> intervals = timeframe.getIntervals(toToday(9, 0, 0));
        assertEquals(7, intervals.size());
        assertEquals(new Interval(toToday(10, 0, 0).toDateTime(), toToday(12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        assertEquals(new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
        assertEquals(new Interval(toDay(2, 10, 0, 0).toDateTime(), toDay(2, 12, 0, 0).toDateTime()), intervals.get(2).getInterval());
        assertEquals(new Interval(toDay(3, 10, 0, 0).toDateTime(), toDay(3, 12, 0, 0).toDateTime()), intervals.get(3).getInterval());
        assertEquals(new Interval(toDay(4, 10, 0, 0).toDateTime(), toDay(4, 12, 0, 0).toDateTime()), intervals.get(4).getInterval());
        assertEquals(new Interval(toDay(5, 10, 0, 0).toDateTime(), toDay(5, 12, 0, 0).toDateTime()), intervals.get(5).getInterval());
        assertEquals(new Interval(toDay(6, 10, 0, 0).toDateTime(), toDay(6, 12, 0, 0).toDateTime()), intervals.get(6).getInterval());
    }

    @Test
    public void getIntervals_AllDayOfWeek_WithinInterval() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        List<TimeframeInterval> intervals = timeframe.getIntervals(toToday(11, 0, 0));
        assertEquals(7, intervals.size());
        assertEquals(new Interval(toToday(10, 0, 0).toDateTime(), toToday(12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        assertEquals(new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
        assertEquals(new Interval(toDay(2, 10, 0, 0).toDateTime(), toDay(2, 12, 0, 0).toDateTime()), intervals.get(2).getInterval());
        assertEquals(new Interval(toDay(3, 10, 0, 0).toDateTime(), toDay(3, 12, 0, 0).toDateTime()), intervals.get(3).getInterval());
        assertEquals(new Interval(toDay(4, 10, 0, 0).toDateTime(), toDay(4, 12, 0, 0).toDateTime()), intervals.get(4).getInterval());
        assertEquals(new Interval(toDay(5, 10, 0, 0).toDateTime(), toDay(5, 12, 0, 0).toDateTime()), intervals.get(5).getInterval());
        assertEquals(new Interval(toDay(6, 10, 0, 0).toDateTime(), toDay(6, 12, 0, 0).toDateTime()), intervals.get(6).getInterval());
    }

    @Test
    public void getIntervals_AllDayOfWeek_AfterIntervalEnd() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        List<TimeframeInterval> intervals = timeframe.getIntervals(toToday(13, 0, 0));
        assertEquals(7, intervals.size());
        assertEquals(new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        assertEquals(new Interval(toDay(2, 10, 0, 0).toDateTime(), toDay(2, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
        assertEquals(new Interval(toDay(3, 10, 0, 0).toDateTime(), toDay(3, 12, 0, 0).toDateTime()), intervals.get(2).getInterval());
        assertEquals(new Interval(toDay(4, 10, 0, 0).toDateTime(), toDay(4, 12, 0, 0).toDateTime()), intervals.get(3).getInterval());
        assertEquals(new Interval(toDay(5, 10, 0, 0).toDateTime(), toDay(5, 12, 0, 0).toDateTime()), intervals.get(4).getInterval());
        assertEquals(new Interval(toDay(6, 10, 0, 0).toDateTime(), toDay(6, 12, 0, 0).toDateTime()), intervals.get(5).getInterval());
        assertEquals(new Interval(toDay(7, 10, 0, 0).toDateTime(), toDay(7, 12, 0, 0).toDateTime()), intervals.get(6).getInterval());
    }

    @Test
    public void getIntervals_DayOfWeekNotMatching_BeforeIntervalStart() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Collections.singletonList(3));
        timeframe.setSchedule(buildScheduleWithRequest());
        LocalDateTime now = toDayOfWeek(1, 9, 0, 0);
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        assertEquals(1, intervals.size());
        assertEquals(new Interval(toDayOfWeek(now, 3, 10, 0, 0).toDateTime(), toDayOfWeek(now, 3, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
    }

    @Test
    public void getIntervals_DayOfWeekNotMatching_WithinInterval() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Collections.singletonList(3));
        timeframe.setSchedule(buildScheduleWithRequest());
        LocalDateTime now = toDayOfWeek(3, 11, 0, 0);
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        assertEquals(1, intervals.size());
        assertEquals(new Interval(toDayOfWeek(now, 3, 10, 0, 0).toDateTime(), toDayOfWeek(now, 3, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
    }

    @Test
    public void getIntervals_ValidOnlyOnHoliday_HolidayNotSet() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Collections.singletonList(8));
        timeframe.setSchedule(buildScheduleWithRequest());
        LocalDateTime now = toDayOfWeek(1, 9, 0, 0);
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        assertEquals(0, intervals.size());
    }

    @Test
    public void getIntervals_ValidOnlyOnHoliday_HolidaySet() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Collections.singletonList(8));
        timeframe.setSchedule(buildScheduleWithRequest());
        LocalDateTime now = toDayOfWeek(1, 9, 0, 0);
        timeframe.setHolidays(Collections.singletonList(now.toLocalDate()));
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        assertEquals(1, intervals.size());
        assertEquals(new Interval(toDayOfWeek(now, 1, 10, 0, 0).toDateTime(), toDayOfWeek(now, 1, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
    }

    @Test
    public void getIntervals_ValidOnSundayAndHoliday_HolidayIsSunday() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Arrays.asList(7,8));
        timeframe.setSchedule(buildScheduleWithRequest());
        LocalDateTime now = toDayOfWeek(1, 9, 0, 0);
        timeframe.setHolidays(Collections.singletonList(now.toLocalDate().plusDays(6)));
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        assertEquals(1, intervals.size());
        assertEquals(new Interval(toDayOfWeek(now, 7, 10, 0, 0).toDateTime(), toDayOfWeek(now, 7, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
    }

    @Test
    public void getIntervals_ValidOnSundayAndHoliday_HolidayIsThursday() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Arrays.asList(7,8));
        timeframe.setSchedule(buildScheduleWithRequest());
        LocalDateTime now = toDayOfWeek(1, 9, 0, 0);
        timeframe.setHolidays(Collections.singletonList(now.toLocalDate().plusDays(3)));
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        assertEquals(2, intervals.size());
        assertEquals(new Interval(toDayOfWeek(now, 4, 10, 0, 0).toDateTime(), toDayOfWeek(now, 4, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        assertEquals(new Interval(toDayOfWeek(now, 7, 10, 0, 0).toDateTime(), toDayOfWeek(now, 7, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
    }

    @Test
    public void buildMidnightAdjustedInterval_NotOverMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(23, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(9, 0, 0));
        assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toToday(23, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_BeforeInterval_BeforeMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(21, 0, 0));
        assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toDay(1, 2, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_WithinInterval_BeforeMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(23, 0, 0));
        assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toDay(1, 2, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_WithinInterval_AfterMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(1, 0, 0));
        assertEquals(new Interval(toDay(-1, 22, 0, 0).toDateTime(), toToday(2, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_AfterInterval_AfterMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        timeframe.setSchedule(buildScheduleWithRequest());
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(3, 0, 0));
        assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toDay(1, 2, 0, 0).toDateTime()), interval);
    }
}
