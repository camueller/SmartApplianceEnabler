/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.meter;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PollPowerMeterTest {
    PollPowerMeter sut;

    PollPowerExecutor pollPowerExecutor = Mockito.mock(PollPowerExecutor.class);
    LocalDateTime now;

    @BeforeEach
    void setup() {
        sut = new PollPowerMeter();
        sut.setApplianceId("F-001");
        sut.start(null, null, pollPowerExecutor);
        Mockito.when(pollPowerExecutor.pollPower()).thenReturn(4000.0);

        now = LocalDateTime.now();
    }

    @Nested
    @DisplayName("After one poll")
    class AfterOnePoll {
        @Test
        void powerReturnedButEnergyIsZero() {
            pollPower(now, 4000, 0.0);
        }
    }

    @Nested
    @DisplayName("After more than one poll")
    class AfterMoreThanOnePoll {

        @Nested
        @DisplayName("Not started")
        class NotStarted {
            @Test
            void powerReturnedButEnergyIsZero() {
                pollPower(now, 4000, 0.0);
                pollPower(now.plusHours(1), 4000, 0.0);
            }
        }

        @Nested
        @DisplayName("Started")
        class Started {
            @Test
            void powerReturnedAndCalculatedEnergyReturned() {
                sut.startEnergyCounter();
                pollPower(now, 4000, 0.0);
                pollPower(now.plusHours(1), 4000, 4.0);
            }
        }

        @Nested
        @DisplayName("Started and Stopped")
        class StartedAndStopped {
            @Test
            void energyOnlyChangesWhileStarted() {
                sut.startEnergyCounter();
                pollPower(now, 4000, 0.0);
                pollPower(now.plusHours(1), 4000, 4.0);
                sut.stopEnergyCounter();
                pollPower(now.plusHours(2), 4000, 4.0);
                sut.startEnergyCounter();
                pollPower(now.plusHours(3), 4000, 8.0);
            }
        }

        @Test
        void afterResetEnergyStartsAtZero() {
            sut.startEnergyCounter();
            pollPower(now, 4000, 0.0);
            pollPower(now.plusHours(1), 4000, 4.0);
            sut.stopEnergyCounter();
            sut.reset();
            sut.startEnergyCounter();
            pollPower(now.plusHours(2), 4000, 0.0);
            pollPower(now.plusHours(3), 4000, 4.0);
        }
    }

    private void pollPower(LocalDateTime now, int averagePower, Double energy) {
        var listener = new OnMeterUpdateAsserter(now, averagePower, energy);
        sut.addMeterUpateListener(listener);
        sut.pollPower(now);
        sut.removeMeterUpateListener(listener);
    }
}
