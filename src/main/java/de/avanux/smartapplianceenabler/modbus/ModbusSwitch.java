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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.avanux.smartapplianceenabler.appliance.ControlStateChangedListener;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import de.avanux.smartapplianceenabler.appliance.Control;

import java.util.ArrayList;
import java.util.List;

public class ModbusSwitch extends ModbusSlave implements Control {

    private transient ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(ModbusSwitch.class));
    @XmlAttribute
    private String registerAddress;
    transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.logger.setApplianceId(applianceId);
    }

    @Override
    public boolean on(boolean switchOn) {
        boolean result = false;
        try {
            logger.info("Switching " + (switchOn ? "on" : "off"));
            WriteCoilExecutor executor = new WriteCoilExecutor(registerAddress, switchOn);
            executor.setApplianceId(getApplianceId());
            executeTransaction(executor, true);
            result = executor.getResult();

            for(ControlStateChangedListener listener : controlStateChangedListeners) {
                listener.controlStateChanged(switchOn);
            }
        }
        catch (Exception e) {
            logger.error("Error switching coil register " + registerAddress, e);
        }
        return switchOn == result;
    }

    @Override
    public boolean isOn() {
        boolean coil = false;
        try {
            ReadCoilExecutor executor = new ReadCoilExecutor(registerAddress);
            executor.setApplianceId(getApplianceId());
            executeTransaction(executor, true);
            coil = executor.getCoil();
        }
        catch (Exception e) {
            logger.error("Error switching coil register " + registerAddress, e);
        }
        return coil;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }
}
