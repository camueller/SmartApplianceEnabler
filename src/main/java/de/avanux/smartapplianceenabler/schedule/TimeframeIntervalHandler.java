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
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TimeframeIntervalHandler implements ApplianceIdConsumer, ControlStateChangedListener {

    private Logger logger = LoggerFactory.getLogger(TimeframeIntervalHandler.class);
    private static final int CONSIDERATION_INTERVAL_DAYS = 2;
    private String applianceId;
    private List<Schedule> schedules;
    private GuardedTimerTask fillQueueTimerTask;
    private GuardedTimerTask updateQueueTimerTask;
    private LinkedList<TimeframeInterval> queue = new LinkedList<>();
    private Set<ActiveIntervalChangedListener> timeframeIntervalChangedListeners = new HashSet<>();
    private Set<TimeframeIntervalStateChangedListener> timeframeIntervalStateChangedListeners = new HashSet<>();
    private boolean controlExists;

    public TimeframeIntervalHandler(List<Schedule> schedules, Control control) {
        this.schedules = schedules;
        if(control != null) {
            control.addControlStateChangedListener(this);
            controlExists = true;
        }
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
        if(controlExists) {
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
        List<TimeframeInterval> timeframeIntervals = findTimeframeIntervals(now, considerationInterval);
        timeframeIntervals.stream()
                .filter(timeframeInterval -> lastTimeframeInterval == null
                        || timeframeInterval.getInterval().getStart().isAfter(lastTimeframeInterval.getInterval().getEnd()))
                .forEach(timeframeInterval -> addTimeframeInterval(now, timeframeInterval, false));
//        addTimeframeIntervalToQueue(now, timeframeIntervals.get(0), false);
    }

    public void updateQueue(LocalDateTime now) {
        logger.debug("{}: Current Queue:", applianceId);
        logQueue();

        Optional<TimeframeInterval> deactivatableTimeframeInterval = queue.stream()
                .filter(timeframeInterval -> timeframeInterval.getState() == TimeframeIntervalState.ACTIVE)
                .filter(timeframeInterval -> timeframeInterval.isDeactivatable(now))
                .findFirst();
        deactivatableTimeframeInterval.ifPresent(timeframeInterval -> {
            if(! prolongOptionalEnergyTimeframeIntervalForEVCharger(now, timeframeInterval)) {
                removeTimeframeInterval(now, timeframeInterval);
            }
        });

        Optional<TimeframeInterval> activatableTimeframeInterval = queue.stream()
                .filter(timeframeInterval -> timeframeInterval.getState() != TimeframeIntervalState.ACTIVE)
                .filter(timeframeInterval -> timeframeInterval.isActivatable(now))
                .findFirst();
        if(! hasActiveTimeframeInterval() && activatableTimeframeInterval.isPresent()) {
            activateTimeframeInterval(now, activatableTimeframeInterval.get());
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
        queue.forEach(timeframeInterval -> logger.debug("{}: {} {}",
                applianceId,
                timeframeInterval.getState(),
                timeframeInterval.toString()));
    }

    public void addTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval, boolean asFirst) {
        logger.debug("{}: Adding timeframeInterval to queue: {}", applianceId, timeframeInterval);

        timeframeIntervalChangedListeners.forEach(
                listener -> listener.timeframeIntervalCreated(now, timeframeInterval));

        if (asFirst) {
            TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
            if(activeTimeframeInterval != null) {
                deactivateTimeframeInterval(now, activeTimeframeInterval);
            }
            queue.addFirst(timeframeInterval);
        } else {
            queue.add(timeframeInterval);
        }
        timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.QUEUED);
    }

    private void activateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Activate timeframe interval: {}", applianceId, timeframeInterval);
        timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.ACTIVE);
    }

    private void deactivateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Deactivate timeframe interval: {}", applianceId, timeframeInterval);
        timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.QUEUED);
    }

    private void removeTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Remove timeframe interval: {}", applianceId, timeframeInterval);
        queue.remove(timeframeInterval);
    }

    private boolean hasActiveTimeframeInterval() {
        return getActiveTimeframeInterval() != null;
    }

    private TimeframeInterval getActiveTimeframeInterval() {
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.getState() == TimeframeIntervalState.ACTIVE)
                .findFirst().orElse(null);
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
                                timeframeIntervalStateChangedListeners
                                        .forEach(timeframeInterval::addTimeframeIntervalStateChangedListener);
                                timeframeIntervals.add(timeframeInterval);
                            }
                        });
                    });
        }
        timeframeIntervals.sort(new TimeframeIntervalComparator());
        return timeframeIntervals;
    }

    private TimeframeInterval createOptionalEnergyTimeframeIntervalForEVCharger(LocalDateTime now, Integer evId) {
        Interval interval = createOptionalEnergyIntervalForEVCharger(now);

        OptionalEnergySocRequest request = new OptionalEnergySocRequest(evId);

        TimeframeInterval timeframeInterval = new TimeframeInterval(null, interval, request);
        timeframeInterval.addTimeframeIntervalStateChangedListener(request);

        return timeframeInterval;
    }

    private boolean prolongOptionalEnergyTimeframeIntervalForEVCharger(LocalDateTime now, TimeframeInterval timeframeInterval) {
        if(timeframeInterval.getRequest() instanceof OptionalEnergySocRequest) {
            logger.debug("{}: Prolong timeframe interval:   {}", applianceId, timeframeInterval);
            timeframeInterval.setInterval(createOptionalEnergyIntervalForEVCharger(now));
            logger.debug("{}: Prolonged timeframe interval: {}", applianceId, timeframeInterval);
            return true;
        }
        return false;
    }

    private Interval createOptionalEnergyIntervalForEVCharger(LocalDateTime now) {
        TimeframeInterval firstTimeframeInterval = queue.peek();
        DateTime timeframeStart = now.toDateTime();
        DateTime timeframeEnd = timeframeStart.plusDays(CONSIDERATION_INTERVAL_DAYS);
        if(firstTimeframeInterval != null) {
            timeframeEnd = firstTimeframeInterval.getInterval().getStart().minusSeconds(1);
        }
        return new Interval(timeframeStart, timeframeEnd);
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
    }

    @Override
    public void onEVChargerStateChanged(LocalDateTime now, EVChargerState previousState, EVChargerState newState,
                                        ElectricVehicle ev) {
        if(newState == EVChargerState.VEHICLE_CONNECTED && ! hasActiveTimeframeInterval()) {
            TimeframeInterval timeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(now, ev.getId());
            addTimeframeInterval(now, timeframeInterval, true);
            activateTimeframeInterval(now, timeframeInterval);
        }
    }

    @Override
    public void onEVChargerSocChanged(LocalDateTime now, Float soc) {
    }
}
