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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Timeframe {
    @XmlElement(name = "DeviceId")
    private String deviceId;
    @XmlElement(name = "EarliestStart")
    private Integer earliestStart;
    @XmlElement(name = "LatestEnd")
    private Integer latestEnd;
    @XmlElement(name = "MinRunningTime")
    private Integer minRunningTime;
    @XmlElement(name = "MaxRunningTime")
    private Integer maxRunningTime;

    public Timeframe() {
    }

    public Timeframe(String deviceId, Integer earliestStart, Integer latestEnd, Integer minRunningTime, Integer maxRunningTime) {
        this.deviceId = deviceId;
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
        this.minRunningTime = minRunningTime;
        this.maxRunningTime = maxRunningTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(Integer earliestStart) {
        this.earliestStart = earliestStart;
    }

    public Integer getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(Integer latestEnd) {
        this.latestEnd = latestEnd;
    }

    public Integer getMinRunningTime() {
        return minRunningTime;
    }

    public void setMinRunningTime(Integer minRunningTime) {
        this.minRunningTime = minRunningTime;
    }

    public Integer getMaxRunningTime() {
        return maxRunningTime;
    }

    public void setMaxRunningTime(Integer maxRunningTime) {
        this.maxRunningTime = maxRunningTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Timeframe timeframe = (Timeframe) o;

        return new EqualsBuilder()
                .append(deviceId, timeframe.deviceId)
                .append(earliestStart, timeframe.earliestStart)
                .append(latestEnd, timeframe.latestEnd)
                .append(minRunningTime, timeframe.minRunningTime)
                .append(maxRunningTime, timeframe.maxRunningTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(deviceId)
                .append(earliestStart)
                .append(latestEnd)
                .append(minRunningTime)
                .append(maxRunningTime)
                .toHashCode();
    }

    @Override
    public String toString() {
        return earliestStart
                + "s-" + latestEnd
                + "s:" + (minRunningTime != null ? minRunningTime : "-")
                + "s/" + maxRunningTime + "s";
    }
}
