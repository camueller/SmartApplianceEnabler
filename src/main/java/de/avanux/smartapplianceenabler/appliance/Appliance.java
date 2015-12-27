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
package de.avanux.smartapplianceenabler.appliance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
public class Appliance {
    @XmlAttribute
    private String id;
    @XmlElement(name = "S0ElectricityMeter")
    private S0ElectricityMeter s0ElectricityMeter;
    @XmlElement(name = "Switch")
    private List<Switch> switches;
    @XmlElement(name = "Timeframe")
    private List<TimeFrame> timeFrames;
    @XmlTransient
    private RunningTimeMonitor runningTimeMonitor;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public S0ElectricityMeter getS0ElectricityMeter() {
        return s0ElectricityMeter;
    }

    public void setS0ElectricityMeter(S0ElectricityMeter s0ElectricityMeter) {
        this.s0ElectricityMeter = s0ElectricityMeter;
    }

    public List<Switch> getSwitches() {
        return switches;
    }

    public void setSwitches(List<Switch> switches) {
        this.switches = switches;
    }

    public List<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void setTimeFrames(List<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
    }

    public Meter getMeter() {
        return new MeterFactory().getMeter(this);
    }
    
    public List<Control> getControls() {
        return new ControlFactory().getControls(this, runningTimeMonitor);
    }

    public RunningTimeMonitor getRunningTimeMonitor() {
        return runningTimeMonitor;
    }

    public void setRunningTimeMonitor(RunningTimeMonitor runningTimeMonitor) {
        this.runningTimeMonitor = runningTimeMonitor;
    }
    
    public Set<GpioControllable> getGpioControllables() {
        Set<GpioControllable> controllables = new HashSet<GpioControllable>();
        if(s0ElectricityMeter != null) {
            controllables.add(s0ElectricityMeter);
        }
        if(switches != null && switches.size() > 0) {
            controllables.addAll(switches);
        }
        return controllables;
    }
    
    public boolean canConsumeOptionalEnergy() {
        if(timeFrames != null) {
            for(TimeFrame timeFrame : timeFrames) {
                if(timeFrame.getMaxRunningTime() != timeFrame.getMinRunningTime()) {
                    return true;
                }
            }
        }
        return false;
    }
}
