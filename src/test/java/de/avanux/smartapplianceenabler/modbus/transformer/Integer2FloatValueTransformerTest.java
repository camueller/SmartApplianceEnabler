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

package de.avanux.smartapplianceenabler.modbus.transformer;

import de.avanux.smartapplianceenabler.modbus.ByteOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Integer2FloatValueTransformerTest {
    private Integer2FloatValueTransformer sut;

    @BeforeEach
    public void setup() throws Exception {
    }

    @Test
    public void getValue() {
        Integer[] byteValues = {63, 65275};
        sut = new Integer2FloatValueTransformer(null, 0.001);
        sut.setByteValues(byteValues);
        assertEquals(4194.0430, sut.getValue(), 0.001);
    }

    @Test
    public void getValue_BigEndian() {
        sut = new Integer2FloatValueTransformer(ByteOrder.BigEndian, 0.01);
        sut.setByteValues(new Integer[]{1, 18254});
        assertEquals(837.90, sut.getValue(), 0.001);
    }

    @Test
    public void getValue_LittleEndian() {
        sut = new Integer2FloatValueTransformer(ByteOrder.LittleEndian, 0.01);
        sut.setByteValues(new Integer[]{18254, 1});
        assertEquals(837.90, sut.getValue(), 0.001);
    }
}
