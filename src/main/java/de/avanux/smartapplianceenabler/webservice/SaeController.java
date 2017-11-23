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
    private static final String BASE_URL = "/sae";
    private static final String APPLIANCES_URL = BASE_URL + "/appliances";
    private static final String APPLIANCE_URL = BASE_URL + "/appliance";
    private static final String CONTROL_URL = BASE_URL + "/control";
    private static final String METER_URL = BASE_URL + "/meter";
    public static final String SCHEDULES_URL = BASE_URL + "/schedules";
    private static final String SETTINGS_URL = BASE_URL + "/settings";
    // only required for development if running via "ng serve"
    private static final String CROSS_ORIGIN_URL = "http://localhost:4200";
    private Logger logger = LoggerFactory.getLogger(SaeController.class);

    public SaeController() {
        logger.info("SAE controller created.");
    }

    private Appliance getAppliance(String applianceId) {
        for (Appliance appliance: ApplianceManager.getInstance().getAppliances()) {
            if(appliance.getId().equals(applianceId)) {
                return appliance;
            }
        }
        return null;
    }

    /**
     * Return the corresponding DeviceInfo for an appliance.
     * @param applianceId
     * @return
     */
    private DeviceInfo getDeviceInfo(String applianceId) {
        for(DeviceInfo deviceInfo : ApplianceManager.getInstance().getDevice2EM().getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                return deviceInfo;
            }
        }
        return null;
    }

    private ApplianceHeader toApplianceHeader(Appliance appliance, DeviceInfo deviceInfo) {
        ApplianceHeader header = new ApplianceHeader();
        header.setId(deviceInfo.getIdentification().getDeviceId());
        header.setName(deviceInfo.getIdentification().getDeviceName());
        header.setVendor(deviceInfo.getIdentification().getDeviceVendor());
        header.setType(deviceInfo.getIdentification().getDeviceType());
        header.setControllable(appliance.isControllable());
        return header;
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
    public List<ApplianceHeader> getAppliances() {
        logger.debug("Received request for ApplianceHeaders");
        List<ApplianceHeader> applianceHeaders = new ArrayList<>();
            List<Appliance> applianceList = ApplianceManager.getInstance().getAppliances();
            Device2EM device2EM = ApplianceManager.getInstance().getDevice2EM();
            if(applianceList != null && device2EM != null) {
                for(Appliance appliance: applianceList) {
                    DeviceInfo deviceInfo = getDeviceInfo(appliance.getId());
                    applianceHeaders.add(toApplianceHeader(appliance, deviceInfo));
                }
            }
        logger.debug("Returning " + applianceHeaders.size() + " ApplianceHeaders");
        return applianceHeaders;
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public ApplianceInfo getApplianceInfo(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for device info");
        Device2EM device2EM = ApplianceManager.getInstance().getDevice2EM();
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
        if(create) {
            Appliance appliance = new Appliance();
            appliance.setId(applianceId);
            ApplianceManager.getInstance().addAppliance(appliance, deviceInfo);
        }
        else {
            ApplianceManager.getInstance().updateAppliance(deviceInfo);
        }
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void deleteAppliance(@RequestParam(value="id") String applianceId) {
        ApplianceManager.getInstance().deleteAppliance(applianceId);
    }

    @RequestMapping(value=CONTROL_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public Control getControl(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for control");
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            Control control = appliance.getControl();
            applianceLogger.debug("Returning control " + control);
            return control;
        }
        else {
            applianceLogger.error("Appliance not found");
        }
        return null;
    }

    @RequestMapping(value=CONTROL_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setControl(@RequestParam(value="id") String applianceId, @RequestBody Control control) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set control " + control);
        ApplianceManager.getInstance().setControl(applianceId, control);
    }

    @RequestMapping(value= CONTROL_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void deleteControl(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to delete control");
        ApplianceManager.getInstance().deleteControl(applianceId);
    }

    @RequestMapping(value=METER_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public Meter getMeter(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for meter");
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            Meter meter = appliance.getMeter();
            applianceLogger.debug("Returning meter " + meter);
            return meter;
        }
        else {
            applianceLogger.error("Appliance not found");
        }
        return null;
    }

    @RequestMapping(value=METER_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setMeter(@RequestParam(value="id") String applianceId, @RequestBody Meter meter) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set meter " + meter);
        ApplianceManager.getInstance().setMeter(applianceId, meter);
    }

    @RequestMapping(value= METER_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void deleteMeter(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to delete meter");
        ApplianceManager.getInstance().deleteMeter(applianceId);
    }

    @RequestMapping(value=SCHEDULES_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public List<Schedule> getSchedules(@RequestParam(value="id") String applianceId) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request for schedules");
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            List<Schedule> schedules = appliance.getSchedules();
            if(schedules == null) {
                schedules = new ArrayList<>();
            }
            applianceLogger.debug("Returning " + schedules.size() + " schedules");
            return schedules;
        }
        else {
            applianceLogger.error("Appliance not found");
        }
        return null;
    }

    @RequestMapping(value=SCHEDULES_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    @ResponseBody
    public void setSchedules(@RequestParam(value="id") String applianceId, @RequestBody List<Schedule> schedules) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        applianceLogger.debug("Received request to set " + schedules.size() + " schedules");
        ApplianceManager.getInstance().setSchedules(applianceId, schedules);
    }

    @RequestMapping(value=SCHEDULES_URL, method= RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void activateSchedules(@RequestParam(value="id") String applianceId, @RequestBody Schedules schedules) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        List<Schedule> schedulesToSet = schedules.getSchedules();
        applianceLogger.debug("Received request to activate " + (schedulesToSet != null ? schedulesToSet.size() : "0") + " schedule(s)");
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

        Appliances appliances = ApplianceManager.getInstance().getAppliancesRoot();

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
            Connectivity connectivity = new Connectivity();
            connectivity.setPulseReceivers(pulseReceivers);
            connectivity.setModbusTCPs(modbusTCPs);
            ApplianceManager.getInstance().setConnectivity(connectivity);
        }

        List<Configuration> configurations = null;
        if(settings.isHolidaysEnabled()) {
            Configuration configuration = new Configuration();
            configuration.setParam(HolidaysDownloader.urlConfigurationParamName);
            configuration.setValue(settings.getHolidaysUrl());
            configurations = Collections.singletonList(configuration);
        }
        ApplianceManager.getInstance().setConfiguration(configurations);
    }
}
