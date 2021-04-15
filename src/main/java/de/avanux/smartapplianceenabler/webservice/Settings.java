/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.webservice;

import java.util.List;

public class Settings {
    private boolean holidaysEnabled;
    private String holidaysUrl;
    private List<ModbusSettings> modbusSettings;
    private String notificationCommand;

    public boolean isHolidaysEnabled() {
        return holidaysEnabled;
    }

    public void setHolidaysEnabled(boolean holidaysEnabled) {
        this.holidaysEnabled = holidaysEnabled;
    }

    public String getHolidaysUrl() {
        return holidaysUrl;
    }

    public void setHolidaysUrl(String holidaysUrl) {
        this.holidaysUrl = holidaysUrl;
    }

    public List<ModbusSettings> getModbusSettings() {
        return modbusSettings;
    }

    public void setModbusSettings(List<ModbusSettings> modbusSettings) {
        this.modbusSettings = modbusSettings;
    }

    public String getNotificationCommand() {
        return notificationCommand;
    }

    public void setNotificationCommand(String notificationCommand) {
        this.notificationCommand = notificationCommand;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "holidaysEnabled=" + holidaysEnabled +
                ", holidaysUrl=" + holidaysUrl +
                ", modbusSettings=" + modbusSettings +
                ", notificationCommand=" + notificationCommand +
                '}';
    }
}
