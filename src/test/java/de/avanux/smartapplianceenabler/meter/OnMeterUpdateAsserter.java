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

package de.avanux.smartapplianceenabler.meter;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnMeterUpdateAsserter implements MeterUpdateListener {
    private LocalDateTime now;
    private int averagePower;
    private Double energy;

    public OnMeterUpdateAsserter(LocalDateTime now, int averagePower, Double energy) {
        this.now = now;
        this.averagePower = averagePower;
        this.energy = energy;
    }

    @Override
    public void onMeterUpdate(LocalDateTime now, int averagePower, Double energy) {
        assertEquals(this.now, now);
        assertEquals(this.averagePower, averagePower);
        assertEquals(this.energy, energy);
    }
}
