package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

public class TimeFrameTest extends TestBase {

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

    private void assertDateTime(LocalDateTime dt1, LocalDateTime dt2) {
        Assert.assertEquals(dt1.get(DateTimeFieldType.dayOfMonth()), dt2.get(DateTimeFieldType.dayOfMonth()));
    }
}
