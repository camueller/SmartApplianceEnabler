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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PollEnergyMeterTest {

    private PollEnergyMeter pollEnergyMeter;
    private TestPollEnergyExecutor testPollEnergyExecutor = new TestPollEnergyExecutor();

    public PollEnergyMeterTest() {
        this.pollEnergyMeter = new PollEnergyMeter();
        this.pollEnergyMeter.setApplianceId(getClass().getSimpleName());
        this.pollEnergyMeter.setPollEnergyExecutor(this.testPollEnergyExecutor);
    }

    @Test
    public void getEnergy_initial() {
        assertEquals(0.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
    }

    @Test
    public void getEnergy_pollValueIncreases() {
        this.testPollEnergyExecutor.addEnergy(10.0f);
        assertEquals(0.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
    }

    @Test
    public void getEnergy_started_pollValueIncreases() {
        this.pollEnergyMeter.startEnergyCounter();
        this.testPollEnergyExecutor.addEnergy(10.0f);
        assertEquals(10.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
    }

    @Test
    public void getEnergy_started_pollValueIncreases_stopped_started_pollValueIncreases() {
        assertEquals(0.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
        this.pollEnergyMeter.startEnergyCounter();
        assertEquals(0.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
        this.testPollEnergyExecutor.addEnergy(10.0f);
        this.pollEnergyMeter.stopEnergyCounter();
        assertEquals(10.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
        this.pollEnergyMeter.startEnergyCounter();
        this.testPollEnergyExecutor.addEnergy(5.0f);
        assertEquals(15.0f, this.pollEnergyMeter.getEnergy(), 0.01f);
    }


    private class TestPollEnergyExecutor implements PollEnergyExecutor {

        public static final float INITIAL_POLL_VALUE = 100.0f;
        private float value = INITIAL_POLL_VALUE;


        public void addEnergy(float energy) {
            this.value += energy;
        }

        @Override
        public Float pollEnergy(LocalDateTime now) {
            return this.value;
        }
    }
}
