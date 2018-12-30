/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

import de.avanux.smartapplianceenabler.meter.Meter;

public class EVControlMock implements EVControl, Meter {

    @Override
    public void validate() {
    }

    @Override
    public boolean isVehicleNotConnected() {
        return false;
    }

    @Override
    public boolean isVehicleConnected() {
        return true;
    }

    @Override
    public boolean isCharging() {
        return true;
    }

    @Override
    public boolean isChargingCompleted() {
        return false;
    }

    @Override
    public boolean isInErrorState() {
        return false;
    }

    @Override
    public void setChargeCurrent(int current) {

    }

    @Override
    public void startCharging() {

    }

    @Override
    public void stopCharging() {

    }

    @Override
    public void setApplianceId(String applianceId) {

    }

    // --------- Meter ------------------------------------------------------

    @Override
    public boolean isOn() {
        return true;
    }

    @Override
    public int getAveragePower() {
        return 0;
    }

    @Override
    public int getMinPower() {
        return 0;
    }

    @Override
    public int getMaxPower() {
        return 0;
    }

    @Override
    public Integer getMeasurementInterval() {
        return null;
    }

    @Override
    public float getEnergy() {
        return 0;
    }

    @Override
    public void startEnergyMeter() {

    }

    @Override
    public void stopEnergyMeter() {

    }

    @Override
    public void resetEnergyMeter() {

    }
}
