/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.appliance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimeFrame {
    @XmlAttribute
    private int minRunningTime;
    @XmlAttribute
    private int maxRunningTime;
    @XmlElement(name = "EarliestStart")
    private TimeOfDay earliestStart;
    @XmlElement(name = "LatestEnd")
    private TimeOfDay latestEnd;
    @XmlElement(name = "DayOfWeek")
    private List<DayOfWeek> daysOfWeek;
    @XmlTransient
    DateTimeFormatter formatter = ISODateTimeFormat.basicTTimeNoMillis();

    public TimeFrame() {
    }

    public TimeFrame(int minRunningTime, int maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd) {
        this(minRunningTime, maxRunningTime, earliestStart, latestEnd, null);
    }

    public TimeFrame(int minRunningTime, int maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd, List<Integer> daysOfWeekValues) {
        this.minRunningTime = minRunningTime;
        this.maxRunningTime = maxRunningTime;
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
        if(daysOfWeekValues != null) {
            this.daysOfWeek = new ArrayList<>();
            for(Integer value : daysOfWeekValues) {
                this.daysOfWeek.add(new DayOfWeek(value));
            }
        }
    }

    public int getMinRunningTime() {
        return minRunningTime;
    }

    public int getMaxRunningTime() {
        return maxRunningTime;
    }

    public List<Integer> getDaysOfWeekValues() {
        if(daysOfWeek == null) {
            return null;
        }
        List<Integer> values = new ArrayList<>();
        for(DayOfWeek dow : daysOfWeek) {
            values.add(dow.getValue());
        }
        return values;
    }

    public Interval getInterval() {
        return getInterval(new LocalDateTime());
    }

    public Interval getInterval(LocalDateTime localDateTime) {
        if(earliestStart != null && latestEnd != null) {
            LocalDateTime earliestStartDateTime = new LocalDate(localDateTime).toLocalDateTime(earliestStart.toLocalTime());
            LocalDateTime latestEndDateTime = new LocalDate(localDateTime).toLocalDateTime(latestEnd.toLocalTime());
            if(isOverMidnight(earliestStartDateTime, latestEndDateTime)) {
                if(localDateTime.toLocalTime().isAfter(earliestStart.toLocalTime())) {
                    // before midnight
                    latestEndDateTime = latestEndDateTime.plusHours(24);
                }
                else if(localDateTime.toLocalTime().isBefore(latestEnd.toLocalTime())){
                    // after midnight, before latestEnd
                    earliestStartDateTime = earliestStartDateTime.minusHours(24);
                }
                else {
                    // after midnight, after latestEnd
                    latestEndDateTime = latestEndDateTime.plusHours(24);
                }
            }
            return new Interval(earliestStartDateTime.toDateTime(), latestEndDateTime.toDateTime()).withChronology(ISOChronology.getInstance());
        }
        return null;
    }

    public boolean isOverMidnight() {
        return isOverMidnight(toDateTimeToday(earliestStart), toDateTimeToday(latestEnd));
    }

    public boolean isOverMidnight(LocalDateTime earliestStartDateTime, LocalDateTime latestEndDateTime) {
        return latestEndDateTime.isBefore(earliestStartDateTime);
    }

    private LocalDateTime toDateTimeToday(TimeOfDay timeOfDay) {
        return new LocalDate().toLocalDateTime(timeOfDay.toLocalTime());
    }

    @Override
    public String toString() {
        String text = "";

        if(daysOfWeek != null) {
            List<String> dowStrings = new ArrayList<>();
            for(DayOfWeek dow : daysOfWeek) {
                dowStrings.add("" + dow.getValue());
            }
            text += "[" + StringUtils.join(dowStrings, ",") + "]";
        }

        Interval interval = getInterval();
        if(interval != null) {
            if(text.length() > 0) {
                text += ":";
            }
            text += formatter.print(interval.getStart().toLocalTime())
                    + "-" + formatter.print(interval.getEnd().toLocalTime());
        }

        if(text.length() > 0) {
            text += ":";
        }
        text += minRunningTime + "s/" + maxRunningTime + "s";
        return text;
    }
}
