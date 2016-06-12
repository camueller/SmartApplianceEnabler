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

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;
import org.joda.time.LocalTime;
import org.joda.time.chrono.ISOChronology;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "hour", "minute", "second" })
public class TimeOfDay {
    @XmlAttribute
    private Integer hour;
    @XmlAttribute
    private Integer minute;
    @XmlAttribute
    private Integer second;

    public TimeOfDay() {
    }

    public TimeOfDay(Integer hour, Integer minute, Integer second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public TimeOfDay(Instant instant) {
        LocalTime localtime = new LocalTime(instant, ISOChronology.getInstance());
        DateTime dateTime = localtime.toDateTimeToday();
        hour = dateTime.get(DateTimeFieldType.hourOfDay());
        minute = dateTime.get(DateTimeFieldType.minuteOfHour());
        second = dateTime.get(DateTimeFieldType.secondOfMinute());
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public Integer getSecond() {
        return second;
    }

    public LocalTime toLocalTime() {
        return new LocalTime(hour, minute, second, 0, ISOChronology.getInstance());
    }
}
