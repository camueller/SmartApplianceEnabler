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
import de.avanux.smartapplianceenabler.control.VariablePowerConsumer;
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeframeIntervalHandler implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(TimeframeIntervalHandler.class);
    final static public String TOPIC = "TimeframeIntervalQueue";
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
            if(message instanceof EVChargerStateChangedEvent) {
                logger.debug("{} Handling event EVChargerStateChanged", applianceId);
                EVChargerStateChangedEvent event = (EVChargerStateChangedEvent) message;
                if(event.newState == EVChargerState.VEHICLE_CONNECTED) {
                    queue.stream()
                            .filter(timeframeInterval -> timeframeInterval.getRequest() instanceof SocRequest)
                            .filter(timeframeInterval -> !(timeframeInterval.getRequest() instanceof OptionalEnergySocRequest))
                            .forEach(timeframeInterval ->
                                    updateSocRequest((SocRequest) timeframeInterval.getRequest(), event.batteryCapacity, null));
                    if(findOptionalEnergyIntervalForEVCharger() == null) {
                        TimeframeInterval timeframeInterval = createOptionalEnergyTimeframeIntervalForEVCharger(
                                event.getTime(), event.evId, applianceId);
                        if(timeframeInterval != null) {
                            updateSocRequest((SocRequest) timeframeInterval.getRequest(),
                                    event.batteryCapacity, event.defaultSocOptionalEnergy);
                            addTimeframeInterval(event.getTime(), timeframeInterval, true, true);
                            updateQueue(event.getTime());
//                            activateTimeframeInterval(event.getTime(), timeframeInterval);
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

    protected Set<TimeframeIntervalChangedListener> getTimeframeIntervalChangedListener() {
        return this.timeframeIntervalChangedListeners;
    }

    public void setTimer(Timer timer) {
        if(control != null) {
            fillQueue(LocalDateTime.now(), false);

            this.updateQueueTimerTask = new GuardedTimerTask(this.applianceId,
                    "UpdateActiveTimeframeInterval", UPDATE_QUEUE_INTERVAL_SECONDS * 1000) {
                @Override
                public void runTask(LocalDateTime now) {
                    updateQueue(now);
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
        logger.debug("{}: Clearing queue", applianceId);
        var intervals = new ArrayList<TimeframeInterval>(queue);
        intervals.forEach(interval -> removeTimeframeInterval(LocalDateTime.now(), interval, false));
        queue.clear();
    }

    public void fillQueue(LocalDateTime now, boolean startFromMidnightTomorrow) {
        logger.debug("{}: Starting to fill queue", applianceId);
        LocalDateTime nextMidnight = LocalDate.now().atStartOfDay().plusDays(1);
        Interval considerationInterval = new Interval(startFromMidnightTomorrow ? nextMidnight : now, now.plusDays(CONSIDERATION_INTERVAL_DAYS));
        TimeframeInterval lastTimeframeInterval = queue.peekLast();
        List<TimeframeInterval> timeframeIntervals = findTimeframeIntervals(now, considerationInterval);
        timeframeIntervals.stream()
                .filter(timeframeInterval -> (lastTimeframeInterval == null
                        || timeframeInterval.getInterval().getStart().isAfter(lastTimeframeInterval.getInterval().getEnd()))
                        && timeframeInterval.isIntervalSufficient(now)
                )
                .limit(
                        (control instanceof StartingCurrentSwitch || control instanceof VariablePowerConsumer)
                                ? (queue.isEmpty() ? 1 : 0) : Integer.MAX_VALUE)
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

    public void updateQueue(LocalDateTime now) {
        updateQueue(now, null);
    }

    public void updateQueue(LocalDateTime now, Integer considerTimeframeIntervalsActivatableWithinSeconds) {
        if(considerTimeframeIntervalsActivatableWithinSeconds != null) {
            logger.warn("{}: Considering timeframe intervals activatable withing {} seconds",
                    applianceId, considerTimeframeIntervalsActivatableWithinSeconds);
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

        Optional<TimeframeInterval> activatableTimeframeInterval = getActivatableTimeframeInterval(now, considerTimeframeIntervalsActivatableWithinSeconds);
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
        Holder<Boolean> removedRequestEnabled = new Holder<>(false);
        deactivatableTimeframeInterval.ifPresent(timeframeInterval -> {
            if(!optionalEnergyTimeframeIntervalMoved.value) {
                timeframeInterval.stateTransitionTo(TimeframeIntervalState.EXPIRED);
                removedRequestEnabled.value = timeframeInterval.getRequest().isEnabled();
                timeframeInterval.getRequest().setEnabled(false);
                if(timeframeInterval.isRemovable(now)) {
                    removeTimeframeInterval(now, timeframeInterval, removedRequestEnabled.value);
                    if(queue.size() > 0) {
                        var nextTimeframeInterval = queue.get(0);
                        if(nextTimeframeInterval.getRequest() instanceof OptionalEnergySocRequest) {
                            adjustTimeframeIntervalStart(nextTimeframeInterval, now);
                        }
                    }
                    if(!(timeframeInterval.getRequest() instanceof OptionalEnergySocRequest) && queue.size() == 0) {
                        createAndActivateOptionalEnergyTimeframeIntervalForEVCharger(now);
                    }
                }
                else {
                    removalPending.value = true;
                }
            }
        });

        // re-evaluate after potential de-activation
        activatableTimeframeInterval = getActivatableTimeframeInterval(now, considerTimeframeIntervalsActivatableWithinSeconds);
        if(activatableTimeframeInterval.isPresent() && ! removalPending.value) {
            if(! hasActiveTimeframeInterval()) {
                activateTimeframeInterval(now, activatableTimeframeInterval.get());
            }
        }

        Optional<TimeframeInterval> removableTimeframeInterval = getRemovableTimeframeInterval(now);
        removableTimeframeInterval.ifPresent(timeframeInterval -> removeTimeframeInterval(
                now, timeframeInterval, removedRequestEnabled.value));

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

    protected void logQueue(LocalDateTime now) {
        queue.forEach(timeframeInterval -> logger.debug("{}: {}",
                applianceId,
                timeframeInterval.toString(now)));
    }

    protected void publishQueue(LocalDateTime now) {
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

    private Optional<TimeframeInterval> getActivatableTimeframeInterval(LocalDateTime now) {
        return getActivatableTimeframeInterval(now, null);
    }

    private Optional<TimeframeInterval> getActivatableTimeframeInterval(LocalDateTime now, Integer withinSeconds) {
        if(hasActiveTimeframeInterval()) {
            return Optional.empty();
        }
        return queue.stream()
                .filter(timeframeInterval -> timeframeInterval.isActivatable(now, withinSeconds))
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
        Request request = timeframeInterval.getRequest();
        request.setApplianceId(applianceId);
        request.init();
        if(request instanceof EnergyRequest && control instanceof ElectricVehicleCharger) {
            var evHandler = ((ElectricVehicleCharger) control).getElectricVehicleHandler();
            if(evHandler.getConnectedVehicle() != null) {
                ((EnergyRequest) request).setSocScriptUsed(evHandler.getConnectedVehicle().getSocScript() != null);
            }
        }
        addTimeframeIntervalChangedListener(request);
        timeframeIntervalChangedListeners.forEach(
                listener -> listener.timeframeIntervalCreated(now, timeframeInterval));

        if (asFirst) {
            TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
            if(activeTimeframeInterval != null) {
                deactivateTimeframeInterval(now, activeTimeframeInterval);
            }
            queue.addFirst(timeframeInterval);
            adjustSecondTimeframeIntervalStart(timeframeInterval, activeTimeframeInterval);
        } else {
            queue.add(timeframeInterval);
        }
        timeframeInterval.stateTransitionTo(TimeframeIntervalState.QUEUED);
        if(updateQueue) {
            updateQueue(now);
        }
    }

    public void addTimeframeIntervalAndAdjustOptionalEnergyTimeframe(LocalDateTime now, TimeframeInterval timeframeInterval, boolean asFirst) {
        addTimeframeInterval(now, timeframeInterval, asFirst, false);
        if(asFirst) {
            adjustOptionalEnergyTimeframeIntervalStart();
        } else {
            adjustOptionalEnergyTimeframeIntervalEnd();
        }
        updateQueue(now);
    }

    protected void activateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Activate timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        timeframeInterval.stateTransitionTo(TimeframeIntervalState.ACTIVE);
    }

    protected void deactivateTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval) {
        logger.debug("{}: Deactivate timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        timeframeInterval.stateTransitionTo(TimeframeIntervalState.QUEUED);
    }

    public void removeTimeframeInterval(LocalDateTime now, TimeframeInterval timeframeInterval, boolean enable) {
        logger.debug("{}: Remove timeframe interval: {}", applianceId, timeframeInterval.toString(now));
        queue.remove(timeframeInterval);
        removeTimeframeIntervalChangedListener(timeframeInterval.getRequest());
        timeframeInterval.getRequest().remove();
        fillQueue(now, control instanceof VariablePowerConsumer);
        if(control instanceof StartingCurrentSwitch) {
            if(enable) {
                getFirstTimeframeInterval().getRequest().setEnabled(true);
            }
        }
    }

    public void removeActiveTimeframeInterval(LocalDateTime now) {
        TimeframeInterval activeTimeframeInterval = getActiveTimeframeInterval();
        if(activeTimeframeInterval != null) {
            removeTimeframeInterval(now, activeTimeframeInterval, false);
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
        var timeframeInterval = new TimeframeInterval(interval, request);
        logger.debug("{}: Created timeframe interval: {}", applianceId, timeframeInterval.toString(now));
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

    public void createAndActivateOptionalEnergyTimeframeIntervalForEVCharger(LocalDateTime now) {
        if(control instanceof ElectricVehicleCharger && findOptionalEnergyIntervalForEVCharger() == null) {
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
            request.updateForced();
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
                updateQueue(now);
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
        activeTimeframeInterval.stateTransitionTo(TimeframeIntervalState.QUEUED);
        activeTimeframeInterval.setInterval(
                createOptionalEnergyIntervalForEVCharger(queue.get(0).getInterval().getEnd().plusSeconds(1),
                        null, queue.size() > 2 ? queue.get(2) : null));
    }

    public void removeOptionalEnergyTimeframe(LocalDateTime now) {
        TimeframeInterval timeframeInterval = findOptionalEnergyIntervalForEVCharger();
        if(timeframeInterval != null) {
            removeTimeframeInterval(now, timeframeInterval, false);
        }
    }

    public void adjustOptionalEnergyTimeframeIntervalStart() {
        TimeframeInterval optionalEnergyTimeframeInterval = findOptionalEnergyIntervalForEVCharger();
        if(optionalEnergyTimeframeInterval != null) {
            var optionalEnergyTimeframeIntervalIndex = queue.indexOf(optionalEnergyTimeframeInterval);
            var optionalEnergyTimeframeIntervalIsLast = queue.size() - 1 == optionalEnergyTimeframeIntervalIndex;
            if(queue.size() > 1 && optionalEnergyTimeframeIntervalIsLast) {
                TimeframeInterval previousTimeframeInterval = queue.get(optionalEnergyTimeframeIntervalIndex - 1);
                adjustSecondTimeframeIntervalStart(previousTimeframeInterval, optionalEnergyTimeframeInterval);
            } else if(queue.size() == 1) {
                adjustTimeframeIntervalStart(optionalEnergyTimeframeInterval, LocalDateTime.now());
            }
        }
    }

    public void adjustOptionalEnergyTimeframeIntervalEnd() {
        TimeframeInterval optionalEnergyTimeframeInterval = findOptionalEnergyIntervalForEVCharger();
        if(optionalEnergyTimeframeInterval != null) {
            var optionalEnergyTimeframeIntervalIndex = queue.indexOf(optionalEnergyTimeframeInterval);
            var optionalEnergyTimeframeIntervalIsLast = queue.size() - 1 == optionalEnergyTimeframeIntervalIndex;
            if(queue.size() > 1 && !optionalEnergyTimeframeIntervalIsLast) {
                TimeframeInterval subsequentTimeframeInterval = queue.get(optionalEnergyTimeframeIntervalIndex + 1);
                adjustFirstTimeframeIntervalEnd(optionalEnergyTimeframeInterval, subsequentTimeframeInterval);
            }
        }
    }

    public void adjustSecondTimeframeIntervalStart(TimeframeInterval interval1, TimeframeInterval interval2) {
        adjustTimeframeIntervalStart(interval2, interval1.getInterval().getEnd().plusSeconds(1));
    }

    public void adjustTimeframeIntervalStart(TimeframeInterval interval, LocalDateTime start) {
        if(start != null && interval != null) {
            if(!interval.getInterval().getStart().equals(start)) {
                logger.debug("{}: Adjust start of timeframe interval to: {}", applianceId, start);
                interval.getInterval().setStart(start);
            }
        }
    }

    private void adjustFirstTimeframeIntervalEnd(TimeframeInterval interval1, TimeframeInterval interval2) {
        LocalDateTime expectedEnd = interval2.getInterval().getStart().plusSeconds(-1);
        if(!interval1.getInterval().getStart().equals(expectedEnd)) {
            logger.debug("{}: Adjust end of timeframe interval to: {}", applianceId, expectedEnd);
            interval1.getInterval().setEnd(expectedEnd);
        }
    }

    protected void updateSocRequest(SocRequest request, Integer batteryCapacity, Integer soc) {
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
        TimeframeInterval timeframeInterval = getFirstTimeframeInterval();
        if(timeframeInterval != null) {
            if(timeframeInterval.getRequest() instanceof RuntimeRequest) {
                timeframeInterval.getInterval().setStart(now);
                timeframeInterval.getInterval().setEnd(requiredIntervalEnd);
                RuntimeRequest request = (RuntimeRequest) timeframeInterval.getRequest();
                request.setEnabled(true);
                request.setMax(runtime);
                request.setAcceptControlRecommendations(acceptControlRecommendations);
            }
        }
    }

    @Override
    public String toString() {
        return "";
    }
}
