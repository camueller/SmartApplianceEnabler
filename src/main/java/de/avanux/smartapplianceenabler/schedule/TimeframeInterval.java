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
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Vector;

/**
 * A TimeframeInterval associates a timeframe with an interval.
 */
public class TimeframeInterval implements ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(TimeframeInterval.class);
    private Interval interval;
    private Request request;
    private transient Vector<TimeframeIntervalState> stateHistory;
    private transient String applianceId;

    public TimeframeInterval(Interval interval, Request request) {
        this.interval = interval;
        this.request = request;
        this.request.setTimeframeInterval(this);
        initState(null);
    }

    public TimeframeInterval(TimeframeIntervalState state, Interval interval, Request request) {
        this.interval = interval;
        this.request = request;
        this.request.setTimeframeInterval(this);
        initState(state);
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
        stateHistory = new Vector<>();
        stateHistory.add(initialState != null ? initialState : TimeframeIntervalState.CREATED);
    }

    public void stateTransitionTo(TimeframeIntervalState state) {
        this.stateHistory.add(state);
    }

    public TimeframeIntervalState getState() {
        return stateHistory != null ? stateHistory.lastElement() : null;
    }

    public boolean wasInState(TimeframeIntervalState state) {
        return stateHistory.contains(state);
    }

    public boolean isActivatable(LocalDateTime now, Integer withinSeconds) {
        return getState() == TimeframeIntervalState.QUEUED
                && (
                        now.isEqual(getInterval().getStart())
                        || now.isAfter(getInterval().getStart())
                        || (withinSeconds != null && now.isAfter(getInterval().getStart().minusSeconds(withinSeconds)))
                )
                && (!(request instanceof RuntimeRequest) || isIntervalSufficient(now)
        );
    }

    public boolean isDeactivatable(LocalDateTime now) {
        return (
                       now.isAfter(getInterval().getEnd())
                    || request.isFinished(now)
                    || (request instanceof RuntimeRequest && ((RuntimeRequest) request).hasStartingCurrentSwitch()
                                && !request.isControlOn() && !isIntervalSufficient(now))
                );
    }

    public boolean isRemovable(LocalDateTime now) {
        return
        (
            (
                getState() == TimeframeIntervalState.EXPIRED
                || (
                        getState() == TimeframeIntervalState.QUEUED
                        && (now.isAfter(getInterval().getEnd()) || getRequest().isFinished(now))
                )
            )
            && ! getRequest().isControlOn()
            && (! (getRequest() instanceof OptionalEnergySocRequest) || (getRequest().getMax(now) != null && getRequest().getMax(now) <= 0))
        )
        || (
                getRequest() instanceof OptionalEnergySocRequest
                        && ((OptionalEnergySocRequest) getRequest()).getEvChargerState() == EVChargerState.VEHICLE_NOT_CONNECTED
        );
    }

    public boolean isProlongable(LocalDateTime now) {
        return getState() == TimeframeIntervalState.ACTIVE
                && (now.isAfter(getInterval().getEnd())
                && (request instanceof OptionalEnergySocRequest)
                && ! request.isFinished(now)
        );
    }

    public boolean isIntervalSufficient(LocalDateTime now) {
        if(getRequest() instanceof AbstractEnergyRequest) {
            return true;
        }
        LocalDateTime latestStart = getLatestStart(now, LocalDateTime.from(interval.getEnd()), getRequestMinOrMax(now));
        return now.isEqual(latestStart) || now.isBefore(latestStart);
    }

    public Integer getEarliestStartSeconds(LocalDateTime now) {
        Integer earliestStart = 0;
        if(interval.getStart().isAfter(now)) {
            earliestStart = Long.valueOf(Duration.between(now, interval.getStart()).toSeconds()).intValue();
        }
        return earliestStart;
    }

    public Integer getLatestEndSeconds(LocalDateTime now) {
        LocalDateTime nowBeforeEnd = LocalDateTime.from(now);
        if(now.isAfter(interval.getEnd())) {
            nowBeforeEnd = now.minusHours(24);
        }
        return Long.valueOf(Duration.between(nowBeforeEnd, interval.getEnd()).toSeconds()).intValue();
    }

    public static LocalDateTime getLatestStart(LocalDateTime now, LocalDateTime intervalEnd, Integer runningTime) {
        return intervalEnd.minusSeconds(runningTime);
    }

    /**
     * Returns the latest start.
     * @return the latest start time in seconds from now or
     * null if input values are null or latest start would be in the past
     */
    public Integer getLatestStartSeconds(LocalDateTime now) {
        LocalDateTime latestStart = interval.getEnd().minusSeconds(getRequestMinOrMax(now));
        if(now.isBefore(latestStart)) {
            return Long.valueOf(Duration.between(now, latestStart).toSeconds()).intValue();
        }
        return null;
    }

    private int getRequestMinOrMax(LocalDateTime now) {
        Integer minOrMax = getRequest().getMinOrMax(now);
        return minOrMax !=null ? minOrMax : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TimeframeInterval that = (TimeframeInterval) o;

        return new EqualsBuilder()
                .append(interval, that.interval)
                .append(getState(), that.getState())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(interval)
                .append(getState())
                .toHashCode();
    }

    @Override
    public String toString() {
        return toString(LocalDateTime.now());
    }

    public String toString(LocalDateTime now) {
        return toString(LocalDateTime.now(), true);
    }

    public String toString(LocalDateTime now, boolean includeRequest) {
        String text = "";
        if(getState() != null) {
            text += getState();
            text += "/";
        }
        if(interval != null) {
            text += interval.toString();
        }
        text += "::";
        if(includeRequest && request != null){
            text += request.toString(now, false);
        }
        return text;
    }
}
