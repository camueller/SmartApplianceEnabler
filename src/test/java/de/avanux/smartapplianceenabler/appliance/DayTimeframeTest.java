package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.apache.tomcat.jni.Local;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class DayTimeframeTest extends TestBase {

    @Test
    public void getIntervals_AllDayOfWeek_BeforeIntervalStart() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0));
        List<TimeframeInterval> intervals = timeframe.getIntervals(toToday(9, 0, 0));
        Assert.assertEquals(7, intervals.size());
        Assert.assertEquals(new Interval(toToday(10, 0, 0).toDateTime(), toToday(12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        Assert.assertEquals(new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
        Assert.assertEquals(new Interval(toDay(2, 10, 0, 0).toDateTime(), toDay(2, 12, 0, 0).toDateTime()), intervals.get(2).getInterval());
        Assert.assertEquals(new Interval(toDay(3, 10, 0, 0).toDateTime(), toDay(3, 12, 0, 0).toDateTime()), intervals.get(3).getInterval());
        Assert.assertEquals(new Interval(toDay(4, 10, 0, 0).toDateTime(), toDay(4, 12, 0, 0).toDateTime()), intervals.get(4).getInterval());
        Assert.assertEquals(new Interval(toDay(5, 10, 0, 0).toDateTime(), toDay(5, 12, 0, 0).toDateTime()), intervals.get(5).getInterval());
        Assert.assertEquals(new Interval(toDay(6, 10, 0, 0).toDateTime(), toDay(6, 12, 0, 0).toDateTime()), intervals.get(6).getInterval());
    }

    @Test
    public void getIntervals_AllDayOfWeek_WithinInterval() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0));
        List<TimeframeInterval> intervals = timeframe.getIntervals(toToday(11, 0, 0));
        Assert.assertEquals(7, intervals.size());
        Assert.assertEquals(new Interval(toToday(10, 0, 0).toDateTime(), toToday(12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        Assert.assertEquals(new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
        Assert.assertEquals(new Interval(toDay(2, 10, 0, 0).toDateTime(), toDay(2, 12, 0, 0).toDateTime()), intervals.get(2).getInterval());
        Assert.assertEquals(new Interval(toDay(3, 10, 0, 0).toDateTime(), toDay(3, 12, 0, 0).toDateTime()), intervals.get(3).getInterval());
        Assert.assertEquals(new Interval(toDay(4, 10, 0, 0).toDateTime(), toDay(4, 12, 0, 0).toDateTime()), intervals.get(4).getInterval());
        Assert.assertEquals(new Interval(toDay(5, 10, 0, 0).toDateTime(), toDay(5, 12, 0, 0).toDateTime()), intervals.get(5).getInterval());
        Assert.assertEquals(new Interval(toDay(6, 10, 0, 0).toDateTime(), toDay(6, 12, 0, 0).toDateTime()), intervals.get(6).getInterval());
    }

    @Test
    public void getIntervals_AllDayOfWeek_AfterIntervalEnd() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0));
        List<TimeframeInterval> intervals = timeframe.getIntervals(toToday(13, 0, 0));
        Assert.assertEquals(7, intervals.size());
        Assert.assertEquals(new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
        Assert.assertEquals(new Interval(toDay(2, 10, 0, 0).toDateTime(), toDay(2, 12, 0, 0).toDateTime()), intervals.get(1).getInterval());
        Assert.assertEquals(new Interval(toDay(3, 10, 0, 0).toDateTime(), toDay(3, 12, 0, 0).toDateTime()), intervals.get(2).getInterval());
        Assert.assertEquals(new Interval(toDay(4, 10, 0, 0).toDateTime(), toDay(4, 12, 0, 0).toDateTime()), intervals.get(3).getInterval());
        Assert.assertEquals(new Interval(toDay(5, 10, 0, 0).toDateTime(), toDay(5, 12, 0, 0).toDateTime()), intervals.get(4).getInterval());
        Assert.assertEquals(new Interval(toDay(6, 10, 0, 0).toDateTime(), toDay(6, 12, 0, 0).toDateTime()), intervals.get(5).getInterval());
        Assert.assertEquals(new Interval(toDay(7, 10, 0, 0).toDateTime(), toDay(7, 12, 0, 0).toDateTime()), intervals.get(6).getInterval());
    }

    @Test
    public void getIntervals_DayOfWeekNotMatching_BeforeIntervalStart() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Collections.singletonList(3));
        LocalDateTime now = toDayOfWeek(1, 9, 0, 0);
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        Assert.assertEquals(new Interval(toDayOfWeek(now, 3, 10, 0, 0).toDateTime(), toDayOfWeek(now, 3, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
    }

    @Test
    public void getIntervals_DayOfWeekNotMatching_WithinInterval() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), Collections.singletonList(3));
        LocalDateTime now = toDayOfWeek(3, 11, 0, 0);
        List<TimeframeInterval> intervals = timeframe.getIntervals(now);
        Assert.assertEquals(1, intervals.size());
        Assert.assertEquals(new Interval(toDayOfWeek(now, 3, 10, 0, 0).toDateTime(), toDayOfWeek(now, 3, 12, 0, 0).toDateTime()), intervals.get(0).getInterval());
    }

    @Test
    public void buildMidnightAdjustedInterval_NotOverMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(23, 0, 0));
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(9, 0, 0));
        Assert.assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toToday(23, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_BeforeInterval_BeforeMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(21, 0, 0));
        Assert.assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toDay(1, 2, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_WithinInterval_BeforeMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(23, 0, 0));
        Assert.assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toDay(1, 2, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_WithinInterval_AfterMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(1, 0, 0));
        Assert.assertEquals(new Interval(toDay(-1, 22, 0, 0).toDateTime(), toToday(2, 0, 0).toDateTime()), interval);
    }

    @Test
    public void buildMidnightAdjustedInterval_OverMidnight_AfterInterval_AfterMidnight() {
        DayTimeframe timeframe = new DayTimeframe(new TimeOfDay(22, 0, 0), new TimeOfDay(2, 0, 0));
        Interval interval = timeframe.buildMidnightAdjustedInterval(toToday(3, 0, 0));
        Assert.assertEquals(new Interval(toToday(22, 0, 0).toDateTime(), toDay(1, 2, 0, 0).toDateTime()), interval);
    }
}
