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

import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.SocValues;
import de.avanux.smartapplianceenabler.mqtt.EVChargerSocChangedEvent;
import de.avanux.smartapplianceenabler.mqtt.EVChargerStateChangedEvent;
import de.avanux.smartapplianceenabler.mqtt.MqttEventName;
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
    private transient SocValues socValues;
    private transient boolean forceEnergyCalculation;

    public SocRequest() {
    }

    public SocRequest(Integer soc, Integer evId) {
        this.soc = soc;
        this.evId = evId;
    }

    /**
     * Should only be used for testing.
     */
    public SocRequest(Integer soc, Integer evId, Integer energy) {
        this.soc = soc;
        this.evId = evId;
        this.energy = energy;
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(SocRequest.class);
    }

    public SocValues socVariablesInitialized() {
        if(socValues == null) {
            socValues = new SocValues();
        }
        return socValues;
    }

    public void setSocInitial(Integer socInitial) {
        socVariablesInitialized().initial = socInitial;
    }

    public void setSocInitialIfNotSet(Integer socInitial) {
        if(socVariablesInitialized().initial == null) {
            socVariablesInitialized().initial = socInitial;
        }
    }

    public void setSocCurrent(Integer socCurrent) {
        socVariablesInitialized().current = socCurrent;
    }

    private Integer getSocCurrentOrDefault() {
        return socVariablesInitialized().current != null
                ? socVariablesInitialized().current : 0;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    public Integer getSocOrDefault() {
        return this.soc != null ? this.soc : 100;
    }

    public Integer getEvId() {
        return evId;
    }

    public void setEvId(Integer evId) {
        this.evId = evId;
    }

    public void setBatteryCapacity(Integer batteryCapacity) {
        socVariablesInitialized().batteryCapacity = batteryCapacity;
    }

    @Override
    public boolean isUsingOptionalEnergy(LocalDateTime now) {
        return false;
    }

    @Override
    public Boolean isAcceptControlRecommendations() {
        return super.isAcceptControlRecommendations() != null ? super.isAcceptControlRecommendations() : true;
    }

    @Override
    public Integer getMin(LocalDateTime now) {
        return energy;
    }

    @Override
    public Integer getMax(LocalDateTime now) {
        return energy;
    }

    @Override
    public void init() {
        super.init();
        getMqttClient().subscribe(MqttEventName.EVChargerSocChanged, (topic, message) -> {
            if(message instanceof EVChargerSocChangedEvent) {
                EVChargerSocChangedEvent event = (EVChargerSocChangedEvent) message;
                getLogger().debug("{}: Using updated SOC values: {}", getApplianceId(), event.socValues);
                if(! isEnabledBefore()) {
                    setEnabled(true);
                }
                this.socValues = new SocValues(event.socValues);
                updateForced();
            }
        });
    }

    @Override
    public void update() {
        if(this.forceEnergyCalculation) {
            Integer batteryCapacity = socVariablesInitialized().batteryCapacity;
            if(batteryCapacity != null) {
                this.energy = calculateEnergy(batteryCapacity);
                this.forceEnergyCalculation = false;
                if(energy != null && energy <= 0) {
                    // make sure we rely on up-to-date values
                    this.energy = calculateEnergy(batteryCapacity);
                    if(energy <= 0) {
                        setEnabled(false);
                    }
                }
            }
            else {
                getLogger().warn("{}: Cannot calculate energy since battery capacity is missing", getApplianceId());
            }
        }
    }

    public void updateForced() {
        this.forceEnergyCalculation = true;
        update();
    }

    public Integer calculateEnergy(int batteryCapacity) {
        Integer currentSoc = getSocCurrentOrDefault();
        Integer targetSoc = getSocOrDefault();
        int energy = Double.valueOf((targetSoc - currentSoc)/100.0 * batteryCapacity).intValue();
        getLogger().debug("{}: energy calculation: {}Wh evId={} batteryCapactiy={}Wh currentSoc={}% targetSoc={}%",
                getApplianceId(), energy, evId, batteryCapacity, currentSoc, targetSoc);
        return energy;
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return energy == null || energy <= 0;
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
