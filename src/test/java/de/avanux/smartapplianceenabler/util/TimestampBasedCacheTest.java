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

package de.avanux.smartapplianceenabler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.TreeMap;

public class TimestampBasedCacheTest {

    private TimestampBasedCache<Integer> cut;
    TreeMap<LocalDateTime,Integer> expectedTimestampsWithValue;

    @BeforeEach
    public void setup() throws Exception {
        cut = new TimestampBasedCache<>("Test");
        cut.setMaxAgeSeconds(60);
        expectedTimestampsWithValue = new TreeMap<>();
    }

    @Test
    public void addValue() {
        LocalDateTime now = LocalDateTime.now();
        cut.addValue(now, 1);
        cut.addValue(now.plusSeconds(25), 2);
        cut.addValue(now.plusSeconds(50), 3);
        cut.addValue(now.plusSeconds(75), 4);
        cut.addValue(now.plusSeconds(100), 5);
        expectedTimestampsWithValue.put(now.plusSeconds(50), 3);
        expectedTimestampsWithValue.put(now.plusSeconds(75), 4);
        expectedTimestampsWithValue.put(now.plusSeconds(100), 5);
        Assertions.assertEquals(expectedTimestampsWithValue, cut.getTimestampWithValue());
    }

    @Test
    public void addValue_keepLastExpired1() {
        cut.setKeepLastExpired(1);
        LocalDateTime now = LocalDateTime.now();
        cut.addValue(now, 1);
        cut.addValue(now.plusSeconds(25), 2);
        cut.addValue(now.plusSeconds(50), 3);
        cut.addValue(now.plusSeconds(75), 4);
        cut.addValue(now.plusSeconds(100), 5);
        expectedTimestampsWithValue.put(now.plusSeconds(25), 2);
        expectedTimestampsWithValue.put(now.plusSeconds(50), 3);
        expectedTimestampsWithValue.put(now.plusSeconds(75), 4);
        expectedTimestampsWithValue.put(now.plusSeconds(100), 5);
        Assertions.assertEquals(expectedTimestampsWithValue, cut.getTimestampWithValue());
    }

}
