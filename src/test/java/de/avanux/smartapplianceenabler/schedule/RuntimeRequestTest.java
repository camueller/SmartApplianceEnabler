/*
 * Copyright (C) 2023 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RuntimeRequestTest extends TestBase {
    private RuntimeRequest sut;
    private TimeframeInterval timeframeInterval = Mockito.mock(TimeframeInterval.class);

    @BeforeEach
    void setup() {
        sut = new RuntimeRequest();
        sut.setApplianceId("F-001");
        sut.setMqttClient(mqttClient);
        sut.setTimeframeInterval(timeframeInterval);
        sut.setMax(3600);
    }

    @Nested
    @DisplayName("TimeframeIntervalState.ACTIVE")
    class TimeframeIntervalStateActive {

        @BeforeEach
        void setup() {
            Mockito.when(timeframeInterval.getState()).thenReturn(TimeframeIntervalState.ACTIVE);
        }

        @Nested
        @DisplayName("No min runtime")
        class NoMinRuntime {
            @Test
            @DisplayName("without control message received runtime remaines unchanged")
            public void noControlMessageReceived() {
                var now = LocalDateTime.now();

                sut.init();

                assertNull(sut.getMin(now));
                assertEquals(3600, sut.getMax(now));
            }

            @Test
            @DisplayName("after a switch-on message max runtime is reduced by the duration since this")
            public void switchOnMessageReceived() {
                var now = LocalDateTime.now();
                mqttMessageArrivedOnSubscribe(Control.TOPIC, true, true,
                        new ControlMessage(now.minusMinutes(30), true)
                );

                sut.init();

                assertNull(sut.getMin(now));
                assertEquals(1800, sut.getMax(now));
            }

            @Nested
            @DisplayName("multiple control messages")
            class MultipleControlMessages {
                @Test
                @DisplayName("if switched on max runtime is reduced by switched-on durations and duration since last switch-on message")
                public void switchedOn() {
                    var now = LocalDateTime.now();
                    mqttMessageArrivedOnSubscribe(Control.TOPIC, true, true,
                            new ControlMessage(now.minusMinutes(60), true),
                            new ControlMessage(now.minusMinutes(50), false),
                            new ControlMessage(now.minusMinutes(20), true)
                    );

                    sut.init();

                    assertNull(sut.getMin(now));
                    assertEquals(1800, sut.getMax(now));
                }

                @Test
                @DisplayName("if switched off max runtime is reduced by switched-on durations")
                public void switchedOff() {
                    var now = LocalDateTime.now();
                    mqttMessageArrivedOnSubscribe(Control.TOPIC, true, true,
                            new ControlMessage(now.minusMinutes(60), true),
                            new ControlMessage(now.minusMinutes(50), false),
                            new ControlMessage(now.minusMinutes(30), true),
                            new ControlMessage(now.minusMinutes(10), false)
                    );

                    sut.init();

                    assertNull(sut.getMin(now));
                    assertEquals(1800, sut.getMax(now));
                }
            }
        }

        @Nested
        @DisplayName("With min runtime")
        class WithMinRuntime {
            @BeforeEach
            void setup() {
                sut.setMin(1800);
            }

            @Test
            @DisplayName("without control message received runtime remaines unchanged")
            public void noControlMessageReceived() {
                var now = LocalDateTime.now();

                sut.init();

                assertEquals(1800, sut.getMin(now));
                assertEquals(3600, sut.getMax(now));
            }

            @Test
            @DisplayName("after a switch-on message max runtime is reduced by the duration since this")
            public void switchOnMessageReceived() {
                var now = LocalDateTime.now();
                mqttMessageArrivedOnSubscribe(Control.TOPIC, true, true,
                        new ControlMessage(now.minusMinutes(15), true)
                );

                sut.init();

                assertEquals(900, sut.getMin(now));
                assertEquals(2700, sut.getMax(now));
            }

            @Nested
            @DisplayName("multiple control messages")
            class MultipleControlMessages {
                @Test
                @DisplayName("if switched on max runtime is reduced by switched-on durations and duration since last switch-on message")
                public void switchedOn() {
                    var now = LocalDateTime.now();
                    mqttMessageArrivedOnSubscribe(Control.TOPIC, true, true,
                            new ControlMessage(now.minusMinutes(60), true),
                            new ControlMessage(now.minusMinutes(50), false),
                            new ControlMessage(now.minusMinutes(10), true)
                    );

                    sut.init();

                    assertEquals(600, sut.getMin(now));
                    assertEquals(2400, sut.getMax(now));
                }

                @Test
                @DisplayName("if switched off max runtime is reduced by switched-on durations")
                public void switchedOff() {
                    var now = LocalDateTime.now();
                    mqttMessageArrivedOnSubscribe(Control.TOPIC, true, true,
                            new ControlMessage(now.minusMinutes(60), true),
                            new ControlMessage(now.minusMinutes(50), false),
                            new ControlMessage(now.minusMinutes(30), true),
                            new ControlMessage(now.minusMinutes(20), false)
                    );

                    sut.init();

                    assertEquals(600, sut.getMin(now));
                    assertEquals(2400, sut.getMax(now));
                }
            }
        }
    }

    @Nested
    @DisplayName("TimeframeIntervalState.QUEUED")
    class TimeframeIntervalStateQueued {

        @BeforeEach
        void init() {
            Mockito.when(timeframeInterval.getState()).thenReturn(TimeframeIntervalState.QUEUED);
        }

        @Nested
        @DisplayName("No min runtime")
        class NoMinRuntime {
            @Test
            @DisplayName("without control message received runtime remaines unchanged")
            public void noControlMessageReceived() {
                var now = LocalDateTime.now();

                sut.init();

                assertNull(sut.getMin(now));
                assertEquals(3600, sut.getMax(now));
            }
        }

        @Nested
        @DisplayName("With min runtime")
        class WithMinRuntime {
            @Test
            @DisplayName("without control message received min/max runtime remain unchanged")
            public void noControlMessageReceived() {
                var now = LocalDateTime.now();
                sut.setMin(1800);

                sut.init();

                assertEquals(1800, sut.getMin(now));
                assertEquals(3600, sut.getMax(now));
            }
        }
    }
}
