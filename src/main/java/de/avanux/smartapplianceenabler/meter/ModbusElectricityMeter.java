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
import de.avanux.smartapplianceenabler.modbus.*;
import de.avanux.smartapplianceenabler.modbus.executor.FloatInputRegisterExecutor;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusExecutorFactory;
import de.avanux.smartapplianceenabler.modbus.executor.ModbusReadTransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Timer;

/**
 * Represents a ModBus electricity meter device accessible by ModBus TCP.
 * The device is polled according to poll interval in order to provide min/avg/max values of the measurement interval.
 * The TCP connection to the device remains established across the polls.
 */
public class ModbusElectricityMeter extends ModbusSlave implements Meter, ApplianceIdConsumer, PollPowerExecutor {

    private transient Logger logger = LoggerFactory.getLogger(ModbusElectricityMeter.class);
    @XmlAttribute
    private String address;
    @XmlAttribute
    private Integer pollInterval; // seconds
    @XmlAttribute
    private Integer measurementInterval; // seconds
    private transient PollElectricityMeter pollElectricityMeter = new PollElectricityMeter();

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.pollElectricityMeter.setApplianceId(applianceId);
    }

    @Override
    public int getAveragePower() {
        int power = pollElectricityMeter.getAveragePower();
        logger.debug("{}: average power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollElectricityMeter.getMinPower();
        logger.debug("{}: min power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public int getMaxPower() {
        int power =  pollElectricityMeter.getMaxPower();
        logger.debug("{}: max power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public boolean isOn() {
        return getPower() > 0;
    }

    public Integer getPollInterval() {
        return pollInterval != null ? pollInterval : ModbusElectricityMeterDefaults.getPollInterval();
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    public void start(Timer timer) {
        pollElectricityMeter.start(timer, getPollInterval(), measurementInterval, this);
    }

    @Override
    public float getPower() {
        try {
            ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                    ModbusRegisterType.InputFloat, address, 2);
            if(executor != null) {
                executeTransaction(executor, true);
                if(executor instanceof FloatInputRegisterExecutor) {
                    Float registerValue = ((FloatInputRegisterExecutor) executor).getValue();
                    if(registerValue != null) {
                        logger.debug("{}: Float value={}", getApplianceId(), registerValue);
                        return registerValue;
                    }
                }
            }
        }
        catch(Exception e) {
            logger.error("{}: Error reading input register {}", getApplianceId(), address, e);
        }
        return 0;
    }
}
