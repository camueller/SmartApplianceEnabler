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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PulsePowerMeterTest {

    private PulsePowerMeter pulsePowerMeter;
    private long currentTimeMillis = 0; // System.currentTimeMillis();

    public PulsePowerMeterTest() {
        pulsePowerMeter = new PulsePowerMeter();
        pulsePowerMeter.setApplianceId(getClass().getSimpleName());
        pulsePowerMeter.setImpulsesPerKwh(1000);
    }

    @Test
    public void getAveragePower_0ts() {
        assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_1ts() {
        addTimestampSeconds(-3600);
        assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_2ts() {
        addTimestampSeconds(-9000, -5400);
        assertEquals(1, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_2ts_millis() {
        addTimestampMillis(-82774, -59047, -58461); // before averaging interval started
        addTimestampMillis(-57488, -56560, -55672, -54802, -53931, -53053, -52176, -51289, -50395, -49492);
        addTimestampMillis(-48584, -47666, -46758, -45845, -44938, -44037, -43149, -42264, -41401, -40545);
        addTimestampMillis(-39702, -38872, -38049, -37226, -36413, -35594, -34776, -33953, -33132, -32306);
        addTimestampMillis(-31482, -30657, -29829, -29001, -28176, -27344, -26513, -25681, -24850, -24018);
        addTimestampMillis(-23189, -22362, -21538, -20718, -19898, -19085, -18277, -17468, -16662, -15856);
        addTimestampMillis(-15053, -14245, -13454, -12654, -11857, -11061, -10266, -9472, -8679, -7887);
        addTimestampMillis(-7095, -6304, -5517, -4726, -3937, -3148, -2360, -1572, -786, 0);
        assertEquals(4347, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_400impKwh_2ts() {
        pulsePowerMeter.setImpulsesPerKwh(400);
        addTimestampSeconds(-10000, -1000);
        assertEquals(1, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_2000impKwh_2ts() {
        pulsePowerMeter.setImpulsesPerKwh(2000);
        addTimestampSeconds(-3600, -1800);
        assertEquals(1, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_2ts_overdue() {
        addTimestampSeconds(-9001, -5401);
        assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_3ts_2tsi() {
        addTimestampSeconds(-999, -60, -30);
        assertEquals(120, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getAveragePower_4ts_3tsi() {
        addTimestampSeconds(-999, -60, -50, -30);
        assertEquals(270, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    @Test
    public void getMinPower_4ts_3tsi() {
        addTimestampSeconds(-999, -60, -50, -30);
        assertEquals(180, pulsePowerMeter.getMinPower(currentTimeMillis));
    }

    @Test
    public void getMaxPower_4ts_3tsi() {
        addTimestampSeconds(-999, -60, -50, -30);
        assertEquals(360, pulsePowerMeter.getMaxPower(currentTimeMillis));
    }

    private void addTimestampSeconds(int... seconds) {
        for(int i=0; i<seconds.length ;i++) {
            pulsePowerMeter.addTimestamp(currentTimeMillis + seconds[i] * 1000L);
        }
    }

    private void addTimestampMillis(int... millis) {
        for(int i=0; i<millis.length ;i++) {
            pulsePowerMeter.addTimestamp(currentTimeMillis + millis[i]);
        }
    }
}
