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

import de.avanux.smartapplianceenabler.modbus.RegisterValueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerValueTransformerTest {
    private IntegerValueTransformer sut;

    @BeforeEach
    public void setup() throws Exception {
        sut = new IntegerValueTransformer(RegisterValueType.Integer);
    }

    @Nested
    @DisplayName("getValue")
    class GetValue {
        @Test
        public void getValue() {
            Integer[] byteValues = {0, 3};
            sut.setByteValues(byteValues);
            assertEquals(3, sut.getValue());
        }
    }

    @Nested
    @DisplayName("getBytes")
    class GetBytes {
        @Test
        public void getBytes_16bit() {
            sut = new IntegerValueTransformer(RegisterValueType.Integer);
            sut.setValue(38);
            Integer byteValues[] = { 0, 38 }; // 2 words: 0026
            assertArrayEquals(byteValues, sut.getByteValues());
        }

        @Test
        public void getBytes_32bit() {
            sut = new IntegerValueTransformer(RegisterValueType.Integer32);
            sut.setValue(38);
            Integer byteValues[] = { 0, 0, 0, 38 }; // 2 words: 0000 0026
            assertArrayEquals(byteValues, sut.getByteValues());
        }
    }
}
