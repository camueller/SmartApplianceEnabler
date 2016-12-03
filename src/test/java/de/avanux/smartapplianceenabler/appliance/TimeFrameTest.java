package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeFrameTest extends TestBase {

    @Test
    public void contains() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0),
                Collections.singletonList(1));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(1, 14, 0, 0)));
        Assert.assertTrue(timeFrame.contains(toDayOfWeek(1, 16, 0, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(1, 19, 0, 0)));

        Assert.assertFalse(timeFrame.contains(toDayOfWeek(2, 14, 0, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(2, 16, 0, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(2, 19, 0, 0)));
    }

    @Test
    public void contains_TimeFrameOverMidnight() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(23, 0, 0), new TimeOfDay(1, 0, 0),
                Collections.singletonList(1));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(1, 22, 0, 0)));
        Assert.assertTrue(timeFrame.contains(toDayOfWeek(1, 23, 30, 0)));
        Assert.assertTrue(timeFrame.contains(toDayOfWeek(2, 0, 30, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(2, 2, 0, 0)));

        Assert.assertFalse(timeFrame.contains(toDayOfWeek(2, 22, 0, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(2, 23, 30, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(3, 0, 30, 0)));
        Assert.assertFalse(timeFrame.contains(toDayOfWeek(3, 2, 0, 0)));
    }

    @Test
    public void getInterval_BeforeTimeFrameStart() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0));
        LocalDateTime earliestStart = toToday(15, 0, 0);
        LocalDateTime latestEnd = toToday(18, 0, 0);

        Interval interval = timeFrame.getInterval(toToday(12, 0, 0));
        assertDateTime(earliestStart, interval.getStart().toLocalDateTime());
        assertDateTime(latestEnd, interval.getEnd().toLocalDateTime());
    }

    @Test
    public void getInterval_AfterTimeFrameStart() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0));
        LocalDateTime earliestStart = toToday(15, 0, 0);
        LocalDateTime latestEnd = toToday(18, 0, 0);

        Interval interval = timeFrame.getInterval(toToday(20, 0, 0));
        assertDateTime(earliestStart, interval.getStart().toLocalDateTime());
        assertDateTime(latestEnd, interval.getEnd().toLocalDateTime());
    }

    @Test
    public void getInterval_TimeFrameOverMidnight_BeforeMidnight() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        LocalDateTime earliestStart = toToday(23, 0, 0);
        LocalDateTime latestEnd = toTomorrow(1, 0, 0);

        Interval interval = timeFrame.getInterval(toToday(19, 30, 0));
        assertDateTime(earliestStart, interval.getStart().toLocalDateTime());
        assertDateTime(latestEnd, interval.getEnd().toLocalDateTime());
    }

    @Test
    public void getInterval_TimeFrameOverMidnight_AfterMidnight_WithinTimeFrame() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        LocalDateTime earliestStart = toYesterday(23, 0, 0);
        LocalDateTime latestEnd = toToday(1, 0, 0);

        Interval interval = timeFrame.getInterval(toToday(0, 30, 0));
        assertDateTime(earliestStart, interval.getStart().toLocalDateTime());
        assertDateTime(latestEnd, interval.getEnd().toLocalDateTime());
    }

    @Test
    public void getInterval_TimeFrameOverMidnight_AfterMidnight_AfterTimeFrame() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        LocalDateTime earliestStart = toToday(23, 0, 0);
        LocalDateTime latestEnd = toTomorrow(1, 0, 0);

        Interval interval = timeFrame.getInterval(toToday(5, 0, 0));
        assertDateTime(earliestStart, interval.getStart().toLocalDateTime());
        assertDateTime(latestEnd, interval.getEnd().toLocalDateTime());
    }

    @Test
    public void getNextTimeFrame_noTimeFrames() {
        Assert.assertNull(TimeFrame.getNextTimeFrame(toToday(20, 0, 0), null));
    }

    @Test
    public void getNextTimeFrame_alreadyStarted_remainingRunningTimeSufficient() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(timeFrames.get(0), TimeFrame.getNextTimeFrame(toToday(11, 0, 0), timeFrames));
    }

    @Test
    public void getNextTimeFrame_alreadyStarted_remainingRunningTimeInsufficient_firstTimeFrameOfDay() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(timeFrames.get(1), TimeFrame.getNextTimeFrame(toToday(13, 0, 0), timeFrames));
    }

    @Test
    public void getNextTimeFrame_alreadyStarted_remainingRunningTimeInsufficient_secondTimeFrameOfDay() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(timeFrames.get(0), TimeFrame.getNextTimeFrame(toToday(17, 0, 0), timeFrames));
    }

    @Test
    public void getNextTimeFrame_notYetStarted_secondTimeFrameOfDay() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(timeFrames.get(1), TimeFrame.getNextTimeFrame(toToday(14, 30, 0), timeFrames));
    }

    @Test
    public void getNextTimeFrame_timeFrameNotValidForDow() {
        LocalDateTime dateTime = toToday(9, 0, 0);
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        // Timeframe only valid yesterday
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0), Collections.singletonList(dateTime.minusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        // Timeframe only valid tomorrow
        timeFrames.add(new TimeFrame(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0), Collections.singletonList(dateTime.plusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        Assert.assertEquals(timeFrames.get(1), TimeFrame.getNextTimeFrame(dateTime, timeFrames));
    }

    private void assertDateTime(LocalDateTime dt1, LocalDateTime dt2) {
        Assert.assertEquals(dt1.get(DateTimeFieldType.dayOfMonth()), dt2.get(DateTimeFieldType.dayOfMonth()));
    }
}
