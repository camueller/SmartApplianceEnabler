/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.test;

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.Appliances;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.MockSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.control.ev.EVControl;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.control.ev.SocScript;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.Characteristics;
import de.avanux.smartapplianceenabler.semp.webservice.Device2EM;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.semp.webservice.Identification;
import de.avanux.smartapplianceenabler.util.DateTimeProvider;
import org.joda.time.LocalDateTime;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class TestBuilder {

    private Appliances appliances = new Appliances();
    private Device2EM device2EM = new Device2EM();

    public TestBuilder appliance(String applianceId, DateTimeProvider dateTimeProvider, LocalDateTime now) {
        Appliance appliance = new Appliance();
        appliance.setDateTimeProvider(dateTimeProvider);
        if(dateTimeProvider != null && now != null) {
            Mockito.when(dateTimeProvider.now()).thenReturn(now);
        }

        appliance.setId(applianceId);
        appliances.setAppliances(Collections.singletonList(appliance));

        Identification identification = new Identification();
        identification.setDeviceId(applianceId);

        Characteristics characteristics = new Characteristics();
        characteristics.setMaxPowerConsumption(1000);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        deviceInfo.setCharacteristics(characteristics);

        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));
        return this;
    }

    public TestBuilder withMockSwitch(boolean asStartingCurrentSwitch) {
        Control control = new MockSwitch();
        if(asStartingCurrentSwitch) {
            StartingCurrentSwitch startingCurrentSwitch = new StartingCurrentSwitch();
            startingCurrentSwitch.setControl(control);
            control = startingCurrentSwitch;
        }

        getAppliance().setControl(control);
        return this;
    }

    public TestBuilder withEvCharger(EVControl evControl) {
        ElectricVehicleCharger evCharger = new ElectricVehicleCharger();
        evCharger.setStartChargingStateDetectionDelay(0);
        evCharger.setControl(evControl);
        getAppliance().setControl(evCharger);
        return this;
    }

    public TestBuilder withElectricVehicle(Integer evId, Integer batteryCapacity) {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setId(evId);
        vehicle.setBatteryCapacity(batteryCapacity);
        addVehicle(vehicle);
        return this;
    }

    public TestBuilder withElectricVehicle(Integer evId, Integer batteryCapacity, Integer defaultSocOptionalEnergy, SocScript socScript) {
        ElectricVehicle vehicle = new ElectricVehicle();
        vehicle.setId(evId);
        vehicle.setBatteryCapacity(batteryCapacity);
        vehicle.setDefaultSocOptionalEnergy(defaultSocOptionalEnergy);
        vehicle.setSocScript(socScript);
        addVehicle(vehicle);
        return this;
    }

    private void addVehicle(ElectricVehicle vehicle) {
        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) getAppliance().getControl();
        List<ElectricVehicle> vehicles = evCharger.getVehicles();
        if(vehicles == null) {
            vehicles = new ArrayList<>();
            evCharger.setVehicles(vehicles);
        }
        vehicles.add(vehicle);
    }

    public TestBuilder withMockMeter() {
        return this.withMeter(Mockito.mock(Meter.class));
    }

    public TestBuilder withMeter(Meter meter) {
        getAppliance().setMeter(meter);
        return this;
    }

    public TestBuilder withSchedule(int startHour, int startMinute, int endHour, int endMinute,
                                    int minRunningTime, Integer maxRunningTime) {
        addSchedule(new Schedule(minRunningTime, maxRunningTime, new TimeOfDay(startHour, startMinute, 0),
                new TimeOfDay(endHour, endMinute, 0)));
        return this;
    }

    public TestBuilder withSchedule(int startHour, int startMinute, int endHour, int endMinute) {
        Schedule schedule = new Schedule(true, new DayTimeframe(
                new TimeOfDay(startHour, startMinute, 0),
                new TimeOfDay(endHour, endMinute, 0)),
                null);
        addSchedule(schedule);
        return this;
    }

    private void addSchedule(Schedule schedule) {
        List<Schedule> schedules = getAppliance().getSchedules();
        if(schedules == null) {
            schedules = new ArrayList<>();
            getAppliance().setSchedules(schedules);
        }
        schedules.add(schedule);
    }

    public TestBuilder withRuntimeRequest(Integer min, Integer max) {
        RuntimeRequest request = new RuntimeRequest();
        request.setMin(min);
        request.setMax(max);
        addRequest(request);
        return this;
    }

    public TestBuilder withSocRequest(Integer evId, Integer soc) {
        SocRequest request = new SocRequest();
        request.setEvId(evId);
        request.setSoc(soc);
        addRequest(request);
        return this;
    }

    private void addRequest(Request request) {
        Vector<Schedule> schedules = new Vector(getAppliance().getSchedules());
        schedules.lastElement().setRequest(request);
    }

    public TestBuilder init() {
        ApplianceManager.getInstanceWithoutTimer().setAppliances(appliances);
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);
        ApplianceManager.getInstanceWithoutTimer().init();
        return this;
    }

    public Appliance getAppliance() {
        return appliances.getAppliances().get(0);
    }

}
