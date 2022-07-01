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
import java.time.format.DateTimeFormatter;

public class MqttMessage {
    private String type;
    private String time;

    public MqttMessage() {
    }

    public MqttMessage(LocalDateTime time) {
        setTime(time);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTime() {
        return toLocalDateTime(time);
    }

    public void setTime(LocalDateTime time) {
        this.time = toString(time);
    }

    protected String toString(LocalDateTime time) {
        return time != null ? time.format(DateTimeFormatter.ISO_DATE_TIME) : null;
    }

    protected LocalDateTime toLocalDateTime(String time) {
        return time != null ? LocalDateTime.parse(time) : null;
    }
}
