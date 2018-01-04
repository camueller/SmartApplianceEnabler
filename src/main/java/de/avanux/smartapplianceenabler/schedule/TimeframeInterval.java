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

/**
 * A TimeframeInterval associates a timeframe with an interval.
 */
public class TimeframeInterval {
    private Timeframe timeframe;
    private Interval interval;

    public TimeframeInterval(Timeframe timeframe, Interval interval) {
        this.timeframe = timeframe;
        this.interval = interval;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public Interval getInterval() {
        return interval;
    }

    public boolean isIntervalSufficient(LocalDateTime now, Integer minRunningTime) {
        return now.isBefore(getLatestStart(now, new LocalDateTime(interval.getEnd()), minRunningTime));
    }

    public static LocalDateTime getLatestStart(LocalDateTime now, LocalDateTime intervalEnd, Integer minRunningTime) {
        return intervalEnd.minusSeconds(minRunningTime);
    }

    /**
     * Returns the latest start.
     * @param intervalEnd the interval end in seconds from now
     * @param minRunningTime the minimum running time
     * @return the latest start time in seconds from now or
     * null if input values are null or latest start would be in the past
     */
    public static Integer getLatestStart(Integer intervalEnd, Integer minRunningTime) {
        if(intervalEnd != null && minRunningTime != null && intervalEnd > minRunningTime) {
            return intervalEnd - minRunningTime;
        }
        return null;
    }

    @Override
    public String toString() {
        String text = "";
        if(interval != null) {
            text += interval.toString();
        }
        if(timeframe != null) {
            if (interval != null) {
                text += "(";
            }
            text += timeframe.toString();
            if (interval != null) {
                text += ")";
            }
        }
        return text;
    }
}
