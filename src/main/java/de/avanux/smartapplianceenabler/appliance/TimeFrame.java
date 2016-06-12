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
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimeFrame {
    @XmlAttribute
    private long minRunningTime;
    @XmlAttribute
    private long maxRunningTime;
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

    public TimeFrame(long minRunningTime, long maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd) {
        this(minRunningTime, maxRunningTime, earliestStart, latestEnd, null);
    }

    public TimeFrame(long minRunningTime, long maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd, List<Integer> daysOfWeekValues) {
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

    public long getMinRunningTime() {
        return minRunningTime;
    }

    public long getMaxRunningTime() {
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
        if(earliestStart != null && latestEnd != null) {
            return new Interval(new Instant(earliestStart.toLocalTime().toDateTimeToday()), 
                    new Instant(latestEnd.toLocalTime().toDateTimeToday()))
            .withChronology(ISOChronology.getInstance());
        }
        return null;
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
