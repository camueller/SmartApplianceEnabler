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

import org.junit.Assert;
import org.junit.Test;

public class PulseEnergyMeterTest {

    private PulseEnergyMeter pulseEnergyMeter;

    public PulseEnergyMeterTest() {
        this.pulseEnergyMeter = new PulseEnergyMeter();
        this.pulseEnergyMeter.setApplianceId(getClass().getSimpleName());
        this.pulseEnergyMeter.setImpulsesPerKwh(1000);
    }

    @Test
    public void getEnergy_initial() {
        Assert.assertEquals(0.0f, this.pulseEnergyMeter.getEnergy(), 0.01f);
    }

    @Test
    public void getEnergy_1pulse() {
        this.pulseEnergyMeter.increasePulseCounter();
        Assert.assertEquals(0.000f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
    }

    @Test
    public void getEnergy_started_1pulse() {
        this.pulseEnergyMeter.startEnergyCounter();
        this.pulseEnergyMeter.increasePulseCounter();
        Assert.assertEquals(0.001f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
    }

    @Test
    public void getEnergy_started_500pulse() {
        this.pulseEnergyMeter.startEnergyCounter();
        increasePulseCounter(500);
        Assert.assertEquals(0.5f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
    }

    @Test
    public void getEnergy_with_interruptions() {
        this.pulseEnergyMeter.startEnergyCounter();
        increasePulseCounter(1000);
        this.pulseEnergyMeter.stopEnergyCounter();
        Assert.assertEquals(1.0f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
        this.pulseEnergyMeter.startEnergyCounter();
        increasePulseCounter(1000);
        this.pulseEnergyMeter.stopEnergyCounter();
        Assert.assertEquals(2.0f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
    }

    @Test
    public void getEnergy_with_reset() {
        this.pulseEnergyMeter.startEnergyCounter();
        increasePulseCounter(1000);
        this.pulseEnergyMeter.stopEnergyCounter();
        Assert.assertEquals(1.0f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
        this.pulseEnergyMeter.resetEnergyCounter();
        this.pulseEnergyMeter.startEnergyCounter();
        increasePulseCounter(1000);
        this.pulseEnergyMeter.stopEnergyCounter();
        Assert.assertEquals(1.0f, this.pulseEnergyMeter.getEnergy(), 0.0001f);
    }

    void increasePulseCounter(int pulseCount) {
        for(int i=0; i<pulseCount; i++) {
            this.pulseEnergyMeter.increasePulseCounter();
        }
    }
}
