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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.time.LocalDateTime;

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
        return toNextOccurrence(LocalDateTime.now());
    }

    public LocalDateTime toNextOccurrence() {
        return toNextOccurrence(LocalDateTime.now());
    }

    public LocalDateTime toNextOccurrence(LocalDateTime now) {
        LocalDateTime dateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
                getHour(), getMinute(), getSecond());
        while(dateTime.getDayOfWeek().getValue() != dayOfWeek) {
            dateTime = dateTime.plusDays(1);
        }
        return dateTime;
    }

    public LocalDateTime toLastOccurrence(LocalDateTime now) {
        LocalDateTime dateTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
                getHour(), getMinute(), getSecond());
        while(dateTime.getDayOfWeek().getValue() != dayOfWeek) {
            dateTime = dateTime.minusDays(1);
        }
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TimeOfDayOfWeek that = (TimeOfDayOfWeek) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(dayOfWeek, that.dayOfWeek)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(dayOfWeek)
                .toHashCode();
    }

    @Override
    public String toString() {
        return toLocalTime().toString() + "[" + (dayOfWeek != null ? dayOfWeek : "?") + "]";
    }
}
