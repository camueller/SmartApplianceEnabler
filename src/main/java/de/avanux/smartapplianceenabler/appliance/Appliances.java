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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Appliances")
@XmlAccessorType(XmlAccessType.FIELD)
public class Appliances {
    @XmlElement(name = "Configuration")
    private List<Configuration> configurationValues;
    @XmlElement(name = "Appliance")
    private List<Appliance> appliances;
    @XmlElement(name = "Connectivity")
    private Connectivity connectivity;

    public String getConfigurationValue(String param) {
        if(configurationValues != null) {
            for(Configuration configuration : configurationValues) {
                if(configuration.getParam().equals(param)) {
                    return configuration.getValue();
                }
            }
        }
        return null;
    }

    public List<Appliance> getAppliances() {
        return appliances;
    }

    public void setAppliances(List<Appliance> appliances) {
        this.appliances = appliances;
    }

    public Connectivity getConnectivity() {
        return connectivity;
    }
}
