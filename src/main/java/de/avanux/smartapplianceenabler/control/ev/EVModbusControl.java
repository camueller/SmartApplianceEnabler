/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

import de.avanux.smartapplianceenabler.modbus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class EVModbusControl extends ModbusSlave implements EVControl {

    private transient Logger logger = LoggerFactory.getLogger(EVModbusControl.class);
    @XmlElement(name = "ModbusRegisterRead")
    private List<ModbusRegisterRead> registerReads;

    public void validate() {
        for(EVModbusRegisterName registerName: EVModbusRegisterName.values()) {
            ModbusRegisterRead registerRead = getRegisterRead(registerName);
            if(registerRead != null) {
                logger.debug("{}: Configured for {} register: {} / poll interval: {}s / extraction regex: {}",
                        getApplianceId(),
                        registerName.name(),
                        registerRead.getAddress(), registerRead.getPollInterval(),
                        registerRead.getSelectedRegisterReadValue().getExtractionRegex());
            }
            else {
                // TODO Start abbrechen
                logger.error("{}: Missing register configuration for {}", getApplianceId(), registerName.name());
            }
        }
    }

    @Override
    public boolean isVehicleConnected() {
        ModbusRegisterRead registerRead = getRegisterRead(EVModbusRegisterName.VehicleConnected);
        if(registerRead != null) {
            try {
                ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                        registerRead.getType(), registerRead.getAddress(), registerRead.getBytes());
                if(executor != null) {
                    executeTransaction(executor, true);
                    String registerValue = executor.getRegisterValueString();
                    logger.debug("{}: string value={}", getApplianceId(), registerValue);
                    return registerValue.matches(registerRead.getSelectedRegisterReadValue().getExtractionRegex());
                }
            }
            catch(Exception e) {
                logger.error("{}: Error reading input register {}", getApplianceId(), registerRead.getAddress(), e);
            }
        }
        return false;
    }

    @Override
    public Integer getVehicleStatusPollInterval() {
        ModbusRegisterRead registerRead = getRegisterRead(EVModbusRegisterName.VehicleConnected);
        if(registerRead != null) {
            return registerRead.getPollInterval();
        }
        return null;
    }

    private ModbusRegisterRead getRegisterRead(EVModbusRegisterName registerName) {
        for(ModbusRegisterRead registerRead: this.registerReads) {
            for(ModbusRegisterReadValue registerReadValue: registerRead.getRegisterReadValues()) {
                if(registerName.name().equals(registerReadValue.getName())) {
                    return new ModbusRegisterRead(registerRead.getAddress(), registerRead.getBytes(),
                            registerRead.getType(), registerRead.getPollInterval(), registerReadValue);
                }
            }
        }
        return null;
    }
}
