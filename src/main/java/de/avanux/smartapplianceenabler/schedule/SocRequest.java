/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class SocRequest extends AbstractEnergyRequest implements Request {
    @XmlAttribute
    private Integer soc;
    @XmlAttribute
    private Integer evId;
    private static transient Logger logger = LoggerFactory.getLogger(SocRequest.class);
    private transient Integer socInitial = 0;
    private transient Integer energy;
    private transient Long energyLastCalculationMillis;

    public SocRequest() {
    }

    public SocRequest(Integer soc, Integer evId) {
        this.soc = soc;
        this.evId = evId;
    }

    public SocRequest(Integer soc, Integer evId, Integer energy) {
        this(soc, evId);
        this.energy = energy;
    }

    public void setSocInitial(Integer socInitial) {
        this.socInitial = socInitial;
    }

    private Integer getSocInitialOrDefault() {
        return socInitial != null ? socInitial : 100;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    public Integer getSoc() {
        return soc;
    }

    private Integer getSocOrDefault() {
        return getSoc() != null ? getSoc() : 100;
    }

    public Integer getEvId() {
        return evId;
    }

    public void setEvId(Integer evId) {
        this.evId = evId;
    }

    @Override
    public boolean isUsingOptionalEnergy() {
        return false;
    }

    @Override
    public Integer getMin(LocalDateTime now) {
        return getEnergy();
    }

    @Override
    public Integer getMax(LocalDateTime now) {
        return getEnergy();
    }

    @Override
    public void update() {
        this.energy = calculateEnergy(((ElectricVehicleCharger) getControl()).getVehicle(evId));
        this.energyLastCalculationMillis = System.currentTimeMillis();
        setEnabled(energy > 0);
    }

    private Integer getEnergy() {
        if(energyLastCalculationMillis == null || System.currentTimeMillis() - energyLastCalculationMillis > 5000) {
            update();
        }
        return this.energy;
    }

    protected void setEnergy(Integer energy) {
        this.energy = energy;
    }


    public Integer calculateEnergy(ElectricVehicle vehicle) {
        float energy = 0.0f;
        if(getMeter() != null) {
            energy = getMeter().getEnergy();
            logger.debug("{}: energy metered: {} kWh", getApplianceId(), energy);
        }
        else {
            logger.debug("{}: No energy meter configured - cannot calculate max energy", getApplianceId());
        }

        int batteryCapacity = 100000; // default is 100 kWh
        int chargeLoss = 10; // default is 10%
        if(vehicle != null) {
            batteryCapacity = vehicle.getBatteryCapacity();
            chargeLoss = vehicle.getChargeLoss();
        }
        else {
            logger.warn("{}: evId not set - using defaults", getApplianceId());
        }
        Integer initialSoc = getSocInitialOrDefault();
        Integer targetSoc = getSocOrDefault();
        logger.debug("{}: energy calculation using evId={} batteryCapactiy={} chargeLoss={}% initialSoc={} targetSoc={}",
                getApplianceId(), evId, batteryCapacity, chargeLoss, initialSoc, targetSoc);
        Integer maxEnergy = Float.valueOf((targetSoc - initialSoc)/100.0f
                * (100 + chargeLoss)/100.0f * batteryCapacity).intValue()
                - Float.valueOf(energy * 1000).intValue();
        logger.debug("{}: energy calculated={}Wh", getApplianceId(), maxEnergy);
        return maxEnergy;
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return getEnergy() <= 0;
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval, TimeframeInterval activatedInterval, boolean wasRunning) {
        super.activeIntervalChanged(now, applianceId, deactivatedInterval, activatedInterval, wasRunning);
        if(activatedInterval != null && activatedInterval.getState() == TimeframeIntervalState.ACTIVE) {
            ((ElectricVehicleCharger) getControl()).retrieveSoc(now);
        }
    }

    @Override
    public void onEVChargerSocChanged(LocalDateTime now, Float soc) {
        logger.debug("{}: Using updated SOC={}", getApplianceId(), soc);
        if(! isEnabledBefore()) {
            setEnabled(true);
        }
        setSocInitial(Float.valueOf(soc).intValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SocRequest that = (SocRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(soc, that.soc)
                .append(evId, that.evId)
                .append(energy, that.energy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(soc)
                .append(evId)
                .append(energy)
                .toHashCode();
    }

    @Override
    public String toString() {
        String text = super.toString();
        text += "/";
        text += "evId=" + evId;
        text += "/";
        text += "soc=" + socInitial;
        text += "%=>";
        text += soc;
        text += "%";
        text += "/";
        text += "energy=" + (energy != null ? energy : 0);
        text += "Wh";
        return text;
    }
}
