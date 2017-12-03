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


public class ApplianceStatus {
    private String id;
    private String name;
    private String type;
    private String vendor;
    private Integer remainingMinRunningTime;
    private Integer remainingMaxRunningTime;
    private boolean planningRequested;
    private boolean earliestStartPassed;
    private boolean on;
    private boolean controllable;


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

    public Integer getRemainingMinRunningTime() {
        return remainingMinRunningTime;
    }

    public void setRemainingMinRunningTime(Integer remainingMinRunningTime) {
        this.remainingMinRunningTime = remainingMinRunningTime;
    }

    public Integer getRemainingMaxRunningTime() {
        return remainingMaxRunningTime;
    }

    public void setRemainingMaxRunningTime(Integer remainingMaxRunningTime) {
        this.remainingMaxRunningTime = remainingMaxRunningTime;
    }

    public boolean isPlanningRequested() {
        return planningRequested;
    }

    public void setPlanningRequested(boolean planningRequested) {
        this.planningRequested = planningRequested;
    }

    public boolean isEarliestStartPassed() {
        return earliestStartPassed;
    }

    public void setEarliestStartPassed(boolean earliestStartPassed) {
        this.earliestStartPassed = earliestStartPassed;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public boolean isControllable() {
        return controllable;
    }

    public void setControllable(boolean controllable) {
        this.controllable = controllable;
    }
}
