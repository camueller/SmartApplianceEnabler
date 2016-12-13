package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleTest extends TestBase {

    @Test
    public void getNextSufficientTimeframe_noTimeFrames() {
        Assert.assertNull(Schedule.getCurrentOrNextTimeframeInterval(toToday(20, 0, 0), null, false, true));
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeSufficient() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(schedules.get(0), Schedule.getCurrentOrNextTimeframeInterval(toToday(11, 0, 0), schedules, false, true).getTimeframe().getSchedule());
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeInsufficient_firstTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(schedules.get(1), Schedule.getCurrentOrNextTimeframeInterval(toToday(13, 0, 0), schedules, false, true).getTimeframe().getSchedule());
    }

    @Test
    public void getNextSufficientTimeframe_alreadyStarted_remainingRunningTimeInsufficient_secondTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(schedules.get(0), Schedule.getCurrentOrNextTimeframeInterval(toToday(17, 0, 0), schedules, false, true).getTimeframe().getSchedule());
    }

    @Test
    public void getNextSufficientTimeframe_notYetStarted_secondTimeFrameOfDay() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0)));
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0)));
        Assert.assertEquals(schedules.get(1), Schedule.getCurrentOrNextTimeframeInterval(toToday(14, 30, 0), schedules, false, true).getTimeframe().getSchedule());
    }

    @Test
    public void getNextSufficientTimeframe_timeFrameNotValidForDow() {
        LocalDateTime now = toToday(9, 0, 0);
        List<Schedule> schedules = new ArrayList<Schedule>();
        // Timeframe only valid yesterday
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(10, 0, 0), new TimeOfDay(14, 0, 0), Collections.singletonList(now.minusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        // Timeframe only valid tomorrow
        schedules.add(new Schedule(7200, 7200, new TimeOfDay(15, 0, 0), new TimeOfDay(18, 0, 0), Collections.singletonList(now.plusDays(1).get(DateTimeFieldType.dayOfWeek()))));
        Assert.assertEquals(schedules.get(1), Schedule.getCurrentOrNextTimeframeInterval(now, schedules, false, true).getTimeframe().getSchedule());
    }
}
