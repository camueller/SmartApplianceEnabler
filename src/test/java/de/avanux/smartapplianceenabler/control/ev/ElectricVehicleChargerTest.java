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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ElectricVehicleChargerTest {

    private ElectricVehicleCharger evCharger = new ElectricVehicleCharger();
    private EVControl evControl = Mockito.mock(EVControl.class);

    public ElectricVehicleChargerTest() {
        evCharger.setEvControl(evControl);
        evCharger.init();
    }

    @Test
    public void getNewState_initial() {
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED));
    }

    @Test
    public void getNewState_connect() {
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED,
                evCharger.getNewState(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED));
    }

    @Test
    public void getNewState_charging() {
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING,
                evCharger.getNewState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnect() {
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnectWhileCharging() {
        Mockito.when(evControl.isCharging()).thenReturn(false);
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(ElectricVehicleCharger.State.CHARGING));
    }

    @Test
    public void updateState_noInterruption() {
        // VEHICLE_NOT_CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        // VEHICLE_CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        // CHARGING_COMPLETED
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING_COMPLETED, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_noInterruption_2cycles() {
        updateState_noInterruption();
        updateState_noInterruption();
    }

    @Test
    public void updateState_interruption() {
        // VEHICLE_NOT_CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        // VEHICLE_CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        // VEHICLE_CONNECTED
        Mockito.when(evControl.isCharging()).thenReturn(false);
        Mockito.when(evControl.isChargingCompleted()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        // CHARGING_COMPLETED
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING_COMPLETED, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_abort() {
        // NOT CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        // CONNECTED
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        // CHARGING
        Mockito.when(evControl.isChargingCompleted()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(true);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        // VEHICLE_NOT_CONNECTED
        Mockito.when(evControl.isChargingCompleted()).thenReturn(false);
        Mockito.when(evControl.isCharging()).thenReturn(false);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(false);
        evCharger.updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }
}
