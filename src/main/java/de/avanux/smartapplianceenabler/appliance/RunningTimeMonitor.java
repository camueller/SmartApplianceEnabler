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

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.joda.time.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Monitors running time of an appliance.
 * Make sure that the timezone is set correctly, otherwise remaining running time will be incorrect.
 * Refer to http://www.gtkdb.de/index_36_2248.html
 */
public class RunningTimeMonitor implements ApplianceIdConsumer {
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(RunningTimeMonitor.class));
    private String applianceId;
    private List<TimeFrame> timeFrames;
    private Set<TimeFrameChangedListener> timeFrameChangedListeners = new HashSet<>();
    private TimeFrame currentTimeFrame;
    private LocalDateTime intervalBegin;
    private LocalDateTime statusChangedAt;
    private LocalDateTime lastUpdate;
    private Integer remainingMinRunningTime;
    private Integer remainingMaxRunningTime;
    private boolean running;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.logger.setApplianceId(applianceId);
    }

    public void setTimeFrames(List<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
        this.currentTimeFrame = null;
        if(logger.isDebugEnabled() && timeFrames != null
                ) {
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
        setRunning(running, new LocalDateTime());
    }

    protected void setRunning(boolean running, LocalDateTime statusChangedAt) {
        this.running = running;
        this.statusChangedAt = statusChangedAt;
    }

    public int getRemainingMinRunningTimeOfCurrentTimeFrame() {
        return getRemainingMinRunningTimeOfCurrentTimeFrame(new LocalDateTime());
    }

    protected int getRemainingMinRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        update(now);
        int remainingRunningTime = 0;
        if(remainingMinRunningTime != null) {
            remainingRunningTime = remainingMinRunningTime;
        }
        logger.debug("remainingMinRunningTime=" + remainingRunningTime);
        return remainingRunningTime;
    }

    public int getRemainingMaxRunningTimeOfCurrentTimeFrame() {
        return getRemainingMaxRunningTimeOfCurrentTimeFrame(new LocalDateTime());
    }

    protected int getRemainingMaxRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        update(now);
        int remainingRunningTime = 0;
        if(remainingMaxRunningTime != null) {
            remainingRunningTime = remainingMaxRunningTime;
        }
        logger.debug("remainingMaxRunningTime=" + remainingRunningTime);
        return remainingRunningTime;
    }

    /**
     * Updates remainingMinRunningTime for the given instant. The value may become negative!
     * Subsequent calls to this method within one second are omitted.
     * @param dateTime
     */
    protected void update(LocalDateTime dateTime) {
        // update not more than once per second in order to avoid spamming the log
        if(lastUpdate == null || dateTime.isBefore(lastUpdate) || new Interval(lastUpdate.toDateTime(), dateTime.toDateTime()).toDurationMillis() > 1000) {

            findAndSetCurrentTimeFrame(dateTime);
            logger.debug("currentTimeFrame=" + currentTimeFrame + " statusChangedAt=" + statusChangedAt + " intervalBegin=" + intervalBegin + " running=" + running);

            Interval interval = null;
            if(running) {
                // running
                if(intervalBegin == null) {
                    // running was set to true after interval begin
                    interval = new Interval(statusChangedAt.toDateTime(), dateTime.toDateTime());
                }
                else {
                    // no status change in interval
                    interval = new Interval(intervalBegin.toDateTime(), dateTime.toDateTime());
                }
                intervalBegin = dateTime;
            }
            else if (intervalBegin != null && statusChangedAt != null) {
                // running was set to false after interval begin
                interval = new Interval(intervalBegin.toDateTime(), statusChangedAt.toDateTime());
                intervalBegin = null;
                statusChangedAt = null;
            }
            if(interval != null) {
                int intervalSeconds = Double.valueOf(interval.toDuration().getMillis() / 1000).intValue();
                remainingMinRunningTime = remainingMinRunningTime - intervalSeconds;
                remainingMaxRunningTime = remainingMaxRunningTime - intervalSeconds;
            }
            lastUpdate = dateTime;
        }
    }

    public TimeFrame findAndSetCurrentTimeFrame() {
        return findAndSetCurrentTimeFrame(new LocalDateTime());
    }

    /**
     * Set the timeframe whose interval contains the given dateTime.
     * If the timeframe has associated days of week the day of week of the given dateTime has to match as well.
     * @param dateTime
     * @return the timeframe set or null, if there is no matching timeframe
     */
    public TimeFrame findAndSetCurrentTimeFrame(LocalDateTime dateTime) {
        TimeFrame timeFrameToBeSet = null;
        if(timeFrames != null) {
            int dowOfInstant = dateTime.get(DateTimeFieldType.dayOfWeek());
            LocalTime instantTime = new LocalTime(dateTime, DateTimeZone.getDefault());
            for(TimeFrame timeFrame : timeFrames) {
                List<Integer> dowValues = timeFrame.getDaysOfWeekValues();
                LocalTime startTimeToday = new LocalTime(timeFrame.getInterval(dateTime).getStart());
                LocalTime endTimeToday = new LocalTime(timeFrame.getInterval(dateTime).getEnd());
                if((dowValues == null || dowValues.contains(dowOfInstant))
                        && (timeFrame.isOverMidnight()
                            ? (instantTime.isAfter(startTimeToday)
                                ? endTimeToday.isBefore(instantTime)
                                : endTimeToday.isAfter(instantTime))
                            : startTimeToday.isBefore(instantTime) && endTimeToday.isAfter(instantTime)
                )) {
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
            remainingMinRunningTime = 0;
            remainingMaxRunningTime = 0;
            intervalBegin = null;
            timeFrameChanged = true;
        }
        logger.debug("Timeframe status: remainingMinRunningTime=" + remainingMinRunningTime + " remainingMaxRunningTime=" + remainingMaxRunningTime);
        TimeFrame oldTimeFrame = currentTimeFrame;
        currentTimeFrame = timeFrameToBeSet;
        if(timeFrameChanged) {
            logger.debug("Time frame changed for appliance " + applianceId + " : oldTimeFrame=" + oldTimeFrame + " newTimeFrame=" + currentTimeFrame);
            for(TimeFrameChangedListener listener : timeFrameChangedListeners) {
                logger.debug("Notifying " + listener.getClass().getSimpleName());
                listener.timeFrameChanged(applianceId, oldTimeFrame, currentTimeFrame);
            }
        }

        return currentTimeFrame;
    }

    /**
     * Returns timeframes starting after the given dateTime.
     * @param dateTime
     * @return a (possibly empty) list of timeframes
     */
    public List<TimeFrame> findFutureTimeFrames(LocalDateTime dateTime) {
        List<TimeFrame> futureTimeFrames = new ArrayList<TimeFrame>();
        int dowOfInstant = dateTime.get(DateTimeFieldType.dayOfWeek());
        if(timeFrames != null) {
            LocalTime instantTime = new LocalTime(dateTime, DateTimeZone.getDefault());
            for(TimeFrame timeFrame : timeFrames) {
                List<Integer> dowValues = timeFrame.getDaysOfWeekValues();
                LocalTime startTimeToday = new LocalTime(timeFrame.getInterval(dateTime).getStart());
                LocalTime endTimeToday = new LocalTime(timeFrame.getInterval(dateTime).getEnd());
                if((dowValues == null || dowValues.contains(dowOfInstant))
                        && (timeFrame.isOverMidnight()
                        ? (!instantTime.isAfter(startTimeToday) && startTimeToday.isAfter(instantTime)) && endTimeToday.isBefore(instantTime)
                        : startTimeToday.isAfter(instantTime)
                )) {
                    futureTimeFrames.add(timeFrame);
                }
            }
        }
        return futureTimeFrames;
    }
}
