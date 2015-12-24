/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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

@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceStatus {
    @XmlElement(name = "DeviceId")
    private String deviceId;
    @XmlElement(name = "EMSignalsAccepted")
    private boolean eMSignalsAccepted;
    @XmlElement(name = "Status")
    private Status status;
    @XmlElement(name = "ErrorCode")
    private String errorCode;
    @XmlElement(name = "PowerConsumption")
    private List<PowerConsumption> powerConsumptions;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean iseMSignalsAccepted() {
        return eMSignalsAccepted;
    }

    public void setEMSignalsAccepted(boolean eMSignalsAccepted) {
        this.eMSignalsAccepted = eMSignalsAccepted;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public List<PowerConsumption> getPowerConsumption() {
        return powerConsumptions;
    }

    public void setPowerConsumption(List<PowerConsumption> powerConsumptions) {
        this.powerConsumptions = powerConsumptions;
    }
}
