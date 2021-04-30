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
package de.avanux.smartapplianceenabler.modbus;

import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import de.avanux.smartapplianceenabler.webservice.SettingsDefaults;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents a ModBus TCP and provides a connection to it.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ModbusTcp {
    @XmlAttribute
    private String id;
    public transient static final String DEFAULT_HOST = "127.0.0.1";
    @XmlAttribute
    private String host;
    public transient static final int DEFAULT_PORT = 502;
    @XmlAttribute
    private Integer port;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    private String getResolvedHost() {
        return host != null ? host : DEFAULT_HOST;
    }

    public Integer getPort() {
        return port;
    }

    private Integer getResolvedPort() {
        return port != null ? port : DEFAULT_PORT;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public TCPMasterConnection getConnection() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(getResolvedHost());
        TCPMasterConnection connection = new TCPMasterConnection(address);
        connection.setPort(getResolvedPort());
        return connection;
    }

    @Override
    public String toString() {
        return id + "@" + getResolvedHost() + ":" + getResolvedPort();
    }
}
