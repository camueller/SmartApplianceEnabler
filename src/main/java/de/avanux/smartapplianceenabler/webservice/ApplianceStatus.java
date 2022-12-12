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
    private Integer runningTime;
    private Integer remainingMinRunningTime;
    private Integer remainingMaxRunningTime;
    private Integer remainingMinEnergy;
    private Integer remainingMaxEnergy;
    private Integer plannedEnergyAmount;
    private Integer chargedEnergyAmount;
    private Integer currentChargePower;
    private boolean planningRequested;
    private Integer earliestStart;
    private Integer latestStart;
    private Integer latestEnd;
    private boolean on;
    private boolean controllable;
    private Integer interruptedSince;
    private boolean optionalEnergy;
    private Integer evIdCharging;
    private String state;
    private Long stateLastChangedTimestamp;
    private Integer soc;
    private Long socTimestamp;
    private Integer socInitial;
    private Long socInitialTimestamp;
    private Integer socTarget;


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

    public Integer getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(Integer runningTime) {
        this.runningTime = runningTime;
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

    public Integer getRemainingMinEnergy() {
        return remainingMinEnergy;
    }

    public void setRemainingMinEnergy(Integer remainingMinEnergy) {
        this.remainingMinEnergy = remainingMinEnergy;
    }

    public Integer getRemainingMaxEnergy() {
        return remainingMaxEnergy;
    }

    public void setRemainingMaxEnergy(Integer remainingMaxEnergy) {
        this.remainingMaxEnergy = remainingMaxEnergy;
    }

    public Integer getPlannedEnergyAmount() {
        return plannedEnergyAmount;
    }

    public void setPlannedEnergyAmount(Integer plannedEnergyAmount) {
        this.plannedEnergyAmount = plannedEnergyAmount;
    }

    public Integer getChargedEnergyAmount() {
        return chargedEnergyAmount;
    }

    public void setChargedEnergyAmount(Integer chargedEnergyAmount) {
        this.chargedEnergyAmount = chargedEnergyAmount;
    }

    public Integer getCurrentChargePower() {
        return currentChargePower;
    }

    public void setCurrentChargePower(Integer currentChargePower) {
        this.currentChargePower = currentChargePower;
    }

    public boolean isPlanningRequested() {
        return planningRequested;
    }

    public void setPlanningRequested(boolean planningRequested) {
        this.planningRequested = planningRequested;
    }

    public Integer getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(Integer earliestStart) {
        this.earliestStart = earliestStart;
    }

    public Integer getLatestStart() {
        return latestStart;
    }

    public void setLatestStart(Integer latestStart) {
        this.latestStart = latestStart;
    }

    public Integer getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(Integer latestEnd) {
        this.latestEnd = latestEnd;
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

    public Integer getInterruptedSince() {
        return interruptedSince;
    }

    public void setInterruptedSince(Integer interruptedSince) {
        this.interruptedSince = interruptedSince;
    }

    public boolean isOptionalEnergy() {
        return optionalEnergy;
    }

    public void setOptionalEnergy(boolean optionalEnergy) {
        this.optionalEnergy = optionalEnergy;
    }

    public Integer getEvIdCharging() {
        return evIdCharging;
    }

    public void setEvIdCharging(Integer evIdCharging) {
        this.evIdCharging = evIdCharging;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getStateLastChangedTimestamp() {
        return stateLastChangedTimestamp;
    }

    public void setStateLastChangedTimestamp(Long stateLastChangedTimestamp) {
        this.stateLastChangedTimestamp = stateLastChangedTimestamp;
    }

    public Integer getSoc() {
        return soc;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    public Long getSocTimestamp() {
        return socTimestamp;
    }

    public void setSocTimestamp(Long socTimestamp) {
        this.socTimestamp = socTimestamp;
    }

    public Integer getSocInitial() {
        return socInitial;
    }

    public void setSocInitial(Integer socInitial) {
        this.socInitial = socInitial;
    }

    public Long getSocInitialTimestamp() {
        return socInitialTimestamp;
    }

    public void setSocInitialTimestamp(Long socInitialTimestamp) {
        this.socInitialTimestamp = socInitialTimestamp;
    }

    public Integer getSocTarget() {
        return socTarget;
    }

    public void setSocTarget(Integer socTarget) {
        this.socTarget = socTarget;
    }
}
