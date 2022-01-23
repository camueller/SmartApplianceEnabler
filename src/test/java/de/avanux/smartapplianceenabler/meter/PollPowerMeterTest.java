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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class PollPowerMeterTest {
    private PollPowerMeter cut;
    LocalDateTime now;

    @BeforeEach
    public void setup() throws Exception {
        cut = new PollPowerMeter();
        now = LocalDateTime.now();
    }

    @Test
    public void getAveragePower_4Values_3ValuesInInterval() {
        cut.addValue(now                , 2.0);
        cut.addValue(now.plusSeconds(20), 4.0);
        cut.addValue(now.plusSeconds(40), 2.0);
        cut.addValue(now.plusSeconds(60), 4.0);
        Assertions.assertEquals(3, getAveragePower(70));
    }

    @Test
    public void getAveragePower_3Values_2ValuesInInterval() {
        cut.addValue(now                , 2.0);
        cut.addValue(now.plusSeconds(30), 4.0);
        cut.addValue(now.plusSeconds(60), 2.0);
        Assertions.assertEquals(3, getAveragePower(75));
    }

    @Test
    public void getAveragePower_2Values_1ValueInInterval() {
        cut.addValue(now                , 2.0);
        cut.addValue(now.plusSeconds(30), 4.0);
        Assertions.assertEquals(4, getAveragePower(75));
    }

    @Test
    public void getAveragePower_1Values_1ValueInInterval() {
        cut.addValue(now.plusSeconds(30), 4.0);
        Assertions.assertEquals(4, getAveragePower(75));
    }

    @Test
    public void getAveragePower_1Values_0ValueInInterval() {
        cut.addValue(now, 2.0);
        Assertions.assertEquals(0, getAveragePower(75));
    }

    @Test
    public void getAveragePower_0Values_0ValueInInterval() {
        Assertions.assertEquals(0, getAveragePower(75));
    }

    private int getAveragePower(int secondsAfterNow) {
        return cut.getAveragePower(now.plusSeconds(secondsAfterNow));
    }

    @Test
    public void getMinPower() {
        cut.addValue(now                , 1.0);
        cut.addValue(now.plusSeconds(20), 3.0);
        cut.addValue(now.plusSeconds(40), 2.0);
        cut.addValue(now.plusSeconds(60), 4.0);
        int power = cut.getMinPower(now.plusSeconds(75));
        Assertions.assertEquals(2, power);
    }

    @Test
    public void getMaxPower() {
        cut.addValue(now                , 8.0);
        cut.addValue(now.plusSeconds(20), 3.0);
        cut.addValue(now.plusSeconds(40), 4.0);
        cut.addValue(now.plusSeconds(60), 2.0);
        int power = cut.getMaxPower(now.plusSeconds(75));
        Assertions.assertEquals(4, power);
    }
}
