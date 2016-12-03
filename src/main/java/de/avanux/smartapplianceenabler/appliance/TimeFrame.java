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

    public boolean contains(LocalDateTime dateTime) {
        int today = dateTime.get(DateTimeFieldType.dayOfWeek());
        LocalTime time = new LocalTime(dateTime, DateTimeZone.getDefault());
        List<Integer> dowValues = getDaysOfWeekValues();
        LocalTime startTimeToday = new LocalTime(getInterval(dateTime).getStart());
        LocalTime endTimeToday = new LocalTime(getInterval(dateTime).getEnd());
        if(isOverMidnight()) {
            if(time.isAfter(startTimeToday)) {
                return endTimeToday.isBefore(time) && (dowValues == null || (dowValues != null && dowValues.contains(today)));
            }
            else {
                int yesterday = new LocalDateTime(dateTime).minusDays(1).get(DateTimeFieldType.dayOfWeek());
                return endTimeToday.isAfter(time) && (dowValues == null || (dowValues != null && dowValues.contains(yesterday)));
            }
        }
        else {
            return startTimeToday.isBefore(time) && endTimeToday.isAfter(time) && (dowValues == null || (dowValues != null && dowValues.contains(today)));
        }
    }

    /**
     * Returns the next timeframe becoming valid with respect to time of day and day of week.
     * @param now the time reference
     * @param timeFrames the list of timeframes to choose from
     * @return the next timeframe becoming valid or null
     */
    public static TimeFrame getNextTimeFrame(LocalDateTime now, List<TimeFrame> timeFrames) {
        if(timeFrames == null) {
            return null;
        }
        TimeFrame nextTimeFrame = null;
        Interval intervalUntilNextTimeFrameStart = null;
        for (TimeFrame timeFrame : timeFrames) {
            if(timeFrame.contains(now) && now.plusSeconds(timeFrame.getMaxRunningTime()).isBefore(new LocalDateTime(timeFrame.getInterval(now).getEnd()))) {
                // timeframe already started with remaining running time sufficient
                return timeFrame;
            }
            else {
                // timeframe not yet started or remaining running time insufficient
                List<Integer> dowValues = timeFrame.getDaysOfWeekValues();

                DateTime timeFrameStart = timeFrame.getInterval(now).getStart();
                int dow = timeFrameStart.get(DateTimeFieldType.dayOfWeek());
                if(now.isAfter(new LocalDateTime(timeFrame.getInterval(now).getStart())) || (dowValues != null && !dowValues.contains(dow))) {
                    // adjust timeframe according to valid days of week
                    for(int i=1;i<7;i++) {
                        timeFrameStart = timeFrameStart.plusDays(1);
                        dow = timeFrameStart.get(DateTimeFieldType.dayOfWeek());
                        if(dowValues == null || (dowValues != null && dowValues.contains(dow))) {
                            break;
                        }
                    }
                }

                // find the timeframe starting next
                Interval intervalUntilTimeFrameStart = new Interval(now.toDateTime(), timeFrameStart);
                if((intervalUntilNextTimeFrameStart == null || intervalUntilTimeFrameStart.toDurationMillis() < intervalUntilNextTimeFrameStart.toDurationMillis())) {
                    nextTimeFrame = timeFrame;
                    intervalUntilNextTimeFrameStart = intervalUntilTimeFrameStart;
                }
            }
        }
        return nextTimeFrame;
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
