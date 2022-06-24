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

import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttEventName;
import de.avanux.smartapplianceenabler.mqtt.StartingCurrentSwitchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "min", "max" })
public class RuntimeRequest extends AbstractRequest {
    @XmlAttribute
    private Integer min;
    @XmlAttribute
    private int max;

    public RuntimeRequest() {
    }

    public RuntimeRequest(Integer min, int max) {
        setMin(min);
        setMax(max);
    }

    @Override
    public void init() {
        super.init();
        getMqttClient().subscribe(MqttEventName.StartingCurrentDetected, ControlMessage.class, (topic, message) -> {
            getLogger().debug("{} Handling event StartingCurrentDetected", getApplianceId());
            resetRuntime();
            setEnabled(true);
        });
        getMqttClient().subscribe(MqttEventName.FinishedCurrentDetected, ControlMessage.class, (topic, message) -> {
            getLogger().debug("{} Handling event FinishedCurrentDetected", getApplianceId());
            setEnabled(false);
            resetEnabledBefore();
        });
    }

    @Override
    public void remove() {
        super.remove();
        getMqttClient().unsubscribe(MqttEventName.StartingCurrentDetected);
        getMqttClient().unsubscribe(MqttEventName.FinishedCurrentDetected);
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(RuntimeRequest.class);
    }

    public Integer getMin(LocalDateTime now) {
        if(min != null && isActive()) {
            int min = this.min - getRuntime(now);
            return min > 0 ? min : 0;
        }
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax(LocalDateTime now) {
        if(isActive()) {
            int max = this.max - getRuntime(now);
            return max > 0 ? max : 0;
        }
        return max;
    }

    public void setMax(int max) {
        this.max = max;
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

    public boolean hasStartingCurrentSwitch() {
        return getControlMessage() instanceof StartingCurrentSwitchMessage;
    }

    @Override
    public boolean isFinished(LocalDateTime now) {
        return getMax(now) <= 0;
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval, TimeframeInterval activatedInterval, boolean wasRunning) {
        super.activeIntervalChanged(now, applianceId, deactivatedInterval, activatedInterval, wasRunning);
        if(deactivatedInterval != null && deactivatedInterval.getRequest() == this) {
            setEnabled(false);
        }
    }

    @Override
    public String toString() {
        return toString(LocalDateTime.now());
    }

    @Override
    public String toString(LocalDateTime now) {
        Integer min = getMin(now);
        Integer max = getMax(now);
        String text = super.toString();
        text += "/";
        if(min != null) {
            text += min.toString();
        }
        else {
            text += "_";
        }
        text += "s/";
        text += max;
        text += "s";
        return text;
    }
}
