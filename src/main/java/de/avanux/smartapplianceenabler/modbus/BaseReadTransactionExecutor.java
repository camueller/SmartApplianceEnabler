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

package de.avanux.smartapplianceenabler.modbus;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;

abstract public class BaseReadTransactionExecutor implements ApplianceIdConsumer {

    private String applianceId;
    private Integer address;
    private int bytes = 1;

    public BaseReadTransactionExecutor(String address, int bytes) {
        if(address.startsWith("0x")) {
            this.address = Integer.parseInt(address.substring(2), 16);
        }
        else {
            this.address = Integer.parseInt(address);
        }
        this.bytes = bytes;
    }


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    protected String getApplianceId() {
        return applianceId;
    }

    protected Integer getAddress() {
        return address;
    }

    protected int getBytes() {
        return bytes;
    }
}
