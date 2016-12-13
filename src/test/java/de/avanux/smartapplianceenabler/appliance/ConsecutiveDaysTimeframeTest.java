package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ConsecutiveDaysTimeframeTest extends TestBase {

    @Test
    public void getIntervals_SameWeek_BeforeIntervalStart() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(5, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(7, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(1, 0, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Test
    public void getIntervals_SameWeek_WithinInterval() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(5, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(7, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(5, 18, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Test
    public void getIntervals_SameWeek_AfterInterval() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(5, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(7, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(7, 21, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Test
    public void getIntervals_AcrossWeeks_BeforeIntervalStart() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(6, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(1, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(3, 0, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Test
    public void getIntervals_AcrossWeeks_WithinInterval() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(6, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(1, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(7, 10, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toLastOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }

    @Test
    public void getIntervals_AcrossWeeks_AfterIntervalEnd() {
        TimeOfDayOfWeek startTimeOfDayOfWeek = new TimeOfDayOfWeek(6, 15, 0, 0);
        TimeOfDayOfWeek endTimeOfDayOfWeek = new TimeOfDayOfWeek(1, 20, 0, 0);
        ConsecutiveDaysTimeframe timeRange = new ConsecutiveDaysTimeframe(startTimeOfDayOfWeek, endTimeOfDayOfWeek);
        LocalDateTime now = toDayOfWeek(1, 21, 0, 0);
        List<TimeframeInterval> intervals = timeRange.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        LocalDateTime start = startTimeOfDayOfWeek.toNextOccurrence(now);
        assertDateTime(start, intervals.get(0).getInterval().getStart().toLocalDateTime());
        assertDateTime(endTimeOfDayOfWeek.toNextOccurrence(start), intervals.get(0).getInterval().getEnd().toLocalDateTime());
    }
}
