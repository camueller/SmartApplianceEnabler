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

import java.util.Timer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

/**
 * Base class for ModBus slaves.
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
abstract public class ModbusSlave implements ApplianceIdConsumer {
    private transient ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(ModbusSlave.class));
    @XmlAttribute
    private String idref;
    @XmlAttribute
    private int slaveAddress;
    private transient String applianceId;
    private transient ModbusTcp modbusTcp;
    private transient TCPMasterConnection connection;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.logger.setApplianceId(applianceId);
    }

    protected String getApplianceId() {
        return applianceId;
    }

    protected ModbusTcp getModbusTcp() {
        return modbusTcp;
    }

    public void setModbusTcp(ModbusTcp modbusTcp) {
        this.modbusTcp = modbusTcp;
    }

    public String getIdref() {
        return idref;
    }

    protected void executeTransaction(ModbusTransactionExecutor modbusTransactionExecutor, boolean closeConnection) throws Exception {
        ModbusTcp modbusTcp = getModbusTcp();
        if(connection == null) {
            logger.debug("Connecting to modbus " + modbusTcp.toString());
            connection = modbusTcp.getConnection();
        }
        if(! connection.isConnected()) {
            connection.connect();
        }
        if(connection.isConnected()) {
            modbusTransactionExecutor.execute(connection, slaveAddress);
            if(closeConnection) {
                connection.close();
                connection = null;
            }
        }
        else {
            logger.error("Cannot connect to modbus " + modbusTcp.toString());
        }
    }
}
