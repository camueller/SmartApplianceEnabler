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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class ElectricVehicleChargerTest {

    private Logger logger = LoggerFactory.getLogger(ElectricVehicleChargerTest.class);
    private ElectricVehicleCharger evCharger = Mockito.spy(new ElectricVehicleCharger());
    private EVChargerControl evChargerControl = Mockito.mock(EVChargerControl.class);
    private LocalDateTime now = new LocalDateTime();

    public ElectricVehicleChargerTest() {
        evCharger.startChargingStateDetectionDelay = 0;
        evCharger.setControl(evChargerControl);
        evCharger.setApplianceId("TEST");
        evCharger.init();
    }

    private void log(String message) {
        logger.debug("*********** " + message);
    }

    private void configureMocks(boolean vehicleNotConnected, boolean vehicleConnected, boolean charging) {
        Mockito.when(evChargerControl.isVehicleNotConnected()).thenReturn(vehicleNotConnected);
        Mockito.when(evChargerControl.isVehicleConnected()).thenReturn(vehicleConnected);
        Mockito.when(evChargerControl.isCharging()).thenReturn(charging);
    }

    private void updateState() {
        // make sure the state is still correct after multiple invocations
        evCharger.updateState(now);
        evCharger.updateState(now);
    }

    @Test
    public void getNewState_initial() {
        Mockito.when(evChargerControl.isVehicleConnected()).thenReturn(false);
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(EVChargerState.VEHICLE_NOT_CONNECTED));
    }

    @Test
    public void getNewState_connect() {
        Mockito.when(evChargerControl.isVehicleConnected()).thenReturn(true);
        assertEquals(EVChargerState.VEHICLE_CONNECTED,
                evCharger.getNewState(EVChargerState.VEHICLE_NOT_CONNECTED));
    }

    @Test
    public void getNewState_charging() {
        evCharger.setStartChargingRequested(true);
        Mockito.when(evChargerControl.isCharging()).thenReturn(true);
        assertEquals(EVChargerState.CHARGING,
                evCharger.getNewState(EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnect() {
        Mockito.when(evChargerControl.isVehicleNotConnected()).thenReturn(true);
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void getNewState_disconnectWhileCharging() {
        Mockito.when(evChargerControl.isVehicleNotConnected()).thenReturn(true);
        Mockito.when(evChargerControl.isCharging()).thenReturn(false);
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(EVChargerState.CHARGING));
    }

    @Test
    public void wasInState() {
        assertFalse(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.CHARGING);
        assertFalse(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.VEHICLE_CONNECTED);
        assertTrue(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.CHARGING);
        assertTrue(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateOneTime() {
        assertFalse(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.CHARGING);
        assertFalse(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.VEHICLE_CONNECTED);
        assertTrue(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.CHARGING);
        assertTrue(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.VEHICLE_CONNECTED);
        assertFalse(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateAfterState() {
        assertFalse(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.VEHICLE_CONNECTED);
        assertFalse(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.CHARGING);
        assertTrue(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(EVChargerState.VEHICLE_CONNECTED);
        assertFalse(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void updateState_noInterruption() {
        log("Vehicle not yet connected");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
        log("Fully charged now");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
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
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
        log("Stop charging");
        evCharger.stopCharging();
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging again");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
        log("Fully charged now");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_initiallyConnected() {
        log("Vehicle initially connected");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
        log("Fully charged now");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_abort() {
        log("Vehicle not yet connected");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_stopAfterStart() {
        log("Vehicle not yet connected");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
        log("Connect vehicle");
        configureMocks(false, true, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Start charging");
        evCharger.startCharging();
        Mockito.when(evCharger.isWithinSwitchChargingStateDetectionDelay()).thenReturn(true);
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Stop charging during ChargingStateDetectionDelay");
        evCharger.stopCharging();
        updateState();
        log("After ChargingStateDetectionDelay");
        Mockito.when(evCharger.isWithinSwitchChargingStateDetectionDelay()).thenReturn(false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_error() {
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        Mockito.when(evChargerControl.isInErrorState()).thenReturn(true);
        updateState();
        assertEquals(EVChargerState.ERROR, evCharger.getState());
        Mockito.when(evChargerControl.isInErrorState()).thenReturn(false);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
    }

    @Test
    public void isWithinStartChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        assertFalse(evCharger.isWithinSwitchChargingStateDetectionDelay(
                startChargingStateDetectionDelay, currentMillis, null));
        assertTrue(evCharger.isWithinSwitchChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        assertFalse(evCharger.isWithinSwitchChargingStateDetectionDelay(startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
    }

    @Test
    public void isOn_startChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        evCharger.setStartChargingRequested(true);
        // initial state != CHARGING
        assertFalse(evCharger.isOn(
                startChargingStateDetectionDelay, currentMillis, null));
        assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        assertFalse(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
        // state = CHARGING
        evCharger.setState(EVChargerState.CHARGING);
        assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay, 0, null));
        assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
    }
}
