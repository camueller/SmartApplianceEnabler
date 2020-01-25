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

import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
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

    /**
     * Returns the current or next timeframe if the remaining time is greater than maximum running time; otherwise the next timeframe is returned.
     * @param now the time reference
     * @param schedules the list of timeframes to choose from (current timeframe has to be first)
     * @param onlyAlreadyStarted consider only timeframe intervals already started
     * @param onlySufficient if true consider timeframe already started only if time to interval end exceeds min running time
     * @return the next timeframe becoming valid or null
     */
    public static TimeframeInterval getCurrentOrNextTimeframeInterval(LocalDateTime now, List<Schedule> schedules, boolean onlyAlreadyStarted, boolean onlySufficient) {
        List<TimeframeInterval> timeframeIntervals = findTimeframeIntervals(now, null, schedules,
                onlyAlreadyStarted, onlySufficient);
        if(timeframeIntervals.size() > 0) {
            return timeframeIntervals.get(0);
        }
        return null;
    }

    /**
     * Returns timeframe intervals starting within a consideration interval.
     * If not consideration interval is given, all timeframe intervals are returned.
     * @param now
     * @param considerationInterval timeframe intervals have to start within this interval
     * @param schedules the schedules from which to create timeframe intervals
     * @param onlyAlreadyStarted if true a timeframe interval in only considered if it has started already
     * @param onlySufficient if true a timeframe interval in only considered if minRunningTime fits before latest end
     * @return a (possibly empty) list of timeframes sorted by starting time
     */
    public static List<TimeframeInterval> findTimeframeIntervals(LocalDateTime now, Interval considerationInterval,
                                                                 List<Schedule> schedules, boolean onlyAlreadyStarted, boolean onlySufficient) {
        List<TimeframeInterval> matchingTimeframeIntervals = new ArrayList<>();
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                if(schedule.isEnabled()) {
                    Request request = schedule.getRequest();
                    Timeframe timeframe = schedule.getTimeframe();
                    List<TimeframeInterval> timeframeIntervals = timeframe.getIntervals(now);
                    for (TimeframeInterval timeframeInterval : timeframeIntervals) {
                        boolean considerationIntervalOk = false;
                        boolean alreadyStartedCheckOk = false;
                        boolean sufficientCheckOk = false;
                        if (considerationInterval == null
                                || considerationInterval.contains(timeframeInterval.getInterval().getStart())
                                || timeframeInterval.getInterval().contains(considerationInterval.getStart())
                                ) {
                            considerationIntervalOk = true;
                        }
                        if(!onlyAlreadyStarted || timeframeInterval.getInterval().contains(now.toDateTime())) {
                            alreadyStartedCheckOk = true;
                        }
                        if (!onlySufficient
                                || (request instanceof RuntimeRequest
                                    && timeframeInterval.isIntervalSufficient(now, request.getMin(), request.getMax()))
                                || (request instanceof EnergyRequest)
                                || (request instanceof SocRequest)
                        ) {
                            sufficientCheckOk = true;
                        }
                        if(considerationIntervalOk && alreadyStartedCheckOk && sufficientCheckOk) {
                            matchingTimeframeIntervals.add(timeframeInterval);
                        }
                    }
                }
            }
        }
        matchingTimeframeIntervals.sort(new TimeframeIntervalComparator());
        return matchingTimeframeIntervals;
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
