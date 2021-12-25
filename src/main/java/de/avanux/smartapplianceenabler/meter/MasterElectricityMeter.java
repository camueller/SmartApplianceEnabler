/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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
import de.avanux.smartapplianceenabler.control.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class MasterElectricityMeter implements ApplianceIdConsumer, Meter, PowerUpdateListener {
    private transient Logger logger = LoggerFactory.getLogger(MasterElectricityMeter.class);
    @XmlAttribute
    private Boolean masterSwitchOn;
    @XmlAttribute
    private Boolean slaveSwitchOn;
    @XmlElements({
            @XmlElement(name = "S0ElectricityMeter", type = S0ElectricityMeter.class),
            @XmlElement(name = "ModbusElectricityMeter", type = ModbusElectricityMeter.class),
            @XmlElement(name = "HttpElectricityMeter", type = HttpElectricityMeter.class),
    })
    private Meter meter;
    private transient SlaveElectricityMeter slaveMeter;
    private transient String applianceId;
    private transient Control masterControl;
    private transient Control slaveControl;
    private transient List<PowerUpdateListener> powerUpdateListeners = new ArrayList<>();

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        if(this.meter instanceof ApplianceIdConsumer) {
            ((ApplianceIdConsumer) this.meter).setApplianceId(applianceId);
        }
    }

    public void setSlaveElectricityMeter(SlaveElectricityMeter slaveMeter) {
        this.slaveMeter = slaveMeter;
    }

    public void setMasterControl(Control masterControl) {
        this.masterControl = masterControl;
    }

    public void setSlaveControl(Control slaveControl) {
        this.slaveControl = slaveControl;
    }

    @Override
    public void init() {
        logger.debug("{}: configured: masterSwitchOn={} slaveSwitchOn={}", applianceId, masterSwitchOn, slaveSwitchOn);
        this.meter.init();
        this.meter.addPowerUpdateListener(this);
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        this.meter.start(now, timer);
    }

    @Override
    public void stop(LocalDateTime now) {
        this.meter.stop(now);
    }

    private boolean isMeteringForSlave() {
        boolean meteringForSlave = false;
        if(masterSwitchOn != null && slaveSwitchOn != null) {
            meteringForSlave = masterSwitchOn == this.masterControl.isOn() && slaveSwitchOn == this.slaveControl.isOn();
        }
        else if(masterSwitchOn != null) {
            meteringForSlave = masterSwitchOn == this.masterControl.isOn();
        }
        else if(slaveSwitchOn != null) {
            meteringForSlave = slaveSwitchOn == this.slaveControl.isOn();
        }
        logger.debug("{}: isMeteringForSlave={} masterSwitchOn={} slaveSwitchOn={}",
                applianceId, meteringForSlave, masterSwitchOn, slaveSwitchOn);
        return meteringForSlave;
    }

    @Override
    public int getAveragePower() {
        return getAveragePower(false);
    }
    public int getAveragePower(boolean slaveIsRequesting) {
        if(slaveIsRequesting) {
            return isMeteringForSlave() ? meter.getAveragePower() : 0;
        }
        return !isMeteringForSlave() ? meter.getAveragePower() : 0;
    }

    @Override
    public int getMinPower() {
        return getMinPower(false);
    }
    public int getMinPower(boolean slaveIsRequesting) {
        if(slaveIsRequesting) {
            return isMeteringForSlave() ? meter.getMinPower() : 0;
        }
        return !isMeteringForSlave() ? meter.getMinPower() : 0;
    }

    @Override
    public int getMaxPower() {
        return getMaxPower(false);
    }
    public int getMaxPower(boolean slaveIsRequesting) {
        if(slaveIsRequesting) {
            return isMeteringForSlave() ? meter.getMaxPower() : 0;
        }
        return !isMeteringForSlave() ? meter.getMaxPower() : 0;
    }

    @Override
    public float getEnergy() {
        return 0;
    }
    public float getEnergy(boolean slaveIsRequesting) {
        if(slaveIsRequesting) {
            return isMeteringForSlave() ? meter.getEnergy() : 0;
        }
        return !isMeteringForSlave() ? meter.getEnergy() : 0;
    }

    @Override
    public void startEnergyMeter() {
        meter.startEnergyMeter();
    }

    @Override
    public void stopEnergyMeter() {
        meter.stopEnergyMeter();
    }

    @Override
    public void resetEnergyMeter() {
        meter.resetEnergyMeter();
    }

    @Override
    public void startAveragingInterval(LocalDateTime now, Timer timer, int nextPollCompletedSecondsFromNow) {
        meter.startAveragingInterval(now, timer, nextPollCompletedSecondsFromNow);
    }

    @Override
    public void addPowerUpdateListener(PowerUpdateListener listener) {
        this.powerUpdateListeners.add(listener);
    }

    @Override
    public void onPowerUpdate(int averagePower) {
        if(isMeteringForSlave()) {
            slaveMeter.onPowerUpdate(averagePower);
        }
        else {
            powerUpdateListeners.forEach(listener -> listener.onPowerUpdate(getAveragePower()));
        }
    }
}
