package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Assert;

abstract public class TestBase {

    protected LocalDateTime toYesterday(Integer hour, Integer minute, Integer second) {
        return toDay(-1, hour, minute, second);
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

    protected LocalDateTime toDayOfWeek(int dayOfWeek, Integer hour, Integer minute, Integer second) {
        return toDayOfWeek(new LocalDateTime(), dayOfWeek, hour, minute, second);
    }

    protected LocalDateTime toDayOfWeek(LocalDateTime now, int dayOfWeek, Integer hour, Integer minute, Integer second) {
        TimeOfDayOfWeek timeOfDayOfWeek = new TimeOfDayOfWeek(dayOfWeek, hour, minute, second);
        return timeOfDayOfWeek.toNextOccurrence(now);
    }

    protected void assertDateTime(LocalDateTime dt1, LocalDateTime dt2) {
        Assert.assertEquals(dt1.get(DateTimeFieldType.dayOfMonth()), dt2.get(DateTimeFieldType.dayOfMonth()));
    }
}
