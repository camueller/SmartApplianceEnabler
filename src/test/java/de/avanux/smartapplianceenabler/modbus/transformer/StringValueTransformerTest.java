/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringValueTransformerTest {

    private StringValueTransformer sut;

    @BeforeEach
    public void setup() throws Exception {
        sut = new StringValueTransformer();
    }

    @Test
    public void getValue_OneCharacterPerWord() {
        // 1 data word as hex: "0041"
        Integer[] byteValues = {65};
        sut.setByteValues(byteValues);
        assertEquals("A", sut.getValue());
    }

    @Test
    public void getValue_TwoCharactersPerWord() {
        // 1 data word as hex: "4332"
        Integer[] byteValues = {17202};
        sut.setByteValues(byteValues);
        assertEquals("C2", sut.getValue());
    }
}
