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

package de.avanux.smartapplianceenabler.webservice;

import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SaeController {
    protected static final String BASE_URL = "/sae";
    protected static final String APPLIANCES_URL = BASE_URL + "/appliances";
    protected static final String APPLIANCE_URL = BASE_URL + "/appliance";
    protected static final String CONTROL_URL = BASE_URL + "/control";
    protected static final String METER_URL = BASE_URL + "/meter";
    protected static final String SCHEDULES_URL = BASE_URL + "/schedules";
    protected static final String SETTINGS_URL = BASE_URL + "/settings";
    // only required for development if running via "ng serve"
    public static final String CROSS_ORIGIN_URL = "http://localhost:4200";
    private Logger logger = LoggerFactory.getLogger(SaeController.class);
    private Device2EM device2EM;
    private Appliances appliances;

    public SaeController() {
        logger.info("SAE controller created.");
    }

    private Device2EM loadDevice2EMIfNeeded() {
        if(this.device2EM == null) {
            FileHandler fileHandler = new FileHandler();
            this.device2EM = fileHandler.load(Device2EM.class);
        }
        return this.device2EM;
    }

    private Appliances loadAppliancesIfNeeded() {
        if(this.appliances == null) {
            FileHandler fileHandler = new FileHandler();
            this.appliances = fileHandler.load(Appliances.class);
        }
        return this.appliances;
    }

    private Appliance getAppliance(String applianceId) {
        for (Appliance appliance: loadAppliancesIfNeeded().getAppliances()) {
            if(appliance.getId().equals(applianceId)) {
                return appliance;
            }
        }
        return null;
    }

    private ApplianceInfo toApplianceInfo(DeviceInfo deviceInfo) {
        ApplianceInfo applianceInfo = new ApplianceInfo();
        applianceInfo.setId(deviceInfo.getIdentification().getDeviceId());
        applianceInfo.setName(deviceInfo.getIdentification().getDeviceName());
        applianceInfo.setVendor(deviceInfo.getIdentification().getDeviceVendor());
        applianceInfo.setSerial(deviceInfo.getIdentification().getDeviceSerial());
        applianceInfo.setType(deviceInfo.getIdentification().getDeviceType());
        applianceInfo.setMaxPowerConsumption(deviceInfo.getCharacteristics().getMaxPowerConsumption());
        applianceInfo.setCurrentPowerMethod(deviceInfo.getCapabilities().getCurrentPowerMethod().name());
        applianceInfo.setInterruptionsAllowed(deviceInfo.getCapabilities().getInterruptionsAllowed());
        return applianceInfo;
    }

    private DeviceInfo toDeviceInfo(ApplianceInfo applianceInfo) {
        Identification identification = new Identification();
        identification.setDeviceId(applianceInfo.getId());
        identification.setDeviceName(applianceInfo.getName());
        identification.setDeviceVendor(applianceInfo.getVendor());
        identification.setDeviceType(applianceInfo.getType());

        Characteristics characteristics = new Characteristics();
        characteristics.setMaxPowerConsumption(applianceInfo.getMaxPowerConsumption());

        Capabilities capabilities = new Capabilities();
        capabilities.setCurrentPowerMethod(CurrentPowerMethod.valueOf(applianceInfo.getCurrentPowerMethod()));
        capabilities.setInterruptionsAllowed(applianceInfo.isInterruptionsAllowed());

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        deviceInfo.setCharacteristics(characteristics);
        deviceInfo.setCapabilities(capabilities);
        return deviceInfo;
    }

    @RequestMapping(value= APPLIANCES_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public List<ApplianceInfo> getAppliances() {
        logger.debug("Received request for ApplianceInfos");
        Device2EM device2EM = loadDevice2EMIfNeeded();
        List<ApplianceInfo> applianceInfos = new ArrayList<>();
        for(DeviceInfo deviceInfo: device2EM.getDeviceInfo()) {
            applianceInfos.add(toApplianceInfo(deviceInfo));
        }
        logger.debug("Returning " + applianceInfos.size() + " ApplianceInfos");
        return applianceInfos;
    }


    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public ApplianceInfo getApplianceInfo(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for device info");
        Device2EM device2EM = loadDevice2EMIfNeeded();
        for(DeviceInfo deviceInfo : device2EM.getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                applianceLogger.debug("Returning device info " + deviceInfo);
                return toApplianceInfo(deviceInfo);
            }
        }
        applianceLogger.error("No device info found.");
        return null;
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setApplianceInfo(@RequestParam(value="id") String applianceId, @RequestParam(value="create")
            Boolean create, @RequestBody ApplianceInfo applianceInfo) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set ApplianceInfo (create=" + create + "): " + applianceInfo);
        if(create) {
            DeviceInfo deviceInfo = toDeviceInfo(applianceInfo);
            Device2EM device2EM = loadDevice2EMIfNeeded();
            device2EM.getDeviceInfo().add(deviceInfo);

            Appliance appliance = new Appliance();
            appliance.setId(applianceId);

            Appliances appliances = loadAppliancesIfNeeded();
            appliances.getAppliances().add(appliance);
        }
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void deleteAppliance(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to delete appliance with id=" + applianceId);

        Device2EM device2EM = loadDevice2EMIfNeeded();
        DeviceInfo deviceInfoToBeDeleted = null;
        for(DeviceInfo deviceInfo : device2EM.getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                deviceInfoToBeDeleted = deviceInfo;
                break;
            }
        }
        device2EM.getDeviceInfo().remove(deviceInfoToBeDeleted);

        // TODO gleiche ID in anderen sections löschen

        Appliances appliances = loadAppliancesIfNeeded();
        Appliance applianceToBeDeleted = getAppliance(applianceId);
        appliances.getAppliances().remove(applianceToBeDeleted);
    }

    @RequestMapping(value=CONTROL_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public Control getControl(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for control");
        Appliance appliance = getAppliance(applianceId);
        List<Control> controls = appliance.getControls();
        if(controls != null && controls.size() > 0) {
            Control control = controls.get(0);
            applianceLogger.debug("Returning control " + control);
            return control;
        }
        return null;
    }

    @RequestMapping(value=CONTROL_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setControl(@RequestParam(value="id") String applianceId, @RequestBody Control control) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set control " + control);
    }

    @RequestMapping(value=METER_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public Meter getMeter(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for meter");
        Appliance appliance = getAppliance(applianceId);
        Meter meter = appliance.getMeter();
        applianceLogger.debug("Returning meter " + meter);
        return meter;
    }

    @RequestMapping(value=METER_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setMeter(@RequestParam(value="id") String applianceId, @RequestBody Meter meter) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set meter " + meter);
    }

    @RequestMapping(value=SCHEDULES_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public List<Schedule> getSchedules(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for schedules");
        Appliance appliance = getAppliance(applianceId);
        List<Schedule> schedules = appliance.getSchedules();
        if(schedules == null) {
            schedules = new ArrayList<>();
        }
        applianceLogger.debug("Returning " + schedules.size() + " schedules");
        return schedules;
    }

    @RequestMapping(value=SCHEDULES_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setSchedules(@RequestParam(value="id") String applianceId, @RequestBody List<Schedule> schedules) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set " + schedules.size() + " schedules");
    }

    @RequestMapping(value=SCHEDULES_URL, method= RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void setSchedules(@RequestParam(value="id") String applianceId, @RequestBody Schedules schedules) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        List<Schedule> schedulesToSet = schedules.getSchedules();
        applianceLogger.debug("Received request to set " + (schedulesToSet != null ? schedulesToSet.size() : "0") + " schedule(s)");
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        appliance.getRunningTimeMonitor().setSchedules(schedulesToSet);
    }

    @RequestMapping(value= SETTINGS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public Settings getSettings() {
        logger.debug("Received request for Settings");
        Settings settings = new Settings();

        settings.setDefaultPulseReceiverPort(PulseReceiver.DEFAULT_PORT);
        settings.setDefaultModbusTcpHost(ModbusTcp.DEFAULT_HOST);
        settings.setDefaultModbusTcpPort(ModbusTcp.DEFAULT_PORT);
        settings.setDefaultHolidaysUrl(Configuration.DEFAULT_HOLIDAYS_URL);

        Connectivity connectivity = loadAppliancesIfNeeded().getConnectivity();
        if(connectivity != null) {
            List<PulseReceiver> pulseReceivers = connectivity.getPulseReceivers();
            if(pulseReceivers != null && pulseReceivers.size() > 0) {
                settings.setPulseReceiverEnabled(true);
                PulseReceiver pulseReceiver = pulseReceivers.get(0);
                settings.setPulseReceiverPort(pulseReceiver.getPort());
            }
        }

        List<ModbusTcp> modbusTCPs = connectivity.getModbusTCPs();
        if(modbusTCPs != null && modbusTCPs.size() > 0) {
            settings.setModbusEnabled(true);
            ModbusTcp modbusTcp = modbusTCPs.get(0);
            settings.setModbusTcpHost(modbusTcp.getHost());
            settings.setModbusTcpPort(modbusTcp.getPort());
        }

        String holidaysUrl = loadAppliancesIfNeeded().getConfigurationValue("Holidays.Url");
        settings.setHolidaysEnabled(holidaysUrl != null);
        settings.setHolidaysUrl(holidaysUrl);

        logger.debug("Returning Settings " + settings);
        return settings;
    }

    @RequestMapping(value=SETTINGS_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setSettings(@RequestBody Settings settings) {
        logger.debug("Received request to set Settings " + settings);
    }
}
