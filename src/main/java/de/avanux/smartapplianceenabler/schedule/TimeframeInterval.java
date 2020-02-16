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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

/**
 * A TimeframeInterval associates a timeframe with an interval.
 */
public class TimeframeInterval implements ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(TimeframeInterval.class);
    private Interval interval;
    private Request request;
    private transient Vector<TimeframeIntervalState> stateHistory = new Vector<>();
    private transient LocalDateTime stateChangedAt;
    private transient String applianceId;
    // FIXME make more generic
    private boolean triggeredByStartingCurrent;

    public TimeframeInterval(Interval interval, Request request) {
        this.interval = interval;
        this.request = request;
        initState(null);
    }

    public TimeframeInterval(TimeframeIntervalState state, Interval interval, Request request) {
        this.interval = interval;
        this.request = request;
        stateHistory.add(state);
    }

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
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

    public void initState(TimeframeIntervalState initialState) {
        this.stateHistory.clear();
        stateHistory.add(initialState != null ? initialState : TimeframeIntervalState.CREATED);
    }

    public void stateTransitionTo(LocalDateTime now, TimeframeIntervalState state) {
        TimeframeIntervalState previousState = this.stateHistory.lastElement();
        this.stateHistory.add(state);
        this.stateChangedAt = now;
    }

    public TimeframeIntervalState getState() {
        return stateHistory.lastElement();
    }

    public boolean wasInState(TimeframeIntervalState state) {
        return stateHistory.contains(state);
    }

    public boolean isActivatable(LocalDateTime now) {
        return getState() == TimeframeIntervalState.QUEUED
                && (now.toDateTime().isEqual(getInterval().getStart()) || now.toDateTime().isAfter(getInterval().getStart()))
                && (!(request instanceof RuntimeRequest) || isIntervalSufficient(now, request.getMax(now))
        );
    }

    public boolean isDeactivatable(LocalDateTime now) {
        return getState() == TimeframeIntervalState.ACTIVE
                && now.toDateTime().isAfter(getInterval().getEnd()) || getRequest().isFinished(now);
    }

    public boolean isRemovable(LocalDateTime now) {
        return (getState() == TimeframeIntervalState.EXPIRED
                || (getState() == TimeframeIntervalState.QUEUED
                    && (now.toDateTime().isAfter(getInterval().getEnd()) || getRequest().isFinished(now)))
        ) && ! getRequest().isControlOn();
    }

    public boolean isIntervalSufficient(LocalDateTime now, Integer maxRunningTime) {
        LocalDateTime latestStart = getLatestStart(now, new LocalDateTime(interval.getEnd()), maxRunningTime);
        return now.isEqual(latestStart) || now.isBefore(latestStart);
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

    public static LocalDateTime getLatestStart(LocalDateTime now, LocalDateTime intervalEnd, Integer maxRunningTime) {
        return intervalEnd.minusSeconds(maxRunningTime);
    }

    /**
     * Returns the latest start.
     * @return the latest start time in seconds from now or
     * null if input values are null or latest start would be in the past
     */
    public Integer getLatestStartSeconds(LocalDateTime now) {
        DateTime latestStart = interval.getEnd().minusSeconds(getRequest().getMax(now));
        if(now.toDateTime().isBefore(latestStart)) {
            return Double.valueOf(
                    new Interval(now.toDateTime(), latestStart)
                            .toDurationMillis() / 1000.0).intValue();
        }
        return null;
    }

    // TriggeredBy: WallboxState
    public boolean isTriggeredByStartingCurrent() {
        return triggeredByStartingCurrent;
    }

    public void setTriggeredByStartingCurrent(boolean triggeredByStartingCurrent) {
        this.triggeredByStartingCurrent = triggeredByStartingCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TimeframeInterval that = (TimeframeInterval) o;

        return new EqualsBuilder()
                .append(interval, that.interval)
                .append(request, that.request)
                .append(getState(), that.getState())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(interval)
                .append(request)
                .append(getState())
                .toHashCode();
    }

    @Override
    public String toString() {
        return toString(new LocalDateTime());
    }

    public String toString(LocalDateTime now) {
        String text = "";
        if(getState() != null) {
            text += getState();
            text += "/";
        }
        if(interval != null) {
            text += interval.toString();
        }
        text += "::";
        text += request.toString(now);
        return text;
    }
}
