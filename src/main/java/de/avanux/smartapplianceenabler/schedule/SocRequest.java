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
import de.avanux.smartapplianceenabler.control.ev.SocValues;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.time.LocalDateTime;
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
    private transient Integer energy;
    private transient SocValues receivedSocVariables;
    private transient SocValues lastEnergyCalculationVariables;

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

    protected Logger getLogger() {
        return LoggerFactory.getLogger(SocRequest.class);
    }

    public void setSocInitial(Integer socInitial) {
        if(this.receivedSocVariables == null) {
            this.receivedSocVariables = new SocValues();
        }
        this.receivedSocVariables.initial = socInitial;
    }

    public void setSocCurrent(Integer socCurrent) {
        if(this.receivedSocVariables == null) {
            this.receivedSocVariables = new SocValues();
        }
        this.receivedSocVariables.current = socCurrent;
    }

    private Integer getSocCurrentOrDefault() {
        return this.lastEnergyCalculationVariables != null && this.lastEnergyCalculationVariables.current != null
                ? this.lastEnergyCalculationVariables.current : 0;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    private Integer getSocOrDefault() {
        return this.soc != null ? this.soc : 100;
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
    public Boolean isAcceptControlRecommendations() {
        return super.isAcceptControlRecommendations() != null ? super.isAcceptControlRecommendations() : true;
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
        if(this.lastEnergyCalculationVariables == null || !this.lastEnergyCalculationVariables.equals(this.receivedSocVariables)) {
            this.lastEnergyCalculationVariables = this.receivedSocVariables;
            this.energy = calculateEnergy();
        }
        if(energy <= 0) {
            setEnabled(false);
        }
    }

    private Integer getEnergy() {
        return this.energy;
    }

    protected void setEnergy(Integer energy) {
        this.energy = energy;
    }

    public Integer calculateEnergy() {
        if(lastEnergyCalculationVariables == null) {
            return null;
        }
        Integer currentSoc = getSocCurrentOrDefault();
        Integer targetSoc = getSocOrDefault();
        int energy = Float.valueOf((targetSoc - currentSoc)/100.0f * lastEnergyCalculationVariables.batteryCapacity).intValue();
        getLogger().debug("{}: energy calculation: {}Wh evId={} batteryCapactiy={} currentSoc={} targetSoc={}",
                getApplianceId(), energy, evId, lastEnergyCalculationVariables.batteryCapacity, currentSoc, targetSoc);
        return energy;
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return getEnergy() <= 0;
    }

    @Override
    public void onEVChargerSocChanged(LocalDateTime now, SocValues socValues) {
        getLogger().debug("{}: Using updated SOC values: {}", getApplianceId(), socValues);
        if(! isEnabledBefore()) {
            setEnabled(true);
        }
        this.receivedSocVariables = new SocValues(socValues.batteryCapacity, socValues.initial, socValues.retrieved, socValues.current);
        update();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SocRequest that = (SocRequest) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getSocOrDefault(), that.getSocOrDefault())
                .append(evId, that.evId)
                .append(energy, that.energy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getSocOrDefault())
                .append(evId)
                .append(energy)
                .toHashCode();
    }

    @Override
    public String toString() {
        return toString(LocalDateTime.now());
    }

    @Override
    public String toString(LocalDateTime now) {
        String text = super.toString();
        text += "/";
        text += "evId=" + evId;
        text += "/";
        text += "soc=" + getSocCurrentOrDefault();
        text += "%=>";
        text += getSocOrDefault();
        text += "%";
        text += "/";
        text += "energy=" + (energy != null ? energy : 0);
        text += "Wh";
        return text;
    }
}
