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

import java.time.LocalDateTime;
import java.util.Arrays;

public class TimeframeIntervalQueueMessage extends MqttMessage {
    private TimeframeIntervalQueueEntry[] entries;

    public TimeframeIntervalQueueMessage() {
    }

    public TimeframeIntervalQueueMessage(LocalDateTime time, TimeframeIntervalQueueEntry[] entries) {
        super(time);
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "TimeframeIntervalQueueMessage{" +
                "entries=" + Arrays.toString(entries) +
                '}';
    }
}
