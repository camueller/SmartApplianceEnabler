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
package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.*;
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

    public TimeOfDay(LocalDateTime dateTime) {
        hour = dateTime.get(DateTimeFieldType.hourOfDay());
        minute = dateTime.get(DateTimeFieldType.minuteOfHour());
        second = dateTime.get(DateTimeFieldType.secondOfMinute());
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public LocalTime toLocalTime() {
        return new LocalTime(hour, minute, second, 0, ISOChronology.getInstance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TimeOfDay timeOfDay = (TimeOfDay) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(hour, timeOfDay.hour)
                .append(minute, timeOfDay.minute)
                .append(second, timeOfDay.second)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(hour)
                .append(minute)
                .append(second)
                .toHashCode();
    }

    @Override
    public String toString() {
        return toLocalTime().toString();
    }
}
