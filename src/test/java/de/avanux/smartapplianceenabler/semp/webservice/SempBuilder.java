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

package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.appliance.Appliances;

import java.util.ArrayList;
import java.util.List;

public class SempBuilder {

    private Device2EM device2EM = new Device2EM();

    public SempBuilder(Appliances appliances) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        appliances.getAppliances().forEach(appliance -> {
            deviceInfos.add(buildDeviceInfo(buildIdentification(appliance.getId())));
        });
        device2EM.setDeviceInfo(deviceInfos);
    }

    public Device2EM build() {
        return device2EM;
    }

    public SempBuilder withMaxPowerConsumption(String deviceId, int maxPowerConsumption) {
        device2EM.getDeviceInfo().stream()
                .filter(deviceInfo -> deviceInfo.getIdentification().getDeviceId().equals(deviceId))
                .forEach(deviceInfo -> getOrBuildCharacteristics(deviceInfo).setMaxPowerConsumption(maxPowerConsumption));
        return this;
    }

    private Characteristics getOrBuildCharacteristics(DeviceInfo deviceInfo) {
        Characteristics characteristics = deviceInfo.getCharacteristics();
        if(characteristics == null) {
            characteristics = new Characteristics();
            deviceInfo.setCharacteristics(characteristics);
        }
        return characteristics;
    }

    private Identification buildIdentification(String deviceId) {
        Identification identification = new Identification();
        identification.setDeviceId(deviceId);
        return identification;
    }

    private DeviceInfo buildDeviceInfo(Identification identification) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        return deviceInfo;
    }
}
