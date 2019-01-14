/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicle {
    @XmlAttribute
    private Integer id;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private Integer batteryCapacity;
    @XmlAttribute
    private Integer phases;
    @XmlAttribute
    private Integer defaultEnergyCharge;
    @XmlElements({
            @XmlElement(name = "SocScript", type = SocScript.class),
    })
    private SocScript socScript;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Integer batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public Integer getPhases() {
        return phases;
    }

    public void setPhases(Integer phases) {
        this.phases = phases;
    }

    public Integer getDefaultEnergyCharge() {
        return defaultEnergyCharge;
    }

    public void setDefaultEnergyCharge(Integer defaultEnergyCharge) {
        this.defaultEnergyCharge = defaultEnergyCharge;
    }

    public SocScript getSocScript() {
        return socScript;
    }

    public void setSocScript(SocScript socScript) {
        this.socScript = socScript;
    }

    @Override
    public String toString() {
        return "ElectricVehicle {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", batteryCapacity=" + batteryCapacity +
                ", phases=" + phases +
                ", defaultEnergyCharge=" + defaultEnergyCharge +
                ", socScript=" + socScript +
                '}';
    }
}
