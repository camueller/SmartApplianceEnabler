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

import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "min", "max" })
public class EnergyRequest extends AbstractEnergyRequest implements Request {
    @XmlAttribute
    private Integer min;
    @XmlAttribute
    private Integer max;
    private transient double energyMetered;

    public EnergyRequest() {
    }

    public EnergyRequest(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(EnergyRequest.class);
    }

    @Override
    public void init() {
        super.init();
        getMqttClient().subscribe(Meter.TOPIC, true, (topic, message) -> {
            var energy = ((MeterMessage) message).energy;
            if(energy > energyMetered) {
                energyMetered = energy;
            }
        });
    }

    public void remove() {
        super.remove();
        getMqttClient().unsubscribe(Meter.TOPIC);
    }

    @Override
    public boolean isUsingOptionalEnergy(LocalDateTime now) {
        Integer min = getMin(now);
        return min != null && min == 0 && getMax(now) > 0;
    }

    @Override
    public Boolean isAcceptControlRecommendations() {
        return super.isAcceptControlRecommendations() != null ? super.isAcceptControlRecommendations() : true;
    }

    public Integer getMin(LocalDateTime now) {
        var min = this.min != null ? this.min - getMeteredEnergy() : 0;
        return min > 0 ? min : 0;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax(LocalDateTime now) {
        var max = this.max != null ? this.max - getMeteredEnergy() : 0;
        return max > 0 ? max : 0;
    }

    private int getMeteredEnergy() {
        return isActive() ? Double.valueOf(energyMetered * 1000.0f).intValue() : 0;
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return getMax(now) <= 0;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return toString(LocalDateTime.now());
    }

    @Override
    public String toString(LocalDateTime now) {
        String text = super.toString();
        text += "/";
        if(min != null) {
            text += getMin(now).toString();
        }
        else {
            text += "_";
        }
        text += "Wh/";
        text += getMax(now).toString();
        text += "Wh";
        return text;
    }
}
