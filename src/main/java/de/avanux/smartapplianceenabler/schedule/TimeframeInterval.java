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

import de.avanux.smartapplianceenabler.appliance.ActiveIntervalChangedListener;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A TimeframeInterval associates a timeframe with an interval.
 */
public class TimeframeInterval implements ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(TimeframeInterval.class);
    /** @deprecated */
    private Timeframe timeframe;
    private Interval interval;
    private Request request;
    private TimeframeIntervalState state;
    private transient Vector<TimeframeIntervalState> stateHistory = new Vector<>();
    private transient LocalDateTime stateChangedAt;
    private transient List<TimeframeIntervalStateChangedListener> timeframeStateChangedListeners = new ArrayList<>();
    private transient String applianceId;
    // FIXME make more generic
    private boolean triggeredByStartingCurrent;

    public TimeframeInterval(Timeframe timeframe, Interval interval, Request request) {
        this.timeframe = timeframe;
        this.interval = interval;
        this.request = request;
        initStateHistory();
    }

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    /** @deprecated */
    public Timeframe getTimeframe() {
        return timeframe;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Request getRequest() {
        return request;
    }

    public void addTimeframeIntervalStateChangedListener(TimeframeIntervalStateChangedListener listener) {
        this.timeframeStateChangedListeners.add(listener);
    }

    private void initStateHistory() {
        this.stateHistory.clear();
        stateHistory.add(TimeframeIntervalState.CREATED);
    }

    public void stateTransitionTo(LocalDateTime now, TimeframeIntervalState state) {
        TimeframeIntervalState previousState = this.stateHistory.lastElement();
        this.stateHistory.add(state);
        this.stateChangedAt = now;
        timeframeStateChangedListeners.forEach(listener -> {
            logger.debug("{}: Notifying {} {}", applianceId, ActiveIntervalChangedListener.class.getSimpleName(),
                    listener.getClass().getSimpleName());
            listener.onTimeframeIntervalStateChanged(now, previousState, state);
        });
    }

    public TimeframeIntervalState getState() {
        return stateHistory.lastElement();
    }

    public boolean wasInState(TimeframeIntervalState state) {
        return stateHistory.contains(state);
    }

    public boolean isActivatable(LocalDateTime now) {
        return now.toDateTime().isAfter(getInterval().getStart())
                && isIntervalSufficient(now, request.getMin(now), request.getMax(now));
    }

    public boolean isDeactivatable(LocalDateTime now) {
        return now.toDateTime().isAfter(getInterval().getEnd()) || getRequest().isFinished(now);
    }

    public boolean isIntervalSufficient(LocalDateTime now, Integer minRunningTime, Integer maxRunningTime) {
        int runningTime = minRunningTime != null ? minRunningTime : maxRunningTime;
        return now.isBefore(getLatestStart(now, new LocalDateTime(interval.getEnd()), runningTime));
    }

    public Integer getEarliestStartSeconds(LocalDateTime now) {
        Integer earliestStart = 0;
        if(interval.getStart().isAfter(now.toDateTime())) {
            earliestStart = Double.valueOf(
                    new Interval(now.toDateTime(), interval.getStart()).toDurationMillis() / 1000.0).intValue();
        }
        return earliestStart;
    }

    public Integer getLatestEndSeconds(LocalDateTime now) {
        LocalDateTime nowBeforeEnd = new LocalDateTime(now);
        if(now.toDateTime().isAfter(interval.getEnd())) {
            nowBeforeEnd = now.minusHours(24);
        }
        return Double.valueOf(
                new Interval(nowBeforeEnd.toDateTime(), interval.getEnd()).toDurationMillis() / 1000.0).intValue();
    }

    public static LocalDateTime getLatestStart(LocalDateTime now, LocalDateTime intervalEnd, Integer minRunningTime) {
        return intervalEnd.minusSeconds(minRunningTime);
    }

    /**
     * Returns the latest start.
     * @return the latest start time in seconds from now or
     * null if input values are null or latest start would be in the past
     */
    public Integer getLatestStartSeconds(LocalDateTime now) {
        return Double.valueOf(
                new Interval(now.toDateTime(), interval.getEnd().minusSeconds(getRequest().getMax(now)))
                        .toDurationMillis() / 1000.0).intValue();
    }

    // TriggeredBy: WallboxState
    public boolean isTriggeredByStartingCurrent() {
        return triggeredByStartingCurrent;
    }

    public void setTriggeredByStartingCurrent(boolean triggeredByStartingCurrent) {
        this.triggeredByStartingCurrent = triggeredByStartingCurrent;
    }

    @Override
    public String toString() {
        String text = "";
        if(interval != null) {
            text += interval.toString();
        }
//        if(timeframe != null) {
//            if (interval != null) {
//                text += "(";
//            }
//            text += timeframe.toString();
//            if (interval != null) {
//                text += ")";
//            }
//        }
        text += "=>";
        text += request;
        return text;
    }
}
