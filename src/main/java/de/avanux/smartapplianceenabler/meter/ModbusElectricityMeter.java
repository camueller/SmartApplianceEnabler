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
import de.avanux.smartapplianceenabler.modbus.ModbusElectricityMeterDefaults;
import de.avanux.smartapplianceenabler.modbus.ModbusRegisterRead;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusExecutorFactory;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusReadTransactionExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ReadDecimalInputRegisterExecutor;
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
    private Integer pollInterval; // seconds
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

    public Integer getPollInterval() {
        return pollInterval != null ? pollInterval : HttpElectricityMeterDefaults.getPollInterval();
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval != null ? measurementInterval : ModbusElectricityMeterDefaults.getMeasurementInterval();
    }

    public void init() {
        validate();
    }

    public void validate() {
        logger.debug("{}: configured: poll interval={}s / measurement interval={}s",
                getApplianceId(), getPollInterval(), getMeasurementInterval());
        boolean valid = true;
        for(RegisterName registerName: RegisterName.values()) {
            ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(registerName.name(), registerReads);
            if(registerRead != null) {
                logger.debug("{}: {} configured: read register={} / bytes={} / byte order={} / type={} / extraction regex={} / factorToValue={}",
                        getApplianceId(),
                        registerName.name(),
                        registerRead.getAddress(),
                        registerRead.getBytes(),
                        registerRead.getByteOrder(),
                        registerRead.getType(),
                        registerRead.getSelectedRegisterReadValue().getExtractionRegex(),
                        registerRead.getFactorToValue());
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

    @Override
    public void start(Timer timer) {
        logger.debug("{}: Starting ...", getApplianceId());
        ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(RegisterName.Power.name(), registerReads);
        pollPowerMeter.start(timer, getPollInterval(), getMeasurementInterval(), this);
    }

    @Override
    public void stop() {
        logger.debug("{}: Stopping ...", getApplianceId());
        pollPowerMeter.cancelTimer();
    }

    @Override
    public float getPower() {
        ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(RegisterName.Power.name(), registerReads);
        return readRegister(registerRead);
    }

    @Override
    public float getEnergy() {
        return this.pollEnergyMeter.getEnergy();
    }

    @Override
    public void startEnergyMeter() {
        logger.debug("{}: Start energy meter ...", getApplianceId());
        this.pollEnergyMeter.startEnergyCounter();
    }

    @Override
    public void stopEnergyMeter() {
        logger.debug("{}: Stop energy meter ...", getApplianceId());
        this.pollEnergyMeter.stopEnergyCounter();
    }

    @Override
    public void resetEnergyMeter() {
        logger.debug("{}: Reset energy meter ...", getApplianceId());
        this.pollEnergyMeter.resetEnergyCounter();
    }

    @Override
    public float pollEnergy() {
        ModbusRegisterRead registerRead = ModbusRegisterRead.getFirstRegisterRead(RegisterName.Energy.name(), registerReads);
        return readRegister(registerRead);
    }

    private float readRegister(ModbusRegisterRead registerRead) {
        try {
            ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                    registerRead.getType(), registerRead.getAddress(), registerRead.getBytes(),
                    registerRead.getByteOrder(), registerRead.getFactorToValue());
            if(executor != null) {
                Float registerValue = null;
                executeTransaction(executor, true);
                if(executor instanceof ReadFloatInputRegisterExecutor) {
                    registerValue = ((ReadFloatInputRegisterExecutor) executor).getValue();
                }
                if(executor instanceof ReadDecimalInputRegisterExecutor) {
                    registerValue = ((ReadDecimalInputRegisterExecutor) executor).getValue().floatValue();
                }
                if(registerValue != null) {
                    logger.debug("{}: Float value={}", getApplianceId(), registerValue);
                    return registerValue;
                }
            }
        }
        catch(Exception e) {
            logger.error("{}: Error reading input register {}", getApplianceId(), registerRead.getAddress(), e);
        }
        return 0;
    }
}
