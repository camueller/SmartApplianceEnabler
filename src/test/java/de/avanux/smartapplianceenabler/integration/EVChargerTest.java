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

package de.avanux.smartapplianceenabler.integration;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceBuilder;
import de.avanux.smartapplianceenabler.control.ev.EVChargerControl;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.control.ev.SocScript;
import de.avanux.smartapplianceenabler.control.ev.SocValues;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.meter.MockElectricityMeter;
import de.avanux.smartapplianceenabler.meter.PollEnergyExecutor;
import de.avanux.smartapplianceenabler.schedule.Interval;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalHandler;
import de.avanux.smartapplianceenabler.schedule.TimeframeIntervalState;
import de.avanux.smartapplianceenabler.util.DateTimeProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EVChargerTest extends TestBase {

    private Logger logger = LoggerFactory.getLogger(EVChargerTest.class);
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private DateTimeProvider dateTimeProvider = mock(DateTimeProvider.class);
    private EVChargerControl evChargerControl = mock(EVChargerControl.class);
    private Meter meter = mock(Meter.class);
    private MockElectricityMeter mockMeter = spy(new MockElectricityMeter());
    private PollEnergyExecutor pollEnergyExecutor = mock(PollEnergyExecutor.class);
    private SocScript socScript = mock(SocScript.class);
    private String applianceId = "F-001";
    private Integer evId = 1;
    private Integer batteryCapacity = 40000;
    private Integer defaultSocOptionalEnergy = 100;

    @Test
    public void optionalEnergyRequest() {
//        LocalDateTime timeInitial = toToday(5, 50, 0);
//
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity)
//                .withMeter(mockMeter)
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(5, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        Interval interval = new Interval(timeVehicleConnected,
//                timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        tick(appliance, timeVehicleConnected, true, false, 10.0);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeStartCharging = toToday(6, 0, 0);
//        log("Start charging", timeStartCharging);
//        appliance.setApplianceState(timeStartCharging,
//                true, 5500, "Switch on");
//        tick(appliance, timeStartCharging, true, true);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter).startEnergyMeter();
//        assertEquals(0.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeAfterStartCharging = toToday(7, 0, 0);
//        log("After start charging", timeAfterStartCharging);
//        tick(appliance, timeAfterStartCharging, true, true, 15.5);
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 13, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeInterruptCharging = toToday(8, 0, 0);
//        log("After interrupt charging", timeInterruptCharging);
//        tick(appliance, timeInterruptCharging,  true, false, 21.0, null,
//                () -> appliance.setApplianceState(timeInterruptCharging, false, null, "Switch off"));
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 25, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter).stopEnergyMeter();
//        Mockito.verify(mockMeter, never()).resetEnergyMeter();
//        assertEquals(11.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeStartChargingAgain = toToday(9, 0, 0);
//        Mockito.reset(mockMeter);
//        log("Start charging again", timeStartChargingAgain);
//        tick(appliance, timeStartChargingAgain, true, true, null, null,
//                () -> appliance.setApplianceState(timeStartChargingAgain, true, 6000, "Switch on"));
//        Mockito.verify(mockMeter).startEnergyMeter();
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 25, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertEquals(11.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeAfterStartChargingAgain = toToday(14, 0, 0);
//        log("After start charging again", timeAfterStartChargingAgain);
//        tick(appliance, timeAfterStartChargingAgain, true, true, 37.5);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 63, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertEquals(27.5f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeManualStartTimeframeIntervalExpired = toToday(14, 0, 0);
//        log("Timeframe interval expired", timeManualStartTimeframeIntervalExpired);
//        tick(appliance, timeManualStartTimeframeIntervalExpired, true, false, 54.0);
//        assertTrue(evCharger.isChargingCompleted());
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//        assertEquals(44.0f, mockMeter.getEnergy(), 0.01);
    }

//    @Test
//    public void optionalEnergyRequest_externalControl() {
//        LocalDateTime timeInitial = toToday(5, 50, 0);
//
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity)
//                .withMeter(mockMeter)
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(5, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        Interval interval = new Interval(timeVehicleConnected,
//                timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        tick(appliance, timeVehicleConnected, true, false, 10.0);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeStartCharging = toToday(6, 0, 0);
//        log("Start charging", timeStartCharging);
//        appliance.setApplianceState(timeStartCharging,
//                true, 5500, "Switch on");
//        tick(appliance, timeStartCharging, true, true);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter).startEnergyMeter();
//        assertEquals(0.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeAfterStartCharging = toToday(7, 0, 0);
//        log("After start charging", timeAfterStartCharging);
//        tick(appliance, timeAfterStartCharging, true, true, 15.5);
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 13, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeInterruptCharging = toToday(8, 0, 0);
//        log("After external interrupt charging", timeInterruptCharging);
//        tick(appliance, timeInterruptCharging,  true, false, 21.0);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 25, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter, never()).resetEnergyMeter();
//        assertEquals(11.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeStartChargingAgain = toToday(9, 0, 0);
//        log("Start external resume charging", timeStartChargingAgain);
//        tick(appliance, timeStartChargingAgain, true, true);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 25, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertEquals(11.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeAfterStartChargingAgain = toToday(14, 0, 0);
//        log("After external resume charging", timeAfterStartChargingAgain);
//        tick(appliance, timeAfterStartChargingAgain, true, true, 37.5);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 63, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertEquals(27.5f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeManualStartTimeframeIntervalExpired = toToday(14, 0, 0);
//        log("Timeframe interval expired", timeManualStartTimeframeIntervalExpired);
//        tick(appliance, timeManualStartTimeframeIntervalExpired, true, false, 54.0);
//        assertTrue(evCharger.isChargingCompleted());
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//        assertEquals(44.0f, mockMeter.getEnergy(), 0.01);
//    }
//
//    @Test
//    public void optionalEnergyRequest_SocScript() {
//        defaultSocOptionalEnergy = 80;
//        int socInitial = 20;
//        int socCurrent = socInitial;
//        LocalDateTime timeInitial = toToday(5, 50, 0);
//
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity, defaultSocOptionalEnergy, socScript)
//                .withMeter(mockMeter)
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(5, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        Interval interval = new Interval(timeVehicleConnected,
//                timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        tick(appliance, timeVehicleConnected, true, false, 10.0, socCurrent);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeStartCharging = toToday(6, 0, 0);
//        log("Start charging", timeStartCharging);
//        appliance.setApplianceState(timeStartCharging,
//                true, 5500, "Switch on");
//        tick(appliance, timeStartCharging, true, true);
//        assertEquals(10.0, evCharger.getChargeLoss());
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter).startEnergyMeter();
//        assertEquals(0.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeAfterStartCharging = toToday(7, 0, 0);
//        log("After start charging", timeAfterStartCharging);
//        socCurrent = 32;
//        tick(appliance, timeAfterStartCharging, true, true, 15.5, socCurrent);
//        assertEquals(14.6, evCharger.getChargeLoss(), 0.1);
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeInterruptCharging = toToday(8, 0, 0);
//        log("After interrupt charging", timeInterruptCharging);
//        socCurrent = 44;
//        tick(appliance, timeInterruptCharging, true, false, 21.0, socCurrent,
//                () -> appliance.setApplianceState(timeInterruptCharging, false, null, "Switch off"));
//        assertEquals(14.6, evCharger.getChargeLoss(), 0.1);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter).stopEnergyMeter();
//        Mockito.verify(mockMeter, never()).resetEnergyMeter();
//        assertEquals(11.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeStartChargingAgain = toToday(9, 0, 0);
//        log("Start charging again", timeStartChargingAgain);
//        appliance.setApplianceState(timeStartChargingAgain,
//                true, 6000, "Switch on");
//        tick(appliance, timeStartChargingAgain, true, true);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertEquals(11.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeAfterStartChargingAgain = toToday(14, 0, 0);
//        log("After start charging again", timeAfterStartChargingAgain);
//        socCurrent = 75;
//        tick(appliance, timeAfterStartChargingAgain, true, true, 35.0, socCurrent);
//        assertEquals(12.9, evCharger.getChargeLoss(), 0.1);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertEquals(25.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeManualStartTimeframeIntervalExpired = toToday(15, 0, 0);
//        log("Timeframe interval expired", timeManualStartTimeframeIntervalExpired);
//        socCurrent = 80;
//        tick(appliance, timeManualStartTimeframeIntervalExpired, true, false, 37.3, socCurrent);
//        assertEquals(15.0, evCharger.getChargeLoss(), 0.1);
//        assertTrue(evCharger.isChargingCompleted());
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//        assertEquals(27.3f, mockMeter.getEnergy(), 0.01);
//    }
//
//    @Test
//    public void optionalEnergyRequest_prolongation() {
//        LocalDateTime timeInitial = toToday(9, 50, 0);
//
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity)
//                .withMeter(mockMeter)
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        tick(appliance, timeInitial, false, false);
//        log("Vehicle not connected", timeInitial);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(9, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        Interval interval = new Interval(timeVehicleConnected,
//                timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(0.0);
//        tick(appliance, timeVehicleConnected, true, false);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeAfterTimeframeProlongation = toDay(2, 9, 56, 0);
//        log("After timeframe prolongation with maxEnergy > 0", timeAfterTimeframeProlongation);
//        interval = new Interval(timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS).plusSeconds(1),
//                timeVehicleConnected.plusDays(2 * TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS).plusMinutes(1));
//        Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(0.0);
//        tick(appliance, timeAfterTimeframeProlongation, true, false);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeStartCharging = toDay(2, 10, 0, 0);
//        log("Start charging", timeStartCharging);
//        appliance.setApplianceState(timeStartCharging,
//                true, 10000, "Switch on");
//        tick(appliance, timeStartCharging, true, true);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        Mockito.verify(mockMeter).startEnergyMeter();
//        assertEquals(0.0f, mockMeter.getEnergy(), 0.01);
//
//        LocalDateTime timeManualStartTimeframeIntervalExpired = toToday(15, 0, 0);
//        log("Timeframe interval expired", timeManualStartTimeframeIntervalExpired);
//        Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(44.0);
//        tick(appliance, timeManualStartTimeframeIntervalExpired, true, false);
//        assertTrue(evCharger.isChargingCompleted());
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//    }
//
//    @Test
//    public void optionalEnergyRequest_disconnect() {
//        LocalDateTime timeInitial = toToday(9, 50, 0);
//
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity)
//                .withMeter(mockMeter)
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(9, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        Interval interval = new Interval(timeVehicleConnected,
//                timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(0.0);
//        tick(appliance, timeVehicleConnected, true, false);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeVehicleDisconnected = toToday(11, 0, 0);
//        log("Vehicle disconnected", timeVehicleDisconnected);
//        tick(appliance, timeVehicleDisconnected, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnectedAgain = toToday(12, 0, 0);
//        log("Vehicle connected again", timeVehicleConnectedAgain);
//        interval = new Interval(timeVehicleConnectedAgain,
//                timeVehicleConnectedAgain.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        tick(appliance, timeVehicleConnectedAgain, true, false);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(interval, TimeframeIntervalState.ACTIVE,
//                0, 0, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//    }
//
//    @Test
//    public void optionalEnergyRequest_SocScript_MaxSocLTInitialSoc() {
//        LocalDateTime timeInitial = toToday(9, 50, 0);
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity, 80, socScript)
//                .withMeter(meter)
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(9, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        tick(appliance, timeVehicleConnected, true, false, null, 90, null);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//    }
//
//    @Test
//    public void daytimeframeSocRequest() {
//        int socInitial = 50;
//        int socTarget = 60;
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Interval interval = new Interval(toToday(10, 0, 0),
//                toToday(16, 0, 0));
//        LocalDateTime timeInitial = toToday(9, 50, 0);
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity)
//                .withMeter(mockMeter)
//                .withSocRequest(timeInitial, interval, evId, batteryCapacity, socTarget, false)
//                .build(false);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.QUEUED, interval,
//                0, 0, socTarget, evId, null, false,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeVehicleConnected = toToday(9, 55, 0);
//        log("After vehicle has been connected", timeVehicleConnected);
//        Interval optionalEnergyInterval = new Interval(timeVehicleConnected,
//                toToday(9, 59, 59));
//        tick(appliance, timeVehicleConnected, true, false, socInitial, socInitial);
//        assertTimeframeIntervalOptionalEnergy(optionalEnergyInterval, TimeframeIntervalState.ACTIVE,
//                socInitial, socInitial, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.QUEUED, interval,
//                socInitial, socInitial, socTarget, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(1));
//
//        LocalDateTime timeStartCharging = toToday(10, 0, 0);
//        log("Start charging", timeStartCharging);
//        optionalEnergyInterval = new Interval(interval.getEnd().plusSeconds(1), interval.getEnd().plusDays(2).plusSeconds(1));
//        tick(appliance, timeInitial, true, false, socInitial, socInitial);
//        appliance.setApplianceState(timeStartCharging,
//                true, 4000, "Switch on");
//        tick(appliance, timeStartCharging, true, true, socInitial, socInitial);
//        assertEquals(2, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.ACTIVE, interval,
//                socInitial, socInitial, socTarget, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertTimeframeIntervalOptionalEnergy(optionalEnergyInterval, TimeframeIntervalState.QUEUED,
//                socInitial, socInitial, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(1));
//
//        LocalDateTime timeSOCReached = toToday(11, 0, 0);
//        log("Requested SOC reached", timeSOCReached);
//        tick(appliance, timeSOCReached, true, true, 4.4);
//        assertEquals(2, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.EXPIRED, interval,
//                socInitial, socTarget, socTarget, evId, batteryCapacity, false,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertTimeframeIntervalOptionalEnergy(optionalEnergyInterval, TimeframeIntervalState.QUEUED,
//                socInitial, socTarget, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(1));
//
//        LocalDateTime timeSwitchOff = toToday(11, 5, 0);
//        log("Switch off", timeSwitchOff);
//        appliance.setApplianceState(timeSwitchOff,
//                false, null, "Switch off");
//        tick(appliance, timeSwitchOff, true, true, 4.4);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(optionalEnergyInterval, TimeframeIntervalState.QUEUED,
//                socInitial, socTarget, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//    }
//
//    @Test
//    public void daytimeframeSocRequest_RequestedSocGTInitialSoc() {
//        int socInitial = 50;
//        int socTarget = 60;
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Interval interval = new Interval(toToday(10, 0, 0),
//                toToday(16, 0, 0));
//        LocalDateTime timeInitial = toToday(11, 0, 0);
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity)
//                .withMeter(mockMeter)
//                .withSocRequest(timeInitial, interval, evId, batteryCapacity, socTarget, true)
//                .build(false);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.ACTIVE, interval,
//                0, 0, socTarget, evId, null, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeVehicleConnected = toToday(12, 0, 0);
//        log("After vehicle has been connected", timeVehicleConnected);
//        tick(appliance, timeVehicleConnected, true, false, socInitial, socInitial);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.ACTIVE, interval,
//                socInitial, socInitial, socTarget, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//    }
//
//    @Test
//    public void manualStart() {
//        int socInitial = 40;
//        int socCurrent = socInitial;
//        int socTarget = 50;
//        LocalDateTime timeInitial = toToday(9, 50, 0);
//
//        mockMeter.setApplianceId(applianceId);
//        mockMeter.getPollEnergyMeter().setPollEnergyExecutor(pollEnergyExecutor);
//
//        Appliance appliance = new ApplianceBuilder(applianceId)
//                .withEvCharger(evChargerControl)
//                .withElectricVehicle(evId, batteryCapacity, null, socScript)
//                .withMeter(mockMeter)
//                .withSempBuilderOperation(sempBuilder -> sempBuilder.withMaxPowerConsumption(applianceId, 4000))
//                .build(true);
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//
//        log("Vehicle not connected", timeInitial);
//        tick(appliance, timeInitial, false, false);
//        assertEquals(0, timeframeIntervalHandler.getQueue().size());
//
//        LocalDateTime timeVehicleConnected = toToday(9, 55, 0);
//        log("Vehicle connected", timeVehicleConnected);
//        Interval intervalOptionalEnergy = new Interval(timeVehicleConnected,
//                timeVehicleConnected.plusDays(TimeframeIntervalHandler.CONSIDERATION_INTERVAL_DAYS));
//        tick(appliance, timeVehicleConnected, true, false, 10.0, socCurrent);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(intervalOptionalEnergy, TimeframeIntervalState.ACTIVE,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//
//        LocalDateTime timeManualStart = toToday(11, 0, 0);
//        log("Manual start", timeManualStart);
//        Interval interval = new Interval(timeManualStart, toToday(12, 0, 0));
//        Interval intervalOptionalEnergyAdjusted = new Interval(toToday(12, 0, 1),
//                intervalOptionalEnergy.getEnd());
//        appliance.setEnergyDemand(timeManualStart, evId, socInitial, socTarget, null);
//        tick(appliance, timeManualStart, true, true);
//        assertEquals(2, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.ACTIVE, interval,
//                socInitial, socCurrent, socTarget, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertTimeframeIntervalOptionalEnergy(intervalOptionalEnergyAdjusted, TimeframeIntervalState.QUEUED,
//                socInitial, socCurrent, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(1));
//
//        LocalDateTime timeAfterManualStart = toToday(12, 0, 0);
//        log("Manual start - charging completed", timeAfterManualStart);
//        tick(appliance, timeAfterManualStart, true, true, 14.4, socTarget);
//        assertEquals(2, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalSocRequest(TimeframeIntervalState.EXPIRED, interval,
//                socInitial, socTarget, socTarget, evId, batteryCapacity, false,
//                timeframeIntervalHandler.getQueue().get(0));
//        assertTimeframeIntervalOptionalEnergy(intervalOptionalEnergyAdjusted, TimeframeIntervalState.QUEUED,
//                socInitial, socTarget, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(1));
//
//        LocalDateTime timeSwitchOff = toToday(11, 5, 0);
//        log("Switch off", timeSwitchOff);
//        appliance.setApplianceState(timeSwitchOff,
//                false, null, "Switch off");
//        tick(appliance, timeSwitchOff, true, false);
//        assertEquals(1, timeframeIntervalHandler.getQueue().size());
//        assertTimeframeIntervalOptionalEnergy(intervalOptionalEnergyAdjusted, TimeframeIntervalState.QUEUED,
//                socInitial, socTarget, defaultSocOptionalEnergy, evId, batteryCapacity, true,
//                timeframeIntervalHandler.getQueue().get(0));
//    }
//
//    private void tick(Appliance appliance, LocalDateTime now, boolean connected, boolean charging) {
//        tick(appliance, now, connected, charging, (Double) null, null);
//    }
//
//    private void tick(Appliance appliance, LocalDateTime now,
//                      boolean connected, boolean charging, Double pollEnergy) {
//        tick(appliance, now, connected, charging, pollEnergy, null);
//    }
//
//    private void tick(Appliance appliance, LocalDateTime now,
//                      boolean connected, boolean charging, Double pollEnergy, Integer socScriptResult) {
//        tick(appliance, now, connected, charging, pollEnergy, socScriptResult, null);
//    }
//
//    private void tick(Appliance appliance, LocalDateTime now,
//                      boolean connected, boolean charging, Double pollEnergy, Integer socScriptResult, Runnable runBeforeUpdate) {
//        tick(appliance, now, connected, charging, pollEnergy, null, null, socScriptResult, runBeforeUpdate);
//    }
//
//    private void tick(Appliance appliance, LocalDateTime now,
//                      boolean connected, boolean charging, Integer socInitial, Integer socCurrent) {
//        tick(appliance, now, connected, charging, null, socInitial, socCurrent, null, null);
//    }
//
//    private void tick(Appliance appliance, LocalDateTime now,
//                      boolean connected, boolean charging, Double pollEnergy,
//                      Integer socInitial, Integer socCurrent, Integer socScriptResult, Runnable runBeforeUpdate) {
//        Mockito.when(dateTimeProvider.now()).thenReturn(now);
//        Mockito.when(evChargerControl.isVehicleConnected()).thenReturn(connected);
//        Mockito.when(evChargerControl.isVehicleNotConnected()).thenReturn(!connected);
//        Mockito.when(evChargerControl.isCharging()).thenReturn(charging);
//        if(pollEnergy != null) {
//            Mockito.when(pollEnergyExecutor.pollEnergy(Mockito.any())).thenReturn(pollEnergy);
//        }
//        if(socScriptResult != null) {
//            Mockito.doReturn(Integer.valueOf(socScriptResult).doubleValue()).when(socScript).getStateOfCharge();
//        }
//        if(runBeforeUpdate != null) {
//            runBeforeUpdate.run();
//        }
//
//        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
//        if(socInitial != null) {
//            evCharger.getSocValues().initial = socInitial;
//        }
//        if(socCurrent != null) {
//            evCharger.getSocValues().current = socCurrent;
//        }
//        evCharger.updateStateTimerTaskImpl(now);
//
//        TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
//        timeframeIntervalHandler.updateQueue(now, false);
//    }
//
//    private void log(String message, LocalDateTime now) {
//        logger.debug("*********** " + message + " - " + dateTimeFormatter.format(now));
//    }
}
