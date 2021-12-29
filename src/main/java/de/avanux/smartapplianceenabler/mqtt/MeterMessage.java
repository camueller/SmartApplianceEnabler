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

package de.avanux.smartapplianceenabler.mqtt;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

public class MeterMessage extends MqttMessage {
    public int power;
    public double energy;

    public MeterMessage() {
    }

    public MeterMessage(LocalDateTime time, int power, double energy) {
        setTime(time);
        this.power = power;
        this.energy = energy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MeterMessage that = (MeterMessage) o;

        return new EqualsBuilder().append(power, that.power).append(energy, that.energy).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(power).append(energy).toHashCode();
    }

    @Override
    public String toString() {
        return "MeterMessage{" +
                "time=" + getTime() +
                ", power=" + power +
                ", energy=" + energy +
                '}';
    }
}
