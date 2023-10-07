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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.schedule.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElectricVehicleChargerTest {

    private Logger logger = LoggerFactory.getLogger(ElectricVehicleChargerTest.class);
    private ElectricVehicleCharger evCharger = Mockito.spy(new ElectricVehicleCharger());
    private EVChargerControl evChargerControl = mock(EVChargerControl.class);
    private ElectricVehicle ev;
    private Appliance appliance = mock(Appliance.class);
    private TimeframeIntervalHandler timeframeIntervalHandler = mock(TimeframeIntervalHandler.class);
    private Request request = mock(Request.class);
    private LocalDateTime now = LocalDateTime.now();
    private String applianceId = "TEST";
    private TimeframeInterval timeframeInterval;

    public ElectricVehicleChargerTest() {
        SocRequest request = new SocRequest(50, 1);
        timeframeInterval = new TimeframeInterval(null, request);

        ev = new ElectricVehicle();
        ev.setName("Nissan Leaf");
        ev.setId(1);
        ev.setBatteryCapacity(40000);

        evCharger.startChargingStateDetectionDelay = 0;
        evCharger.setControl(evChargerControl);
        evCharger.setApplianceId(applianceId);
        evCharger.setAppliance(appliance);
        evCharger.setVehicles(Collections.singletonList(ev));
        evCharger.init();

        when(appliance.getTimeframeIntervalHandler()).thenReturn(timeframeIntervalHandler);
        when(timeframeIntervalHandler.findTimeframeIntervalsUntilFirstGap()).thenReturn(new ArrayList<>());
        when(timeframeIntervalHandler.getActiveTimeframeInterval()).thenReturn(timeframeInterval);
        when(timeframeIntervalHandler.getQueue()).thenReturn(Collections.singletonList(timeframeInterval));
    }

    private void log(String message) {
        logger.debug("*********** " + message);
    }

    private void configureMocks(boolean vehicleNotConnected, boolean vehicleConnected, boolean charging) {
        configureMocks(vehicleNotConnected, vehicleConnected, charging, 5000);
    }

    private void configureMocks(boolean vehicleNotConnected, boolean vehicleConnected, boolean charging, int max) {
        when(evChargerControl.isVehicleNotConnected()).thenReturn(vehicleNotConnected);
        when(evChargerControl.isVehicleConnected()).thenReturn(vehicleConnected);
        when(evChargerControl.isCharging()).thenReturn(charging);

        when(request.getMax(any())).thenReturn(max);
        List<TimeframeInterval> intervals = new ArrayList<>();
        intervals.add(new TimeframeInterval(mock(Interval.class), request));
        when(timeframeIntervalHandler.findTimeframeIntervalsUntilFirstGap()).thenReturn(intervals);
    }

    private void updateState() {
        // make sure the state is still correct after multiple invocations
        evCharger.updateState(now);
        evCharger.updateState(now);
    }

    @Test
    public void getNewState_initial() {
        when(evChargerControl.isVehicleConnected()).thenReturn(false);
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(now, EVChargerState.VEHICLE_NOT_CONNECTED, false));
    }

    @Test
    public void getNewState_connect() {
        when(evChargerControl.isVehicleConnected()).thenReturn(true);
        assertEquals(EVChargerState.VEHICLE_CONNECTED,
                evCharger.getNewState(now, EVChargerState.VEHICLE_NOT_CONNECTED, false));
    }

    @Test
    public void getNewState_charging() {
        evCharger.setStartChargingRequested(true);
        when(evChargerControl.isCharging()).thenReturn(true);
        assertEquals(EVChargerState.CHARGING,
                evCharger.getNewState(now, EVChargerState.VEHICLE_CONNECTED, false));
    }

    @Test
    public void getNewState_chargingCompleted() {
        evCharger.setStartChargingRequested(true);
        when(evChargerControl.isCharging()).thenReturn(false);
        assertEquals(EVChargerState.CHARGING_COMPLETED,
                evCharger.getNewState(now, EVChargerState.VEHICLE_CONNECTED, true));
    }

    @Test
    public void getNewState_disconnect() {
        when(evChargerControl.isVehicleNotConnected()).thenReturn(true);
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(now, EVChargerState.VEHICLE_CONNECTED, false));
    }

    @Test
    public void getNewState_disconnectWhileCharging() {
        when(evChargerControl.isVehicleNotConnected()).thenReturn(true);
        when(evChargerControl.isCharging()).thenReturn(false);
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED,
                evCharger.getNewState(now, EVChargerState.CHARGING, false));
    }

    @Test
    public void wasInState() {
        assertFalse(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.CHARGING);
        assertFalse(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.VEHICLE_CONNECTED);
        assertTrue(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.CHARGING);
        assertTrue(evCharger.wasInState(EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateOneTime() {
        assertFalse(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.CHARGING);
        assertFalse(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.VEHICLE_CONNECTED);
        assertTrue(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.CHARGING);
        assertTrue(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.VEHICLE_CONNECTED);
        assertFalse(evCharger.wasInStateOneTime(EVChargerState.VEHICLE_CONNECTED));
    }

    @Test
    public void wasInStateAfterState() {
        assertFalse(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.VEHICLE_CONNECTED);
        assertFalse(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.CHARGING);
        assertTrue(evCharger.wasInStateAfterLastState(EVChargerState.CHARGING, EVChargerState.VEHICLE_CONNECTED));
        evCharger.setState(now, EVChargerState.VEHICLE_CONNECTED);
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
        log("Timeframe interval expired");
        configureMocks(false, true, false);
        updateState();
        evCharger.activeIntervalChanged(now, applianceId, timeframeInterval, null, false);
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
        log("Timeframe interval expired");
        configureMocks(false, true, false);
        updateState();
        evCharger.activeIntervalChanged(now, applianceId, timeframeInterval, null, false);
        assertEquals(EVChargerState.CHARGING_COMPLETED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_requestEmptyButNotEmptyRequestExist()throws Exception {
        List<TimeframeInterval> intervals = new ArrayList<>();
        intervals.add(timeframeInterval);

        var scheduledTimeframeInterval = new TimeframeInterval(null, new SocRequest(100, 1, 10000));
        intervals.add(scheduledTimeframeInterval);

        when(timeframeIntervalHandler.getQueue()).thenReturn(intervals);

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
        log("Active timeframe interval request empty");
        configureMocks(false, true, false, 50);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Disconnect vehicle");
        configureMocks(true, false, false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_NOT_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_completed()throws Exception {
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
        log("Request empty");
        configureMocks(false, true, false, 50);
        evCharger.setStopChargingRequested(true);
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
        log("Timeframe interval expired");
        configureMocks(false, true, false);
        updateState();
        evCharger.activeIntervalChanged(now, applianceId, timeframeInterval, null, false);
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
        when(evCharger.isWithinSwitchChargingStateDetectionDelay(false)).thenReturn(true);
        configureMocks(false, true, true);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
        log("Stop charging during ChargingStateDetectionDelay");
        evCharger.stopCharging();
        configureMocks(false, true, false);
        updateState();
        log("After ChargingStateDetectionDelay");
        when(evCharger.isWithinSwitchChargingStateDetectionDelay(false)).thenReturn(false);
        updateState();
        assertEquals(EVChargerState.VEHICLE_CONNECTED, evCharger.getState());
    }

    @Test
    public void updateState_error() {
        log("Start charging");
        evCharger.startCharging();
        configureMocks(false, true, true);
        updateState();
        when(evChargerControl.isInErrorState()).thenReturn(true);
        updateState();
        assertEquals(EVChargerState.ERROR, evCharger.getState());
        when(evChargerControl.isInErrorState()).thenReturn(false);
        updateState();
        assertEquals(EVChargerState.CHARGING, evCharger.getState());
    }

    @Test
    public void isWithinStartChargingStateDetectionDelay() {
        int startChargingStateDetectionDelay = 300;
        long currentMillis = 1000000;
        assertFalse(evCharger.isWithinSwitchChargingStateDetectionDelay(false,
                startChargingStateDetectionDelay, currentMillis, null));
        assertTrue(evCharger.isWithinSwitchChargingStateDetectionDelay(false,
                startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay - 1) * 1000));
        assertFalse(evCharger.isWithinSwitchChargingStateDetectionDelay(false,
                startChargingStateDetectionDelay, currentMillis, currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
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
        assertTrue(evCharger.isOn(
                startChargingStateDetectionDelay,
                currentMillis,
                currentMillis - (startChargingStateDetectionDelay + 1) * 1000));
        // state = CHARGING
        evCharger.setState(now, EVChargerState.VEHICLE_CONNECTED);
        evCharger.setState(now, EVChargerState.CHARGING);
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
