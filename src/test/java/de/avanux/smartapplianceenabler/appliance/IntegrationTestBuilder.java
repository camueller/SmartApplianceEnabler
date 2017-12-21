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

package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.MockSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitch;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.semp.webservice.Characteristics;
import de.avanux.smartapplianceenabler.semp.webservice.Device2EM;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.semp.webservice.Identification;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by axel on 18.12.17.
 */
public class IntegrationTestBuilder {

    private Appliances appliances = new Appliances();
    private Device2EM device2EM = new Device2EM();

    public IntegrationTestBuilder appliance(String applianceId) {
        Appliance appliance = new Appliance();
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

    public IntegrationTestBuilder withMockSwitch(boolean asStartingCurrentSwitch) {
        Control control = new MockSwitch();
        if(asStartingCurrentSwitch) {
            StartingCurrentSwitch startingCurrentSwitch = new StartingCurrentSwitch();
            startingCurrentSwitch.setControl(control);
            control = startingCurrentSwitch;
        }

        getAppliance().setControl(control);
        return this;
    }

    public IntegrationTestBuilder withMockMeter() {
        Meter meter = Mockito.mock(Meter.class);
        getAppliance().setMeter(meter);
        return this;
    }

    public IntegrationTestBuilder withSchedule(int startHour, int startMinute, int endHour, int endMinute,
                                               int minRunningTime, Integer maxRunningTime) {
        List<Schedule> schedules = getAppliance().getSchedules();
        if(schedules == null) {
            schedules = new ArrayList<>();
            getAppliance().setSchedules(schedules);
        }
        schedules.add(new Schedule(minRunningTime, maxRunningTime, new TimeOfDay(startHour, startMinute, 0),
                new TimeOfDay(endHour, endMinute, 0)));
        return this;
    }

    public IntegrationTestBuilder init() {
        ApplianceManager.getInstanceWithoutTimer().setAppliances(appliances);
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);
        ApplianceManager.getInstanceWithoutTimer().init();
        return this;
    }

    public Appliance getAppliance() {
        return appliances.getAppliances().get(0);
    }

}
