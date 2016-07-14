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
package de.avanux.smartapplianceenabler.modbus;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.appliance.Meter;
import de.avanux.smartapplianceenabler.appliance.PollElectricityMeter;
import de.avanux.smartapplianceenabler.appliance.PollPowerExecutor;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Timer;

/**
 * Represents a ModBus electricity meter device accessible by ModBus TCP.
 * The device is polled according to poll interval in order to provide min/avg/max values of the measurement interval.
 * The TCP connection to the device remains established across the polls.
 */
public class ModbusElectricityMeter extends ModbusSlave implements Meter, ApplianceIdConsumer, PollPowerExecutor {

    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(ModbusElectricityMeter.class));
    @XmlAttribute
    private String registerAddress;
    @XmlAttribute
    private Integer pollInterval = 10; // seconds
    @XmlAttribute
    private Integer measurementInterval; // seconds
    @XmlTransient
    private PollElectricityMeter pollElectricityMeter = new PollElectricityMeter();

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.logger.setApplianceId(applianceId);
    }

    @Override
    public int getAveragePower() {
        int power = pollElectricityMeter.getAveragePower();
        logger.debug("average power = " + power + "W");
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollElectricityMeter.getMinPower();
        logger.debug("min power = " + power + "W");
        return power;
    }

    @Override
    public int getMaxPower() {
        int power =  pollElectricityMeter.getMaxPower();
        logger.debug("max power = " + power + "W");
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

    public void start(Timer timer) {
        pollElectricityMeter.start(timer, pollInterval, measurementInterval, this);
    }

    @Override
    public float getPower() {
        try {
            ReadInputRegisterExecutor executor = new ReadInputRegisterExecutor(registerAddress);
            executor.setApplianceId(getApplianceId());
            executeTransaction(executor, true);
            Float registerValue = executor.getRegisterValue();
            if(registerValue != null) {
                return registerValue;
            }
        }
        catch(Exception e) {
            logger.error("Error reading input register " + registerAddress, e);
        }
        return 0;
    }
}
