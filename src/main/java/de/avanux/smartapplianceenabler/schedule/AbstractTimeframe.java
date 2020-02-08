/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.Interval;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
abstract public class AbstractTimeframe {

    protected TimeframeInterval createTimeframeInterval(Interval interval, Schedule schedule) {
        Request clonedRequest = SerializationUtils.clone(schedule.getRequest());
        // "enabled" has to be transient since it is not contained in XML; therefore it has to be set after cloning
        clonedRequest.setEnabled(true);

        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, clonedRequest);
        timeframeInterval.addTimeframeIntervalStateChangedListener(clonedRequest);
        return timeframeInterval;
    }
}
