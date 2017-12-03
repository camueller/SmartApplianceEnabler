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
package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.schedule.Timeframe;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Monitors running time of an appliance.
 * Make sure that the timezone is set correctly, otherwise remaining running time will be incorrect.
 * Refer to http://www.gtkdb.de/index_36_2248.html
 */
public class RunningTimeMonitor implements ApplianceIdConsumer {
    private Logger logger = LoggerFactory.getLogger(RunningTimeMonitor.class);
    private String applianceId;
    private List<Schedule> schedules;
    private Set<ActiveIntervalChangedListener> scheduleChangedListeners = new HashSet<>();
    private TimeframeInterval activeTimeframeInterval;
    private LocalDateTime statusChangedAt;
    // remaining min running time while not running or when running started
    private Integer remainingMinRunningTimeWhileNotRunning;
    // remaining max running time while not running or when running started
    private Integer remainingMaxRunningTimeWhileNotRunning;
    private boolean running;
    private Timer timer;
    private TimerTask updateActiveTimeframeIntervalTimerTask;

    public RunningTimeMonitor() {
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
        this.updateActiveTimeframeIntervalTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateActiveTimeframeInterval(new LocalDateTime());
            }
        };
        this.timer.schedule(updateActiveTimeframeIntervalTimerTask, 0, 30000);
    }

    public void setSchedules(List<Schedule> schedules) {
        List<Schedule> enabledSchedules = new ArrayList<>();
        for(Schedule schedule : schedules) {
            if(schedule.isEnabled()) {
                logger.debug("{}: Using enabled time frame {}", applianceId, schedule.toString());
                enabledSchedules.add(schedule);
            }
            else {
                logger.debug("{}: Ignoring disabled time frame {}", applianceId, schedule.toString());
            }
        }
        this.schedules = enabledSchedules;
        this.activeTimeframeInterval = null;
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

    protected void setRunning(boolean running, LocalDateTime now) {
        if(! running) {
            int secondsSinceStatusChange = getSecondsSinceStatusChange(now);
            if(this.remainingMinRunningTimeWhileNotRunning != null) {
                if(this.remainingMinRunningTimeWhileNotRunning - secondsSinceStatusChange >= 0) {
                    this.remainingMinRunningTimeWhileNotRunning -= secondsSinceStatusChange;
                }
                else {
                    this.remainingMinRunningTimeWhileNotRunning = 0;
                }
            }
            if(this.remainingMaxRunningTimeWhileNotRunning != null) {
                if(this.remainingMaxRunningTimeWhileNotRunning - secondsSinceStatusChange >= 0) {
                    this.remainingMaxRunningTimeWhileNotRunning -= secondsSinceStatusChange;
                }
                else {
                    this.remainingMaxRunningTimeWhileNotRunning = 0;
                }
            }
        }
        this.running = running;
        this.statusChangedAt = now;
    }

    public LocalDateTime getStatusChangedAt() {
        return statusChangedAt;
    }

    protected int getSecondsSinceStatusChange(LocalDateTime now) {
        Interval runtimeSinceStatusChange = new Interval(statusChangedAt.toDateTime(), now.toDateTime());
        return Double.valueOf(runtimeSinceStatusChange.toDuration().getMillis() / 1000).intValue();
    }

    public int getRemainingMinRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        int remainingRunningTime = 0;
        if(remainingMinRunningTimeWhileNotRunning != null) {
            if(running) {
                remainingRunningTime = remainingMinRunningTimeWhileNotRunning - getSecondsSinceStatusChange(now);
            }
            else {
                remainingRunningTime = remainingMinRunningTimeWhileNotRunning;
            }
        }
        return remainingRunningTime;
    }

    public int getRemainingMaxRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        int remainingRunningTime = 0;
        if(remainingMaxRunningTimeWhileNotRunning != null) {
            if(running) {
                remainingRunningTime = this.remainingMaxRunningTimeWhileNotRunning - getSecondsSinceStatusChange(now);
            }
            else {
                remainingRunningTime = remainingMaxRunningTimeWhileNotRunning;
            }
        }
        return remainingRunningTime;
    }

    protected void updateActiveTimeframeInterval(LocalDateTime now) {
        activateTimeframeInterval(now, schedules);
        deactivateExpiredTimeframeInterval(now);
        logger.debug("{}: activeTimeframeInterval={}", applianceId, activeTimeframeInterval);
        logger.debug("{}: remainingMinRunning={} remainingMaxRunningTime={}",
                applianceId, getRemainingMinRunningTimeOfCurrentTimeFrame(now),
                getRemainingMaxRunningTimeOfCurrentTimeFrame(now));
        logger.debug("{}: running={} statusChangedAt={} ", applianceId, running, statusChangedAt);
    }

    /**
     * Returns the active timeframe interval.
     * @return
     */
    public TimeframeInterval getActiveTimeframeInterval(LocalDateTime now) {
        return activeTimeframeInterval;
    }

    /**
     * Activate the current or next timeframe interval from one of the given schedules.
     * @param now
     * @param schedules
     */
    public void activateTimeframeInterval(LocalDateTime now, List<Schedule> schedules) {
        if(schedules != null && schedules.size() > 0) {
            TimeframeInterval nextSufficientTimeframeInterval =
                    Schedule.getCurrentOrNextTimeframeInterval(now, schedules, true, false);
            activateTimeframeInterval(now, nextSufficientTimeframeInterval);
        }
    }

    public void activateTimeframeInterval(LocalDateTime now, Integer runtime) {
        activeTimeframeInterval = null;
        Schedule schedule = new Schedule(runtime, runtime, new TimeOfDay(now), new TimeOfDay(now));
        Interval interval = new Interval(now.toDateTime(), now.plusSeconds(runtime).toDateTime());
        Timeframe timeframe = schedule.getTimeframe();
        timeframe.setSchedule(schedule);
        activateTimeframeInterval(now, new TimeframeInterval(timeframe, interval));
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
            remainingMinRunningTimeWhileNotRunning = schedule.getMinRunningTime();
            remainingMaxRunningTimeWhileNotRunning = schedule.getMaxRunningTime();
            intervalChanged = true;
            logger.debug("{}: Interval activated: ", applianceId, timeframeIntervalToBeActivated);
        }
        else if(timeframeIntervalToBeActivated == null && activeTimeframeInterval != null) {
            logger.debug("{}: Interval expired: {}", applianceId, activeTimeframeInterval);
            remainingMinRunningTimeWhileNotRunning = null;
            remainingMaxRunningTimeWhileNotRunning = null;
            intervalChanged = true;
        }
        TimeframeInterval deactivatedTimeframeInterval = activeTimeframeInterval;
        activeTimeframeInterval = timeframeIntervalToBeActivated;
        if(intervalChanged) {
            logger.debug("{}: Active interval changed. deactivatedTimeframeInterval={} activeTimeframeInterval={}",
                    applianceId, deactivatedTimeframeInterval, activeTimeframeInterval);
            for(ActiveIntervalChangedListener listener : scheduleChangedListeners) {
                logger.debug("{}: Notifying {} {}", applianceId, ActiveIntervalChangedListener.class.getSimpleName(),
                        listener.getClass().getSimpleName());
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
