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

public class ScheduleTest extends TestBase {

    @Test
    public void getNextSufficientTimeframe_noTimeFrames() {
        Assert.assertNull(Schedule.getCurrentOrNextTimeframeInterval(toToday(20, 0, 0), null, false, false));
    }

    @Test
    public void getNextSufficientTimeframe_ignoreDisabledSchedules() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(false, 7200, 7200, new TimeOfDay(14, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertNull(Schedule.getCurrentOrNextTimeframeInterval(toToday(16, 0, 0), schedules, false, false));
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeSufficientWithAdditionalRunningTimeZero() {
        Schedule.setAdditionalRunningTime(0);
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(14, 0, 0), new TimeOfDay(18, 0, 0)));
        Interval expectedInterval = new Interval(toToday(10, 0, 0).toDateTime(), toToday(14, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(toToday(11, 59, 0), schedules, false, true).getInterval());
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeSufficientWithAdditionalRunningTimeInsufficient() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(14, 0, 0), new TimeOfDay(18, 0, 0)));
        Interval expectedInterval = new Interval(toToday(14, 0, 0).toDateTime(), toToday(18, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(toToday(11, 59, 0), schedules, false, true).getInterval());
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeInsufficient_firstTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(14, 0, 0), new TimeOfDay(18, 0, 0)));
        Interval expectedInterval = new Interval(toToday(14, 0, 0).toDateTime(), toToday(18, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(toToday(12, 1, 0), schedules, false, true).getInterval());
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeInsufficient_secondTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Interval expectedInterval = new Interval(toDay(1, 10, 0, 0).toDateTime(), toDay(1, 14, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(toToday(16, 1, 0), schedules, false, true).getInterval());
    }

    @Test
    public void getNextSufficientTimeframe_notYetStarted_secondTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Interval expectedInterval = new Interval(toToday(15, 0, 0).toDateTime(), toToday(18, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(toToday(14, 30, 0), schedules, false, false).getInterval());
    }

    @Test
    public void getNextSufficientTimeframe_timeFrameNotValidForDow() {
        LocalDateTime now = toToday(9, 0, 0);
        List<Schedule> schedules = new ArrayList<Schedule>();
        // Timeframe only valid yesterday
        schedules.add(new Schedule(true, 7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0), Collections.singletonList(now.minusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        // Timeframe only valid tomorrow
        schedules.add(new Schedule(true, 7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0), Collections.singletonList(now.plusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        Interval expectedInterval = new Interval(toDay(1, 15, 0, 0).toDateTime(), toDay(1, 18, 0, 0).toDateTime());
        Assert.assertEquals(expectedInterval, Schedule.getCurrentOrNextTimeframeInterval(now, schedules, false, false).getInterval());
    }
}
