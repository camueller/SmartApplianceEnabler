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

package de.avanux.smartapplianceenabler.webservice;

/**
 * Created by axel on 15.10.17.
 */
public class ApplianceInfo {
    private String id;
    private String name;
    private String serial;
    private String type;
    private String vendor;
    private Integer minPowerConsumption;
    private Integer maxPowerConsumption;
    private String currentPowerMethod;
    private boolean interruptionsAllowed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getMinPowerConsumption() {
        return minPowerConsumption;
    }

    public void setMinPowerConsumption(Integer minPowerConsumption) {
        this.minPowerConsumption = minPowerConsumption;
    }

    public Integer getMaxPowerConsumption() {
        return maxPowerConsumption;
    }

    public void setMaxPowerConsumption(Integer maxPowerConsumption) {
        this.maxPowerConsumption = maxPowerConsumption;
    }

    public String getCurrentPowerMethod() {
        return currentPowerMethod;
    }

    public void setCurrentPowerMethod(String currentPowerMethod) {
        this.currentPowerMethod = currentPowerMethod;
    }

    public boolean isInterruptionsAllowed() {
        return interruptionsAllowed;
    }

    public void setInterruptionsAllowed(boolean interruptionsAllowed) {
        this.interruptionsAllowed = interruptionsAllowed;
    }

    @Override
    public String toString() {
        return "ApplianceInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", serial='" + serial + '\'' +
                ", type='" + type + '\'' +
                ", vendor='" + vendor + '\'' +
                ", maxPowerConsumption=" + maxPowerConsumption +
                ", currentPowerMethod='" + currentPowerMethod + '\'' +
                ", interruptionsAllowed=" + interruptionsAllowed +
                '}';
    }
}
