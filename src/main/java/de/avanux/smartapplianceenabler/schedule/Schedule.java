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

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Schedule {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private boolean enabled = true;
    @XmlElements({
            @XmlElement(name = "RuntimeRequest", type = RuntimeRequest.class),
            @XmlElement(name = "EnergyRequest", type = EnergyRequest.class),
            @XmlElement(name = "SocRequest", type = SocRequest.class)
    })
    private Request request;
    @XmlElements({
            @XmlElement(name = "DayTimeframe", type = DayTimeframe.class),
            @XmlElement(name = "ConsecutiveDaysTimeframe", type = ConsecutiveDaysTimeframe.class)
    })
    private Timeframe timeframe;


    public Schedule() {
    }

    public Schedule(Integer minRunningTime, Integer maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd) {
        this(true, minRunningTime, maxRunningTime, earliestStart, latestEnd);
    }

    public Schedule(boolean enabled, Integer minRunningTime, Integer maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd) {
        this(enabled, minRunningTime, maxRunningTime, earliestStart, latestEnd, null);
    }

    public Schedule(boolean enabled, Integer minRunningTime, Integer maxRunningTime, TimeOfDay earliestStart, TimeOfDay latestEnd, List<Integer> daysOfWeekValues) {
        this.enabled = enabled;
        this.request = new RuntimeRequest(minRunningTime, maxRunningTime);
        this.timeframe = new DayTimeframe(earliestStart, latestEnd, daysOfWeekValues);
    }

    public Schedule(boolean enabled, Timeframe timeframe, Request request) {
        this.enabled = enabled;
        this.timeframe = timeframe;
        this.request = request;
    }

    public static Schedule withEnergyRequest(Integer minEnergy, Integer maxEnergy, TimeOfDay earliestStart, TimeOfDay latestEnd) {
        Schedule schedule = new Schedule();
        schedule.enabled = true;
        schedule.request = new EnergyRequest(minEnergy, maxEnergy);
        schedule.timeframe = new DayTimeframe(earliestStart, latestEnd);
        return schedule;
    }

    public String getId() {
        return id;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Timeframe getTimeframe() {
        if(timeframe.getSchedule() == null) {
            timeframe.setSchedule(this);
        }
        return timeframe;
    }

    @Override
    public String toString() {
        String text = "";

        if(timeframe != null) {
            text += timeframe.toString();
            text += "/";
        }
        if(request != null) {
            text += request.toString();

        }
        return text;
    }
}
