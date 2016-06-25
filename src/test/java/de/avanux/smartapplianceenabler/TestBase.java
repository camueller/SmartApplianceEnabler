package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.appliance.TimeOfDay;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

abstract public class TestBase {

    protected DateTime toDateTimeToday(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday();
    }

    protected DateTime toDateTimeTomorrow(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday().plus(24 * 3600 * 1000);
    }

    protected Instant toInstant(Integer hour, Integer minute, Integer second) {
        return toDateTimeToday(hour, minute, second).toInstant();
    }

    protected Instant toInstantYesterday(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday().toInstant().minus(24 * 3600 * 1000);
    }

    protected Instant toInstantTomorrow(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday().toInstant().plus(24 * 3600 * 1000);
    }

    protected ReadableInstant toInstant(int dayOfWeek, Integer hour, Integer minute, Integer second) {
        DateTime instant = new DateTime(2016, 6, 13, hour, minute, second);
        instant = instant.plusHours((dayOfWeek - 1) * 24);
        return instant;
    }

}
