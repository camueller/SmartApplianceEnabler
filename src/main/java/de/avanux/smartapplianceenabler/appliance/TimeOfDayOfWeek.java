package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimeOfDayOfWeek extends TimeOfDay {
    @XmlAttribute
    private Integer dayOfWeek;

    public TimeOfDayOfWeek() {
    }

    public TimeOfDayOfWeek(Integer dayOfWeek, Integer hour, Integer minute, Integer second) {
        super(hour, minute, second);
        this.dayOfWeek = dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalDateTime toLocalDateTime() {
        return toNextOccurrence(new LocalDateTime());
    }

    public LocalDateTime toNextOccurrence() {
        return toNextOccurrence(new LocalDateTime());
    }

    public LocalDateTime toNextOccurrence(LocalDateTime now) {
        LocalDateTime dateTime = new LocalDateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), getHour(), getMinute(), getSecond());
        while(dateTime.get(DateTimeFieldType.dayOfWeek()) != dayOfWeek) {
            dateTime = dateTime.plusDays(1);
        }
        return dateTime;
    }

    public LocalDateTime toLastOccurrence(LocalDateTime now) {
        LocalDateTime dateTime = new LocalDateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), getHour(), getMinute(), getSecond());
        while(dateTime.get(DateTimeFieldType.dayOfWeek()) != dayOfWeek) {
            dateTime = dateTime.minusDays(1);
        }
        return dateTime;
    }

    @Override
    public String toString() {
        return toLocalTime().toString() + "[" + (dayOfWeek != null ? dayOfWeek : "?") + "]";
    }
}
