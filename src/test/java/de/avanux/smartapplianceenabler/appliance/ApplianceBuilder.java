/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.MockSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.ev.*;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.Device2EM;
import de.avanux.smartapplianceenabler.semp.webservice.SempBuilder;
import de.avanux.smartapplianceenabler.semp.webservice.SempBuilderCall;
import java.time.LocalDateTime;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplianceBuilder {

    private Appliance appliance;
    private boolean initialized = false;
    private List<SempBuilderCall> sempBuilderCalls = new ArrayList<>();

    public ApplianceBuilder(String applianceId) {
        appliance = new Appliance();
        appliance.setId(applianceId);
    }

    public ApplianceBuilder withMockSwitch(boolean asStartingCurrentSwitch) {
        Control control = new MockSwitch();
        if(asStartingCurrentSwitch) {
            StartingCurrentSwitch startingCurrentSwitch = new StartingCurrentSwitch();
            startingCurrentSwitch.setControl(control);
            control = startingCurrentSwitch;
        }

        appliance.setControl(control);
        return this;
    }

    public ApplianceBuilder withEvCharger(EVChargerControl evChargerControl) {
        ElectricVehicleCharger evCharger = Mockito.spy(new ElectricVehicleCharger());
        evCharger.setSocScriptAsync(false);
        return withEvCharger(evCharger, evChargerControl);
    }

    public ApplianceBuilder withEvCharger(ElectricVehicleCharger evCharger, EVChargerControl evChargerControl) {
        evCharger.setStartChargingStateDetectionDelay(0);
        evCharger.setControl(evChargerControl);
        appliance.setControl(evCharger);
        return this;
    }

    public ApplianceBuilder withElectricVehicle(Integer evId, Integer batteryCapacity) {
        return withElectricVehicle(evId, batteryCapacity, null, null);
    }

    public ApplianceBuilder withElectricVehicle(Integer evId, Integer batteryCapacity, SocScript socScript) {
        return withElectricVehicle(evId, batteryCapacity, null, socScript);
    }

    public ApplianceBuilder withElectricVehicle(Integer evId, Integer batteryCapacity, Integer defaultSocOptionalEnergy, SocScript socScript) {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setId(evId);
        vehicle.setBatteryCapacity(batteryCapacity);
        vehicle.setDefaultSocOptionalEnergy(defaultSocOptionalEnergy);
        vehicle.setSocScript(socScript);
        addVehicle(vehicle);
        return this;
    }

    private void addVehicle(ElectricVehicle vehicle) {
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
        List<ElectricVehicle> vehicles = evCharger.getVehicles();
        if(vehicles == null) {
            vehicles = new ArrayList<>();
            evCharger.setVehicles(vehicles);
        }
        vehicles.add(vehicle);
    }

    public ApplianceBuilder withMockMeter() {
        return this.withMeter(Mockito.mock(Meter.class));
    }

    public ApplianceBuilder withMeter(Meter meter) {
        appliance.setMeter(meter);
        return this;
    }

    public ApplianceBuilder withSchedule(int startHour, int startMinute, int endHour, int endMinute,
                                         Integer minRunningTime, int maxRunningTime) {
        Schedule schedule = new Schedule(minRunningTime, maxRunningTime, new TimeOfDay(startHour, startMinute, 0),
                new TimeOfDay(endHour, endMinute, 0));
        List<Schedule> schedules = appliance.getSchedules();
        if(schedules == null) {
            schedules = new ArrayList<>();
            appliance.setSchedules(schedules);
        }
        schedules.add(schedule);
        return this;
    }

    public ApplianceBuilder withRuntimeRequest(LocalDateTime now, LocalDateTime intervalStart, LocalDateTime intervalEnd,
                                               Integer min, Integer max, boolean enabled) {
        Interval interval = new Interval(intervalStart, intervalEnd);
        RuntimeRequest request = new RuntimeRequest(min, max);
        request.setEnabled(enabled);
        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);
        getTimeframeIntervalHandler().addTimeframeInterval(now, timeframeInterval, false, true);
        return this;
    }

    public ApplianceBuilder withSocRequest(LocalDateTime now, Interval interval,
                                           Integer evId, Integer batteryCapacity, Integer soc, boolean enabled) {
        SocRequest request = new SocRequest(soc, evId);
        request.setEnabled(enabled);
        TimeframeInterval timeframeInterval = new TimeframeInterval(interval, request);
        getTimeframeIntervalHandler().addTimeframeInterval(now, timeframeInterval, false, true);
        return this;
    }

    public ApplianceBuilder withSempBuilderOperation(SempBuilderCall call) {
        sempBuilderCalls.add(call);
        return this;
    }

    public static void init(List<Appliance> applianceList, List<SempBuilderCall> sempBuilderCalls) {
        Appliances appliances = new Appliances();
        appliances.setAppliances(applianceList);
        ApplianceManager.getInstanceWithoutTimer().setAppliances(appliances);

        SempBuilder sempBuilder = new SempBuilder(appliances);
        if(sempBuilderCalls != null) {
            sempBuilderCalls.forEach(sempBuilderOperation -> sempBuilderOperation.call(sempBuilder));
        }
        Device2EM device2EM = sempBuilder.build();
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);

        ApplianceManager.getInstanceWithoutTimer().init();
    }

    public ApplianceBuilder init() {
        ApplianceBuilder.init(Collections.singletonList(appliance), sempBuilderCalls);
        initialized = true;
        return this;
    }

    public Appliance build(boolean init) {
        if(init) {
            ApplianceBuilder.init(Collections.singletonList(appliance), sempBuilderCalls);
        }
        return appliance;
    }

    private TimeframeIntervalHandler getTimeframeIntervalHandler() {
        if(! initialized) {
            init();
        }
        return appliance.getTimeframeIntervalHandler();
    }
}
