/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TimeframeIntervalHandler implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(TimeframeIntervalHandler.class);
    private static final int CONSIDERATION_INTERVAL_DAYS = 2;
    private String applianceId;
    private List<Schedule> schedules;
    private GuardedTimerTask fillQueueTimerTask;
    private GuardedTimerTask updateQueueTimerTask;
    private LinkedList<TimeframeInterval> queue = new LinkedList<>();
    private Set<ActiveIntervalChangedListener> timeframeIntervalChangedListeners = new HashSet<>();
    private Set<TimeframeIntervalStateChangedListener> timeframeIntervalStateChangedListeners = new HashSet<>();

    public TimeframeIntervalHandler(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void addTimeFrameIntervalChangedListener(ActiveIntervalChangedListener listener) {
        this.timeframeIntervalChangedListeners.add(listener);
    }

    public void addTimeframeIntervalStateChangedListener(TimeframeIntervalStateChangedListener listener) {
        this.timeframeIntervalStateChangedListeners.add(listener);
    }

    public void setTimer(Timer timer) {

        this.fillQueueTimerTask = new GuardedTimerTask(this.applianceId, "FillQueueTimerTask", 1 * 3600 * 1000) {
            @Override
            public void runTask() {
                fillQueue(new LocalDateTime());
            }
        };
        if (timer != null) {
            timer.schedule(fillQueueTimerTask, 0, fillQueueTimerTask.getPeriod());
        }

        this.updateQueueTimerTask = new GuardedTimerTask(this.applianceId,
                "UpdateActiveTimeframeInterval", 30000) {
            @Override
            public void runTask() {
                updateQueue(new LocalDateTime());
            }
        };
        if (timer != null) {
            timer.schedule(updateQueueTimerTask, 0, updateQueueTimerTask.getPeriod());
        }
    }

    public void cancelTimer() {
        logger.info("{}: Cancel timer tasks", applianceId);
        if (this.updateQueueTimerTask != null) {
            this.updateQueueTimerTask.cancel();
        }
        if (this.fillQueueTimerTask != null) {
            this.fillQueueTimerTask.cancel();
        }
    }

    public List<TimeframeInterval> getQueue() {
        return new ArrayList<>(queue);
    }

    private void fillQueue(LocalDateTime now) {
        logger.debug("{}: Starting to fill queue", applianceId);
        Interval considerationInterval = new Interval(now.toDateTime(), now.plusDays(CONSIDERATION_INTERVAL_DAYS).toDateTime());
        TimeframeInterval lastTimeframeInterval = queue.peekLast();
        List<TimeframeInterval> timeFrameIntervals = findTimeframeIntervals(now, considerationInterval);
        timeFrameIntervals.stream()
                .filter(timeFrameInterval -> lastTimeframeInterval == null
                        || timeFrameInterval.getInterval().getStart().isAfter(lastTimeframeInterval.getInterval().getEnd()))
                .peek(timeframeInterval ->
                        logger.debug("{}: Adding timeframeInterval to queue: {}", applianceId, timeframeInterval))
                .forEach(timeFrameInterval -> {
                    queue.add(timeFrameInterval);
                    timeFrameInterval.stateTransitionTo(now, TimeframeIntervalState.QUEUED);
                });
    }

    public void updateQueue(LocalDateTime now) {
        logger.debug("{}: Current Queue:", applianceId);
        logQueue();

        Optional<TimeframeInterval> deactivatableTimeframeInterval = queue.stream()
                .filter(timeframeInterval -> timeframeInterval.getState() == TimeframeIntervalState.ACTIVE)
                .filter(timeframeInterval -> timeframeInterval.isDeactivatable(now))
                .findFirst();
        if(deactivatableTimeframeInterval.isPresent()) {
            logger.debug("{}: Deactivate timeframe interval: {}", applianceId, deactivatableTimeframeInterval.get());
            queue.remove(deactivatableTimeframeInterval.get());
        }

        Optional<TimeframeInterval> activatableTimeframeInterval = queue.stream()
                .filter(timeframeInterval -> timeframeInterval.getState() != TimeframeIntervalState.ACTIVE)
                .filter(timeframeInterval -> timeframeInterval.isActivatable(now))
                .findFirst();
        if(! hasActiveTimeframeInterval() && activatableTimeframeInterval.isPresent()) {
            logger.debug("{}: Activate timeframe interval: {}", applianceId, activatableTimeframeInterval.get());
            activatableTimeframeInterval.get().stateTransitionTo(now, TimeframeIntervalState.ACTIVE);
        }

        if(deactivatableTimeframeInterval.isPresent() || activatableTimeframeInterval.isPresent()) {
            logger.debug("{}: Updated queue:", applianceId);
            logQueue();
            for(ActiveIntervalChangedListener listener : timeframeIntervalChangedListeners) {
                logger.debug("{}: Notifying {} {}", applianceId, ActiveIntervalChangedListener.class.getSimpleName(),
                        listener.getClass().getSimpleName());
                listener.activeIntervalChanged(now, applianceId,
                        deactivatableTimeframeInterval.orElse(null),
                        activatableTimeframeInterval.orElse(null),
                        deactivatableTimeframeInterval
                                .map(timeframeInterval -> timeframeInterval.wasInState(TimeframeIntervalState.ACTIVE))
                                .orElse(false));
            }
        }
    }

    private void logQueue() {
        queue.forEach(timeFrameInterval -> logger.debug("{}: {} {}",
                applianceId,
                timeFrameInterval.getState(),
                timeFrameInterval.toString()));
    }

    private boolean hasActiveTimeframeInterval() {
        return queue.stream()
                .anyMatch(timeframeInterval -> timeframeInterval.getState() == TimeframeIntervalState.ACTIVE);
    }

    public void deactivateCurrentlyActiveTimeframeInterval() {
    }

    /**
     * Returns timeframe intervals starting within a consideration interval.
     * If not consideration interval is given, all timeframe intervals are returned.
     *
     * @param now
     * @param considerationInterval timeframe intervals have to start within this interval
     * @return a (possibly empty) list of timeframes sorted by starting time
     */
    private List<TimeframeInterval> findTimeframeIntervals(LocalDateTime now, Interval considerationInterval) {
        List<TimeframeInterval> timeframeIntervals = new ArrayList<>();
        if (schedules != null) {
            schedules
                    .stream()
                    .filter(Schedule::isEnabled)
                    .forEach(schedule -> {
                        Timeframe timeframe = schedule.getTimeframe();
                        timeframe.getIntervals(now).forEach(timeframeInterval -> {
                            if (considerationInterval == null
                                    || considerationInterval.contains(timeframeInterval.getInterval().getStart())
                                    || timeframeInterval.getInterval().contains(considerationInterval.getStart())
                            ) {
                                timeframeInterval.setApplianceId(applianceId);
                                timeframeInterval.getRequest().setApplianceId(applianceId);
                                timeframeIntervalStateChangedListeners.forEach(listener ->
                                        timeframeInterval.addTimeframeIntervalStateChangedListener(listener));
                                timeframeIntervalChangedListeners.forEach(
                                        listener -> listener.timeframeIntervalCreated(now, timeframeInterval));
                                timeframeIntervals.add(timeframeInterval);
                            }
                        });
                    });
        }
        timeframeIntervals.sort(new TimeframeIntervalComparator());
        return timeframeIntervals;
    }
}
