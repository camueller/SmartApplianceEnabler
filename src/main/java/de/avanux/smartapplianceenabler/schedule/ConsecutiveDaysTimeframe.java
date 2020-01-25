/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek;
import de.avanux.smartapplianceenabler.schedule.Timeframe;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.ISOChronology;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;

/**
 * A time range being valid from the start time on one day of week until the end time on another day of week.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsecutiveDaysTimeframe extends AbstractTimeframe implements Timeframe {
    @XmlElement(name = "Start")
    private TimeOfDayOfWeek start;
    @XmlElement(name = "End")
    private TimeOfDayOfWeek end;
    private transient Schedule schedule;

    public ConsecutiveDaysTimeframe() {
    }

    public ConsecutiveDaysTimeframe(TimeOfDayOfWeek start, TimeOfDayOfWeek end) {
        this.start = start;
        this.end = end;
    }

    public TimeOfDayOfWeek getStart() {
        return start;
    }

    public TimeOfDayOfWeek getEnd() {
        return end;
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
            TimeframeInterval timeframeInterval = createTimeframeInterval(this, interval, schedule);
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
