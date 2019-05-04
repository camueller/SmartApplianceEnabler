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

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElectricVehicleChargerTest {

    private Logger logger = LoggerFactory.getLogger(ElectricVehicleChargerTest.class);
    private ElectricVehicleCharger evCharger = new ElectricVehicleCharger();
    private EVControl evControl = Mockito.mock(EVControl.class);
    private LocalDateTime now = new LocalDateTime();

    public ElectricVehicleChargerTest() {
        evCharger.startChargingStateDetectionDelay = 0;
        evCharger.setControl(evControl);
        evCharger.setApplianceId("TEST");
        evCharger.init();
    }

    private void log(String message) {
        logger.debug("*********** " + message);
    }

    private void configureMocks(boolean vehicleNotConnected, boolean vehicleConnected, boolean charging) {
        Mockito.when(evControl.isVehicleNotConnected()).thenReturn(vehicleNotConnected);
        Mockito.when(evControl.isVehicleConnected()).thenReturn(vehicleConnected);
        Mockito.when(evControl.isCharging()).thenReturn(charging);
    }

    private void updateState() {
        // make sure the state is still correct after multiple invocations
        evCharger.updateState(now);
        evCharger.updateState(now);
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
        evCharger.setStartChargingRequested(true);
        Mockito.when(evControl.isCharging()).thenReturn(true);
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING,
                evCharger.getNewState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnect() {
        Mockito.when(evControl.isVehicleNotConnected()).thenReturn(true);
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnectWhileCharging() {
        Mockito.when(evControl.isVehicleNotConnected()).thenReturn(true);
        Mockito.when(evControl.isCharging()).thenReturn(false);
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(ElectricVehicleCharger.State.CHARGING));
    }

    @Test
    public void wasInState() {
        Assert.assertFalse(evCharger.wasInState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.CHARGING);
        Assert.assertFalse(evCharger.wasInState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.VEHICLE_CONNECTED);
        Assert.assertTrue(evCharger.wasInState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.CHARGING);
        Assert.assertTrue(evCharger.wasInState(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateOneTime() {
        Assert.assertFalse(evCharger.wasInStateOneTime(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.CHARGING);
        Assert.assertFalse(evCharger.wasInStateOneTime(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.VEHICLE_CONNECTED);
        Assert.assertTrue(evCharger.wasInStateOneTime(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.CHARGING);
        Assert.assertTrue(evCharger.wasInStateOneTime(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.VEHICLE_CONNECTED);
        Assert.assertFalse(evCharger.wasInStateOneTime(ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateAfterState() {
        Assert.assertFalse(evCharger.wasInStateAfterLastState(ElectricVehicleCharger.State.CHARGING, ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.VEHICLE_CONNECTED);
        Assert.assertFalse(evCharger.wasInStateAfterLastState(ElectricVehicleCharger.State.CHARGING, ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.CHARGING);
        Assert.assertTrue(evCharger.wasInStateAfterLastState(ElectricVehicleCharger.State.CHARGING, ElectricVehicleCharger.State.VEHICLE_CONNECTED));
        evCharger.setState(ElectricVehicleCharger.State.VEHICLE_CONNECTED);
        Assert.assertFalse(evCharger.wasInStateAfterLastState(ElectricVehicleCharger.State.CHARGING, ElectricVehicleCharger.State.VEHICLE_CONNECTED));
    }

    @Test
    public void updateState_noInterruption() {
        log("Vehicle not yet connected");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        log("Fully charged now");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_noInterruption_2cycles() {
        updateState_noInterruption();
        updateState_noInterruption();
    }

    @Test
    public void updateState_interruption()throws Exception {
        log("Vehicle not yet connected");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        log("Stop charging");
        evCharger.stopCharging();
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging again");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        log("Fully charged now");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_initiallyConnected() {
        log("Vehicle initially connected");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        log("Fully charged now");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_abort() {
        log("Vehicle not yet connected");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_error() {
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Mockito.when(evControl.isInErrorState()).thenReturn(true);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.ERROR, evCharger.getState());
        Mockito.when(evControl.isInErrorState()).thenReturn(false);
        updateState();
        Assert.assertEquals(ElectricVehicleCharger.State.CHARGING, evCharger.getState());
    }

    @Test
    public void isWithinStartChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        Assert.assertFalse(evCharger.isWithinStartChargingStateDetectionDelay(
                startChargingStateDetectionDelay, currentMillis, null));
        Assert.assertTrue(evCharger.isWithinStartChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        Assert.assertFalse(evCharger.isWithinStartChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
    }

    @Test
    public void isOn_startChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        // initial state != CHARGING
        Assert.assertFalse(evCharger.isOn(
                startChargingStateDetectionDelay, currentMillis, null));
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        Assert.assertFalse(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
        // state = CHARGING
        evCharger.setState(ElectricVehicleCharger.State.CHARGING);
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay, 0, null));
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        Assert.assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
    }
}
