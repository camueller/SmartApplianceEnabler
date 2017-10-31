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

import de.avanux.smartapplianceenabler.HolidaysDownloader;
import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
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

    private Device2EM loadDevice2EMIfNeeded(boolean create) {
        if(this.device2EM == null) {
            FileHandler fileHandler = new FileHandler();
            this.device2EM = fileHandler.load(Device2EM.class);
            if(device2EM == null && create) {
                this.device2EM = new Device2EM();
            }
        }
        return this.device2EM;
    }

    private Appliances loadAppliancesIfNeeded(boolean create) {
        if(this.appliances == null) {
            FileHandler fileHandler = new FileHandler();
            this.appliances = fileHandler.load(Appliances.class);
            if(appliances == null) {
                this.appliances = new Appliances();
            }
        }
        return this.appliances;
    }

    private Appliance getAppliance(String applianceId) {
        for (Appliance appliance: loadAppliancesIfNeeded(false).getAppliances()) {
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
        identification.setDeviceSerial(applianceInfo.getSerial());

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
        List<ApplianceInfo> applianceInfos = new ArrayList<>();
        Device2EM device2EM = loadDevice2EMIfNeeded(false);
        if(device2EM != null) {
            for(DeviceInfo deviceInfo: device2EM.getDeviceInfo()) {
                applianceInfos.add(toApplianceInfo(deviceInfo));
            }
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
        Device2EM device2EM = loadDevice2EMIfNeeded(false);
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
        DeviceInfo deviceInfo = toDeviceInfo(applianceInfo);
        Device2EM device2EM = loadDevice2EMIfNeeded(true);
        if(create) {
            List<DeviceInfo> deviceInfos = device2EM.getDeviceInfo();
            if(deviceInfos == null) {
                deviceInfos = new ArrayList<>();
                device2EM.setDeviceInfo(deviceInfos);
            }
            deviceInfos.add(deviceInfo);

            Appliance appliance = new Appliance();
            appliance.setId(applianceId);

            Appliances appliances = loadAppliancesIfNeeded(true);
            List<Appliance> applianceList = appliances.getAppliances();
            if(applianceList == null) {
                applianceList = new ArrayList<>();
                appliances.setAppliances(applianceList);
            }
            applianceList.add(appliance);
        }
        else {
            Integer replaceIndex = null;
            for(int i=0;i<device2EM.getDeviceInfo().size();i++) {
                if(deviceInfo.getIdentification().getDeviceId().equals(device2EM.getDeviceInfo().get(i).getIdentification().getDeviceId())) {
                    replaceIndex = i;
                    break;
                }
            }
            if(replaceIndex != null) {
                device2EM.getDeviceInfo().remove(replaceIndex.intValue());
                device2EM.getDeviceInfo().add(replaceIndex, deviceInfo);
            }
        }
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void deleteAppliance(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to delete appliance with id=" + applianceId);

        Device2EM device2EM = loadDevice2EMIfNeeded(false);
        DeviceInfo deviceInfoToBeDeleted = null;
        for(DeviceInfo deviceInfo : device2EM.getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                deviceInfoToBeDeleted = deviceInfo;
                break;
            }
        }
        device2EM.getDeviceInfo().remove(deviceInfoToBeDeleted);

        Appliances appliances = loadAppliancesIfNeeded(false);
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
        Appliance appliance = getAppliance(applianceId);
        List<Control> controls = appliance.getControls();
        if(controls == null) {
            controls = new ArrayList<>();
            appliance.setControls(controls);
        }
        controls.clear();
        controls.add(control);
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
        getAppliance(applianceId).setMeter(meter);
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
        getAppliance(applianceId).setSchedules(schedules);
    }

    @RequestMapping(value=SCHEDULES_URL, method= RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void activateSchedules(@RequestParam(value="id") String applianceId, @RequestBody Schedules schedules) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        List<Schedule> schedulesToSet = schedules.getSchedules();
        applianceLogger.debug("Received request to set " + (schedulesToSet != null ? schedulesToSet.size() : "0") + " schedule(s)");
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        if(appliance != null) {
            if(appliance.getRunningTimeMonitor() != null) {
                appliance.getRunningTimeMonitor().setSchedules(schedulesToSet);
            }
            else {
                applianceLogger.error("Appliance has no RunningTimeMonitor");
            }
        }
        else {
            applianceLogger.error("Appliance not found");
        }
    }

    @RequestMapping(value= SETTINGS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public Settings getSettings() {
        logger.debug("Received request for Settings");
        Settings settings = new Settings();

        Appliances appliances = loadAppliancesIfNeeded(false);

        settings.setDefaultPulseReceiverPort(PulseReceiver.DEFAULT_PORT);
        settings.setDefaultModbusTcpHost(ModbusTcp.DEFAULT_HOST);
        settings.setDefaultModbusTcpPort(ModbusTcp.DEFAULT_PORT);
        settings.setDefaultHolidaysUrl(Configuration.DEFAULT_HOLIDAYS_URL);

        Connectivity connectivity = appliances.getConnectivity();
        if(connectivity != null) {
            List<PulseReceiver> pulseReceivers = connectivity.getPulseReceivers();
            if(pulseReceivers != null && pulseReceivers.size() > 0) {
                settings.setPulseReceiverEnabled(true);
                PulseReceiver pulseReceiver = pulseReceivers.get(0);
                settings.setPulseReceiverPort(pulseReceiver.getPort());
            }

            List<ModbusTcp> modbusTCPs = connectivity.getModbusTCPs();
            if(modbusTCPs != null && modbusTCPs.size() > 0) {
                settings.setModbusEnabled(true);
                ModbusTcp modbusTcp = modbusTCPs.get(0);
                settings.setModbusTcpHost(modbusTcp.getHost());
                settings.setModbusTcpPort(modbusTcp.getPort());
            }
        }

        String holidaysUrl = appliances.getConfigurationValue(HolidaysDownloader.urlConfigurationParamName);
        settings.setHolidaysEnabled(holidaysUrl != null);
        settings.setHolidaysUrl(holidaysUrl);

        logger.debug("Returning Settings " + settings);
        return settings;
    }

    @RequestMapping(value=SETTINGS_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setSettings(@RequestBody Settings settings) {
        logger.debug("Received request to set " + settings);

        List<PulseReceiver> pulseReceivers = null;
        if(settings.isPulseReceiverEnabled()) {
            PulseReceiver pulseReceiver = new PulseReceiver();
            pulseReceiver.setId("default");
            pulseReceiver.setPort(settings.getPulseReceiverPort());
            pulseReceivers = Collections.singletonList(pulseReceiver);
        }

        List<ModbusTcp> modbusTCPs = null;
        if(settings.isModbusEnabled()) {
            ModbusTcp modbusTcp = new ModbusTcp();
            modbusTcp.setHost(settings.getModbusTcpHost());
            modbusTcp.setPort(settings.getModbusTcpPort());
            modbusTCPs = Collections.singletonList(modbusTcp);
        }

        if(pulseReceivers != null || modbusTCPs != null) {
            Connectivity connectivity = loadAppliancesIfNeeded(true).getConnectivity();
            if(connectivity == null) {
                connectivity = new Connectivity();
                appliances.setConnectivity(connectivity);
            }
            connectivity.setPulseReceivers(pulseReceivers);
            connectivity.setModbusTCPs(modbusTCPs);
        }

        List<Configuration> configurations = null;
        if(settings.isHolidaysEnabled()) {
            Configuration configuration = new Configuration();
            configuration.setParam(HolidaysDownloader.urlConfigurationParamName);
            configuration.setValue(settings.getHolidaysUrl());
            configurations = Collections.singletonList(configuration);
        }
        this.appliances.setConfigurations(configurations);
    }
}
