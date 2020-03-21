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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.appliance.TimeframeIntervalChangedListener;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

public class TimeframeIntervalHandler implements ApplianceIdConsumer, ControlStateChangedListener {

    private Logger logger = LoggerFactory.getLogger(TimeframeIntervalHandler.class);
    public static final int CONSIDERATION_INTERVAL_DAYS = 2;
    private String applianceId;
    private List<Schedule> schedules;
    private GuardedTimerTask fillQueueTimerTask;
    private GuardedTimerTask updateQueueTimerTask;
    private LinkedList<TimeframeInterval> queue = new LinkedList<>();
    private Set<TimeframeIntervalChangedListener> timeframeIntervalChangedListeners = new HashSet<>();
    private Control control;

    public TimeframeIntervalHandler(List<Schedule> schedules, Control control) {
        this.schedules = schedules;
        this.control = control;
        if(control != null) {
            control.addControlStateChangedListener(this);
        }
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void addTimeframeIntervalChangedListener(TimeframeIntervalChangedListener listener) {
        this.timeframeIntervalChangedListeners.add(listener);
    }

    public void removeTimeframeIntervalChangedListener(TimeframeIntervalChangedListener listener) {
        this.timeframeIntervalChangedListeners.remove(listener);
    }

    public void setTimer(Timer timer) {
        if(control != null) {
            this.fillQueueTimerTask = new GuardedTimerTask(this.applianceId, "FillQueueTimerTask", 1 * 3600 * 1000) {
                @Override
                public void runTask() {
                    fillQueue(LocalDateTime.now());
                }
            };
            if (timer != null) {
                timer.schedule(fillQueueTimerTask, 0, fillQueueTimerTask.getPeriod());
            }

            this.updateQueueTimerTask = new GuardedTimerTask(this.applianceId,
                    "UpdateActiveTimeframeInterval", 30000) {
                @Override
                public void runTask() {
                    updateQueue(LocalDateTime.now());
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

    public void clearQueue() {
        logger.debug("{}: Cleaing queue", applianceId);
        queue.clear();
    }

    public void fillQueue(LocalDateTime now) {
        logger.debug("{}: Starting to fill queue", applianceId);
        Interval considerationInterval = new Interval(now, now.plusDays(CONSIDERATION_INTERVAL_DAYS));
        TimeframeInterval lastTimeframeInterval = queue.peekLast();
        List<TimeframeInterval> timeframeIntervals = findTimeframeIntervals(now, considerationInterval);
        timeframeIntervals.stream()
                .filter(timeframeInterval -> (lastTimeframeInterval == null
                        || timeframeInterval.getInterval().getStart().isAfter(lastTimeframeInterval.getInterval().getEnd()))
                        && timeframeInterval.isIntervalSufficient(now)
                )
                .limit(control instanceof StartingCurrentSwitch ? 1 : Integer.MAX_VALUE)
                .forEach(timeframeInterval -> {
                    if(control instanceof StartingCurrentSwitch) {
                        timeframeInterval.getRequest().setEnabled(false);
                    }
                    addTimeframeInterval(now, timeframeInterval, false, true);
                });
    }

    public void updateQueue(LocalDateTime now) {
        queue.forEach(timeframeInterval -> timeframeInterval.getRequest().update());
        logger.debug("{}: Current Queue:", applianceId);
        logQueue(now);

        Optional<TimeframeInterval> prolongableTimeframeInterval = getProlongableTimeframeInterval(now);
        prolongableTimeframeInterval.ifPresent(timeframeInterval -> {
            int indexProlongableTimeframeInterval = queue.indexOf(timeframeInterval);
            TimeframeInterval successor = queue.size() > indexProlongableTimeframeInterval + 1 ?
                    queue.get(indexProlongableTimeframeInterval + 1) : null;
            if(! prolongOptionalEnergyTimeframeIntervalForEVCharger(now, timeframeInterval, successor)) {
                moveOptionalEnergyTimeframeIntervalToSecondPosition(now);
            }
        });

        Optional<TimeframeInterval> activatableTimeframeInterval = getActivatableTimeframeInterval(now);
        Holder<Boolean> optionalEnergyTimeframeIntervalMoved = new Holder<>(false);
        if(activatableTimeframeInterval.isPresent()) {
            TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
            if(activeTimeframeInterval != null
                    && activeTimeframeInterval.getRequest() instanceof OptionalEnergySocRequest) {
                moveOptionalEnergyTimeframeIntervalToSecondPosition(now);
                optionalEnergyTimeframeIntervalMoved.value = true;
            }
        }

        Optional<TimeframeInterval> deactivatableTimeframeInterval = getDeactivatableTimeframeInterval(now);
        Holder<Boolean> removalPending = new Holder<>(false);
        deactivatableTimeframeInterval.ifPresent(timeframeInterval -> {
            if(!optionalEnergyTimeframeIntervalMoved.value) {
                timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.EXPIRED);
                timeframeInterval.getRequest().setEnabled(false);
                if(timeframeInterval.isRemovable(now)) {
                    removeTimeframeInterval(now, timeframeInterval);
                    if(control instanceof ElectricVehicleCharger
                            && !(timeframeInterval.getRequest() instanceof OptionalEnergySocRequest)
                            && queue.size() == 0) {
                        TimeframeInterval optionalEnergyTimeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(now,
                                ((ElectricVehicleCharger) control).getConnectedVehicleId());
                        addTimeframeInterval(now, optionalEnergyTimeframeInterval, false, false);
                    }
                }
                else {
                    removalPending.value = true;
                }
            }
        });

        // re-evaluate after potential de-activation
        activatableTimeframeInterval = getActivatableTimeframeInterval(now);
        if(activatableTimeframeInterval.isPresent() && ! removalPending.value) {
            if(! hasActiveTimeframeInterval()) {
                activateTimeframeInterval(now, activatableTimeframeInterval.get());
            }
        }

        Optional<TimeframeInterval> removableTimeframeInterval = getRemovableTimeframeInterval(now);
        removableTimeframeInterval.ifPresent(timeframeInterval -> removeTimeframeInterval(now, timeframeInterval));

        for(int i=0; i<queue.size(); i++) {
            queue.get(i).getRequest().setNext(i == 0);
        }

        if(deactivatableTimeframeInterval.isPresent()
                || activatableTimeframeInterval.isPresent()
                || removableTimeframeInterval.isPresent()) {
            queue.forEach(timeframeInterval -> timeframeInterval.getRequest().update());
            logger.debug("{}: Updated queue:", applianceId);
            logQueue(now);
        }

        if(deactivatableTimeframeInterval.isPresent() || activatableTimeframeInterval.isPresent()) {
            for(TimeframeIntervalChangedListener listener : timeframeIntervalChangedListeners) {
                logger.debug("{}: Notifying {} {} {}", applianceId, TimeframeIntervalChangedListener.class.getSimpleName(),
                        listener.getClass().getSimpleName(), listener);
                listener.activeIntervalChanged(now, applianceId,
                        deactivatableTimeframeInterval.orElse(null),
                        activatableTimeframeInterval.orElse(null),
                        deactivatableTimeframeInterval
                                .map(timeframeInterval -> timeframeInterval.wasInState(TimeframeIntervalState.ACTIVE))
                                .orElse(false));
            }
        }
    }

    private void logQueue(LocalDateTime now) {
        queue.forEach(timeframeInterval -> logger.debug("{}: {}",
                applianceId,
                timeframeInterval.toString(now)));
    }

    private Optional<TimeframeInterval> getActivatableTimeframeInterval(LocalDateTime now) {
        if(hasActiveTimeframeInterval()) {
            return Optional.empty();
        }
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.isActivatable(now))
                .findFirst();
    }

    private Optional<TimeframeInterval> getDeactivatableTimeframeInterval(LocalDateTime now) {
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.isDeactivatable(now))
                .findFirst();
    }

    private Optional<TimeframeInterval> getRemovableTimeframeInterval(LocalDateTime now) {
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.isRemovable(now))
                .findFirst();
    }

    private Optional<TimeframeInterval> getProlongableTimeframeInterval(LocalDateTime now) {
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.isProlongable(now))
                .findFirst();
    }

    public void addTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval, boolean asFirst, boolean updateQueue) {
        logger.debug("{}: Adding timeframeInterval to queue: {}", applianceId, timeframeInterval.toString(now));

        addTimeframeIntervalChangedListener(timeframeInterval.getRequest());
        timeframeIntervalChangedListeners.forEach(
                listener -> listener.timeframeIntervalCreated(now, timeframeInterval));

        if (asFirst) {
            TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
            if(activeTimeframeInterval != null) {
                deactivateTimeframeInterval(now, activeTimeframeInterval);

                LocalDateTime firstIntervalStart = activeTimeframeInterval.getInterval().getStart();
                LocalDateTime addedIntervalEnd = timeframeInterval.getInterval().getEnd();
                if(firstIntervalStart.isEqual(addedIntervalEnd) || firstIntervalStart.isBefore(addedIntervalEnd)) {
                    Interval firstIntervalAdjusted = new Interval(addedIntervalEnd.plusSeconds(1),
                            activeTimeframeInterval.getInterval().getEnd());
                    activeTimeframeInterval.setInterval(firstIntervalAdjusted);
                }
            }
            queue.addFirst(timeframeInterval);
        } else {
            queue.add(timeframeInterval);
        }
        timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.QUEUED);
        if(updateQueue) {
            updateQueue(now);
        }
    }

    private void activateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Activate timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.ACTIVE);
    }

    private void deactivateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Deactivate timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        timeframeInterval.stateTransitionTo(now, TimeframeIntervalState.QUEUED);
    }

    private void removeTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Remove timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        queue.remove(timeframeInterval);
        removeTimeframeIntervalChangedListener(timeframeInterval.getRequest());
        control.removeControlStateChangedListener(timeframeInterval.getRequest());
        if(control instanceof StartingCurrentSwitch && timeframeInterval.getRequest() instanceof RuntimeRequest) {
            ((StartingCurrentSwitch) control)
                    .removeStartingCurrentSwitchListener((RuntimeRequest) timeframeInterval.getRequest());
            fillQueue(now);
        }
    }

    private boolean hasActiveTimeframeInterval() {
        return getActiveTimeframeInterval() != null;
    }

    public TimeframeInterval getActiveTimeframeInterval() {
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
    protected List<TimeframeInterval> findTimeframeIntervals(LocalDateTime now, Interval considerationInterval) {
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
                                timeframeIntervals.add(timeframeInterval);
                            }
                        });
                    });
        }
        timeframeIntervals.sort(new TimeframeIntervalComparator());
        return timeframeIntervals;
    }

    private TimeframeInterval createOptionalEnergyTimeframeIntervalForEVCharger(LocalDateTime now, Integer evId) {
        Interval interval = createOptionalEnergyIntervalForEVCharger(now, null,
                queue.size() > 0 ? queue.get(0) : null);
        if(interval == null) {
            return null;
        }
        OptionalEnergySocRequest request = new OptionalEnergySocRequest(evId);

        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);

        return timeframeInterval;
    }

    private Interval createOptionalEnergyIntervalForEVCharger(LocalDateTime now,
                                                              TimeframeInterval predecessor,
                                                              TimeframeInterval successor) {
        LocalDateTime timeframeStart = LocalDateTime.from(now);
        LocalDateTime timeframeEnd = timeframeStart.plusDays(CONSIDERATION_INTERVAL_DAYS);
        if(predecessor != null) {
            timeframeStart = predecessor.getInterval().getEnd().plusSeconds(1);
        }
        if(successor != null) {
            timeframeEnd = successor.getInterval().getStart().minusSeconds(1);
        }
        if(timeframeStart.isBefore(timeframeEnd)) {
            return new Interval(timeframeStart, timeframeEnd);
        }
        return null;
    }

    private boolean prolongOptionalEnergyTimeframeIntervalForEVCharger(
            LocalDateTime now, TimeframeInterval timeframeInterval, TimeframeInterval successor) {
        if(timeframeInterval.getRequest() instanceof OptionalEnergySocRequest) {
            OptionalEnergySocRequest optionalEnergySocRequest = (OptionalEnergySocRequest) timeframeInterval.getRequest();
            if(! optionalEnergySocRequest.isFinished(now)) {
                Interval prolongedInterval = createOptionalEnergyIntervalForEVCharger(now, timeframeInterval, successor);
                if(prolongedInterval != null) {
                    logger.debug("{}: Prolong timeframe interval:   {}", applianceId, timeframeInterval.toString(now));
                    timeframeInterval.setInterval(prolongedInterval);
                    logger.debug("{}: Prolonged timeframe interval: {}", applianceId, timeframeInterval.toString(now));
                    return true;
                }
            }
        }
        return false;
    }

    private void moveOptionalEnergyTimeframeIntervalToSecondPosition(LocalDateTime now) {
        TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
        logger.debug("{}: Moving to second place in queue: {}", applianceId, getActiveTimeframeInterval().toString(now));
        queue.remove(activeTimeframeInterval);
        queue.add(1, activeTimeframeInterval);
        activeTimeframeInterval.stateTransitionTo(now, TimeframeIntervalState.QUEUED);
        activeTimeframeInterval.setInterval(
                createOptionalEnergyIntervalForEVCharger(now, null, queue.size() > 2 ? queue.get(2) : null));
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
    }

    @Override
    public void onEVChargerStateChanged(LocalDateTime now, EVChargerState previousState, EVChargerState newState,
                                        ElectricVehicle ev) {
        if(newState == EVChargerState.VEHICLE_CONNECTED) {
            if(! hasActiveTimeframeInterval()) {
                TimeframeInterval timeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(now, ev.getId());
                if(timeframeInterval != null) {
                    addTimeframeInterval(now, timeframeInterval, true, true);
                    updateQueue(now);
                    activateTimeframeInterval(now, timeframeInterval);
                }
            }
        }
    }

    @Override
    public void onEVChargerSocChanged(LocalDateTime now, Float soc) {
//        queue.forEach(timeframeInterval -> timeframeInterval.getRequest().onEVChargerSocChanged(now ,soc));
    }

    public Integer suggestRuntime() {
        LocalDateTime now = LocalDateTime.now();
        if(queue.size() > 0) {
            TimeframeInterval timeframeInterval = queue.get(0);
            return timeframeInterval.getRequest().getMax(now);
        }
        return null;
    }

    public void setRuntimeDemand(LocalDateTime now, Integer runtime, boolean acceptControlRecommendations) {
        TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
        if(activeTimeframeInterval != null) {
            if(activeTimeframeInterval.getRequest() instanceof RuntimeRequest) {
                RuntimeRequest request = (RuntimeRequest) activeTimeframeInterval.getRequest();
                request.setMax(runtime);
                request.setAcceptControlRecommendations(acceptControlRecommendations);
            }
        }
        else {
            RuntimeRequest request = new RuntimeRequest(null, runtime);
            request.setEnabled(true);
            request.setAcceptControlRecommendations(acceptControlRecommendations);
            Interval interval = new Interval(now, now.plusSeconds(runtime));
            TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);
            queue.forEach(queuedTimeframeInterval -> queuedTimeframeInterval.getRequest().setEnabled(false));
            addTimeframeInterval(now, timeframeInterval, true, true);
        }
    }

    @Override
    public String toString() {
        return "";
    }
}
