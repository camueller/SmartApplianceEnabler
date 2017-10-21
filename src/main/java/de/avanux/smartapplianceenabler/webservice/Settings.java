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

/**
 * Created by axel on 19.10.17.
 */
public class Settings {
    boolean holidaysEnabled;
    String defaultHolidaysUrl;
    String holidaysUrl;

    boolean modbusEnabled;
    String defaultModbusTcpHost;
    String modbusTcpHost;
    Integer defaultModbusTcpPort;
    Integer modbusTcpPort;

    boolean pulseReceiverEnabled;
    Integer defaultPulseReceiverPort;
    Integer pulseReceiverPort;

    public boolean isHolidaysEnabled() {
        return holidaysEnabled;
    }

    public void setHolidaysEnabled(boolean holidaysEnabled) {
        this.holidaysEnabled = holidaysEnabled;
    }

    public String getDefaultHolidaysUrl() {
        return defaultHolidaysUrl;
    }

    public void setDefaultHolidaysUrl(String defaultHolidaysUrl) {
        this.defaultHolidaysUrl = defaultHolidaysUrl;
    }

    public String getHolidaysUrl() {
        return holidaysUrl;
    }

    public void setHolidaysUrl(String holidaysUrl) {
        this.holidaysUrl = holidaysUrl;
    }

    public boolean isModbusEnabled() {
        return modbusEnabled;
    }

    public void setModbusEnabled(boolean modbusEnabled) {
        this.modbusEnabled = modbusEnabled;
    }

    public String getDefaultModbusTcpHost() {
        return defaultModbusTcpHost;
    }

    public void setDefaultModbusTcpHost(String defaultModbusTcpHost) {
        this.defaultModbusTcpHost = defaultModbusTcpHost;
    }

    public String getModbusTcpHost() {
        return modbusTcpHost;
    }

    public void setModbusTcpHost(String modbusTcpHost) {
        this.modbusTcpHost = modbusTcpHost;
    }

    public Integer getDefaultModbusTcpPort() {
        return defaultModbusTcpPort;
    }

    public void setDefaultModbusTcpPort(Integer defaultModbusTcpPort) {
        this.defaultModbusTcpPort = defaultModbusTcpPort;
    }

    public Integer getModbusTcpPort() {
        return modbusTcpPort;
    }

    public void setModbusTcpPort(Integer modbusTcpPort) {
        this.modbusTcpPort = modbusTcpPort;
    }

    public boolean isPulseReceiverEnabled() {
        return pulseReceiverEnabled;
    }

    public void setPulseReceiverEnabled(boolean pulseReceiverEnabled) {
        this.pulseReceiverEnabled = pulseReceiverEnabled;
    }

    public Integer getDefaultPulseReceiverPort() {
        return defaultPulseReceiverPort;
    }

    public void setDefaultPulseReceiverPort(Integer defaultPulseReceiverPort) {
        this.defaultPulseReceiverPort = defaultPulseReceiverPort;
    }

    public Integer getPulseReceiverPort() {
        return pulseReceiverPort;
    }

    public void setPulseReceiverPort(Integer pulseReceiverPort) {
        this.pulseReceiverPort = pulseReceiverPort;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "holidaysEnabled=" + holidaysEnabled +
                ", defaultHolidaysUrl='" + defaultHolidaysUrl + '\'' +
                ", holidaysUrl='" + holidaysUrl + '\'' +
                ", modbusEnabled=" + modbusEnabled +
                ", defaultModbusTcpHost='" + defaultModbusTcpHost + '\'' +
                ", modbusTcpHost='" + modbusTcpHost + '\'' +
                ", defaultModbusTcpPort=" + defaultModbusTcpPort +
                ", modbusTcpPort=" + modbusTcpPort +
                ", pulseReceiverEnabled=" + pulseReceiverEnabled +
                ", defaultPulseReceiverPort=" + defaultPulseReceiverPort +
                ", pulseReceiverPort=" + pulseReceiverPort +
                '}';
    }
}
