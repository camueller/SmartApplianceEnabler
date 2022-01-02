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

package de.avanux.smartapplianceenabler.mqtt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MqttBroker {
    public transient static final String DEFAULT_HOST = "127.0.0.1";
    public transient static final String DEFAULT_NODERED_DASHBOARD_URL = "http://localhost:1880/ui";
    @XmlAttribute
    private String host;
    public transient static final int DEFAULT_PORT = 1883;
    @XmlAttribute
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getResolvedHost() {
        return host != null ? host : DEFAULT_HOST;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getResolvedPort() {
        return port != null ? port : DEFAULT_PORT;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return getResolvedHost() + ":" + getResolvedPort();
    }
}
