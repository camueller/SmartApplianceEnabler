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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatValueTransformerTest {
    private FloatValueTransformer sut;

    @BeforeEach
    public void setup() throws Exception {
        sut = new FloatValueTransformer(null);
    }

    @Test
    public void getValue_2Words() {
        Integer[] byteValues = {17676, 21823};
        sut.setByteValues(byteValues);
        assertEquals(2245.328, sut.getValue(), 0.001);
    }

    @Test
    public void getValue_2Words_FactorToValue() {
        Integer[] byteValues = {17676, 21823};
        sut = new FloatValueTransformer(0.1);
        sut.setByteValues(byteValues);
        assertEquals(224.5328, sut.getValue(), 0.001);
    }

    @Test
    public void getValue_4Words() {
        // 4 data words as hex: "4147 7507 0000 0000"
        Integer[] byteValues = {16711, 29959, 0, 0};
        sut.setByteValues(byteValues);
        assertEquals(3074574, sut.getValue(), 0.001);
    }

    @Test
    public void getValue_4Words_2() {
        Integer[] byteValues = {16711, 30408, 32768, 0};
        sut.setByteValues(byteValues);
        assertEquals(3075473, sut.getValue(), 0.001);
    }

    @Test
    public void getValue_4Words_FactorToValue() {
        // 4 data words as hex: "4147 7507 0000 0000"
        Integer[] byteValues = {16711, 29959, 0, 0};
        sut = new FloatValueTransformer(0.001);
        sut.setByteValues(byteValues);
        assertEquals(3074.574, sut.getValue(), 0.001);
    }
}
