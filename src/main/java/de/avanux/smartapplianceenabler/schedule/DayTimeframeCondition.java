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

import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A condition identified by a time range being valid between start time and end time.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DayTimeframeCondition {
    @XmlAttribute
    private String idref;
    @XmlElement(name = "Start")
    private TimeOfDay start;
    @XmlElement(name = "End")
    private TimeOfDay end;

    public String getIdref() {
        return idref;
    }

    /**
     * Returns true, if the given time is contained by the interval identified by start and end.
     * @param now
     * @return
     */
    public boolean isMet(LocalDateTime now) {
        if(start.toLocalTime().isBefore(now.toLocalTime()) && end.toLocalTime().isAfter(now.toLocalTime())) {
            return true;
        }
        return false;
    }
}
