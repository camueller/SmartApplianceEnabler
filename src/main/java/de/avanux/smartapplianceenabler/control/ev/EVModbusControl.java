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
import de.avanux.smartapplianceenabler.modbus.executor.*;
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
    @XmlElement(name = "ModbusRegisterWrite")
    private List<ModbusRegisterWrite> registerWrites;

    private enum State {
        VEHICLE_CONNECTED,
        CHARGING_POSSIBLE,
        CHARGING_COMPLETED
    }


    public void validate() {
        boolean valid = true;
        for(EVModbusReadRegisterName registerName: EVModbusReadRegisterName.values()) {
            ModbusRegisterRead registerRead = getRegisterRead(registerName);
            if(registerRead != null) {
                logger.debug("{}: {} configured: read register={} / poll interval={}s / extraction regex={}",
                        getApplianceId(),
                        registerName.name(),
                        registerRead.getAddress(), registerRead.getPollInterval(),
                        registerRead.getSelectedRegisterReadValue().getExtractionRegex());
            }
            else {
                logger.error("{}: Missing register configuration for {}", getApplianceId(), registerName.name());
                valid = false;
            }
        }

        for(EVModbusWriteRegisterName registerName: EVModbusWriteRegisterName.values()) {
            ModbusRegisterWrite registerWrite = getRegisterWrite(registerName);
            if(registerWrite != null) {
                logger.debug("{}: {} configured: write register={} / value={}",
                        getApplianceId(),
                        registerName.name(),
                        registerWrite.getAddress(),
                        registerWrite.getSelectedRegisterWriteValue().getValue());
                if(EVModbusWriteRegisterName.ChargingCurrent.equals(registerName)) {
                    /* Alternative, falls Ladestrom am Controller nur auf feste Werte gesetzt werden kann
                    <ModbusRegisterWriteValue name="ChargingCurrent" param="2000" value="1" />
                    <ModbusRegisterWriteValue name="ChargingCurrent" param="4000" value="2" />
                    <ModbusRegisterWriteValue name="ChargingCurrent" param="6000" value="3" />
                    */
                }
            }
            else {
                logger.error("{}: Missing register configuration for {}", getApplianceId(), registerName.name());
                valid = false;
            }
        }
        if(! valid) {
            logger.error("{}: Terminating because of incorrect configuration", getApplianceId());
            System.exit(-1);
        }
    }

    // FIXME remove
    int counter = 0;

    @Override
    public boolean isVehicleConnected() {
        //return isMatchingVehicleStatus(EVModbusReadRegisterName.VehicleConnected);
        // FIXME remove
        counter++;
        return counter > 3;
    }

    @Override
    public boolean isChargingPossible() {
        //return isMatchingVehicleStatus(EVModbusReadRegisterName.ChargingPossible);
        // FIXME remove
        return counter > 6;
    }

    @Override
    public boolean isChargingCompleted() {
        //return isMatchingVehicleStatus(EVModbusReadRegisterName.ChargingCompleted);
        // FIXME remove
        return counter > 800; // 720 pro Stunde
    }

    public boolean isMatchingVehicleStatus(EVModbusReadRegisterName registerName) {
        ModbusRegisterRead registerRead = getRegisterRead(registerName);
        if(registerRead != null) {
            try {
                ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                        registerRead.getType(), registerRead.getAddress(), registerRead.getBytes());
                if(executor != null) {
                    executeTransaction(executor, false);
                    if(executor instanceof StringInputRegisterExecutor) {
                        String registerValue = ((StringInputRegisterExecutor) executor).getValue();
                        logger.debug("{}: Vehicle status={}", getApplianceId(), registerValue);
                        return registerValue.matches(registerRead.getSelectedRegisterReadValue().getExtractionRegex());
                    }
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
        ModbusRegisterRead registerRead = getRegisterRead(EVModbusReadRegisterName.VehicleConnected);
        if(registerRead != null) {
            return registerRead.getPollInterval();
        }
        return null;
    }

    @Override
    public void setChargeCurrent(int current) {
        logger.debug("{}: Set charge current {}A", getApplianceId(), current);
        ModbusRegisterWrite registerWrite = getRegisterWrite(EVModbusWriteRegisterName.ChargingCurrent);
        if(registerWrite != null) {
            try {
                ModbusWriteTransactionExecutor executor = ModbusExecutorFactory.getWriteExecutor(getApplianceId(),
                        registerWrite.getType(), registerWrite.getAddress());
                if(executor != null) {
                    executor.setValue(current);
                    executeTransaction(executor, false);
                }
            }
            catch(Exception e) {
                logger.error("{}: Error setting charge current in register {}", getApplianceId(), registerWrite.getAddress(), e);
            }
        }
    }

    @Override
    public void startCharging() {
        setCharging(EVModbusWriteRegisterName.StartCharging);
    }

    @Override
    public void stopCharging() {
        setCharging(EVModbusWriteRegisterName.StopCharging);
    }

    private void setCharging(EVModbusWriteRegisterName registerName) {
        ModbusRegisterWrite registerWrite = getRegisterWrite(registerName);
        if(registerWrite != null) {
            try {
                ModbusWriteTransactionExecutor executor = ModbusExecutorFactory.getWriteExecutor(getApplianceId(),
                        registerWrite.getType(), registerWrite.getAddress());
                if(executor != null) {
                    String stringValue = registerWrite.getSelectedRegisterWriteValue().getValue();
                    Object value = null;
                    if(ModbusRegisterType.Coil.equals(registerWrite.getType())) {
                        value = "1".equals(stringValue);
                    }
                    executor.setValue(value);
                    executeTransaction(executor, true);
                }
            }
            catch(Exception e) {
                logger.error("{}: Error enable/disable charging process in register {}", getApplianceId(), registerWrite.getAddress(), e);
            }
        }
    }

    @Override
    public boolean isCharging() {
        boolean charging = false;
        ModbusRegisterRead registerRead = getRegisterRead(EVModbusReadRegisterName.Charging);
        if(registerRead != null) {
            try {
                ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                        registerRead.getType(), registerRead.getAddress());
                executeTransaction(executor, true);
                if(executor instanceof CoilExecutor) {
                    charging = ((CoilExecutor) executor).getValue();
                }
            }
            catch (Exception e) {
                logger.error("{}: Error reading {} register {}", getApplianceId(), registerRead.getType(),
                        registerRead.getAddress(), e);
            }
        }
        return charging;
    }

    private ModbusRegisterRead getRegisterRead(EVModbusReadRegisterName registerName) {
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

    private ModbusRegisterWrite getRegisterWrite(EVModbusWriteRegisterName registerName) {
        for(ModbusRegisterWrite registerWrite: this.registerWrites) {
            for(ModbusRegisterWriteValue registerWriteValue: registerWrite.getRegisterWriteValues()) {
                if(registerName.name().equals(registerWriteValue.getName())) {
                    return new ModbusRegisterWrite(registerWrite.getAddress(), registerWrite.getType(),
                            registerWriteValue);
                }
            }
        }
        return null;
    }
}
