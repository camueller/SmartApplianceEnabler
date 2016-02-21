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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.avanux.smartapplianceenabler.appliance.Control;
import de.avanux.smartapplianceenabler.appliance.RunningTimeController;

public class ModbusSwitch extends ModbusSlave implements Control {

    @XmlTransient
    private Logger logger = LoggerFactory.getLogger(ModbusElectricityMeter.class);
    @XmlAttribute
    private String registerAddress;
    @XmlTransient
    RunningTimeController runningTimeController;    

    public boolean on(boolean switchOn) {
        boolean result = false;
        try {
            logger.info("Switching " + (switchOn ? "on" : "off"));
            WriteCoilExecutor executor = new WriteCoilExecutor(registerAddress, switchOn);
            executeTransaction(executor, false);
            result = executor.getResult();
            
            if(runningTimeController != null) {
                runningTimeController.setRunning(switchOn);
            }
        }
        catch (Exception e) {
            logger.error("Error switching coil register " + registerAddress, e);
        }
        return ! (switchOn ^ result);
    }

    @Override
    public boolean isOn() {
        boolean coil = false;
        try {
            ReadCoilExecutor executor = new ReadCoilExecutor(registerAddress);
            executeTransaction(executor, false);
            coil = executor.getCoil();
        }
        catch (Exception e) {
            logger.error("Error switching coil register " + registerAddress, e);
        }
        return coil;
    }
    
    public void setRunningTimeController(RunningTimeController runningTimeController) {
        this.runningTimeController = runningTimeController;
    }
}
