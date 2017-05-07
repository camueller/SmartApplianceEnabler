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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Device2EM")
@XmlAccessorType(XmlAccessType.FIELD)
public class Device2EM {
    @XmlElement(name = "DeviceInfo")
    private List<DeviceInfo> deviceInfo;
    @XmlElement(name = "DeviceStatus")
    private List<DeviceStatus> deviceStatus;
    @XmlElement(name = "PlanningRequest")
    private List<PlanningRequest> planningRequest;
    

    public List<DeviceInfo> getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(List<DeviceInfo> deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<DeviceStatus> getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(List<DeviceStatus> deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public List<PlanningRequest> getPlanningRequest() {
        return planningRequest;
    }

    public void setPlanningRequest(List<PlanningRequest> planningRequest) {
        this.planningRequest = planningRequest;
    }
}
