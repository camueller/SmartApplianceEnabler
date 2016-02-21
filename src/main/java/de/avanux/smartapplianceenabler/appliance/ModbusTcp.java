/*
 * Copyright (C) 2016 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.appliance;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

/**
 * Represents a ModBus TCP and provides a connection to it.
 *
 */
public class ModbusTcp {
    private static final int DEFAULT_PORT = 502;
    @XmlTransient
    private Logger logger = LoggerFactory.getLogger(ModbusTcp.class);
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String host;
    @XmlAttribute
    private int port = DEFAULT_PORT;

    public String getId() {
        return id;
    }

    public TCPMasterConnection getConnection() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        TCPMasterConnection connection = new TCPMasterConnection(address);
        connection.setPort(port);
        return connection;
    }

    @Override
    public String toString() {
        return id + "//" + host + ":" + port;
    }
}
