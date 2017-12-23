package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Assert;

abstract public class TestBase {

    protected LocalDateTime toYesterday(Integer hour, Integer minute, Integer second) {
        return toDay(-1, hour, minute, second);
    }

    protected LocalDateTime toToday(Integer hour, Integer minute) {
        return toDay(0, hour, minute, 0);
    }

    protected LocalDateTime toToday(Integer hour, Integer minute, Integer second) {
        return toDay(0, hour, minute, second);
    }

    protected LocalDateTime toDay(Integer dayOffset, Integer hour, Integer minute, Integer second) {
        return new LocalDate().toLocalDateTime(new TimeOfDay(hour, minute, second).toLocalTime()).plusDays(dayOffset);
    }

    protected LocalDateTime toTomorrow(Integer hour, Integer minute, Integer second) {
        return toDay(1, hour, minute, second);
    }

    protected LocalDateTime toDayAfterTomorrow(Integer hour, Integer minute, Integer second) {
        return toDay(2, hour, minute, second);
    }

    protected LocalDateTime toDayOfWeek(int dayOfWeek, Integer hour, Integer minute) {
        return toDayOfWeek(new LocalDateTime(), dayOfWeek, hour, minute, 0);
    }

    protected LocalDateTime toDayOfWeek(int dayOfWeek, Integer hour, Integer minute, Integer second) {
        return toDayOfWeek(new LocalDateTime(), dayOfWeek, hour, minute, second);
    }

    protected LocalDateTime toDayOfWeek(LocalDateTime now, int dayOfWeek, Integer hour, Integer minute, Integer second) {
        TimeOfDayOfWeek timeOfDayOfWeek = new TimeOfDayOfWeek(dayOfWeek, hour, minute, second);
        return timeOfDayOfWeek.toNextOccurrence(now);
    }

    protected Interval toIntervalToday(Integer startHour, Integer startMinutes, Integer endHour, Integer endMinutes) {
        return toIntervalToday(startHour, startMinutes, 0, endHour, endMinutes, 0);
    }

    protected Interval toIntervalToday(Integer startHour, Integer startMinutes, Integer startSeconds,
                                       Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toToday(startHour, startMinutes, startSeconds).toDateTime(),
                toToday(endHour, endMinutes, endSeconds).toDateTime());
    }

    protected Interval toInterval(Integer startDayOffset, Integer startHour, Integer startMinutes,
                                  Integer endDayOffset, Integer endHour, Integer endMinutes) {
        return toInterval(startDayOffset, startHour, startMinutes, 0,
                endDayOffset, endHour, endMinutes, 0);
    }

    protected Interval toInterval(Integer startDayOffset, Integer startHour, Integer startMinutes, Integer startSeconds,
                                  Integer endDayOffset, Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toDay(startDayOffset, startHour, startMinutes, startSeconds).toDateTime(),
                toDay(endDayOffset, endHour, endMinutes, endSeconds).toDateTime());
    }

    protected Interval toIntervalByDow(Integer startDow, Integer startHour, Integer startMinutes,
                                       Integer endDow, Integer endHour, Integer endMinutes) {
        return toIntervalByDow(startDow, startHour, startMinutes, 0, endDow, endHour, endMinutes, 0);
    }

    protected Interval toIntervalByDow(Integer startDow, Integer startHour, Integer startMinutes, Integer startSeconds,
                                  Integer endDow, Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toDayOfWeek(startDow, startHour, startMinutes, startSeconds).toDateTime(),
                toDayOfWeek(endDow, endHour, endMinutes, endSeconds).toDateTime());
    }

    protected void assertDateTime(LocalDateTime dt1, LocalDateTime dt2) {
        Assert.assertEquals(dt1.get(DateTimeFieldType.dayOfMonth()), dt2.get(DateTimeFieldType.dayOfMonth()));
    }
}
