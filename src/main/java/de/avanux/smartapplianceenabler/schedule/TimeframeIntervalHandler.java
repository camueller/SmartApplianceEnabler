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
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeframeIntervalHandler implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(TimeframeIntervalHandler.class);
    String TOPIC = "TimeframeIntervalQueue";
    public static final int CONSIDERATION_INTERVAL_DAYS = 2;
    public static final int FILL_QUEUE_INTERVAL_SECONDS = 3600;
    public static final int UPDATE_QUEUE_INTERVAL_SECONDS = 30;
    private String applianceId;
    private List<Schedule> schedules;
    private GuardedTimerTask fillQueueTimerTask;
    private GuardedTimerTask updateQueueTimerTask;
    private LinkedList<TimeframeInterval> queue = new LinkedList<>();
    private transient MqttClient mqttClient;
    private transient ControlMessage wrappedControlMessage;
    private Set<TimeframeIntervalChangedListener> timeframeIntervalChangedListeners = new HashSet<>();
    private Control control;

    public TimeframeIntervalHandler(List<Schedule> schedules, Control control) {
        this.schedules = schedules;
        this.control = control;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        mqttClient.subscribe(MqttEventName.EnableRuntimeRequest, (topic, message) -> {
            logger.debug("{} Handling event EnableRuntimeRequest", applianceId);
            var timeframe = getFirstTimeframeInterval();
            if(timeframe != null) {
                var runtimeRequest = (RuntimeRequest) timeframe.getRequest();
                runtimeRequest.resetRuntime();
                runtimeRequest.setEnabled(true);
            }
            else {
                logger.error("No timeframe found.");
            }

        });
        mqttClient.subscribe(MqttEventName.DisableRuntimeRequest, (topic, message) -> {
            logger.debug("{} Handling event DisableRuntimeRequest", applianceId);
            var timeframe = getFirstTimeframeInterval();
            if(timeframe != null) {
                var runtimeRequest = (RuntimeRequest) timeframe.getRequest();
                runtimeRequest.setEnabled(false);
                runtimeRequest.resetEnabledBefore();
            }
            else {
                logger.error("No timeframe found.");
            }
        });
        mqttClient.subscribe(StartingCurrentSwitch.WRAPPED_CONTROL_TOPIC, true, (topic, message) -> {
            if(message instanceof ControlMessage && topic.equals(StartingCurrentSwitch.WRAPPED_CONTROL_TOPIC)) {
                wrappedControlMessage = (ControlMessage) message;
            }
        });
        this.mqttClient.subscribe(MqttEventName.EVChargerStateChanged, (topic, message) -> {
            if(message instanceof EVChargerStateChangedEvent && control instanceof ElectricVehicleCharger) {
                logger.debug("{} Handling event EVChargerStateChanged", applianceId);
                EVChargerStateChangedEvent event = (EVChargerStateChangedEvent) message;
                if(event.newState == EVChargerState.VEHICLE_CONNECTED) {
                    ElectricVehicleCharger evCharger = (ElectricVehicleCharger) control;
                    if(evCharger == null || evCharger.getElectricVehicleHandler() == null) {
                        return;
                    }
                    ElectricVehicle ev = evCharger.getElectricVehicleHandler().getVehicle(event.evId);
                    if(ev != null) {
                        queue.stream()
                                .filter(timeframeInterval -> timeframeInterval.getRequest() instanceof SocRequest)
                                .filter(timeframeInterval -> !(timeframeInterval.getRequest() instanceof OptionalEnergySocRequest))
                                .forEach(timeframeInterval ->
                                        updateSocRequest((SocRequest) timeframeInterval.getRequest(), ev.getBatteryCapacity(), null));
                        if(findOptionalEnergyIntervalForEVCharger() == null) {
                            TimeframeInterval timeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(
                                    event.getTime(), ev.getId(), applianceId);
                            if(timeframeInterval != null) {
                                updateSocRequest((SocRequest) timeframeInterval.getRequest(),
                                        ev.getBatteryCapacity(), ev.getDefaultSocOptionalEnergy());
                                addTimeframeInterval(event.getTime(), timeframeInterval, true, true);
                                updateQueue(event.getTime(), false);
                                activateTimeframeInterval(event.getTime(), timeframeInterval);
                            }
                        }
                    }
                }
            }
        });
    }

    public void disconnectMqttClient() {
        queue.forEach(timeframeInterval -> timeframeInterval.getRequest().disconnectMqttClient());
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public void addTimeframeIntervalChangedListener(TimeframeIntervalChangedListener listener) {
        this.timeframeIntervalChangedListeners.add(listener);
    }

    public void removeTimeframeIntervalChangedListener(TimeframeIntervalChangedListener listener) {
        this.timeframeIntervalChangedListeners.remove(listener);
    }

    public void setTimer(Timer timer) {
        if(control != null) {
            this.fillQueueTimerTask = new GuardedTimerTask(this.applianceId, "FillQueueTimerTask",
                    FILL_QUEUE_INTERVAL_SECONDS * 1000) {
                @Override
                public void runTask() {
                    fillQueue(LocalDateTime.now());
                }
            };
            if (timer != null) {
                timer.schedule(fillQueueTimerTask, 0, fillQueueTimerTask.getPeriod());
            }

            this.updateQueueTimerTask = new GuardedTimerTask(this.applianceId,
                    "UpdateActiveTimeframeInterval", UPDATE_QUEUE_INTERVAL_SECONDS * 1000) {
                @Override
                public void runTask() {
                    updateQueue(LocalDateTime.now(), false);
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
                .limit(control instanceof StartingCurrentSwitch ? (queue.size() == 0 ? 1 : 0) : Integer.MAX_VALUE)
                .forEach(timeframeInterval -> {
                    if(control instanceof StartingCurrentSwitch) {
                        // if appliance is switched off, the starting current has already been detected and the
                        // request has to be enabled therefore
                        timeframeInterval.getRequest().setEnabled(wrappedControlMessage != null && ! wrappedControlMessage.on);
                    }
                    if(control instanceof ElectricVehicleCharger) {
                        timeframeInterval.getRequest().setEnabled(((ElectricVehicleCharger) control).isVehicleConnected());
                    }
                    addTimeframeInterval(now, timeframeInterval, false, true);
                });
        publishQueue(now);
    }

    public void updateQueue(LocalDateTime now, boolean ignoreStartTime) {
        if(ignoreStartTime) {
            logger.warn("{}: Forcing queue update with ignored timeframe interval start time", applianceId);
        }
        queue.forEach(timeframeInterval -> timeframeInterval.getRequest().update());
        logger.debug("{}: Current Queue{}", applianceId, queue.size() > 0 ? ":" : " is empty");
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

        Optional<TimeframeInterval> activatableTimeframeInterval = getActivatableTimeframeInterval(now, ignoreStartTime);
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
                        ElectricVehicle connectedVehicle = ((ElectricVehicleCharger) control).getElectricVehicleHandler().getConnectedVehicle();
                        TimeframeInterval optionalEnergyTimeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(
                                now, connectedVehicle.getId(), applianceId);
                        if(optionalEnergyTimeframeInterval != null) {
                            updateSocRequest((SocRequest) optionalEnergyTimeframeInterval.getRequest(),
                                    connectedVehicle.getBatteryCapacity(),
                                    connectedVehicle.getDefaultSocOptionalEnergy());
                        }
                        addTimeframeInterval(now, optionalEnergyTimeframeInterval, false, false);
                    }
                }
                else {
                    removalPending.value = true;
                }
            }
        });

        // re-evaluate after potential de-activation
        activatableTimeframeInterval = getActivatableTimeframeInterval(now, ignoreStartTime);
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

        publishQueue(now);
    }

    private void logQueue(LocalDateTime now) {
        queue.forEach(timeframeInterval -> logger.debug("{}: {}",
                applianceId,
                timeframeInterval.toString(now)));
    }

    private void publishQueue(LocalDateTime now) {
        TimeframeIntervalQueueEntry[] queueEntries = queue.stream().map(timeframeInterval -> new TimeframeIntervalQueueEntry(
                timeframeInterval.getState().toString(),
                timeframeInterval.getInterval().getStart().format(DateTimeFormatter.ISO_DATE_TIME),
                timeframeInterval.getInterval().getEnd().format(DateTimeFormatter.ISO_DATE_TIME),
                timeframeInterval.getRequest().getClass().getSimpleName(),
                timeframeInterval.getRequest().getMin(now),
                timeframeInterval.getRequest().getMax(now),
                timeframeInterval.getRequest().isEnabled()
        )).toArray(TimeframeIntervalQueueEntry[]::new);

        TimeframeIntervalQueueMessage message = new TimeframeIntervalQueueMessage(now, queueEntries);
        mqttClient.publish(TOPIC, message, false);
    }

    private Optional<TimeframeInterval> getActivatableTimeframeInterval(LocalDateTime now, boolean ignoreStartTime) {
        if(hasActiveTimeframeInterval()) {
            return Optional.empty();
        }
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.isActivatable(now, ignoreStartTime))
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
        timeframeInterval.setApplianceId(applianceId);
        timeframeInterval.getRequest().setApplianceId(applianceId);
        Request request = timeframeInterval.getRequest();
        request.init();
        if(request instanceof EnergyRequest && control instanceof ElectricVehicleCharger) {
            var evHandler = ((ElectricVehicleCharger) control).getElectricVehicleHandler();
            if(evHandler.getConnectedVehicle() != null) {
                ((EnergyRequest) request).setSocScript(evHandler.getConnectedVehicle().getSocScript() != null);
            }
        }
        addTimeframeIntervalChangedListener(request);
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
            updateQueue(now, false);
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

    public void removeTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Remove timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        queue.remove(timeframeInterval);
        removeTimeframeIntervalChangedListener(timeframeInterval.getRequest());
        timeframeInterval.getRequest().remove();
        if(control instanceof StartingCurrentSwitch) {
            fillQueue(now);
        }
    }

    public void removeActiveTimeframeInterval(LocalDateTime now) {
        TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
        if(activeTimeframeInterval != null) {
            removeTimeframeInterval(now, activeTimeframeInterval);
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

    public TimeframeInterval getFirstTimeframeInterval(TimeframeIntervalState... states) {
        if(states.length > 0) {
            return queue.stream()
                    .filter(timeframeInterval -> Arrays.stream(states).anyMatch(state -> state == timeframeInterval.getState()))
                    .findFirst().orElse(null);
        }
        if(queue.size() > 0) {
            return queue.get(0);
        }
        return null;
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

    public List<TimeframeInterval> findTimeframeIntervalsUntilFirstGap() {
        List<TimeframeInterval> intervals = new ArrayList<>();
        TimeframeInterval firstTimeframeInterval = getFirstTimeframeInterval();
        if(firstTimeframeInterval != null) {
            intervals.add(firstTimeframeInterval);
            if(queue.size() > 1) {
                TimeframeInterval secondTimeframeInterval = queue.get(1);
                if(secondTimeframeInterval.getInterval().getStart().isEqual(firstTimeframeInterval.getInterval().getEnd().plusSeconds(1))) {
                    intervals.add(secondTimeframeInterval);
                }
            }
        }
        return intervals;
    }

    private TimeframeInterval findOptionalEnergyIntervalForEVCharger() {
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.getRequest() instanceof OptionalEnergySocRequest)
                .findFirst().orElse(null);
    }

    private TimeframeInterval createOptionalEnergyTimeframeIntervalForEVCharger(LocalDateTime now, Integer evId, String applianceId) {
        Interval interval = createOptionalEnergyIntervalForEVCharger(now, null,
                queue.size() > 0 ? queue.get(0) : null);
        if(interval == null) {
            return null;
        }
        Request request = new OptionalEnergySocRequest(evId);
        request.setApplianceId(applianceId);
        return new TimeframeInterval(interval, request);
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

    public void updateSocOfOptionalEnergyTimeframeIntervalForEVCharger(LocalDateTime now,  Integer evId, Integer batteryCapacity,
                                                                       Integer socCurrent, Integer socTarget) {
        TimeframeInterval timeframeInterval = findOptionalEnergyIntervalForEVCharger();
        if(timeframeInterval != null) {
            logger.debug("{}: update optional energy timeframe interval with socCurrent={} socTarget={}",
                    applianceId, socCurrent, socTarget);
            if(timeframeInterval == getFirstTimeframeInterval()) {
                timeframeInterval.getInterval().setStart(now);
            }
            OptionalEnergySocRequest request = (OptionalEnergySocRequest) timeframeInterval.getRequest();
            if(socCurrent != null) {
                request.setSocInitialIfNotSet(socCurrent);
                request.setSocCurrent(socCurrent);
            }
            if(socTarget != null) {
                request.setSoc(socTarget);
            }
            request.setEnabled(true);
        }
        else {
            logger.debug("{}: create optional energy timeframe interval with evId={} batteryCapacity={} socCurrent={} socTarget={}",
                    applianceId, evId, batteryCapacity, socCurrent, socTarget);
            timeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(now, evId, applianceId);
            if(timeframeInterval != null) {
                OptionalEnergySocRequest request = (OptionalEnergySocRequest) timeframeInterval.getRequest();
                request.setSocInitial(socCurrent);
                request.setSocCurrent(socCurrent);
                request.setEnabled(true);
                updateSocRequest((SocRequest) timeframeInterval.getRequest(), batteryCapacity, socTarget);
                addTimeframeInterval(now, timeframeInterval, true, true);
                updateQueue(now, false);
                activateTimeframeInterval(now, timeframeInterval);
            }
        }
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
                createOptionalEnergyIntervalForEVCharger(queue.get(0).getInterval().getEnd().plusSeconds(1),
                        null, queue.size() > 2 ? queue.get(2) : null));
    }

    public void adjustOptionalEnergyTimeframeIntervalStart() {
        if(queue.size() > 1) {
            TimeframeInterval secondTimeframeInterval = queue.get(1);
            if(secondTimeframeInterval.getRequest() instanceof OptionalEnergySocRequest) {
                TimeframeInterval firstTimeframeInterval = queue.get(0);
                LocalDateTime expectedStart = firstTimeframeInterval.getInterval().getEnd().plusSeconds(1);
                if(!secondTimeframeInterval.getInterval().getStart().equals(expectedStart)) {
                    logger.debug("{}: Adjust start of optional energy timeframe interval to: {}", applianceId, expectedStart);
                    secondTimeframeInterval.getInterval().setStart(expectedStart);
                }
            }
        }
    }

    private void updateSocRequest(SocRequest request, Integer batteryCapacity, Integer soc) {
        if(soc != null) {
            request.setSoc(soc);
        }
        if(batteryCapacity != null) {
            request.setBatteryCapacity(batteryCapacity);
        }
        if(soc != null || batteryCapacity != null) {
            request.updateForced();
        }
    }

    public Integer suggestRuntime() {
        LocalDateTime now = LocalDateTime.now();
        if(queue.size() > 0) {
            TimeframeInterval timeframeInterval = queue.get(0);
            return timeframeInterval.getRequest().getMax(now);
        }
        return null;
    }

    public void setRuntimeDemand(LocalDateTime now, Integer runtime, Integer latestEnd, boolean acceptControlRecommendations) {
        final int additionalIntervalSeconds = UPDATE_QUEUE_INTERVAL_SECONDS + 1; // in order to make interval sufficient!
        final LocalDateTime requiredIntervalEnd = now.plusSeconds(
                latestEnd != null ? latestEnd : runtime + additionalIntervalSeconds);
        TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
        if(activeTimeframeInterval != null) {
            if(activeTimeframeInterval.getRequest() instanceof RuntimeRequest) {
                if(requiredIntervalEnd.isAfter(activeTimeframeInterval.getInterval().getEnd())) {
                    activeTimeframeInterval.getInterval().setEnd(requiredIntervalEnd);
                }
                RuntimeRequest request = (RuntimeRequest) activeTimeframeInterval.getRequest();
                request.setEnabled(true);
                request.setMax(runtime);
                request.setAcceptControlRecommendations(acceptControlRecommendations);
            }
        }
        else {
            RuntimeRequest request = new RuntimeRequest(null, runtime);
            request.setEnabled(true);
            request.setAcceptControlRecommendations(acceptControlRecommendations);
            Interval interval = new Interval(now, requiredIntervalEnd);
            TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);
            if(control instanceof StartingCurrentSwitch) {
                clearQueue();
            }
            addTimeframeInterval(now, timeframeInterval, true, true);
        }
    }

    @Override
    public String toString() {
        return "";
    }
}
