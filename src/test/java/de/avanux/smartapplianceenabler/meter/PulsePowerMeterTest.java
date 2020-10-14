/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.control.AlwaysOnSwitch;
import de.avanux.smartapplianceenabler.control.Control;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PulsePowerMeterTest {

    private PulsePowerMeter pulsePowerMeter;
    private long currentTimeMillis = 0; // System.currentTimeMillis();

    public PulsePowerMeterTest() {
        pulsePowerMeter = new PulsePowerMeter();
        pulsePowerMeter.setApplianceId(getClass().getSimpleName());
        pulsePowerMeter.setImpulsesPerKwh(1000);
        pulsePowerMeter.setMeasurementInterval(60);
    }

    @Test
    public void isOn_powerOnAlways() {
        pulsePowerMeter.setControl(new AlwaysOnSwitch());
        assertTrue(pulsePowerMeter.isOn(currentTimeMillis));
    }

    @Test
    public void isOn_0ts() {
        assertFalse(pulsePowerMeter.isOn(currentTimeMillis));
    }

    @Test
    public void isOn_1ts() {
        addTimestamps(-3600);
        assertFalse(pulsePowerMeter.isOn(currentTimeMillis));
    }

    @Test
    public void isOn_2ts() {
        addTimestamps(-9000, -5400);
        assertTrue(pulsePowerMeter.isOn(currentTimeMillis));
    }

    @Test
    public void isOn_2ts_overdue() {
        addTimestamps(-9001, -5401);
        assertFalse(pulsePowerMeter.isOn(currentTimeMillis));
    }

    @Test
    public void getAveragePower_0ts() {
        assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_1ts() {
        addTimestamps(-3600);
        assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_2ts() {
        addTimestamps(-9000, -5400);
        assertEquals(1, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_400impKwh_2ts() {
        pulsePowerMeter.setImpulsesPerKwh(400);
        addTimestamps(-10000, -1000);
        assertEquals(1, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_2ts_overdue() {
        addTimestamps(-9001, -5401);
        assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_3ts_2tsi() {
        addTimestamps(-999, -60, -30);
        assertEquals(120, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_4ts_3tsi() {
        addTimestamps(-999, -60, -50, -30);
        assertEquals(270, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getMinPower_4ts_3tsi() {
        addTimestamps(-999, -60, -50, -30);
        assertEquals(180, pulsePowerMeter.getMinPower(currentTimeMillis));
    }

    @Test
    public void getMaxPower_4ts_3tsi() {
        addTimestamps(-999, -60, -50, -30);
        assertEquals(360, pulsePowerMeter.getMaxPower(currentTimeMillis));
    }

    private void addTimestamps(int... seconds) {
        for(int i=0; i<seconds.length ;i++) {
            pulsePowerMeter.addTimestamp(currentTimeMillis + seconds[i] * 1000L);
        }
    }
}
