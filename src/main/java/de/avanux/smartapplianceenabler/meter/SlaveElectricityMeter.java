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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@XmlAccessorType(XmlAccessType.FIELD)
public class SlaveElectricityMeter implements ApplianceIdConsumer, Meter, PowerUpdateListener {
    private transient Logger logger = LoggerFactory.getLogger(SlaveElectricityMeter.class);
    @XmlAttribute
    private String masterId;
    private transient String applianceId;
    private transient MasterElectricityMeter masterMeter;
    private transient List<PowerUpdateListener> powerUpdateListeners = new ArrayList<>();

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setMasterElectricityMeter(MasterElectricityMeter masterMeter) {
        this.masterMeter = masterMeter;
    }

    public String getMasterId() {
        return masterId;
    }

    @Override
    public void init() {
        logger.debug("{}: configured: masterId={}", applianceId, masterId);
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
    }

    @Override
    public void stop(LocalDateTime now) {
    }

    @Override
    public int getAveragePower() {
        return this.masterMeter.getAveragePower(true);
    }

    @Override
    public int getMinPower() {
        return this.masterMeter.getMinPower(true);
    }

    @Override
    public int getMaxPower() {
        return this.masterMeter.getMaxPower(true);
    }

    @Override
    public float getEnergy() {
        return this.masterMeter.getEnergy(true);
    }

    @Override
    public void startEnergyMeter() {
    }

    @Override
    public void stopEnergyMeter() {
    }

    @Override
    public void resetEnergyMeter() {
    }

    @Override
    public void startAveragingInterval(LocalDateTime now, Timer timer, int nextPollCompletedSecondsFromNow) {
    }

    @Override
    public void addPowerUpdateListener(PowerUpdateListener listener) {
        this.powerUpdateListeners.add(listener);
    }

    @Override
    public void onPowerUpdate(int averagePower) {
        powerUpdateListeners.forEach(listener -> listener.onPowerUpdate(getAveragePower()));
    }
}
