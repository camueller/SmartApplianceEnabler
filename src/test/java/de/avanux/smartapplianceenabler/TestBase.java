package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.appliance.TimeOfDay;
import org.joda.time.*;

abstract public class TestBase {

    protected LocalDateTime toYesterday(Integer hour, Integer minute, Integer second) {
        return toToday(hour, minute, second).minusHours(24);
    }

    protected LocalDateTime toToday(Integer hour, Integer minute, Integer second) {
        return new LocalDate().toLocalDateTime(new TimeOfDay(hour, minute, second).toLocalTime()); //.withTime(hour, minute, second, 0);
    }

    protected LocalDateTime toTomorrow(Integer hour, Integer minute, Integer second) {
        return toToday(hour, minute, second).plusHours(24);
    }

    protected LocalDateTime toDayAfterTomorrow(Integer hour, Integer minute, Integer second) {
        return toToday(hour, minute, second).plusHours(48);
    }

    protected LocalDateTime toDayOfWeek(int dayOfWeek, Integer hour, Integer minute, Integer second) {
        LocalDateTime dateTime = new LocalDateTime(2016, 6, 13, hour, minute, second);
        dateTime = dateTime.plusDays(dayOfWeek - 1);
        return dateTime;
    }

}
