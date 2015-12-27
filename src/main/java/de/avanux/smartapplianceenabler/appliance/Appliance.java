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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
public class Appliance {
    @XmlAttribute
    private String id;
    @XmlElement(name = "Configuration")
    private ApplianceConfiguration configuration;
    @XmlTransient
    private RunningTimeMonitor runningTimeMonitor;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ApplianceConfiguration getConfiguration() {
        return configuration;
    }

    public Meter getMeter() {
        return new MeterFactory().getMeter(configuration);
    }
    
    public List<Control> getControls() {
        return new ControlFactory().getControls(configuration, runningTimeMonitor);
    }

    public RunningTimeMonitor getRunningTimeMonitor() {
        return runningTimeMonitor;
    }

    public void setRunningTimeMonitor(RunningTimeMonitor runningTimeMonitor) {
        this.runningTimeMonitor = runningTimeMonitor;
    }
    
    public boolean canConsumeOptionalEnergy() {
        if(configuration != null) {
            List<TimeFrame> timeFrames = configuration.getTimeFrames();
            if(timeFrames != null) {
                for(TimeFrame timeFrame : timeFrames) {
                    if(timeFrame.getMaxRunningTime() != timeFrame.getMinRunningTime()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
