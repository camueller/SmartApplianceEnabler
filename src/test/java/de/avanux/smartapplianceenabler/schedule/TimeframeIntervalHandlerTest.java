/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.control.MockSwitch;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimeframeIntervalHandlerTest extends TestBase {

    private TimeframeIntervalHandler buildTimeframeIntervalHandler(List<Schedule> schedules) {
        return new TimeframeIntervalHandler(schedules, new MockSwitch());
    }

    private void addSchedule(List<Schedule> schedules, Integer maxRunningTime,
                             int startHour, int startMinutes, int endHour, int endMinutes) {
        addSchedule(schedules, true, null, maxRunningTime, startHour, startMinutes,
                endHour, endMinutes, null);
    }

    private void addSchedule(List<Schedule> schedules, Integer maxRunningTime,
                             int startHour, int startMinutes, int endHour, int endMinutes, List<Integer> daysOfWeekValues) {
        addSchedule(schedules, true, null, maxRunningTime, startHour, startMinutes,
                endHour, endMinutes, daysOfWeekValues);
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

    @Test
    public void findTimeframeIntervals_noSchedule() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        assertEquals(0, buildTimeframeIntervalHandler(schedules).findTimeframeIntervals(
                toToday(16, 1), null).size());
    }

    @Test
    public void findTimeframeIntervals_oneSchedule() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 16, 0);
        List<TimeframeInterval> timeframeIntervals = buildTimeframeIntervalHandler(schedules).findTimeframeIntervals(
                toToday(9, 0), null);
        assertEquals(7, timeframeIntervals.size());
        for(int i=0; i<7; i++) {
            assertEquals(toInterval(i,10,0, i, 16, 0),
                    timeframeIntervals.get(i).getInterval());
        }
    }

    @Test
    public void findTimeframeIntervals_multipleSchedules_differentDaysOfWeek() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        addSchedule(schedules, 3600, 10, 0, 12, 0, dowList(3));
        addSchedule(schedules, 3600, 12, 0, 14, 0, dowList(2,4));
        addSchedule(schedules, 3600, 14, 0, 16, 0, dowList(1,5));
        LocalDateTime now = toDayOfWeek(1, 9, 0);
        List<TimeframeInterval> timeframeIntervals = buildTimeframeIntervalHandler(schedules).findTimeframeIntervals(
                now, null);
        assertEquals(5, timeframeIntervals.size());
        assertEquals(toIntervalByDow(now,1,14,0,1, 16, 0),
                timeframeIntervals.get(0).getInterval());
        assertEquals(toIntervalByDow(now,2,12,0,2, 14, 0),
                timeframeIntervals.get(1).getInterval());
        assertEquals(toIntervalByDow(now,3,10,0,3, 12, 0),
                timeframeIntervals.get(2).getInterval());
        assertEquals(toIntervalByDow(now,4,12,0,4, 14, 0),
                timeframeIntervals.get(3).getInterval());
        assertEquals(toIntervalByDow(now,5,14,0,5, 16, 0),
                timeframeIntervals.get(4).getInterval());
    }
}
