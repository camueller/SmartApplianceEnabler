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

import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
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
    private RuntimeState state;
    private RuntimeState previousState;
    private GuardedTimerTask updateActiveTimeframeIntervalTimerTask;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void init() {
        this.initState();
    }

    private void initState() {
        logger.debug("{}: Initialize state", applianceId);
        this.state = new RuntimeState();
    }

    public void setTimer(Timer timer) {
//        this.updateActiveTimeframeIntervalTimerTask = new GuardedTimerTask(this.applianceId,
//                "UpdateActiveTimeframeInterval", 30000) {
//            @Override
//            public void runTask() {
//                updateActiveTimeframeInterval(new LocalDateTime());
//            }
//        };
//        if(timer != null) {
//            timer.schedule(updateActiveTimeframeIntervalTimerTask, 0,
//                    updateActiveTimeframeIntervalTimerTask.getPeriod());
//        }
    }

    public void cancelTimer() {
        if(this.updateActiveTimeframeIntervalTimerTask != null) {
            this.updateActiveTimeframeIntervalTimerTask.cancel();
        }
    }

    public void setSchedules(List<Schedule> schedules, LocalDateTime now) {
        List<Schedule> enabledSchedules = new ArrayList<>();
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                if(schedule.isEnabled()) {
                    logger.debug("{}: Using enabled time frame {}", applianceId, schedule.toString());
                    enabledSchedules.add(schedule);
                }
                else {
                    logger.debug("{}: Ignoring disabled time frame {}", applianceId, schedule.toString());
                }
            }

        }
        else {
            logger.debug("{}: No schedules to set", applianceId);
        }
        this.schedules = enabledSchedules;
        this.state.activeTimeframeInterval = null;
        updateActiveTimeframeInterval(now);
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void addTimeFrameChangedListener(ActiveIntervalChangedListener listener) {
        this.scheduleChangedListeners.add(listener);
    }

    public void setRunning(boolean running, LocalDateTime now) {
        if(! running) {
            int secondsSinceStatusChange = getSecondsSinceStatusChange(now);
            if(this.state.runningTime != null) {
                this.state.runningTime += secondsSinceStatusChange;
            }
            if(this.state.remainingMinRunningTimeWhileNotRunning != null) {
                if(this.state.remainingMinRunningTimeWhileNotRunning - secondsSinceStatusChange >= 0) {
                    this.state.remainingMinRunningTimeWhileNotRunning -= secondsSinceStatusChange;
                }
                else {
                    this.state.remainingMinRunningTimeWhileNotRunning = 0;
                }
            }
            if(this.state.remainingMaxRunningTimeWhileNotRunning != null) {
                if(this.state.remainingMaxRunningTimeWhileNotRunning - secondsSinceStatusChange >= 0) {
                    this.state.remainingMaxRunningTimeWhileNotRunning -= secondsSinceStatusChange;
                }
                else {
                    this.state.remainingMaxRunningTimeWhileNotRunning = 0;
                }
            }
            if(this.state.activeTimeframeInterval != null) {
                this.state.interrupted = this.state.wasRunning;
            }
        }
        else {
            this.state.interrupted = false;
            this.state.wasRunning = true;
        }
        this.state.running = running;
        this.state.statusChangedAt = now;
        logger.debug("{}: Set running={} statusChangedAt={} ", applianceId, running, this.state.statusChangedAt);
    }

    public LocalDateTime getStatusChangedAt() {
        return this.state.statusChangedAt;
    }

    protected int getSecondsSinceStatusChange(LocalDateTime now) {
        try {
            if(this.state.statusChangedAt != null && now != null) {
                Interval runtimeSinceStatusChange = new Interval(this.state.statusChangedAt.toDateTime(), now.toDateTime());
                return Double.valueOf(runtimeSinceStatusChange.toDuration().getMillis() / 1000).intValue();
            }
        }
        catch(IllegalArgumentException e) {
            logger.warn("{} Invalid interval: start={} end={}", applianceId, this.state.statusChangedAt.toDateTime(),
                    now.toDateTime());
        }
        return 0;
    }

    public Integer getRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        Integer runningTime = null;
        if(this.state.runningTime != null) {
            if(this.state.running) {
                runningTime = this.state.runningTime + getSecondsSinceStatusChange(now);
            }
            else {
                runningTime = this.state.runningTime;
            }
        }
        return runningTime;
    }

    public Integer getRemainingMinRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        Integer remainingRunningTime = null;
        if(this.state.remainingMinRunningTimeWhileNotRunning != null) {
            if(this.state.running) {
                int calculatedRemainingRunningTime
                        = this.state.remainingMinRunningTimeWhileNotRunning - getSecondsSinceStatusChange(now);
                remainingRunningTime = calculatedRemainingRunningTime > 0 ? calculatedRemainingRunningTime : 0;
            }
            else {
                remainingRunningTime = this.state.remainingMinRunningTimeWhileNotRunning;
            }
        }
        return remainingRunningTime;
    }

    public Integer getRemainingMaxRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        Integer remainingRunningTime = null;
        if(this.state.remainingMaxRunningTimeWhileNotRunning != null) {
            if(this.state.running) {
                int calculatedRemainingRunningTime
                        = this.state.remainingMaxRunningTimeWhileNotRunning - getSecondsSinceStatusChange(now);
                remainingRunningTime = calculatedRemainingRunningTime > 0 ? calculatedRemainingRunningTime : 0;
            }
            else {
                remainingRunningTime = this.state.remainingMaxRunningTimeWhileNotRunning;
            }
        }
        return remainingRunningTime;
    }

    public boolean isRunning() {
        return this.state.running;
    }

    public boolean isInterrupted() {
        return this.state.interrupted;
    }

    public void updateActiveTimeframeInterval(LocalDateTime now) {
        activateTimeframeInterval(now, schedules);
        deactivateExpiredTimeframeInterval(now);
        logger.debug("{}: activeTimeframeInterval={}", applianceId, this.state.activeTimeframeInterval);
        logger.debug("{}: runningTime={} remainingMinRunningTime={} remainingMaxRunningTime={}",
                applianceId, getRunningTimeOfCurrentTimeFrame(now), getRemainingMinRunningTimeOfCurrentTimeFrame(now),
                getRemainingMaxRunningTimeOfCurrentTimeFrame(now));
        logger.debug("{}: running={} interrupted={} statusChangedAt={} ", applianceId, this.state.running,
                this.state.interrupted, this.state.statusChangedAt);
    }

    /**
     * Returns the active timeframe interval.
     * @return
     */
    public TimeframeInterval getActiveTimeframeInterval() {
        return this.state.activeTimeframeInterval;
    }

    public Request getActiveRequest() {
        return this.state.activeRequest;
    }

    /**
     * Activate the current or next timeframe interval from one of the given schedules.
     * @param now
     * @param schedules
     */
    public void activateTimeframeInterval(LocalDateTime now, List<Schedule> schedules) {
        if(this.state.activeTimeframeInterval == null && schedules != null && schedules.size() > 0) {
            TimeframeInterval nextSufficientTimeframeInterval =
                    Schedule.getCurrentOrNextTimeframeInterval(now, schedules, true, false);
            activateTimeframeInterval(now, nextSufficientTimeframeInterval);
        }
        else {
            for(ActiveIntervalChangedListener listener : scheduleChangedListeners) {
                logger.debug("{}: Timeframe interval activation check: notifying {} {}", applianceId,
                        ActiveIntervalChangedListener.class.getSimpleName(), listener.getClass().getSimpleName());
                listener.activeIntervalChecked(now, applianceId, this.state.activeTimeframeInterval);
            }
        }
    }

    public void activateTimeframeInterval(LocalDateTime now, Integer runtime) {
        Schedule schedule = new Schedule(runtime, null, new TimeOfDay(now), new TimeOfDay(now));
        Interval interval = new Interval(now.toDateTime(), now.plusSeconds(runtime).toDateTime());
        Timeframe timeframe = schedule.getTimeframe();
        TimeframeInterval runtimeTimeframeInterval = new TimeframeInterval(timeframe, interval, schedule.getRequest());
        try {
            this.previousState = (RuntimeState) this.state.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("{}: Error cloning state.", applianceId, e);
        }
        logger.debug("{}: Saving active timeframe interval: {}", applianceId, this.previousState.activeTimeframeInterval);
        this.initState();
        activateTimeframeInterval(now, runtimeTimeframeInterval);
    }

    public void activateTimeframeInterval(LocalDateTime now, Integer energy, LocalDateTime chargeEnd) {
        logger.debug("{}: Activate timeframe interval: energy={}Wh chargeEnd={}",
                applianceId, energy, chargeEnd);
        Schedule schedule = Schedule.withEnergyRequest(energy, energy, new TimeOfDay(now), new TimeOfDay(now));
        Interval interval = new Interval(now.toDateTime(), chargeEnd.toDateTime());
        // FIXME: geht das nicht besser?
        Timeframe timeframe = schedule.getTimeframe();
        activateTimeframeInterval(now, new TimeframeInterval(timeframe, interval, schedule.getRequest()));
    }

    /**
     * Activate the given timeframe interval.
     * @param now
     * @param timeframeIntervalToBeActivated the timeframe interval or null
     */
    public synchronized void activateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeIntervalToBeActivated) {
        logger.debug("{}: Begin interval (de-)activation: {}", applianceId, timeframeIntervalToBeActivated);
        TimeframeInterval deactivatedTimeframeInterval = this.state.activeTimeframeInterval;
        boolean wasRunning = this.state.wasRunning;
        boolean intervalChanged = false;
        if(timeframeIntervalToBeActivated != null && this.state.activeTimeframeInterval == null) {
            logger.debug("{}: No active interval but will activate this interval: {}", applianceId, timeframeIntervalToBeActivated);
            Schedule schedule = timeframeIntervalToBeActivated.getTimeframe().getSchedule();
            this.state.runningTime = 0;
            this.state.activeRequest = schedule.getRequest();
            if(schedule.getRequest() instanceof RuntimeRequest || schedule.getRequest() instanceof SocRequest) {
                if(schedule.getRequest() instanceof RuntimeRequest) {
                    this.state.remainingMinRunningTimeWhileNotRunning = schedule.getRequest().getMin();
                    this.state.remainingMaxRunningTimeWhileNotRunning = schedule.getRequest().getMax();
                    logger.debug("{}: Remaining running time reset to schedule defaults.", applianceId);
                }
                intervalChanged = true;
                logger.debug("{}: Interval updated from schedule.", applianceId);
            }
        }
        else if(timeframeIntervalToBeActivated == null && this.state.activeTimeframeInterval != null) {
            logger.debug("{}: Interval expired: {}", applianceId, this.state.activeTimeframeInterval);
            if(this.previousState != null && this.previousState.remainingMinRunningTimeWhileNotRunning != null) {
                if(this.previousState.remainingMinRunningTimeWhileNotRunning == 0) {
                    logger.debug("{}: Restore original Interval: {}", applianceId, previousState.activeTimeframeInterval);
                    timeframeIntervalToBeActivated = this.previousState.activeTimeframeInterval;
                    this.state = this.previousState;
                }
                this.previousState = null;
            }
            else {
                this.initState();
            }
            intervalChanged = true;
        }
        this.state.activeTimeframeInterval = timeframeIntervalToBeActivated;
        if(intervalChanged) {
            logger.debug("{}: Active interval changed. deactivatedTimeframeInterval={} activeTimeframeInterval={}",
                    applianceId, deactivatedTimeframeInterval, this.state.activeTimeframeInterval);
            for(ActiveIntervalChangedListener listener : scheduleChangedListeners) {
                logger.debug("{}: Notifying {} {}", applianceId, ActiveIntervalChangedListener.class.getSimpleName(),
                        listener.getClass().getSimpleName());
                listener.activeIntervalChanged(now, applianceId, deactivatedTimeframeInterval,
                        this.state.activeTimeframeInterval, wasRunning);
            }
        }
    }

    /**
     * Deactivate the active timeframe interval if it is expired.
     * @param now
     */
    protected void deactivateExpiredTimeframeInterval(LocalDateTime now) {
        if(this.state.activeTimeframeInterval != null) {
            if(now.toDateTime().isAfter(this.state.activeTimeframeInterval.getInterval().getEnd())) {
                activateTimeframeInterval(now, (TimeframeInterval) null);
            }
        }
    }
}
