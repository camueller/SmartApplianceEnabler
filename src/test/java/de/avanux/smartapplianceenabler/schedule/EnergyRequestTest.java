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
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnergyRequestTest extends TestBase {
    private EnergyRequest sut;
    private TimeframeIntervalStateProvider timeframeIntervalStateProvider = Mockito.mock(TimeframeIntervalStateProvider.class);

    @BeforeEach
    void setup() {
        sut = new EnergyRequest();
        sut.setApplianceId("F-001");
        sut.setMqttClient(mqttClient);
        sut.setTimeframeIntervalStateProvider(timeframeIntervalStateProvider);
    }

    @Nested
    @DisplayName("TimeframeIntervalState.ACTIVE")
    class TimeframeIntervalStateActive {

        @BeforeEach
        void init() {
            Mockito.when(timeframeIntervalStateProvider.getState()).thenReturn(TimeframeIntervalState.ACTIVE);
        }

        @Test
        @DisplayName("don't decrease energyMetered because of meter reset caused by timeframe interval change")
        public void energyMeteredDecrease() {
            sut.setMin(5000);
            sut.setMax(10000);
            mqttMessageArrivedOnSubscribe(Meter.TOPIC, true,
                    new MeterMessage(LocalDateTime.now(), 1000, 1.5),
                    new MeterMessage(LocalDateTime.now(), 1000, 0)
            );

            sut.init();

            assertEquals(3500, sut.getMin(LocalDateTime.now()));
            assertEquals(8500, sut.getMax(LocalDateTime.now()));
        }
    }

    @Nested
    @DisplayName("TimeframeIntervalState.QUEUED")
    class TimeframeIntervalStateQueued {

        @BeforeEach
        void setup() {
            Mockito.when(timeframeIntervalStateProvider.getState()).thenReturn(TimeframeIntervalState.QUEUED);
        }

        @Test
        @DisplayName("energyMetered should remain unchanged if if meter message is received")
        public void energyMeteredDecrease() {
            sut.setMin(5000);
            sut.setMax(10000);
            mqttMessageArrivedOnSubscribe(Meter.TOPIC, true, new MeterMessage(LocalDateTime.now(), 1000, 1.5));

            sut.init();

            assertEquals(5000, sut.getMin(LocalDateTime.now()));
            assertEquals(10000, sut.getMax(LocalDateTime.now()));
        }

    }
}
