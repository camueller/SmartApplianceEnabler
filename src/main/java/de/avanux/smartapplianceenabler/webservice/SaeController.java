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
import de.avanux.smartapplianceenabler.configuration.Configuration;
import de.avanux.smartapplianceenabler.configuration.Connectivity;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlDefaults;
import de.avanux.smartapplianceenabler.control.ModbusSwitch;
import de.avanux.smartapplianceenabler.control.StartingCurrentSwitchDefaults;
import de.avanux.smartapplianceenabler.meter.*;
import de.avanux.smartapplianceenabler.modbus.ModbusElectricityMeterDefaults;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.schedule.RuntimeRequest;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class SaeController {
    private static final String BASE_URL = "/sae";
    private static final String APPLIANCES_URL = BASE_URL + "/appliances";
    private static final String APPLIANCE_URL = BASE_URL + "/appliance";
    private static final String CONTROLDEFAULTS_URL = BASE_URL + "/controldefaults";
    private static final String CONTROL_URL = BASE_URL + "/control";
    private static final String METERDEFAULTS_URL = BASE_URL + "/meterdefaults";
    private static final String METER_URL = BASE_URL + "/meter";
    public static final String SCHEDULES_URL = BASE_URL + "/schedules";
    private static final String SETTINGS_URL = BASE_URL + "/settings";
    private static final String SETTINGSDEFAULTS_URL = BASE_URL + "/settingsdefaults";
    private static final String STATUS_URL = BASE_URL + "/status";
    private static final String RUNTIME_URL = BASE_URL + "/runtime";
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
    public ApplianceInfo getApplianceInfo(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        logger.debug("{}: Received request for ApplianceInfo", applianceId);
        Device2EM device2EM = ApplianceManager.getInstance().getDevice2EM();
        for(DeviceInfo deviceInfo : device2EM.getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                ApplianceInfo applianceInfo = toApplianceInfo(deviceInfo);
                logger.debug("{}: Returning ApplianceInfo {}", applianceId, applianceInfo);
                return applianceInfo;
            }
        }
        logger.error("{}: Appliance not found.", applianceId);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setApplianceInfo(HttpServletResponse response, @RequestParam(value="id") String applianceId,
                                 @RequestParam(value="create") Boolean create,
                                 @RequestBody ApplianceInfo applianceInfo) {
        logger.debug("{}: Received request to set ApplianceInfo (create={}): {}", applianceId, create, applianceInfo);
        DeviceInfo deviceInfo = toDeviceInfo(applianceInfo);
        if(create) {
            Appliance appliance = new Appliance();
            appliance.setId(applianceId);
            ApplianceManager.getInstance().addAppliance(appliance, deviceInfo);
        }
        else {
            if(! ApplianceManager.getInstance().updateAppliance(deviceInfo)) {
                logger.error("{}: Appliance not found.", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @RequestMapping(value= APPLIANCE_URL, method=RequestMethod.DELETE)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void deleteAppliance(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        if(! ApplianceManager.getInstance().deleteAppliance(applianceId)) {
            logger.error("{}: Appliance not found.", applianceId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value=CONTROLDEFAULTS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public ControlDefaults getControlDefaults() {
        ControlDefaults defaults = new ControlDefaults();
        defaults.setStartingCurrentSwitchDefaults(new StartingCurrentSwitchDefaults());
        return defaults;
    }

    @RequestMapping(value=CONTROL_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Control getControl(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        logger.debug("{}: Received request for control", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            Control control = appliance.getControl();
            logger.debug("{}: Returning control {}", applianceId, control);
            if(control == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            return control;
        }
        logger.error("{}: Appliance not found", applianceId);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    @RequestMapping(value=CONTROL_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setControl(HttpServletResponse response, @RequestParam(value="id") String applianceId,
                           @RequestBody Control control) {
        logger.debug("{}: Received request to set control {}", applianceId, control);
        if(control instanceof ModbusSwitch) {
            ((ModbusSwitch) control).setIdref(PulseReceiver.DEFAULT_ID);
        }
        if(! ApplianceManager.getInstance().setControl(applianceId, control)) {
            logger.error("{}: Appliance not found", applianceId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value= CONTROL_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void deleteControl(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        logger.debug("{}: Received request to delete control", applianceId);
        if(! ApplianceManager.getInstance().deleteControl(applianceId)) {
            logger.error("{}: Appliance not found", applianceId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value=METERDEFAULTS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public MeterDefaults getMeterDefaults() {
        MeterDefaults defaults = new MeterDefaults();
        defaults.setS0ElectricityMeter(new S0ElectricityMeterDefaults());
        defaults.setHttpElectricityMeter(new HttpElectricityMeterDefaults());
        defaults.setModbusElectricityMeter(new ModbusElectricityMeterDefaults());
        return defaults;
    }

    @RequestMapping(value=METER_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Meter getMeter(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        logger.debug("{}: Received request for meter", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            Meter meter = appliance.getMeter();
            logger.debug("{}: Returning meter {}", applianceId, meter);
            if(meter == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            return meter;
        }
        logger.error("{}: Appliance not found", applianceId);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    @RequestMapping(value=METER_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setMeter(HttpServletResponse response, @RequestParam(value="id") String applianceId,
                         @RequestBody Meter meter) {
        logger.debug("{}: Received request to set meter {}", applianceId, meter);
        if(meter instanceof S0ElectricityMeterNetworked) {
            ((S0ElectricityMeterNetworked) meter).setIdref(PulseReceiver.DEFAULT_ID);
        }
        if(meter instanceof ModbusElectricityMeter) {
            ((ModbusElectricityMeter) meter).setIdref(PulseReceiver.DEFAULT_ID);
        }
        if(! ApplianceManager.getInstance().setMeter(applianceId, meter)) {
            logger.error("{}: Appliance not found", applianceId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value= METER_URL, method=RequestMethod.DELETE, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void deleteMeter(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        logger.debug("{}: Received request to delete meter", applianceId);
        if(! ApplianceManager.getInstance().deleteMeter(applianceId)) {
            logger.error("{}: Appliance not found", applianceId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value=SCHEDULES_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public List<Schedule> getSchedules(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        logger.debug("{}: Received request for schedules", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            List<Schedule> schedules = appliance.getSchedules();
            if(schedules == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            logger.debug("{}: Returning {} schedules", applianceId, schedules != null ? schedules.size() : 0);
            return schedules;
        }
        logger.error("{}: Appliance not found", applianceId);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    @RequestMapping(value=SCHEDULES_URL, method=RequestMethod.PUT, consumes="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setSchedules(@RequestParam(value="id") String applianceId, @RequestBody List<Schedule> schedules) {
        logger.debug("{}: Received request to set {} schedules", applianceId, schedules.size());
        ApplianceManager.getInstance().setSchedules(applianceId, schedules);
    }

    @RequestMapping(value=SCHEDULES_URL, method= RequestMethod.POST, consumes="application/xml")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void activateSchedules(HttpServletResponse response, @RequestParam(value="id") String applianceId,
                                  @RequestBody Schedules schedules) {
        List<Schedule> schedulesToSet = schedules.getSchedules();
        logger.debug("{}: Received request to activate {} schedule(s)", applianceId,
                (schedulesToSet != null ? schedulesToSet.size() : "0"));
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        if(appliance != null) {
            appliance.getRunningTimeMonitor().setSchedules(schedulesToSet);
            return;
        }
        logger.error("{}: Appliance not found", applianceId);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @RequestMapping(value=RUNTIME_URL, method=RequestMethod.GET)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Integer suggestRuntime(HttpServletResponse response, @RequestParam(value="id") String applianceId) {
        Integer suggestedRuntime = suggestRuntime(applianceId);
        if(suggestedRuntime == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return suggestedRuntime;
    }

    /**
     * Suggest a runtime for immediate start.
     * @param applianceId
     * @return the suggested runtime in s; 0 if no timeframe interval was found; null if no appliance was found for the given id
     */
    public Integer suggestRuntime(String applianceId) {
        logger.debug("{}: Received request to suggest runtime", applianceId);
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        if(appliance != null) {
            LocalDateTime now = new LocalDateTime();
            List<TimeframeInterval> timeframeIntervals = Schedule.findTimeframeIntervals(now,
                    null, appliance.getSchedules(), false, false);
            if(timeframeIntervals.size() > 0) {
                Schedule schedule = timeframeIntervals.get(0).getTimeframe().getSchedule();
                if(schedule.getRequest() instanceof RuntimeRequest) {
                    return schedule.getRequest().getMin();
                }
            }
        }
        else {
            logger.error("{}: Appliance not found", applianceId);
            return null;
        }
        return 0;
    }

    @RequestMapping(value=RUNTIME_URL, method=RequestMethod.PUT)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setRuntimeDemand(HttpServletResponse response, @RequestParam(value="id") String applianceId,
                           @RequestParam(value="runtime") Integer runtime) {
        if(! setRuntimeDemand(new LocalDateTime(), applianceId, runtime)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Activate immediatly starting timeframe.
     * @param now
     * @param applianceId
     * @param runtime
     * @return true, if the appliance was found; false if no appliance with the given id was found
     */
    public boolean setRuntimeDemand(LocalDateTime now, String applianceId, Integer runtime) {
        logger.debug("{}: Received request to set runtime to {}s", applianceId, runtime);
        return activateTimeframe(now, applianceId, null, runtime, false);
    }

    /**
     * Activate immediatly starting timeframe with energy demand.
     * @param now
     * @param applianceId
     * @param energy
     * @param runtime
     * @return
     */
    public boolean setEnergyDemand(LocalDateTime now, String applianceId, Integer energy, Integer runtime) {
        logger.debug("{}: Received request for {}Wh within {}s", applianceId, energy, runtime);
        return activateTimeframe(now, applianceId, energy, runtime, true);
    }

    public boolean activateTimeframe(LocalDateTime now, String applianceId, Integer energy, Integer runtime,
                                    boolean acceptControlRecommendations) {
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        if(appliance != null) {
            RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
            if(runningTimeMonitor != null) {
                runningTimeMonitor.activateTimeframeInterval(now, runtime);
            }
            appliance.setAcceptControlRecommendations(acceptControlRecommendations);
            return true;
        }
        logger.error("{}: Appliance not found", applianceId);
        return false;
    }

    @RequestMapping(value=SETTINGSDEFAULTS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public SettingsDefaults getSettingsDefaults() {
        return new SettingsDefaults();
    }

    @RequestMapping(value= SETTINGS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Settings getSettings() {
        logger.debug("Received request for Settings");
        Settings settings = new Settings();

        Appliances appliances = ApplianceManager.getInstance().getAppliancesRoot();

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
    public void setSettings(@RequestBody Settings settings) {
        logger.debug("Received request to set " + settings);

        List<PulseReceiver> pulseReceivers = null;
        if(settings.isPulseReceiverEnabled()) {
            PulseReceiver pulseReceiver = new PulseReceiver();
            pulseReceiver.setId(PulseReceiver.DEFAULT_ID);
            pulseReceiver.setPort(settings.getPulseReceiverPort());
            pulseReceivers = Collections.singletonList(pulseReceiver);
        }

        List<ModbusTcp> modbusTCPs = null;
        if(settings.isModbusEnabled()) {
            ModbusTcp modbusTcp = new ModbusTcp();
            modbusTcp.setId(ModbusTcp.DEFAULT_ID);
            modbusTcp.setHost(settings.getModbusTcpHost());
            modbusTcp.setPort(settings.getModbusTcpPort());
            modbusTCPs = Collections.singletonList(modbusTcp);
        }

        Connectivity connectivity = null;
        if(pulseReceivers != null || modbusTCPs != null) {
            connectivity = new Connectivity();
            connectivity.setPulseReceivers(pulseReceivers);
            connectivity.setModbusTCPs(modbusTCPs);
        }
        ApplianceManager.getInstance().setConnectivity(connectivity);

        List<Configuration> configurations = null;
        if(settings.isHolidaysEnabled()) {
            Configuration configuration = new Configuration();
            configuration.setParam(HolidaysDownloader.urlConfigurationParamName);
            configuration.setValue(settings.getHolidaysUrl());
            configurations = Collections.singletonList(configuration);
        }
        ApplianceManager.getInstance().setConfiguration(configurations);
    }

    @RequestMapping(value= STATUS_URL, method=RequestMethod.GET, produces="application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public List<ApplianceStatus> getApplianceStatus() {
        return getApplianceStatus(new LocalDateTime());
    }

    public List<ApplianceStatus> getApplianceStatus(LocalDateTime now) {
        logger.debug("Received request for ApplianceStatus");
        List<ApplianceStatus> applianceStatuses = new ArrayList<>();
        for(Appliance appliance : ApplianceManager.getInstance().getAppliances()) {
            DeviceInfo deviceInfo = getDeviceInfo(appliance.getId());

            ApplianceStatus applianceStatus = new ApplianceStatus();
            applianceStatus.setId(appliance.getId());
            applianceStatus.setName(deviceInfo.getIdentification().getDeviceName());
            applianceStatus.setVendor(deviceInfo.getIdentification().getDeviceVendor());
            applianceStatus.setType(deviceInfo.getIdentification().getDeviceType());

            List<RuntimeInterval> runtimeIntervals = appliance.getRuntimeIntervals(now, true);

            if(appliance.isControllable()) {
                applianceStatus.setControllable(true);
                if(runtimeIntervals.size() > 0) {
                    RuntimeInterval nextRuntimeInterval = runtimeIntervals.get(0);
                    applianceStatus.setPlanningRequested(true);
                    applianceStatus.setEarliestStart(nextRuntimeInterval.getEarliestStart());
                    applianceStatus.setLatestStart(TimeframeInterval.getLatestStart(nextRuntimeInterval.getLatestEnd(),
                            nextRuntimeInterval.getMinRunningTime()));
                    applianceStatus.setOn(appliance.getControl().isOn());
                    RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
                    if(runningTimeMonitor.getActiveTimeframeInterval() == null) {
                        applianceStatus.setRunningTime(0);
                        applianceStatus.setRemainingMinRunningTime(nextRuntimeInterval.getMinRunningTime());
                        applianceStatus.setRemainingMaxRunningTime(nextRuntimeInterval.getMaxRunningTime());
                    }
                }
                applianceStatus.setOn(appliance.getControl().isOn());
                RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
                if(runningTimeMonitor.getActiveTimeframeInterval() != null) {
                    applianceStatus.setPlanningRequested(true);
                    Integer runningTime = runningTimeMonitor.getRunningTimeOfCurrentTimeFrame(now);
                    applianceStatus.setRunningTime(runningTime);
                    Integer minRunningTime = runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now);
                    applianceStatus.setRemainingMinRunningTime(minRunningTime != null ? minRunningTime : 0);
                    Integer maxRunningTime = runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now);
                    applianceStatus.setRemainingMaxRunningTime(maxRunningTime);
                    if(runningTimeMonitor.isInterrupted()) {
                        Interval interrupted = new Interval(runningTimeMonitor.getStatusChangedAt().toDateTime(),
                                now.toDateTime());
                        applianceStatus.setInterruptedSince(
                                new Double(interrupted.toDurationMillis() / 1000).intValue());
                    }
                }
            }

            applianceStatuses.add(applianceStatus);
        }
        return applianceStatuses;
    }
}
