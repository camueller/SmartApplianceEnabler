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
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ScheduleTest extends TestBase {

    @Test
    public void findTimeframeIntervals_noTimeFrames() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        Assert.assertEquals(0, Schedule.findTimeframeIntervals(
                toToday(16, 1), null, schedules,
                false, true).size());
    }

    @Test
    public void findTimeframeIntervals_beforeIntervalStart() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 16, 0);
        List<TimeframeInterval> timeframeIntervals = Schedule.findTimeframeIntervals(
                toToday(9, 0), null, schedules,
                false, true);
        Assert.assertEquals(7, timeframeIntervals.size());
        for(int i=0; i<7; i++) {
            Assert.assertEquals("Error for i=" + i,
                    toInterval(i,10,0, i, 16, 0),
                    timeframeIntervals.get(i).getInterval());
        }
    }

    @Test
    public void findTimeframeIntervals_IntervalStartedAndSufficient() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 16, 0);
        List<TimeframeInterval> timeframeIntervals = Schedule.findTimeframeIntervals(
                toToday(14, 0), null, schedules,
                false, false);
        Assert.assertEquals(7, timeframeIntervals.size());
        for(int i=0; i<7; i++) {
            Assert.assertEquals("Error for i=" + i,
                    toInterval(i,10,0, i, 16, 0),
                    timeframeIntervals.get(i).getInterval());
        }
    }

    @Test
    public void findTimeframeIntervals_IntervalStartedButNotSufficient_OnlySufficientFalse() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 16, 0);
        List<TimeframeInterval> timeframeIntervals = Schedule.findTimeframeIntervals(
                toToday(15, 1), null, schedules,
                false, false);
        Assert.assertEquals(7, timeframeIntervals.size());
        for(int i=0; i<7; i++) {
            Assert.assertEquals("Error for i=" + i,
                    toInterval(i,10,0, i, 16, 0),
                    timeframeIntervals.get(i).getInterval());
        }
    }

    @Test
    public void findTimeframeIntervals_IntervalStartedButNotSufficient_OnlySufficientTrue() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 16, 0);
        List<TimeframeInterval> timeframeIntervals = Schedule.findTimeframeIntervals(
                toToday(15, 1), null, schedules,
                false, true);
        Assert.assertEquals(6, timeframeIntervals.size());
        for(int i=0; i<6; i++) {
            Assert.assertEquals("Error for i=" + i,
                    toInterval(i+1,10,0,
                            i+1, 16, 0),
                    timeframeIntervals.get(i).getInterval());
        }
    }

    @Test
    public void findTimeframeIntervals_multipleSchedules_differentDaysOfWeek() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 12, 0, dowList(3));
        addSchedule(schedules, 3600, 12, 0, 14, 0, dowList(2,4));
        addSchedule(schedules, 3600, 14, 0, 16, 0, dowList(1,5));
        List<TimeframeInterval> timeframeIntervals = Schedule.findTimeframeIntervals(
                toDayOfWeek(1,9, 0), null, schedules,
                false, true);
        Assert.assertEquals(5, timeframeIntervals.size());
        Assert.assertEquals(toIntervalByDow(1,14,0,1, 16, 0),
                timeframeIntervals.get(0).getInterval());
        Assert.assertEquals(toIntervalByDow(2,12,0,2, 14, 0),
                timeframeIntervals.get(1).getInterval());
        Assert.assertEquals(toIntervalByDow(3,10,0,3, 12, 0),
                timeframeIntervals.get(2).getInterval());
        Assert.assertEquals(toIntervalByDow(4,12,0,4, 14, 0),
                timeframeIntervals.get(3).getInterval());
        Assert.assertEquals(toIntervalByDow(5,14,0,5, 16, 0),
                timeframeIntervals.get(4).getInterval());
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_noTimeFrames() {
        Assert.assertNull(Schedule.getCurrentOrNextTimeframeInterval(toToday(20, 0),
                null, false, false));
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_ignoreDisabledSchedules() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, false, 7200, null,
                14, 0, 18, 0, null);
        Assert.assertNull(Schedule.getCurrentOrNextTimeframeInterval(toToday(16, 0),
                schedules, false, false));
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_alreadyStarted_remainingRunningTimeSufficient() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 7200, 14, 0, 18, 0);
        addSchedule(schedules, 7200, 10, 0, 14, 0);
        Assert.assertEquals(toIntervalToday(10, 0, 14, 0),
                Schedule.getCurrentOrNextTimeframeInterval(
                        toToday(11, 59), schedules,
                        false, true).getInterval());
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_alreadyStarted_remainingRunningTimeInsufficient_firstTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 7200, 14, 0, 18, 0);
        addSchedule(schedules, 7200, 10, 0, 14, 0);
        Assert.assertEquals(toIntervalToday(14, 0, 18, 0),
                Schedule.getCurrentOrNextTimeframeInterval(
                        toToday(12, 1), schedules,
                        false, true).getInterval());
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_alreadyStarted_remainingRunningTimeInsufficient_secondTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 7200, 15, 0, 18, 0);
        addSchedule(schedules, 7200, 10, 0, 14, 0);
        Interval expectedInterval = new Interval(toDay(1, 10, 0, 0).toDateTime(),
                toDay(1, 14, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(
                toToday(16, 1), schedules, false, true).getInterval());
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_notYetStarted_secondTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 7200, 15, 0, 18, 0);
        addSchedule(schedules, 7200, 10, 0, 14, 0);
        Assert.assertEquals(toIntervalToday(15, 0, 18, 0),
                Schedule.getCurrentOrNextTimeframeInterval(
                        toToday(14, 30), schedules,
                        false, true).getInterval());
    }

    @Test
    public void getCurrentOrNextTimeframeInterval_timeFrameNotValidForDow() {
        LocalDateTime now = toToday(9, 0, 0);
        List<Schedule> schedules = new ArrayList<Schedule>();
        // Timeframe only valid yesterday
        schedules.add(new Schedule(true, 7200, null,
                new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0),
                Collections.singletonList(now.minusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        // Timeframe only valid tomorrow
        schedules.add(new Schedule(true, 7200, null,
                new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0),
                Collections.singletonList(now.plusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        Assert.assertEquals(
                new Interval(toDay(1, 15, 0, 0).toDateTime(),
                toDay(1, 18, 0, 0).toDateTime()),
                Schedule.getCurrentOrNextTimeframeInterval(now, schedules,
                false, false).getInterval());
    }

    private void addSchedule(List<Schedule> schedules, Integer minRunningTime,
                             int startHour, int startMinutes, int endHour, int endMinutes) {
        addSchedule(schedules, true, minRunningTime, null, startHour, startMinutes,
                endHour, endMinutes, null);
    }

    private void addSchedule(List<Schedule> schedules, Integer minRunningTime,
                             int startHour, int startMinutes, int endHour, int endMinutes, List<Integer> daysOfWeekValues) {
        addSchedule(schedules, true, minRunningTime, null, startHour, startMinutes,
                endHour, endMinutes, daysOfWeekValues);
    }

    private void addSchedule(List<Schedule> schedules, Integer minRunningTime, Integer maxRunningTime,
                             int startHour, int startMinutes, int endHour, int endMinutes) {
        addSchedule(schedules, true, minRunningTime, maxRunningTime, startHour, startMinutes,
                endHour, endMinutes, null);
    }

    private void addSchedule(List<Schedule> schedules, boolean enabled, Integer minRunningTime, Integer maxRunningTime,
                             int startHour, int startMinutes, int endHour, int endMinutes, List<Integer> daysOfWeekValues) {
        schedules.add(new Schedule(enabled, minRunningTime, maxRunningTime,
                new TimeOfDay(startHour, startMinutes, 0), new TimeOfDay(endHour, endMinutes, 0),
                daysOfWeekValues));
    }

    private List<Integer> dowList(Integer... dows) {
        return Arrays.asList(dows);
    }
}
