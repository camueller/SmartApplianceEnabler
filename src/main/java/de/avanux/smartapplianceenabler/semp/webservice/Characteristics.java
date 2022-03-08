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
package de.avanux.smartapplianceenabler.semp.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Characteristics {
    @XmlElement(name = "MaxPowerConsumption")
    private int maxPowerConsumption;
    @XmlElement(name = "MinPowerConsumption")
    private Integer minPowerConsumption;
    @XmlElement(name = "MinOnTime")
    private Integer minOnTime;
    @XmlElement(name = "MaxOnTime")
    private Integer maxOnTime;
    @XmlElement(name = "MinOffTime")
    private Integer minOffTime;
    @XmlElement(name = "MaxOffTime")
    private Integer maxOffTime;
    @XmlElement(name = "PowerLevels")
    private PowerLevels powerLevels;

    public Integer getMinPowerConsumption() {
        return minPowerConsumption;
    }

    public void setMinPowerConsumption(Integer minPowerConsumption) {
        this.minPowerConsumption = minPowerConsumption;
    }

    public int getMaxPowerConsumption() {
        return maxPowerConsumption;
    }

    public void setMaxPowerConsumption(int maxPowerConsumption) {
        this.maxPowerConsumption = maxPowerConsumption;
    }

    public Integer getMinOnTime() {
        return minOnTime;
    }

    public void setMinOnTime(Integer minOnTime) {
        this.minOnTime = minOnTime;
    }

    public Integer getMaxOnTime() {
        return maxOnTime;
    }

    public void setMaxOnTime(Integer maxOnTime) {
        this.maxOnTime = maxOnTime;
    }

    public Integer getMinOffTime() {
        return minOffTime;
    }

    public void setMinOffTime(Integer minOffTime) {
        this.minOffTime = minOffTime;
    }

    public Integer getMaxOffTime() {
        return maxOffTime;
    }

    public void setMaxOffTime(Integer maxOffTime) {
        this.maxOffTime = maxOffTime;
    }

    public PowerLevels getPowerLevels() {
        return powerLevels;
    }

    public void setPowerLevels(PowerLevels powerLevels) {
        this.powerLevels = powerLevels;
    }

    @Override
    public String toString() {
        return "Characteristics{" +
                "maxPowerConsumption=" + maxPowerConsumption +
                ", minPowerConsumption=" + minPowerConsumption +
                ", minOnTime=" + minOnTime +
                ", maxOnTime=" + maxOnTime +
                ", minOffTime=" + minOffTime +
                ", maxOffTime=" + maxOffTime +
                '}';
    }
}
