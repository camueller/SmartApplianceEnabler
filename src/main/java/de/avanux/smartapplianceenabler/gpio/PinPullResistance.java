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

package de.avanux.smartapplianceenabler.gpio;

import com.pi4j.io.gpio.digital.PullResistance;

public enum PinPullResistance {
    OFF,
    PULL_DOWN,
    PULL_UP;

    public PullResistance toPi4jPullResistance() {
        switch (this) {
            case PULL_UP:   return PullResistance.PULL_UP;
            case PULL_DOWN: return PullResistance.PULL_DOWN;
            default:        return PullResistance.OFF;
        }
    }
}
