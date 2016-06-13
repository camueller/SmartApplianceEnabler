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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.slf4j.LoggerFactory;

/**
 * Monitors running time of an appliance.
 * Make sure that the timezone is set correctly, otherwise remaining running time will be incorrect.
 * Refer to http://www.gtkdb.de/index_36_2248.html
 */
public class RunningTimeMonitor implements RunningTimeController, ApplianceIdConsumer {
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(RunningTimeMonitor.class));
    private String applianceId;
    private List<TimeFrame> timeFrames;
    private Set<TimeFrameChangedListener> timeFrameChangedListeners = new HashSet<>();
    private TimeFrame currentTimeFrame;
    private ReadableInstant intervalBegin;
    private ReadableInstant statusChangedAt;
    private ReadableInstant lastUpdate;
    private Long remainingMinRunningTime;
    private Long remainingMaxRunningTime;
    private boolean running;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.logger.setApplianceId(applianceId);
    }

    public void setTimeFrames(List<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
        if(logger.isDebugEnabled()) {
            for(TimeFrame timeFrame : timeFrames) {
                logger.debug("Configured time frame is " + timeFrame.toString());
            }
        }
    }
    
    public List<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void addTimeFrameChangedListener(TimeFrameChangedListener listener) {
        this.timeFrameChangedListeners.add(listener);
    }

    public void setRunning(boolean running) {
        setRunning(running, new Instant());
    }

    protected void setRunning(boolean running, ReadableInstant statusChangedAt) {
        this.running = running;
        this.statusChangedAt = statusChangedAt;
    }

    public long getRemainingMinRunningTimeOfCurrentTimeFrame() {
        return getRemainingMinRunningTimeOfCurrentTimeFrame(new Instant());
    }

    protected long getRemainingMinRunningTimeOfCurrentTimeFrame(ReadableInstant now) {
        update(now);
        long remainingRunningTime = 0;
        if(remainingMinRunningTime != null) {
            remainingRunningTime = remainingMinRunningTime;
        }
        logger.debug("remainingMinRunningTime=" + remainingRunningTime);
        return remainingRunningTime;
    }

    public long getRemainingMaxRunningTimeOfCurrentTimeFrame() {
        return getRemainingMaxRunningTimeOfCurrentTimeFrame(new Instant());
    }

    protected long getRemainingMaxRunningTimeOfCurrentTimeFrame(ReadableInstant now) {
        update(now);
        long remainingRunningTime = 0;
        if(remainingMaxRunningTime != null) {
            remainingRunningTime = remainingMaxRunningTime;
        }
        logger.debug("remainingMaxRunningTime=" + remainingRunningTime);
        return remainingRunningTime;
    }

    /**
     * Updates remainingMinRunningTime for the given instant. The value may become negative!
     * Subsequent calls to this method within one second are omitted.
     * @param now
     */
    protected void update(ReadableInstant now) {
        // update not more than once per second in order to avoid spamming the log
        if(lastUpdate == null || now.isBefore(lastUpdate) || new Interval(lastUpdate, now).toDurationMillis() > 1000) {

            findAndSetCurrentTimeFrame(now);
            logger.debug("currentTimeFrame=" + currentTimeFrame + " statusChangedAt=" + statusChangedAt + " intervalBegin=" + intervalBegin + " running=" + running);

            Interval interval = null;
            if(running) {
                // running
                if(intervalBegin == null) {
                    // running was set to true after interval begin
                    interval = new Interval(statusChangedAt, now);
                }
                else {
                    // no status change in interval
                    interval = new Interval(intervalBegin, now);
                }
                intervalBegin = now;
            }
            else if (intervalBegin != null && statusChangedAt != null) {
                // running was set to false after interval begin
                interval = new Interval(intervalBegin, statusChangedAt);
                intervalBegin = null;
                statusChangedAt = null;
            }
            if(interval != null) {
                long intervalSeconds = Double.valueOf(interval.toDuration().getMillis() / 1000).longValue();
                remainingMinRunningTime = remainingMinRunningTime - intervalSeconds;
                remainingMaxRunningTime = remainingMaxRunningTime - intervalSeconds;
            }
            lastUpdate = now;
        }
    }

    public TimeFrame findAndSetCurrentTimeFrame() {
        return findAndSetCurrentTimeFrame(new Instant());
    }

    /**
     * Set the timeframe whose interval contains the given instant.
     * If the timeframe has associated days of week the day of week of the given instant has to match as well.
     * @param instant
     * @return the timeframe set or null, if there is no matching timeframe
     */
    public TimeFrame findAndSetCurrentTimeFrame(ReadableInstant instant) {
        TimeFrame timeFrameToBeSet = null;
        if(timeFrames != null) {
            int dowOfInstant = instant.get(DateTimeFieldType.dayOfWeek());
            for(TimeFrame timeFrame : timeFrames) {
                List<Integer> dowValues = timeFrame.getDaysOfWeekValues();
                // For testing instant may have a different DATE than today; therefore we have to transfer the time to today
                DateTime instantToday = new LocalTime(instant, ISOChronology.getInstance()).toDateTimeToday();
                if((dowValues == null || dowValues.contains(dowOfInstant)) && timeFrame.getInterval().contains(instantToday)) {
                    timeFrameToBeSet = timeFrame;
                    break;
                }
            }
        }
        boolean timeFrameChanged = false;
        if(timeFrameToBeSet != null && currentTimeFrame == null) {
            remainingMinRunningTime = timeFrameToBeSet.getMinRunningTime();
            remainingMaxRunningTime = timeFrameToBeSet.getMaxRunningTime();
            intervalBegin = null;
            timeFrameChanged = true;
            logger.debug("Timeframe started: " + timeFrameToBeSet);
        }
        else if(timeFrameToBeSet == null && currentTimeFrame != null) {
            logger.debug("Timeframe expired: " + currentTimeFrame);
            remainingMinRunningTime = 0l;
            remainingMaxRunningTime = 0l;
            intervalBegin = null;
            timeFrameChanged = true;
        }
        logger.debug("Timeframe status: remainingMinRunningTime=" + remainingMinRunningTime + " remainingMaxRunningTime=" + remainingMaxRunningTime);
        TimeFrame oldTimeFrame = currentTimeFrame;
        currentTimeFrame = timeFrameToBeSet;
        if(timeFrameChanged) {
            for(TimeFrameChangedListener listener : timeFrameChangedListeners) {
                listener.timeFrameChanged(applianceId, oldTimeFrame, currentTimeFrame);
            }
        }

        return currentTimeFrame;
    }

    /**
     * Returns timeframes starting after the given instant.
     * @param instant
     * @return a (possibly empty) list of timeframes
     */
    public List<TimeFrame> findFutureTimeFrames(Instant instant) {
        List<TimeFrame> futureTimeFrames = new ArrayList<TimeFrame>();
        int dowInstant = instant.get(DateTimeFieldType.dayOfWeek());
        if(timeFrames != null) {
            for(TimeFrame timeFrame : timeFrames) {
                List<Integer> dowValues = timeFrame.getDaysOfWeekValues();
                if((dowValues == null || dowValues.contains(dowInstant)) && timeFrame.getInterval().getStart().isAfter(instant)) {
                    futureTimeFrames.add(timeFrame);
                }
            }
        }
        return futureTimeFrames;
    }
}
