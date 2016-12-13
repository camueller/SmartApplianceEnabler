package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collections;
import java.util.List;

/**
 * A time range being valid from the start time on one day of week until the end time on another day of week.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsecutiveDaysTimeframe implements Timeframe {
    @XmlElement(name = "Start")
    private TimeOfDayOfWeek start;
    @XmlElement(name = "End")
    private TimeOfDayOfWeek end;
    @XmlTransient
    private Schedule schedule;

    public ConsecutiveDaysTimeframe() {
    }

    public ConsecutiveDaysTimeframe(TimeOfDayOfWeek start, TimeOfDayOfWeek end) {
        this.start = start;
        this.end = end;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public List<TimeframeInterval> getIntervals(LocalDateTime now) {
        if(start != null && end != null) {
            LocalDateTime earliestStartNextOccurrence = start.toNextOccurrence(now);
            LocalDateTime latestEndNextOccurrence = end.toNextOccurrence(now);
            LocalDateTime earliestStartDateTime = earliestStartNextOccurrence;
            if(latestEndNextOccurrence.isBefore(earliestStartNextOccurrence) && now.isBefore(latestEndNextOccurrence)) {
                earliestStartDateTime = start.toLastOccurrence(now);
            }
            LocalDateTime latestEndDateTime = end.toNextOccurrence(earliestStartDateTime);
            Interval interval = new Interval(earliestStartDateTime.toDateTime(), latestEndDateTime.toDateTime()).withChronology(ISOChronology.getInstance());
            TimeframeInterval timeframeInterval = new TimeframeInterval(this, interval);
            return Collections.singletonList(timeframeInterval);
        }
        return null;
    }

    @Override
    public String toString() {
        String text = "";

        if(start != null) {
            text += start.toString();
        }
        else {
            text += "?";
        }
        text += "-";
        if(end != null) {
            text += end.toString();
        }
        else {
            text += "?";
        }

        return text;
    }
}
