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
package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.modbus.ModbusReadRegisterType;
import de.avanux.smartapplianceenabler.modbus.ModbusRegisterRead;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusExecutorFactory;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusReadTransactionExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ReadFloatInputRegisterExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Timer;

/**
 * Represents a ModBus electricity meter device accessible by ModBus TCP.
 * The device is polled according to poll interval in order to provide min/avg/max values of the measurement interval.
 * The TCP connection to the device remains established across the polls.
 */
public class ModbusElectricityMeter extends ModbusSlave implements Meter, ApplianceIdConsumer, PollPowerExecutor, PollEnergyExecutor {

    private transient Logger logger = LoggerFactory.getLogger(ModbusElectricityMeter.class);
    @XmlElement(name = "ModbusRegisterRead")
    private List<ModbusRegisterRead> registerReads;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    private transient PollPowerMeter pollPowerMeter = new PollPowerMeter();
    private transient PollEnergyMeter pollEnergyMeter = new PollEnergyMeter();

    public enum RegisterName {
        Power,
        Energy
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.pollPowerMeter.setApplianceId(applianceId);
        this.pollEnergyMeter.setApplianceId(applianceId);
        this.pollEnergyMeter.setPollEnergyExecutor(this);
    }

    @Override
    public int getAveragePower() {
        int power = pollPowerMeter.getAveragePower();
        logger.debug("{}: average power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollPowerMeter.getMinPower();
        logger.debug("{}: min power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public int getMaxPower() {
        int power =  pollPowerMeter.getMaxPower();
        logger.debug("{}: max power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public boolean isOn() {
        return getPower() > 0;
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void validate() {
        boolean valid = true;
        for(RegisterName registerName: RegisterName.values()) {
            ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(registerName.name(), registerReads);
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
        if(! valid) {
            logger.error("{}: Terminating because of incorrect configuration", getApplianceId());
            System.exit(-1);
        }
    }

    public void start(Timer timer) {
        ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(RegisterName.Power.name(), registerReads);
        pollPowerMeter.start(timer, registerRead.getPollInterval(), measurementInterval, this);
    }

    @Override
    public float getPower() {
        ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(RegisterName.Power.name(), registerReads);
        try {
            ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                    ModbusReadRegisterType.InputFloat, registerRead.getAddress(), registerRead.getBytes());
            if(executor != null) {
                executeTransaction(executor, true);
                if(executor instanceof ReadFloatInputRegisterExecutor) {
                    Float registerValue = ((ReadFloatInputRegisterExecutor) executor).getValue();
                    if(registerValue != null) {
                        logger.debug("{}: Float value={}", getApplianceId(), registerValue);
                        return registerValue;
                    }
                }
            }
        }
        catch(Exception e) {
            logger.error("{}: Error reading input register {}", getApplianceId(), registerRead.getAddress(), e);
        }
        return 0;
    }

    @Override
    public float getEnergy() {
        return this.pollEnergyMeter.getEnergy();
    }

    @Override
    public void startEnergyMeter() {
        this.pollEnergyMeter.startEnergyCounter();
    }

    @Override
    public void stopEnergyMeter() {
        this.pollEnergyMeter.stopEnergyCounter();
    }

    @Override
    public void resetEnergyMeter() {
        this.pollEnergyMeter.resetEnergyCounter();
    }

    @Override
    public float pollEnergy() {
        ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(RegisterName.Energy.name(), registerReads);
        try {
            ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                    ModbusReadRegisterType.InputFloat, registerRead.getAddress(), registerRead.getBytes());
            if(executor != null) {
                executeTransaction(executor, true);
                if(executor instanceof ReadFloatInputRegisterExecutor) {
                    Float registerValue = ((ReadFloatInputRegisterExecutor) executor).getValue();
                    if(registerValue != null) {
                        logger.debug("{}: Float value={}", getApplianceId(), registerValue);
                        return registerValue;
                    }
                }
            }
        }
        catch(Exception e) {
            logger.error("{}: Error reading input register {}", getApplianceId(), registerRead.getAddress(), e);
        }
        return 0;
    }
}
