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

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A time range being valid between start time and end time on particular days of week.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DayTimeframe extends AbstractTimeframe implements Timeframe {
    public static final int DOW_HOLIDAYS = 8;
    @XmlElement(name = "Start")
    private TimeOfDay start;
    @XmlElement(name = "End")
    private TimeOfDay end;
    @XmlElement(name = "DayOfWeek")
    private List<DayOfWeek> daysOfWeek;
    private transient Schedule schedule;
    private transient List<LocalDate> holidays;

    public DayTimeframe() {
    }

    public DayTimeframe(TimeOfDay start, TimeOfDay end) {
        this(start, end, null);
    }

    public DayTimeframe(TimeOfDay start, TimeOfDay end, List<Integer> daysOfWeekValues) {
        this.start = start;
        this.end = end;
        if(daysOfWeekValues != null) {
            this.daysOfWeek = new ArrayList<>();
            for(Integer value : daysOfWeekValues) {
                this.daysOfWeek.add(new DayOfWeek(value));
            }
        }
    }

    public TimeOfDay getStart() {
        return start;
    }

    public TimeOfDay getEnd() {
        return end;
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

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    protected Interval buildMidnightAdjustedInterval(LocalDateTime now) {
        if(start != null && end != null) {
            LocalDateTime earliestStartDateTime = LocalDate.from(now).atTime(start.toLocalTime());
            LocalDateTime latestEndDateTime = LocalDate.from(now).atTime(end.toLocalTime());
            if(isOverMidnight(earliestStartDateTime, latestEndDateTime)) {
                if(now.toLocalTime().isAfter(start.toLocalTime())) {
                    // before midnight
                    latestEndDateTime = latestEndDateTime.plusHours(24);
                }
                else if(now.toLocalTime().isBefore(end.toLocalTime())){
                    // after midnight, before end
                    earliestStartDateTime = earliestStartDateTime.minusHours(24);
                }
                else {
                    // after midnight, after end
                    latestEndDateTime = latestEndDateTime.plusHours(24);
                }
            }
            return new Interval(earliestStartDateTime, latestEndDateTime);
        }
        return null;
    }

    @Override
    public List<TimeframeInterval> getIntervals(LocalDateTime now) {
        List<TimeframeInterval> intervals = new ArrayList<>();
        if(start != null && end != null) {
            Interval interval = buildMidnightAdjustedInterval(now);
            List<Integer> dowValues = getDaysOfWeekValues();
            // if today's interval already ended we ignore today
            int dayOffset = (interval.getEnd().isBefore(now) ? 1 : 0);
            for(int i=dayOffset; i<7+dayOffset; i++) {
                LocalDateTime timeFrameStart = interval.getStart().plusDays(i);
                LocalDateTime timeFrameEnd = interval.getEnd().plusDays(i);
                if(dowValues != null) {
                    int dow = timeFrameStart.getDayOfWeek().getValue();
                    if(dowValues.contains(DOW_HOLIDAYS) && isHoliday(timeFrameStart.toLocalDate())) {
                        dow = DOW_HOLIDAYS;
                    }
                    if(dowValues.contains(dow)) {
                        intervals.add(createTimeframeInterval(new Interval(timeFrameStart, timeFrameEnd), schedule.getRequest()));
                    }
                }
                else {
                    intervals.add(createTimeframeInterval(new Interval(timeFrameStart, timeFrameEnd), schedule.getRequest()));
                }
            }
        }
        return intervals;
    }

    /**
     * Returns true, if the end time is after midnight.
     * @param earliestStartDateTime the start time
     * @param latestEndDateTime the end time
     * @return
     */
    private boolean isOverMidnight(LocalDateTime earliestStartDateTime, LocalDateTime latestEndDateTime) {
        return latestEndDateTime.isBefore(earliestStartDateTime);
    }

    public void setHolidays(List<LocalDate> holidays) {
        this.holidays = holidays;
    }

    protected boolean isHoliday(LocalDate date) {
        if(holidays != null && holidays.contains(date)) {
            return true;
        }
        return false;
    }

    private LocalDateTime toDateTimeToday(TimeOfDay timeOfDay) {
        return LocalDate.now().atTime(timeOfDay.toLocalTime());
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
        if(daysOfWeek != null) {
            List<String> dowStrings = new ArrayList<>();
            for(DayOfWeek dow : daysOfWeek) {
                dowStrings.add("" + dow.getValue());
            }
            text += "[" + StringUtils.join(dowStrings, ",") + "]";
        }

        return text;
    }
}
