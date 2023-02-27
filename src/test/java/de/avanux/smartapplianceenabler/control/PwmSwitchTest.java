/*
 * Copyright (C) 2023 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PwmSwitchTest {

    private PwmSwitch sut;

    @BeforeEach
    public void setup() throws Exception {
        sut = new PwmSwitch("F-00000001-000000000001-00", 50, 20, 85, 5000);
        sut.setMinPower(150);
        sut.setMaxPower(3000);
    }

    @Test
    public void calculateDutyCycle_Min() {
        assertEquals(162, sut.calculateDutyCycle(150));
    }

    @Test
    public void calculateDutyCycle_Max() {
        assertEquals(3250, sut.calculateDutyCycle(3000));
    }

}
