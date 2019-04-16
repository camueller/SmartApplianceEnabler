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

import de.avanux.smartapplianceenabler.protocol.JsonProtocol;
import org.junit.Assert;
import org.junit.Test;

public class EVHttpControlTest {

    private EVHttpControl control;

    public EVHttpControlTest() {
        this.control = new EVHttpControl(new JsonProtocol());
    }

    @Test
    public void getCarState() {
        this.control.parse("{ \"car\": \"9\" }");
        Assert.assertEquals(9, this.control.getCarState().intValue());
    }

    @Test
    public void isVehicleNotConnected() {
        this.control.parse("{ \"car\": \"1\" }");
        Assert.assertTrue(this.control.isVehicleNotConnected());
    }

    @Test
    public void isVehicleConnected() {
        this.control.parse("{ \"car\": \"3\" }");
        Assert.assertTrue(this.control.isVehicleConnected());
    }

    @Test
    public void isCharging() {
        this.control.parse("{ \"car\": \"2\" }");
        Assert.assertTrue(this.control.isCharging());
    }

    @Test
    public void isChargingCompleted() {
        this.control.parse("{ \"car\": \"4\" }");
        Assert.assertTrue(this.control.isChargingCompleted());
    }

    @Test
    public void isInErrorState_True() {
        this.control.parse("{ \"err\": \"1\" }");
        Assert.assertTrue(this.control.isInErrorState());
    }

    @Test
    public void isInErrorState_False() {
        this.control.parse("{ \"err\": \"0\" }");
        Assert.assertFalse(this.control.isInErrorState());
    }
}
