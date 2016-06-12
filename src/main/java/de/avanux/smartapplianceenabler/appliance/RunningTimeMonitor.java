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
import java.util.List;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.slf4j.LoggerFactory;

/**
 * Monitors running time of an appliance.
 * Make sure that the timezone is set correctly, otherwise remaining running time will be incorrect.
 * Refer to http://www.gtkdb.de/index_36_2248.html
 */
public class RunningTimeMonitor implements ApplianceIdConsumer {
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(RunningTimeMonitor.class));
    private List<TimeFrame> timeFrames;
    private TimeFrame currentTimeFrame;
    private Instant intervalBegin;
    private Instant statusChangedAt;
    private Instant lastUpdate;
    private Long remainingMinRunningTime;
    private Long remainingMaxRunningTime;
    private boolean running;

    @Override
    public void setApplianceId(String applianceId) {
        this.logger.setApplianceId(applianceId);
    }

    public void setTimeFrames(List<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
        if(logger.isDebugEnabled()) {
            for(TimeFrame timeFrame : timeFrames) {
                logger.debug("Configured timeframe is " + timeFrame.toString());
            }
        }
    }
    
    public List<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void setRunning(boolean running) {
        setRunning(running, new Instant());
    }

    protected void setRunning(boolean running, Instant statusChangedAt) {
        this.running = running;
        this.statusChangedAt = statusChangedAt;
    }

    public long getRemainingMinRunningTimeOfCurrentTimeFrame() {
        return getRemainingMinRunningTimeOfCurrentTimeFrame(new Instant());
    }

    protected long getRemainingMinRunningTimeOfCurrentTimeFrame(Instant now) {
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

    protected long getRemainingMaxRunningTimeOfCurrentTimeFrame(Instant now) {
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
    protected void update(Instant now) {
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

    public TimeFrame findAndSetCurrentTimeFrame(Instant instant) {
        TimeFrame timeFrameToBeSet = null;
        if(timeFrames != null) {
            for(TimeFrame timeFrame : timeFrames) {
                if(timeFrame.getInterval().contains(instant)) {
                    timeFrameToBeSet = timeFrame;
                    break;
                }
            }
        }
        if(timeFrameToBeSet != null && currentTimeFrame == null) {
            remainingMinRunningTime = timeFrameToBeSet.getMinRunningTime();
            remainingMaxRunningTime = timeFrameToBeSet.getMaxRunningTime();
            intervalBegin = null;
            logger.debug("Timeframe started: " + timeFrameToBeSet);
        }
        else if(timeFrameToBeSet == null && currentTimeFrame != null) {
            logger.debug("Timeframe expired: " + currentTimeFrame);
            remainingMinRunningTime = 0l;
            remainingMaxRunningTime = 0l;
            intervalBegin = null;
        }
        logger.debug("Timeframe status: remainingMinRunningTime=" + remainingMinRunningTime + " remainingMaxRunningTime=" + remainingMaxRunningTime);
        currentTimeFrame = timeFrameToBeSet;
        return currentTimeFrame;
    }
    
    public List<TimeFrame> findFutureTimeFrames(Instant instant) {
        List<TimeFrame> futureTimeFrames = new ArrayList<TimeFrame>();
        for(TimeFrame timeFrame : timeFrames) {
            if(timeFrame.getInterval().getStart().isAfter(instant)) {
                futureTimeFrames.add(timeFrame);
            }
        }
        return futureTimeFrames;
    }
}
