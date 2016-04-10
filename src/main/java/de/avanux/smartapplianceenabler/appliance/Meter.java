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

public interface Meter extends ControlMonitor {

    /**
     * Average power consumption during measurement interval in watt.
     * @return
     */
    int getAveragePower();

    /**
     * Minimum power consumption during measurement interval in watt.
     * @return
     */
    int getMinPower();

    /**
     * Maximum power consumption during measurement interval in watt.
     * @return
     */
    int getMaxPower();

    /**
     * Interval for which average power will be calculated. 
     * @return
     */
    Integer getMeasurementInterval();
}
