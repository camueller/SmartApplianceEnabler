/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev.http;

import de.avanux.smartapplianceenabler.control.ev.EVControl;
import de.avanux.smartapplianceenabler.protocol.Protocol;

public class EVHttpControl implements EVControl {

    private Protocol protocol;

    public EVHttpControl(Protocol protocol) {
        this.protocol = protocol;
    }

    public void parse(String response) {
        this.protocol.parse(response);
    }

    @Override
    public void setPollInterval(Integer pollInterval) {
    }

    @Override
    public void init(boolean checkRegisterConfiguration) {

    }

    @Override
    public boolean isVehicleNotConnected() {
        return getCarState() == 1;
    }

    @Override
    public boolean isVehicleConnected() {
        return getCarState() == 3;
    }

    @Override
    public boolean isCharging() {
        return getCarState() == 2;
    }

    @Override
    public boolean isChargingCompleted() {
        return getCarState() == 4;
    }

    @Override
    public boolean isInErrorState() {
        return getErrState() != 0;
    }

    protected Integer getCarState() {
        return this.protocol.readIntegerValue("$.car");
    }

    protected Integer getErrState() {
        return this.protocol.readIntegerValue("$.err");
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
}
