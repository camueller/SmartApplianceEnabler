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

package de.avanux.smartapplianceenabler.schedule;

import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "min", "max" })
public class EnergyRequest extends AbstractEnergyRequest implements Request {
    @XmlAttribute
    private Integer min;
    @XmlAttribute
    private Integer max;

    public EnergyRequest() {
    }

    public EnergyRequest(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    public Integer getMin(LocalDateTime now) {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax(LocalDateTime now) {
        return max;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return false;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    @Override
    public String toString() {
        String text = "";

        if(min != null) {
            text += min.toString();
        }
        else {
            text += "?";
        }
        text += "Wh/";
        if(max != null) {
            text += max.toString();
        }
        else {
            text += "?";
        }
        text += "Wh";

        return text;
    }
}
