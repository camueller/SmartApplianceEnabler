/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

public class MqttSettings {
    String host;
    Integer port;
    String username;
    String password;
    String rootTopic;
    Boolean brokerAvailable;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRootTopic() {
        return rootTopic;
    }

    public void setRootTopic(String rootTopic) {
        this.rootTopic = rootTopic;
    }

    public Boolean getBrokerAvailable() {
        return brokerAvailable;
    }

    public void setBrokerAvailable(Boolean brokerAvailable) {
        this.brokerAvailable = brokerAvailable;
    }

    @Override
    public String toString() {
        return "MqttSettings{" +
                "brokerHost='" + host + '\'' +
                ", brokerPort=" + port +
                ", brokerUsername=" + username +
                ", brokerPasswort=" + (password != null ? "length(" + password.length() + ")" : "null" ) +
                ", brokerAvailable=" + brokerAvailable +
                ", rootTopic=" + rootTopic +
                '}';
    }
}
