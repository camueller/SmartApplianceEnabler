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
package de.avanux.smartapplianceenabler.control;

import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.modbus.ModbusWrite;
import de.avanux.smartapplianceenabler.modbus.ModbusWriteValue;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusValidator;
import de.avanux.smartapplianceenabler.modbus.executor.*;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationKey;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class ModbusSwitch extends ModbusSlave implements Control, Validateable, NotificationProvider {

    private transient Logger logger = LoggerFactory.getLogger(ModbusSwitch.class);
    @XmlElement(name = "ModbusWrite")
    private List<ModbusWrite> modbusWrites;
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient NotificationHandler notificationHandler;

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(this.notificationHandler != null) {
            this.notificationHandler.addRequestedNotifications(notifications);
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", getApplianceId());
        boolean valid = true;
        ModbusValidator validator = new ModbusValidator(getApplianceId());
        for(ControlValueName valueName: ControlValueName.values()) {
            ParentWithChild<ModbusWrite, ModbusWriteValue> write
                    = ModbusWrite.getFirstRegisterWrite(valueName.name(), this.modbusWrites);
            valid = validator.validateWrites(valueName.name(), Collections.singletonList(write));
        }
        if(! valid) {
            throw new ConfigurationException();
        }
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
    }

    @Override
    public void stop(LocalDateTime now) {
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        boolean result = false;
        logger.info("{}: Switching {}", getApplianceId(), (switchOn ? "on" : "off"));
        ParentWithChild<ModbusWrite, ModbusWriteValue> write
                = ModbusWrite.getFirstRegisterWrite(getValueName(switchOn).name(), this.modbusWrites);
        if (write != null) {
            ModbusWrite registerWrite = write.parent();
            try {
                ModbusWriteTransactionExecutor executor = ModbusExecutorFactory.getWriteExecutor(getApplianceId(),
                        registerWrite.getType(), registerWrite.getAddress(),registerWrite.getFactorToValue());
                executeTransaction(executor, true);
                if(executor instanceof WriteCoilExecutor) {
                    result = switchOn == ((WriteCoilExecutor) executor).getResult();
                }
                else if(executor instanceof WriteHoldingRegisterExecutor) {
                    result = 1 == ((WriteHoldingRegisterExecutor) executor).getResult();
                }
                if(this.notificationHandler != null) {
                    this.notificationHandler.sendNotification(switchOn ? NotificationKey.CONTROL_ON : NotificationKey.CONTROL_OFF);
                }
                for(ControlStateChangedListener listener : new ArrayList<>(controlStateChangedListeners)) {
                    listener.controlStateChanged(now, switchOn);
                }
            }
            catch (Exception e) {
                if(this.notificationHandler != null) {
                    this.notificationHandler.sendNotification(NotificationKey.CONTROL_COMMUNICATION_ERROR);
                }
                logger.error("{}: Error switching {} using register {}", getApplianceId(),  (switchOn ? "on" : "off"),
                        registerWrite.getAddress(), e);
            }
        }
        return result;
    }

    @Override
    public boolean isOn() {
        boolean on = false;
        ParentWithChild<ModbusWrite, ModbusWriteValue> write
                = ModbusWrite.getFirstRegisterWrite(ControlValueName.On.name(), this.modbusWrites);
        if(write != null) {
            ModbusWrite registerWrite = write.parent();
            try {
                ModbusReadTransactionExecutor executor = ModbusExecutorFactory.getReadExecutor(getApplianceId(),
                        registerWrite.getReadRegisterType(), registerWrite.getAddress());
                executeTransaction(executor, true);
                if(executor instanceof ReadCoilExecutorImpl) {
                    on = ((ReadCoilExecutorImpl) executor).getValue();
                }
            }
            catch (Exception e) {
                logger.error("{}: Error reading coil register {}", getApplianceId(), registerWrite.getAddress(), e);
            }
        }
        return on;
    }

    private ControlValueName getValueName(boolean switchOn) {
        return switchOn ? ControlValueName.On : ControlValueName.Off;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void removeControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.remove(listener);
    }
}
