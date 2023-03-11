/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PollEnergyMeterTest {

    private PollEnergyMeter sut;
    PollEnergyExecutor pollEnergyExecutor = Mockito.mock(PollEnergyExecutor.class);
    LocalDateTime now;

    @BeforeEach
    void setup() {
        sut = new PollEnergyMeter();
        sut.setApplianceId("F-001");
        sut.start(null, pollEnergyExecutor);
        Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(0.0);

        now = LocalDateTime.now();
    }

    @Nested
    @DisplayName("After one poll")
    class AfterOnePoll {
        @Test
        void powerReturnedButEnergyIsZero() {
            pollEnergy(now, 0, 0.0, 1.0);
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
                pollEnergy(now, 0, 0.0, 1.0);
                pollEnergy(now.plusHours(1), 4000, 0.0, 5.0);
            }
        }

        @Nested
        @DisplayName("Started")
        class Started {
            @Test
            void powerReturnedAndCalculatedEnergyReturned() {
                pollEnergy(now, 0, 0.0, 1.0);
                sut.startEnergyCounter();
                pollEnergy(now.plusHours(1), 4000, 4.0, 5.0);
            }
        }

        @Nested
        @DisplayName("Started and Stopped")
        class StartedAndStopped {
            @Test
            void energyOnlyChangesWhileStarted() {
                pollEnergy(now, 0, 0.0, 1.0);
                sut.startEnergyCounter();
                pollEnergy(now.plusHours(1), 4000, 4.0, 5.0);
                sut.stopEnergyCounter();
                pollEnergy(now.plusHours(2), 2000, 4.0, 7.0);
                sut.startEnergyCounter();
                pollEnergy(now.plusHours(3), 2000, 6.0, 9.0);
            }
        }

        @Test
        void afterResetEnergyStartsAtZero() {
            pollEnergy(now, 0, 0.0, 1.0);
            sut.startEnergyCounter();
            pollEnergy(now.plusHours(1), 4000, 4.0, 5.0);
            sut.stopEnergyCounter();
            sut.reset();
            pollEnergy(now.plusHours(2), 0, 0.0, 5.0);
            sut.startEnergyCounter();
            pollEnergy(now.plusHours(3), 2000, 2.0, 7.0);
        }
    }

    private void pollEnergy(LocalDateTime now, int averagePower, Double energy, Double energyMeter) {
        Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(energyMeter);
        var listener = new OnMeterUpdateAsserter(now, averagePower, energy);
        sut.addMeterUpateListener(listener);
        sut.pollEnergy(now);
        sut.removeMeterUpateListener(listener);
    }
}
