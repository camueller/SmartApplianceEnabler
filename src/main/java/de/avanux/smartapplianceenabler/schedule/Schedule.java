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

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@XmlAccessorType(XmlAccessType.FIELD)
public class Schedule {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private boolean enabled = true;
    @XmlAttribute
    private Integer minRunningTime;
    @XmlAttribute
    private Integer maxRunningTime;
    @XmlElements({
            @XmlElement(name = "DayTimeframe", type = DayTimeframe.class),
            @XmlElement(name = "ConsecutiveDaysTimeframe", type = ConsecutiveDaysTimeframe.class)
    })
    private Timeframe timeframe;
    transient DateTimeFormatter formatter = ISODateTimeFormat.basicTTimeNoMillis();


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
        this.minRunningTime = minRunningTime;
        this.maxRunningTime = maxRunningTime;
        this.timeframe = new DayTimeframe(earliestStart, latestEnd, daysOfWeekValues);
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

    public Integer getMinRunningTime() {
        return minRunningTime;
    }

    public Integer getMaxRunningTime() {
        return maxRunningTime;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    /**
     * Returns the current or next timeframe if the remaining time is greater than maximum running time; otherwise the next timeframe is returned.
     * @param now the time reference
     * @param schedules the list of timeframes to choose from (current timeframe has to be first)
     * @param onlyAlreadyStarted consider only timeframe intervals already started
     * @param onlySufficient if true consider timeframe already started only if time to interval end exceeds min running time
     * @return the next timeframe becoming valid or null
     */
    public static TimeframeInterval getCurrentOrNextTimeframeInterval(LocalDateTime now, List<Schedule> schedules, boolean onlyAlreadyStarted, boolean onlySufficient) {
        if(schedules == null || schedules.size() == 0) {
            return null;
        }
        Map<Long,TimeframeInterval> startDelayOfTimeframeInterval = new TreeMap<>();
        for(Schedule schedule : schedules) {
            if(schedule.isEnabled()) {
                Timeframe timeframe = schedule.getTimeframe();
                timeframe.setSchedule(schedule);
                List<TimeframeInterval> timeframeIntervals = timeframe.getIntervals(now);
                for(TimeframeInterval timeframeInterval : timeframeIntervals) {
                    Interval interval = timeframeInterval.getInterval();
                    if(interval.contains(now.toDateTime())) {
                        // interval already started ...
                        if(onlySufficient) {
                            if(timeframeInterval.isIntervalSufficient(now, schedule.getMinRunningTime())) {
                                return timeframeInterval;
                            }
                        }
                        else {
                            return timeframeInterval;
                        }
                    }
                    else if (! onlyAlreadyStarted) {
                        // interval starts in future
                        startDelayOfTimeframeInterval.put(interval.getStartMillis() - now.toDateTime().getMillis(),
                                timeframeInterval);
                    }
                }
            }
        }
        if(startDelayOfTimeframeInterval.size() > 0) {
            Long startDelay = startDelayOfTimeframeInterval.keySet().iterator().next();
            return startDelayOfTimeframeInterval.get(startDelay);
        }
        return null;
    }

    /**
     * Returns schedules starting within the given interval.
     * @param considerationInterval
     * @return a (possibly empty) list of timeframes
     */
    public static List<TimeframeInterval> findTimeframeIntervals(LocalDateTime now, Interval considerationInterval, List<Schedule> schedules, boolean onlySufficient) {
        List<TimeframeInterval> matchingTimeframeIntervals = new ArrayList<>();
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                if(schedule.isEnabled()) {
                    Timeframe timeframe = schedule.getTimeframe();
                    List<TimeframeInterval> timeframeIntervals = timeframe.getIntervals(now);
                    for (TimeframeInterval timeframeInterval : timeframeIntervals) {
                        if (considerationInterval.contains(timeframeInterval.getInterval().getStart())
                                && (!onlySufficient || timeframeInterval.isIntervalSufficient(now,
                                schedule.getMinRunningTime()))) {
                            matchingTimeframeIntervals.add(timeframeInterval);
                        }
                    }
                }
            }
        }
        return matchingTimeframeIntervals;
    }

    /**
     * Returns timeframe intervals sorted by start time.
     * @param now
     * @param schedules
     * @return a (possibly empty) list of timeframes sorted by start time
     */
    public static List<TimeframeInterval> getSortedTimeframeIntervals(LocalDateTime now, List<Schedule> schedules) {
        Map<DateTime, TimeframeInterval> sortedTimeframeIntervals = new TreeMap<>();
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                if(schedule.isEnabled()) {
                    Timeframe timeframe = schedule.getTimeframe();
                    List<TimeframeInterval> timeframeIntervals = timeframe.getIntervals(now);
                    for (TimeframeInterval timeframeInterval : timeframeIntervals) {
                        sortedTimeframeIntervals.put(timeframeInterval.getInterval().getStart(), timeframeInterval);
                    }
                }
            }
        }
        return new ArrayList<>(sortedTimeframeIntervals.values());
    }

    @Override
    public String toString() {
        String text = "";

        if(timeframe != null) {
            text += timeframe.toString();
            text += "/";
        }

        text += minRunningTime + "s/" + maxRunningTime + "s";
        return text;
    }
}
