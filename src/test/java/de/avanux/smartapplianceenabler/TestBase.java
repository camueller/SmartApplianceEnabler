package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.schedule.*;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    protected Interval toIntervalByDow(LocalDateTime now, Integer startDow, Integer startHour, Integer startMinutes,
                                       Integer endDow, Integer endHour, Integer endMinutes) {
        return toIntervalByDow(now, startDow, startHour, startMinutes, 0, endDow, endHour, endMinutes, 0);
    }

    protected Interval toIntervalByDow(LocalDateTime now, Integer startDow, Integer startHour, Integer startMinutes, Integer startSeconds,
                                  Integer endDow, Integer endHour, Integer endMinutes, Integer endSeconds) {
        return new Interval(toDayOfWeek(now, startDow, startHour, startMinutes, startSeconds).toDateTime(),
                toDayOfWeek(now, endDow, endHour, endMinutes, endSeconds).toDateTime());
    }

    protected void assertDateTime(LocalDateTime dt1, LocalDateTime dt2) {
        assertEquals(dt1.get(DateTimeFieldType.dayOfMonth()), dt2.get(DateTimeFieldType.dayOfMonth()));
    }

    protected void assertTimeframeIntervalOptionalEnergy(Interval interval,
                                                         TimeframeIntervalState state,
                                                         Integer socInitial,
                                                         Integer evId,
                                                         Integer energy,
                                                         boolean enabled,
                                                         TimeframeInterval actual) {
        OptionalEnergySocRequest request = new OptionalEnergySocRequest(evId, energy);
        request.setEnabled(enabled);
        request.setSocInitial(socInitial);
        request.setSoc(100);
        TimeframeInterval expected = new TimeframeInterval(interval, request);
        expected.initState(state);
        assertEquals(expected, actual);
    }

    protected void assertTimeframeIntervalSocRequest(TimeframeIntervalState state,
                                                     Interval interval,
                                                     Integer socInitial,
                                                     Integer soc,
                                                     Integer evId,
                                                     Integer energy,
                                                     boolean enabled,
                                                     TimeframeInterval actual) {
        SocRequest request = new SocRequest(soc, evId, energy);
        request.setSocInitial(socInitial);
        request.setEnabled(enabled);
        assertEquals(new TimeframeInterval(state, interval, request), actual);
    }
}
