/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SocValues {
    public Integer batteryCapacity;
    public Integer initial;
    public Integer retrieved;
    public Integer current;

    public SocValues() {
    }

    public SocValues(SocValues input) {
        this(input.batteryCapacity, input.initial, input.retrieved, input.current);
    }

    public SocValues(Integer batteryCapacity, Integer initial, Integer retrieved, Integer current) {
        this.initial = initial;
        this.retrieved = retrieved;
        this.current = current;
        this.batteryCapacity = batteryCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SocValues socValues = (SocValues) o;

        return new EqualsBuilder()
                .append(batteryCapacity, socValues.batteryCapacity)
                .append(initial, socValues.initial)
                .append(retrieved, socValues.retrieved)
                .append(current, socValues.current)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(batteryCapacity)
                .append(initial)
                .append(retrieved)
                .append(current)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "SocValues{" +
                "batteryCapacity=" + batteryCapacity +
                ", initial=" + initial +
                ", retrieved=" + retrieved +
                ", current=" + current +
                '}';
    }
}
