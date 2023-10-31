/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class PowerLevel {
    @XmlAttribute
    private Integer power;
    @XmlElement(name = "SwitchStatus", type = SwitchStatus.class)
    private List<SwitchStatus> switchStatuses;

    public PowerLevel() {
    }

    public PowerLevel(Integer power, List<SwitchStatus> switchStatuses) {
        this.power = power;
        this.switchStatuses = switchStatuses;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public List<SwitchStatus> getSwitchStatuses() {
        return switchStatuses;
    }

    public void setSwitchStatuses(List<SwitchStatus> switchStatuses) {
        this.switchStatuses = switchStatuses;
    }

    @Override
    public String toString() {
        return "PowerLevel{" +
                "power=" + power +
                ", switchStatuses=" + switchStatuses +
                '}';
    }
}
