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

public class TimeframeIntervalQueueEntry {
    private String state;
    private String start;
    private String end;
    private String type;
    private Integer min;
    private Integer max;
    private Boolean enabled;

    public TimeframeIntervalQueueEntry() {
    }

    public TimeframeIntervalQueueEntry(String state, String start, String end, String type, Integer min, Integer max, Boolean enabled) {
        this.state = state;
        this.start = start;
        this.end = end;
        this.type = type;
        this.min = min;
        this.max = max;
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "TimeframeIntervalQueueEntry{" +
                "state=" + state +
                "start=" + start +
                ", end=" + end +
                ", type='" + type + '\'' +
                ", min=" + min +
                ", max=" + max +
                ", enabled=" + enabled +
                '}';
    }
}
