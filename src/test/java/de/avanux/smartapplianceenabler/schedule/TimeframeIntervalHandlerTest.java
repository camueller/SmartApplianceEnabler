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

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.ev.*;
import de.avanux.smartapplianceenabler.mqtt.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class TimeframeIntervalHandlerTest extends TestBase {

    private TimeframeIntervalHandler sut;
    private Integer evId = 1;
    private Control control;
    private MqttClient mqttClient;
    private LocalDateTime now;


    @BeforeEach
    void setup() {
        setup(mock(Control.class));
    }

    void setup(Control control) {
        this.control = control;
        sut = spy(new TimeframeIntervalHandler(new ArrayList<Schedule>(), control));

        now = LocalDateTime.now();

        mqttClient = spy(new MqttClientMock(applianceId, TimeframeIntervalHandler.class));

        sut.setApplianceId(applianceId);
        sut.setMqttClient(mqttClient);
    }

    @Nested
    @DisplayName("setMqttClient")
    class setMqttClient {
        @Test
        public void enableRuntimeRequest() {
            var request = spy(buildRuntimeRequest());
            sut.addTimeframeInterval(now, buildTimeframeIntervalForNow(request), true, true);
            mqttClient.publishMessage(fullEventTopic(applianceId, MqttEventName.EnableRuntimeRequest), new MqttMessage(now), false);
            assertTrue(request.isEnabled());
            verify(request).resetRuntime();
        }

        @Test
        public void disableRuntimeRequest() {
            var request = spy(buildRuntimeRequest());
            request.setEnabled(true);
            sut.addTimeframeInterval(now, buildTimeframeIntervalForNow(request), true, true);
            mqttClient.publishMessage(fullEventTopic(applianceId, MqttEventName.DisableRuntimeRequest), new MqttMessage(now), false);
            assertFalse(request.isEnabled());
            verify(request).resetEnabledBefore();
        }

        @Test
        public void evChargerStateChanged() {
            var request = spy(buildSocRequest());
            var timeframeInterval = buildTimeframeIntervalForNow(request);
            sut.addTimeframeInterval(now, timeframeInterval, true, true);

            doAnswer(invocation -> {
                var requestArgument = (SocRequest) invocation.getArgument(0);
                requestArgument.setMqttClient(mqttClient);
                return null;
            }).when(sut).updateSocRequest(any(SocRequest.class), anyInt(), anyInt());

            var event = new EVChargerStateChangedEvent(now, EVChargerState.VEHICLE_NOT_CONNECTED, EVChargerState.VEHICLE_CONNECTED);
            event.evId = evId;
            event.batteryCapacity = 36000;
            event.defaultSocOptionalEnergy = 80;
            mqttClient.publishMessage(fullEventTopic(applianceId, MqttEventName.EVChargerStateChanged), event, false);
            assertQueueEntry(0, now, timeframeInterval.getInterval().getStart().minusSeconds(1), new OptionalEnergySocRequest(evId));
            assertQueueEntry(1, timeframeInterval.getInterval().getStart(), timeframeInterval.getInterval().getEnd(), request);
            assertEquals(event.batteryCapacity, ((SocRequest) sut.getQueue().get(1).getRequest()).getBatteryCapacity());
            verify(request).updateForced();
        }
    }

    @Test
    public void clearQueue() {
        var existingTimeframeInterval = buildTimeframeIntervalForNow(LocalTime.now().minusHours(1).getHour(), 0, 0, 23, 59, 59, buildOptionalEnergySocRequest());
        sut.addTimeframeInterval(now, existingTimeframeInterval, true, true);
        sut.clearQueue();
        assertEquals(0, sut.getQueue().size());
    }

    @Nested
    @DisplayName("fillQueue")
    class FillQueue {

        void setMqttClientWhenAddTimeframeInterval() {
            doAnswer(invocation -> {
                var timeframeInterval = (TimeframeInterval) invocation.getArgument(1);
                timeframeInterval.getRequest().setMqttClient(mqttClient);
                invocation.callRealMethod();
                return null;
            }).when(sut).addTimeframeInterval(any(), any(), anyBoolean(), anyBoolean());
        }

        @Nested
        @DisplayName("not StartingCurrentSwitch")
        class NotStartingCurrentSwitch {
            //            schedules.add(new Schedule(true, null, 3600,
            //                                               new TimeOfDay(10, 0, 0), new TimeOfDay(12, 0, 0), List.of(1,2,3,4,5)));

            @BeforeEach
            public void beforeEach() {
                // Wednesday 12 at 10am
                now = LocalDateTime.of(2023, 11, 15, 10, 0, 0);
                setMqttClientWhenAddTimeframeInterval();
            }

            @Test
            public void sufficiant() {
                var timeframe = new DayTimeframe(new TimeOfDay(11, 0, 0), new TimeOfDay(13, 0, 0));
                var schedule = new Schedule(true, timeframe, buildRuntimeRequest());
                sut.setSchedules(Collections.singletonList(schedule));

                sut.fillQueue(now);

                assertEquals(2, sut.getQueue().size());
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 0, 11, 0, 0, 13, 0, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(0));
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 1, 11, 0, 0, 13, 0, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(1));
            }

            @Test
            public void insufficiant() {
                var timeframe = new DayTimeframe(new TimeOfDay(8, 0, 0), new TimeOfDay(10, 30, 0));
                var schedule = new Schedule(true, timeframe, buildRuntimeRequest());
                sut.setSchedules(Collections.singletonList(schedule));

                sut.fillQueue(now);

                assertEquals(2, sut.getQueue().size());
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 1, 8, 0, 0, 10, 30, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(0));
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 2, 8, 0, 0, 10, 30, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(1));
            }
        }

        @Nested
        @DisplayName("StartingCurrentSwitch")
        class IsStartingCurrentSwitch {
            @BeforeEach
            public void beforeEach() {
                var control = new StartingCurrentSwitch();
                setup(control);
                // Wednesday 12 at 10am
                now = LocalDateTime.of(2023, 11, 15, 10, 0, 0);
                setMqttClientWhenAddTimeframeInterval();
            }

            @Test
            public void sufficiant() {
                var timeframe = new DayTimeframe(new TimeOfDay(11, 0, 0), new TimeOfDay(13, 0, 0));
                var request = buildRuntimeRequest();
                request.setEnabled(false);
                var schedule = new Schedule(true, timeframe, request);
                sut.setSchedules(Collections.singletonList(schedule));

                sut.fillQueue(now);

                assertEquals(1, sut.getQueue().size());
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 0, 11, 0, 0, 13, 0, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(0));
            }

            @Test
            public void insufficiant() {
                var timeframe = new DayTimeframe(new TimeOfDay(8, 0, 0), new TimeOfDay(10, 30, 0));
                var request = buildRuntimeRequest();
                request.setEnabled(false);
                var schedule = new Schedule(true, timeframe, request);
                sut.setSchedules(Collections.singletonList(schedule));

                sut.fillQueue(now);

                assertEquals(1, sut.getQueue().size());
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 1, 8, 0, 0, 10, 30, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(0));
            }
        }

        @Nested
        @DisplayName("ElectricVehicleCharger")
        class IsElectricVehicleCharger {
            @BeforeEach
            public void beforeEach() {
                var control = spy(new ElectricVehicleCharger());
                setup(control);
                // Wednesday 12 at 10am
                now = LocalDateTime.of(2023, 11, 15, 10, 0, 0);
                setMqttClientWhenAddTimeframeInterval();
                doReturn(false).when(control).isVehicleConnected();
            }

            @Test
            public void vehicelNotConnected() {
                var timeframe = new DayTimeframe(new TimeOfDay(11, 0, 0), new TimeOfDay(13, 0, 0));
                var request = buildRuntimeRequest();
                request.setEnabled(false);
                var schedule = new Schedule(true, timeframe, request);
                sut.setSchedules(Collections.singletonList(schedule));

                sut.fillQueue(now);

                assertEquals(2, sut.getQueue().size());
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 0, 11, 0, 0, 13, 0, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(0));
                assertEquals(buildTimeframeInterval(now.toLocalDate(), 1, 11, 0, 0, 13, 0, 0, schedule.getRequest(), TimeframeIntervalState.QUEUED), sut.getQueue().get(1));
            }
        }
    }

    @Nested
    @DisplayName("updateQueue")
    class UpdateQueue {
        @Nested
        @DisplayName("deactivatableTimeframeInterval")
        class DeactivatableTimeframeInterval {
            @Test
            public void withOptionalEnergyTimeframeNext() {
                var activeTimeframeInterval = buildTimeframeIntervalForNow(LocalTime.now().minusHours(1).getHour(), 0, 0, 21, 29, 59, buildSocRequest());
                sut.addTimeframeInterval(now, activeTimeframeInterval, true, true);
                var nextTimeframeInterval = buildTimeframeIntervalForNow(22, 0, 0, 22, 59, 59, buildOptionalEnergySocRequest());
                sut.addTimeframeInterval(now, nextTimeframeInterval, false, true);

                now = LocalDateTime.of(now.toLocalDate(), LocalTime.of(21, 30, 0));
                sut.updateQueue(now, false);

                assertEquals(now, sut.getQueue().get(0).getInterval().getStart());
            }

            @Test
            public void withOptionalEnergyTimeframeFinished() {
                var request = spy(buildOptionalEnergySocRequest());
                var activeTimeframeInterval = buildTimeframeIntervalForNow(LocalTime.now().minusHours(1).getHour(), 0, 0, 21, 29, 59, request);
                sut.addTimeframeInterval(now, activeTimeframeInterval, true, true);

                when(request.isFinished(any(LocalDateTime.class))).thenReturn(true);
                when(request.getMax(any(LocalDateTime.class))).thenReturn(0);
                now = LocalDateTime.of(now.toLocalDate(), LocalTime.of(21, 30, 0));
                sut.updateQueue(now, false);

                assertEquals(0, sut.getQueue().size());
            }
        }
    }

    @Test
    public void publishQueue() {
        var reqeust = buildRuntimeRequest();
        var existingTimeframeInterval = buildTimeframeIntervalForNow(21, 0, 0, 21, 59, 59, reqeust);
        sut.addTimeframeInterval(now, existingTimeframeInterval, true, false);

        sut.publishQueue(now);

        var queueEntries = new TimeframeIntervalQueueEntry[1];
        queueEntries[0] = new TimeframeIntervalQueueEntry(existingTimeframeInterval.getState().name(),
                now.withHour(21).withMinute(0).withSecond(0).withNano(0).format(DateTimeFormatter.ISO_DATE_TIME),
                now.withHour(21).withMinute(59).withSecond(59).withNano(0).format(DateTimeFormatter.ISO_DATE_TIME),
                "RuntimeRequest", reqeust.getMin(now), reqeust.getMax(now), reqeust.isEnabled());
        TimeframeIntervalQueueMessage message = new TimeframeIntervalQueueMessage(now, queueEntries);
        verify(mqttClient).publishMessage(fullTopic(applianceId, TimeframeIntervalHandler.TOPIC), message, false);
    }

    @Nested
    @DisplayName("addTimeframeInterval")
    class AddTimeframeInterval {
        @Nested
        @DisplayName("RuntimeRequest")
        class RuntimeRequest {
            @Test
            public void asFirstTrue() {
                var existingTimeframeInterval = buildTimeframeIntervalForNow(LocalTime.now().minusHours(1).getHour(), 0, 0, 23, 59, 59, buildOptionalEnergySocRequest());
                sut.addTimeframeInterval(now, existingTimeframeInterval, true, true);
                assertEquals(TimeframeIntervalState.ACTIVE, sut.getQueue().get(0).getState());

                var request = spy(buildRuntimeRequest());
                var newTimeframeInterval = buildTimeframeIntervalForNow(request);
                sut.addTimeframeInterval(now, newTimeframeInterval, true, true);
                var timeframeIntervalAddedToQueue = sut.getQueue().get(0);
                var timeframeIntervalExistingInQueue = sut.getQueue().get(1);
                assertEquals(newTimeframeInterval, timeframeIntervalAddedToQueue);
                assertEquals(existingTimeframeInterval, timeframeIntervalExistingInQueue);
                verify(request).init();
                verify(request).timeframeIntervalCreated(now, newTimeframeInterval);
                assertEquals(TimeframeIntervalState.QUEUED, timeframeIntervalAddedToQueue.getState());
                assertEquals(TimeframeIntervalState.QUEUED, timeframeIntervalExistingInQueue.getState());
            }

            @Test
            public void asFirstFalse() {
                var existingTimeframeInterval = buildTimeframeIntervalForNow(21, 0, 0, 21, 59, 59, buildOptionalEnergySocRequest());
                sut.addTimeframeInterval(now, existingTimeframeInterval, true, true);
                var newTimeframeInterval = buildTimeframeIntervalForNow(buildRuntimeRequest());
                sut.addTimeframeInterval(now, newTimeframeInterval, false, true);
                assertEquals(existingTimeframeInterval, sut.getQueue().get(0));
                assertEquals(newTimeframeInterval, sut.getQueue().get(1));
            }
        }

        @Nested
        @DisplayName("RuntimeRequest")
        class EnergyRequest {
            @Test
            public void asFirstTrue() {
                var ev = mock(ElectricVehicle.class);
                when(ev.getSocScript()).thenReturn(mock(SocScript.class));
                var evHandler = mock(ElectricVehicleHandler.class);
                when(evHandler.getConnectedVehicle()).thenReturn(ev);
                var control = new ElectricVehicleCharger();
                control.setElectricVehicleHandler(evHandler);
                setup(control);

                var request = buildEnergyRequest();
                var newTimeframeInterval = buildTimeframeIntervalForNow(request);
                sut.addTimeframeInterval(now, newTimeframeInterval, true, false);
                var timeframeIntervalAddedToQueue = sut.getQueue().get(0);
                assertEquals(newTimeframeInterval, timeframeIntervalAddedToQueue);
                assertEquals(TimeframeIntervalState.QUEUED, timeframeIntervalAddedToQueue.getState());
                assertTrue(request.isSocScriptUsed());
            }
        }
    }


    @Nested
    @DisplayName("addTimeframeIntervalAndAdjustOptionalEnergyTimeframe")
    class addTimeframeIntervalAndAdjustOptionalEnergyTimeframe {
        @BeforeEach
        void beforeEach() {
            sut.addTimeframeInterval(now, buildTimeframeIntervalForNow(21, 0, 0, 23, 59, 59, buildOptionalEnergySocRequest()), true, true);
        }

        @Test
        public void asFirstTrue() {
            var request = buildSocRequest();
            var timeframeInterval = buildTimeframeIntervalForNow(22, 0, 0, 22, 59, 59, request);
            sut.addTimeframeIntervalAndAdjustOptionalEnergyTimeframe(now, timeframeInterval, true);
            assertQueueEntry(0, 22,0, 0, 22, 59, 59, request);
            assertQueueEntry(1, 23,0, 0, 23, 59, 59, buildOptionalEnergySocRequest());
        }

        @Test
        public void asFirstFalse() throws Exception {
            var request = buildSocRequest();
            var timeframeInterval = buildTimeframeIntervalForNow(22, 0, 0, 23, 0, 0, request);
            sut.addTimeframeIntervalAndAdjustOptionalEnergyTimeframe(now, timeframeInterval, false);
            assertQueueEntry(0, 21,0, 0, 21, 59, 59, buildOptionalEnergySocRequest());
            assertQueueEntry(1, 22,0, 0, 23, 0, 0, request);
        }
    }

    @Test
    public void activateTimeframeInterval() {
        var timeframeInterval = buildTimeframeIntervalForNow(21, 0, 0, 23, 59, 59, buildOptionalEnergySocRequest());
        sut.addTimeframeInterval(now, timeframeInterval, true, true);
        sut.activateTimeframeInterval(now, timeframeInterval);
        sut.logQueue(now);
        assertEquals(TimeframeIntervalState.ACTIVE, timeframeInterval.getState());
    }

    @Test
    public void deactivateTimeframeInterval() {
        var timeframeInterval = buildTimeframeIntervalForNow(LocalTime.now().minusHours(1).getHour(), 0, 0, 23, 59, 59, buildOptionalEnergySocRequest());
        sut.addTimeframeInterval(now, timeframeInterval, true, true);
        assertEquals(TimeframeIntervalState.ACTIVE, sut.getQueue().get(0).getState());
        sut.deactivateTimeframeInterval(now, timeframeInterval);
        sut.logQueue(now);
        assertEquals(TimeframeIntervalState.QUEUED, timeframeInterval.getState());
    }

    @Nested
    @DisplayName("removeTimeframeInterval")
    class removeTimeframeInterval {
        TimeframeInterval timeframeInterval = null;
        @BeforeEach
        void beforeEach() {
            timeframeInterval = buildTimeframeIntervalForNow(21, 0, 0, 23, 59, 59, spy(buildOptionalEnergySocRequest()));
            sut.addTimeframeInterval(now, timeframeInterval, true, true);
        }

        @Test
        public void nonStartingCurrentControl() {
            assertEquals(1, sut.getTimeframeIntervalChangedListener().size());
            sut.removeTimeframeInterval(now, timeframeInterval);
            assertEquals(0, sut.getQueue().size());
            assertEquals(0, sut.getTimeframeIntervalChangedListener().size());
            verify(timeframeInterval.getRequest()).remove();
        }
    }

    private RuntimeRequest buildRuntimeRequest() {
        return decorateRequest(new RuntimeRequest(3600, 3600));
    }

    private OptionalEnergySocRequest buildOptionalEnergySocRequest() {
        return decorateRequest(new OptionalEnergySocRequest(80, evId, 10000));
    }

    private SocRequest buildSocRequest() {
        return decorateRequest(new SocRequest(100, evId, 15000));
    }

    private EnergyRequest buildEnergyRequest() {
        return decorateRequest(new EnergyRequest());
    }

    private <T extends Request> T decorateRequest(T request) {
        request.setMqttClient(mqttClient);
        return request;
    }

    private TimeframeInterval buildTimeframeIntervalForNow(Request request) {
        return buildTimeframeIntervalForNow(22, 0, 0, 22, 59, 59, request);
    }

    private TimeframeInterval buildTimeframeIntervalForNow(int startHour, int startMinute, int startSeconds, int endHour, int endMinute, int endSeconds, Request request) {
        var start = LocalDateTime.of(now.toLocalDate(), LocalTime.of(startHour, startMinute, startSeconds));
        var end = LocalDateTime.of(now.toLocalDate(), LocalTime.of(endHour, endMinute, endSeconds));
        return new TimeframeInterval(new Interval(start, end), request);
    }

    private TimeframeInterval buildTimeframeInterval(LocalDate baseDate, int dayOffset, int startHour, int startMinute, int startSeconds, int endHour, int endMinute, int endSeconds, Request request, TimeframeIntervalState state) {
        var start = LocalDateTime.of(baseDate.plusDays(dayOffset), LocalTime.of(startHour, startMinute, startSeconds));
        var end = LocalDateTime.of(baseDate.plusDays(dayOffset), LocalTime.of(endHour, endMinute, endSeconds));
        var timeframeInterval = new TimeframeInterval(new Interval(start, end), request);
        timeframeInterval.stateTransitionTo(state);
        return timeframeInterval;
    }

    private void assertQueueEntry(int index, LocalDateTime start, LocalDateTime end, Request request) {
        assertQueueEntry(index, start.getHour(), start.getMinute(), start.getSecond(), end.getHour(), end.getMinute(), end.getSecond(), request);
    }

    private void assertQueueEntry(int index, int startHour, int startMinute, int startSeconds, int endHour, int endMinute, int endSeconds, Request request) {
        var timeframeInterval = sut.getQueue().get(index);
        assertIntervalStartOrEnd(timeframeInterval.getInterval().getStart(), startHour, startMinute, startSeconds);
        assertIntervalStartOrEnd(timeframeInterval.getInterval().getEnd(), endHour, endMinute, endSeconds);
        assertEquals(request, timeframeInterval.getRequest());
    }

    private void assertIntervalStartOrEnd(LocalDateTime startOrEnd, int hour, int minute, int seconds) {
        assertEquals(startOrEnd.getHour(), hour);
        assertEquals(startOrEnd.getMinute(), minute);
        assertEquals(startOrEnd.getSecond(), seconds);
    }
}
