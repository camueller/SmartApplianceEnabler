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

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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
    @XmlTransient
    DateTimeFormatter formatter = ISODateTimeFormat.basicTTimeNoMillis();

    public TimeFrame() {
    }

    public TimeFrame(long minRunningTime, long maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd) {
        this.minRunningTime = minRunningTime;
        this.maxRunningTime = maxRunningTime;
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
    }

    public long getMinRunningTime() {
        return minRunningTime;
    }

    public long getMaxRunningTime() {
        return maxRunningTime;
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
        Interval interval = getInterval();
        if(interval != null) {
            return formatter.print(interval.getStart().toLocalTime())
                    + "-" + formatter.print(interval.getEnd().toLocalTime()); 
        }
        return null;
    }
}
