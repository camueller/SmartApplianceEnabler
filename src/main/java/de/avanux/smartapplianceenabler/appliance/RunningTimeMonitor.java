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
    private List<Schedule> schedules;
    private Set<ActiveIntervalChangedListener> scheduleChangedListeners = new HashSet<>();
    private TimeframeInterval activeTimeframeInterval;
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

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
        this.activeTimeframeInterval = null;
        if(logger.isDebugEnabled() && schedules != null
                ) {
            for(Schedule schedule : schedules) {
                logger.debug("Configured time frame is " + schedule.toString());
            }
        }
    }
    
    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void addTimeFrameChangedListener(ActiveIntervalChangedListener listener) {
        this.scheduleChangedListeners.add(listener);
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
     * @param now
     */
    protected void update(LocalDateTime now) {
        // update not more than once per second in order to avoid spamming the log
        if(lastUpdate == null || now.isBefore(lastUpdate) || new Interval(lastUpdate.toDateTime(), now.toDateTime()).toDurationMillis() > 1000) {
            activateTimeframeInterval(now, schedules);
            deactivateExpiredTimeframeInterval(now);
            logger.debug("activeTimeframeInterval=" + activeTimeframeInterval + " statusChangedAt=" + statusChangedAt + " intervalBegin=" + intervalBegin + " running=" + running);

            Interval interval = null;
            if(running) {
                // running
                if(intervalBegin == null) {
                    // running was set to true after interval begin
                    interval = new Interval(statusChangedAt.toDateTime(), now.toDateTime());
                }
                else {
                    // no status change in interval
                    interval = new Interval(intervalBegin.toDateTime(), now.toDateTime());
                }
                intervalBegin = now;
            }
            else if (intervalBegin != null && statusChangedAt != null) {
                // running was set to false after interval begin
                interval = new Interval(intervalBegin.toDateTime(), statusChangedAt.toDateTime());
                intervalBegin = null;
                statusChangedAt = null;
            }
            if(interval != null && remainingMinRunningTime != null && remainingMaxRunningTime != null) {
                int intervalSeconds = Double.valueOf(interval.toDuration().getMillis() / 1000).intValue();
                remainingMinRunningTime = remainingMinRunningTime - intervalSeconds;
                remainingMaxRunningTime = remainingMaxRunningTime - intervalSeconds;
            }
            lastUpdate = now;
        }
    }

    /**
     * Returns the active timeframe interval.
     * @return
     */
    public TimeframeInterval getActiveTimeframeInterval() {
        update(new LocalDateTime());
        return activeTimeframeInterval;
    }

    /**
     * Activate the current or next timeframe interval from one of the given schedules.
     * @param now
     * @param schedules
     */
    public void activateTimeframeInterval(LocalDateTime now, List<Schedule> schedules) {
        if(schedules != null && schedules.size() > 0) {
            TimeframeInterval nextSufficientTimeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, schedules, true, false);
            activateTimeframeInterval(now, nextSufficientTimeframeInterval);
        }
    }

    /**
     * Activate the given timeframe interval.
     * @param now
     * @param timeframeIntervalToBeActivated the timeframe interval or null
     */
    public void activateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeIntervalToBeActivated) {
        boolean intervalChanged = false;
        if(timeframeIntervalToBeActivated != null && activeTimeframeInterval == null) {
            Schedule schedule = timeframeIntervalToBeActivated.getTimeframe().getSchedule();
            remainingMinRunningTime = schedule.getMinRunningTime();
            remainingMaxRunningTime = schedule.getMaxRunningTime();
            intervalBegin = null;
            intervalChanged = true;
            logger.debug("Interval activated: " + timeframeIntervalToBeActivated);
        }
        else if(timeframeIntervalToBeActivated == null && activeTimeframeInterval != null) {
            logger.debug("Interval expired: " + activeTimeframeInterval);
            remainingMinRunningTime = 0;
            remainingMaxRunningTime = 0;
            intervalBegin = null;
            intervalChanged = true;
        }
        logger.debug("Active interval status: remainingMinRunningTime=" + remainingMinRunningTime + " remainingMaxRunningTime=" + remainingMaxRunningTime);
        TimeframeInterval deactivatedTimeframeInterval = activeTimeframeInterval;
        activeTimeframeInterval = timeframeIntervalToBeActivated;
        if(intervalChanged) {
            logger.debug("Active interval changed for appliance " + applianceId + " : deactivatedTimeframeInterval=" + deactivatedTimeframeInterval + " activeTimeframeInterval=" + activeTimeframeInterval);
            for(ActiveIntervalChangedListener listener : scheduleChangedListeners) {
                logger.debug("Notifying " + listener.getClass().getSimpleName());
                listener.activeIntervalChanged(applianceId, deactivatedTimeframeInterval, activeTimeframeInterval);
            }
        }
    }

    /**
     * Deactivate the active timeframe interval if it is expired.
     * @param now
     */
    protected void deactivateExpiredTimeframeInterval(LocalDateTime now) {
        if(activeTimeframeInterval != null) {
            if(now.toDateTime().isAfter(activeTimeframeInterval.getInterval().getEnd())) {
                activateTimeframeInterval(now, (TimeframeInterval) null);
            }
        }
    }
}
