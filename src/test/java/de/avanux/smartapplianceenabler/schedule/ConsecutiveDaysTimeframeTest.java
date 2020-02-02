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
import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsecutiveDaysTimeframeTest extends TestBase {

    @Ignore
    public void getIntervals_SameWeek_BeforeIntervalStart() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(5, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(7, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(1, 0, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Ignore
    public void getIntervals_SameWeek_WithinInterval() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(5, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(7, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(5, 18, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Ignore
    public void getIntervals_SameWeek_AfterInterval() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(5, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(7, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(7, 21, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Ignore
    public void getIntervals_AcrossWeeks_BeforeIntervalStart() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(6, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(1, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(3, 0, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Ignore
    public void getIntervals_AcrossWeeks_WithinInterval() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(6, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(1, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(7, 10, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toLastOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Ignore
    public void getIntervals_AcrossWeeks_AfterIntervalEnd() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(6, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(1, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(1, 21, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }
}
