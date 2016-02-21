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

public class MeterFactory {

    public Meter getMeter(Appliance appliance) {
        if(appliance.getS0ElectricityMeter() != null) {
            S0ElectricityMeter s0ElectricityMeter = appliance.getS0ElectricityMeter();
            // inject control required to get appliance state
            List<Switch> switches = appliance.getSwitches();
            if(switches != null && switches.size() > 0) {
                s0ElectricityMeter.setControl(switches.get(0));
            }
            return s0ElectricityMeter;
        }
        else if(appliance.getModbusElectricityMeter() != null) {
            return appliance.getModbusElectricityMeter();
        }
        return null;
    }
}
