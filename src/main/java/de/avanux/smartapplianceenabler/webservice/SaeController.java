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

import de.avanux.smartapplianceenabler.Application;
import de.avanux.smartapplianceenabler.HolidaysDownloader;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.Appliances;
import de.avanux.smartapplianceenabler.configuration.Configuration;
import de.avanux.smartapplianceenabler.configuration.Connectivity;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ControlDefaults;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.meter.MeterDefaults;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.notification.Notification;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import de.avanux.smartapplianceenabler.util.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String CONTROLRECOMMENDATIONS_URL = BASE_URL + "/controlrecommendations";
    private static final String EV_URL = BASE_URL + "/ev";
    private static final String EVCHARGE_URL = BASE_URL + "/evcharge";
    private static final String FILE_URL = BASE_URL + "/file";
    private static final String INFO_URL = BASE_URL + "/info";
    private static final String TASMOTA_COMMAND_URL = "/cm";
    // only required for development if running via "ng serve"
    private static final String CROSS_ORIGIN_URL = "http://localhost:4200";
    private Logger logger = LoggerFactory.getLogger(SaeController.class);
    // the lock ensures that no data is changed or read while appliances are restarted
    private final Object lock = new Object();
    private MqttClient mqttClient;
    private Map<String, MeterMessage> meterMessages = new HashMap<>();
    private Map<String, ControlMessage> controlMessages = new HashMap<>();

    public SaeController() {
        logger.info("SAE controller created.");
        mqttClient = new MqttClient("", getClass());

        String meterTopic = mqttClient.getTopicPrefix() + "/+/" + Meter.TOPIC;
        mqttClient.subscribe(meterTopic, false, MeterMessage.class, (topic, message) -> {
            this.meterMessages.put(topic, (MeterMessage) message);
        });

        String controlTopic = mqttClient.getTopicPrefix() + "/+/" + Control.TOPIC;
        mqttClient.subscribe(controlTopic, false, ControlMessage.class, (topic, message) -> {
            this.controlMessages.put(topic, (ControlMessage) message);
        });
    }

    /**
     * Return the corresponding DeviceInfo for an appliance.
     *
     * @param applianceId
     * @return
     */
    private DeviceInfo getDeviceInfo(String applianceId) {
        for (DeviceInfo deviceInfo : ApplianceManager.getInstance().getDevice2EM().getDeviceInfo()) {
            if (deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
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
        applianceInfo.setMinPowerConsumption(deviceInfo.getCharacteristics().getMinPowerConsumption());
        applianceInfo.setMaxPowerConsumption(deviceInfo.getCharacteristics().getMaxPowerConsumption());
        applianceInfo.setMinOnTime(deviceInfo.getCharacteristics().getMinOnTime());
        applianceInfo.setMaxOnTime(deviceInfo.getCharacteristics().getMaxOnTime());
        applianceInfo.setMinOffTime(deviceInfo.getCharacteristics().getMinOffTime());
        applianceInfo.setMaxOffTime(deviceInfo.getCharacteristics().getMaxOffTime());
        if (deviceInfo.getCapabilities().getCurrentPowerMethod() != null) {
            applianceInfo.setCurrentPowerMethod(deviceInfo.getCapabilities().getCurrentPowerMethod().name());
        }
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
        characteristics.setMinPowerConsumption(applianceInfo.getMinPowerConsumption());
        characteristics.setMaxPowerConsumption(applianceInfo.getMaxPowerConsumption());
        characteristics.setMinOnTime(applianceInfo.getMinOnTime());
        characteristics.setMaxOnTime(applianceInfo.getMaxOnTime());
        characteristics.setMinOffTime(applianceInfo.getMinOffTime());
        characteristics.setMaxOffTime(applianceInfo.getMaxOffTime());

        Capabilities capabilities = new Capabilities();
        capabilities.setCurrentPowerMethod(applianceInfo.getCurrentPowerMethod() != null ?
                CurrentPowerMethod.valueOf(applianceInfo.getCurrentPowerMethod()) : CurrentPowerMethod.Estimation);
        capabilities.setInterruptionsAllowed(applianceInfo.isInterruptionsAllowed());
        capabilities.setOptionalEnergy(false);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        deviceInfo.setCharacteristics(characteristics);
        deviceInfo.setCapabilities(capabilities);
        return deviceInfo;
    }

    @RequestMapping(value = APPLIANCES_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public List<ApplianceHeader> getAppliances() {
        synchronized (lock) {
            try {
                logger.debug("Received request for ApplianceHeaders");
                List<ApplianceHeader> applianceHeaders = new ArrayList<>();
                List<Appliance> applianceList = ApplianceManager.getInstance().getAppliances();
                Device2EM device2EM = ApplianceManager.getInstance().getDevice2EM();
                if (applianceList != null && device2EM != null) {
                    for (Appliance appliance : applianceList) {
                        DeviceInfo deviceInfo = getDeviceInfo(appliance.getId());
                        applianceHeaders.add(toApplianceHeader(appliance, deviceInfo));
                    }
                }
                logger.debug("Returning " + applianceHeaders.size() + " ApplianceHeaders");
                return applianceHeaders;
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @RequestMapping(value = APPLIANCE_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public ApplianceInfo getApplianceInfo(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request for ApplianceInfo", applianceId);
                Device2EM device2EM = ApplianceManager.getInstance().getDevice2EM();
                for (DeviceInfo deviceInfo : device2EM.getDeviceInfo()) {
                    if (deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                        ApplianceInfo applianceInfo = toApplianceInfo(deviceInfo);

                        Appliance appliance = ApplianceManager.getInstance().getAppliance(applianceId);
                        if(appliance != null && appliance.getNotification() != null) {
                            applianceInfo.setNotificationSenderId(appliance.getNotification().getSenderId());
                        }
                        logger.debug("{}: Returning ApplianceInfo {}", applianceId, applianceInfo);
                        return applianceInfo;
                    }
                }
                logger.error("{}: Appliance not found.", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @RequestMapping(value = APPLIANCE_URL, method = RequestMethod.PUT, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setApplianceInfo(HttpServletResponse response, @RequestParam(value = "id") String applianceId,
                                 @RequestParam(value = "create") Boolean create,
                                 @RequestBody ApplianceInfo applianceInfo) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to set ApplianceInfo (create={}): {}", applianceId, create, applianceInfo);
                LocalDateTime now = LocalDateTime.now();

                Notification notification = null;
                if(applianceInfo.getNotificationSenderId() != null) {
                    notification = new Notification();
                    notification.setSenderId(applianceInfo.getNotificationSenderId());
                }

                DeviceInfo deviceInfo = toDeviceInfo(applianceInfo);
                if (create) {
                    Appliance appliance = new Appliance();
                    appliance.setId(applianceId);
                    appliance.setNotification(notification);
                    ApplianceManager.getInstance().addAppliance(appliance, deviceInfo);
                } else {
                    Appliance appliance = ApplianceManager.getInstance().getAppliance(applianceId);
                    if (appliance != null) {
                        deviceInfo.getCapabilities().setOptionalEnergy(appliance.canConsumeOptionalEnergy(now));
                        appliance.setNotification(notification);
                        ApplianceManager.getInstance().updateAppliance(appliance, deviceInfo);
                    }
                    else {
                        logger.error("{}: Appliance not found.", applianceId);
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = APPLIANCE_URL, method = RequestMethod.DELETE)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void deleteAppliance(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                if (!ApplianceManager.getInstance().deleteAppliance(applianceId)) {
                    logger.error("{}: Appliance not found.", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = CONTROLDEFAULTS_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public ControlDefaults getControlDefaults() {
        try {
            return new ControlDefaults();
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    @RequestMapping(value = CONTROL_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Control getControl(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request for control", applianceId);
                Appliance appliance = ApplianceManager.getInstance().getAppliance(applianceId);
                if (appliance != null) {
                    Control control = appliance.getControl();
                    logger.debug("{}: Returning control {}", applianceId, control);
                    if (control == null) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                    return control;
                }
                logger.error("{}: Appliance not found", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @RequestMapping(value = CONTROL_URL, method = RequestMethod.PUT, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setControl(HttpServletResponse response, @RequestParam(value = "id") String applianceId,
                           @RequestBody Control control) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to set control {}", applianceId, control);
                if (!ApplianceManager.getInstance().setControl(applianceId, control)) {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = CONTROL_URL, method = RequestMethod.DELETE, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void deleteControl(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to delete control", applianceId);
                if (!ApplianceManager.getInstance().deleteControl(applianceId)) {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = METERDEFAULTS_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public MeterDefaults getMeterDefaults() {
        try {
            return new MeterDefaults();
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    @RequestMapping(value = METER_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Meter getMeter(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request for meter", applianceId);
                Appliance appliance = ApplianceManager.getInstance().getAppliance(applianceId);
                if (appliance != null) {
                    Meter meter = appliance.getMeter();
                    logger.debug("{}: Returning meter {}", applianceId, meter);
                    if (meter == null) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                    return meter;
                }
                logger.error("{}: Appliance not found", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @RequestMapping(value = METER_URL, method = RequestMethod.PUT, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setMeter(HttpServletResponse response, @RequestParam(value = "id") String applianceId,
                         @RequestBody Meter meter) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to set meter {}", applianceId, meter);
                if (!ApplianceManager.getInstance().setMeter(applianceId, meter)) {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = METER_URL, method = RequestMethod.DELETE, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void deleteMeter(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to delete meter", applianceId);
                if (!ApplianceManager.getInstance().deleteMeter(applianceId)) {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = SCHEDULES_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public List<Schedule> getSchedules(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request for schedules", applianceId);
                Appliance appliance = ApplianceManager.getInstance().getAppliance(applianceId);
                if (appliance != null) {
                    List<Schedule> schedules = appliance.getSchedules();
                    if (schedules == null) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                    logger.debug("{}: Returning {} schedules", applianceId, schedules != null ? schedules.size() : 0);
                    return schedules;
                }
                logger.error("{}: Appliance not found", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @RequestMapping(value = SCHEDULES_URL, method = RequestMethod.PUT, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setSchedules(@RequestParam(value = "id") String applianceId, @RequestBody List<Schedule> schedules) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to set {} schedules", applianceId, schedules.size());
                ApplianceManager.getInstance().setSchedules(applianceId, schedules);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = SCHEDULES_URL, method = RequestMethod.POST, consumes = "application/xml")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void activateSchedules(HttpServletResponse response, @RequestParam(value = "id") String applianceId,
                                  @RequestBody Schedules schedules) {
        synchronized (lock) {
            try {
                LocalDateTime now = LocalDateTime.now();
                List<Schedule> schedulesToSet = schedules.getSchedules();
                logger.debug("{}: Received request to activate {} schedule(s)", applianceId,
                        (schedulesToSet != null ? schedulesToSet.size() : "0"));
                Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
                if (appliance != null) {
                    if (appliance.getMeter() != null) {
                        appliance.getMeter().resetEnergyMeter();
                    }
                    appliance.setSchedules(schedulesToSet);
                    return;
                }
                logger.error("{}: Appliance not found", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = CONTROLRECOMMENDATIONS_URL, method = RequestMethod.PUT)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setAcceptControlRecommendations(
            HttpServletResponse response,
            @RequestParam(value = "id") String applianceId,
            @RequestParam(value = "accept") Boolean acceptControlRecommendations) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to set control recommendations to {}",
                        applianceId, acceptControlRecommendations);
                Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
                if (appliance != null) {
                    TimeframeIntervalHandler timeframeIntervalHandler = appliance.getTimeframeIntervalHandler();
                    if(timeframeIntervalHandler != null) {
                        TimeframeInterval activeTimeframeInterval = timeframeIntervalHandler.getActiveTimeframeInterval();
                        if(activeTimeframeInterval != null) {
                            activeTimeframeInterval.getRequest().setAcceptControlRecommendations(acceptControlRecommendations);
                        }
                    }
                } else {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = CONTROLRECOMMENDATIONS_URL, method = RequestMethod.DELETE)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void resetAcceptControlRecommendations(
            HttpServletResponse response,
            @RequestParam(value = "id") String applianceId) {
        setAcceptControlRecommendations(response, applianceId, null);
    }

    @RequestMapping(value = RUNTIME_URL, method = RequestMethod.GET)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Integer suggestRuntime(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                Integer suggestedRuntime = suggestRuntime(applianceId);
                if (suggestedRuntime == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return suggestedRuntime;
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    /**
     * Suggest a runtime for immediate start.
     *
     * @param applianceId
     * @return the suggested runtime in s; 0 if no timeframe interval was found; null if no appliance was found for the given id
     */
    public Integer suggestRuntime(String applianceId) {
        try {
            logger.debug("{}: Received request to suggest runtime", applianceId);
            Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
            if (appliance != null) {
                Integer runtime = appliance.getTimeframeIntervalHandler().suggestRuntime();
                return runtime != null ? runtime : 0;
            } else {
                logger.error("{}: Appliance not found", applianceId);
                return null;
            }
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    // Refer to https://tasmota.github.io/docs/Commands
    @RequestMapping(value = TASMOTA_COMMAND_URL, method = RequestMethod.GET)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void executeTasmotaCommand(HttpServletResponse response,
                                   @RequestParam(value = "cmnd") String command) {
        synchronized (lock) {
            try {
                logger.debug("Received Tasmota Web request {}", command);
                String[] commandParts = command.split(" ");
                if(commandParts.length < 3) {
                    logger.error("Required Syntax is: ID F-xxxxxxxx-xxxxxxxxxxxx-xx RUNTIME ...");
                }
                if ("ID".equals(commandParts[0])) {
                    String applianceId = commandParts[1];

                    if ("RUNTIME".equals(commandParts[2])) {
                        if(commandParts.length < 5) {
                            logger.error("{}: Required Syntax is: RUNTIME <runtime in s> <latestEnd in s from now>",
                                    applianceId);
                        }
                        int runtimeSeconds = Integer.parseInt(commandParts[3]);
                        int latestEndSeconds = Integer.parseInt(commandParts[4]);
                        logger.debug("{}: Set runtime demand: runtime={}s latestEnd={}s",
                                applianceId, runtimeSeconds, latestEndSeconds);
                        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
                        if(appliance != null) {
                            LocalDateTime now = LocalDateTime.now();
                            Control control = appliance.getControl();
                            if(control != null) {
                                control.on(now, false);
                            }
                            activateTimeframe(now, applianceId, runtimeSeconds, latestEndSeconds, true);
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        }
                    }
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = RUNTIME_URL, method = RequestMethod.PUT)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setRuntimeDemand(HttpServletResponse response,
                                 @RequestParam(value = "id") String applianceId,
                                 @RequestParam(value = "runtime") Integer runtime) {
        synchronized (lock) {
            try {
                if (!setRuntimeDemand(LocalDateTime.now(), applianceId, runtime, null)) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Activate immediatly starting timeframe.
     *
     * @param now
     * @param applianceId
     * @param runtime
     * @return true, if the appliance was found; false if no appliance with the given id was found
     */
    public boolean setRuntimeDemand(LocalDateTime now, String applianceId, Integer runtime, Integer latestEnd) {
        logger.debug("{}: Received request to set runtime to {}s", applianceId, runtime);
        return activateTimeframe(now, applianceId, runtime, latestEnd, false);
    }

    @RequestMapping(value = EV_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public List<ElectricVehicle> getElectricVehicles(HttpServletResponse response, @RequestParam(value = "id") String applianceId) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request for electric vehicles", applianceId);
                Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
                if (appliance != null) {
                    if (appliance.getControl() instanceof ElectricVehicleCharger) {
                        ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
                        return evCharger.getVehicles();
                    } else {
                        logger.debug("{}: Appliance has no electric vehicle charger", applianceId);
                        return new ArrayList<>();
                    }
                } else {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @RequestMapping(value = EVCHARGE_URL, method = RequestMethod.PUT)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setEnergyDemand(HttpServletResponse response,
                                @RequestParam(value = "applianceid") String applianceId,
                                @RequestParam(value = "evid") Integer evId,
                                @RequestParam(value = "socCurrent", required = false) Integer socCurrent,
                                @RequestParam(value = "socRequested", required = false) Integer socRequested,
                                @RequestParam(value = "chargeEnd", required = false) String chargeEndString
    ) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received energy request: evId={} socCurrent={} socRequested={} chargeEnd={}",
                        applianceId, evId, socCurrent, socRequested, chargeEndString);
                LocalDateTime chargeEnd = null;
                if (chargeEndString != null) {
                    chargeEnd = ZonedDateTime.parse(chargeEndString).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                }
                Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
                if (appliance != null) {
                    appliance.setEnergyDemand(LocalDateTime.now(), evId, socCurrent, socRequested, chargeEnd);
                } else {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = EVCHARGE_URL, method = RequestMethod.PATCH)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void updateSoc(HttpServletResponse response,
                          @RequestParam(value = "applianceid") String applianceId,
                          @RequestParam(value = "socCurrent", required = false) Integer socCurrent,
                          @RequestParam(value = "socRequested", required = false) Integer socRequested
    ) {
        synchronized (lock) {
            try {
                logger.debug("{}: Received request to update SOC: socCurrent={} socRequested={}",
                        applianceId, socCurrent, socRequested);
                Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
                if (appliance != null) {
                    appliance.updateSoc(LocalDateTime.now(), socCurrent, socRequested);
                } else {
                    logger.error("{}: Appliance not found", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
    }

    @RequestMapping(value = EVCHARGE_URL, method = RequestMethod.GET)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Float getSoc(
            HttpServletResponse response,
            @RequestParam(value = "applianceid") String applianceId,
            @RequestParam(value = "evid") Integer evId) {
        try {
            logger.debug("{}: Received SOC request: evId={}", applianceId, evId);
            Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
            if (appliance != null) {
                if (appliance.getControl() instanceof ElectricVehicleCharger) {
                    ElectricVehicleCharger evCharger = (ElectricVehicleCharger) appliance.getControl();
                    Integer soc = evCharger.getSocCurrent();
                    if (soc != null) {
                        logger.debug("{}: Return SOC={}", applianceId, soc);
                        return Integer.valueOf(soc).floatValue();
                    } else {
                        logger.error("{}: SOC not available", applianceId);
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    logger.error("{}: Appliance has no electric vehicle charger", applianceId);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                logger.error("{}: Appliance not found", applianceId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            return 0.0f;
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    public boolean activateTimeframe(LocalDateTime now, String applianceId, Integer runtime, Integer latestEnd,
                                     boolean acceptControlRecommendations) {
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        if (appliance != null) {
            appliance.getTimeframeIntervalHandler().setRuntimeDemand(now, runtime, latestEnd, acceptControlRecommendations);
            return true;
        }
        logger.error("{}: Appliance not found", applianceId);
        return false;
    }

    @RequestMapping(value = SETTINGSDEFAULTS_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public SettingsDefaults getSettingsDefaults() {
        try {
            return new SettingsDefaults();
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    @RequestMapping(value = SETTINGS_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Settings getSettings() {
        try {
            logger.debug("Received request for Settings");
            Settings settings = new Settings();

            Appliances appliances = ApplianceManager.getInstance().getAppliancesRoot();

            Connectivity connectivity = appliances.getConnectivity();
            if (connectivity != null) {
                List<ModbusTcp> modbusTCPs = connectivity.getModbusTCPs();
                if (modbusTCPs != null) {
                    List<ModbusSettings> modbusSettingsList = new ArrayList<>();
                    for (ModbusTcp modbusTcp : modbusTCPs) {
                        ModbusSettings modbusSettings = new ModbusSettings();
                        modbusSettings.setModbusTcpId(modbusTcp.getId());
                        modbusSettings.setModbusTcpHost(modbusTcp.getHost());
                        modbusSettings.setModbusTcpPort(modbusTcp.getPort());
                        modbusSettingsList.add(modbusSettings);
                    }
                    settings.setModbusSettings(modbusSettingsList);
                }
            }

            String holidaysUrl = appliances.getConfigurationValue(HolidaysDownloader.urlConfigurationParamName);
            settings.setHolidaysEnabled(holidaysUrl != null);
            settings.setHolidaysUrl(holidaysUrl);

            String notificationCommand = appliances.getConfigurationValue(NotificationHandler.CONFIGURATION_KEY_NOTIFICATION_COMMAND);
            settings.setNotificationCommand(notificationCommand);

            logger.debug("Returning Settings " + settings);
            return settings;
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    @RequestMapping(value = SETTINGS_URL, method = RequestMethod.PUT, consumes = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void setSettings(@RequestBody Settings settings) {
        try {
            logger.debug("Received request to set " + settings);

            List<ModbusTcp> modbusTCPs = null;
            List<ModbusSettings> modbusSettingsList = settings.getModbusSettings();
            if (modbusSettingsList != null) {
                modbusTCPs = new ArrayList<>();
                for (ModbusSettings modbusSettings : modbusSettingsList) {
                    ModbusTcp modbusTcp = new ModbusTcp();
                    modbusTcp.setId(modbusSettings.getModbusTcpId());
                    modbusTcp.setHost(modbusSettings.getModbusTcpHost());
                    modbusTcp.setPort(modbusSettings.getModbusTcpPort());
                    modbusTCPs.add(modbusTcp);
                }
            }

            Connectivity connectivity = null;
            if (modbusTCPs != null) {
                connectivity = new Connectivity();
                connectivity.setModbusTCPs(modbusTCPs);
            }
            ApplianceManager.getInstance().setConnectivity(connectivity);

            List<Configuration> configurations = new ArrayList<>();
            if (settings.isHolidaysEnabled()) {
                configurations.add(
                        new Configuration(HolidaysDownloader.urlConfigurationParamName, settings.getHolidaysUrl()));
            }
            if(settings.getNotificationCommand() != null) {
                configurations.add(
                        new Configuration(NotificationHandler.CONFIGURATION_KEY_NOTIFICATION_COMMAND,
                                settings.getNotificationCommand()));
            }
            ApplianceManager.getInstance().setConfiguration(configurations);
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
    }

    @RequestMapping(value = STATUS_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public List<ApplianceStatus> getApplianceStatus(HttpServletResponse response) {
        synchronized (lock) {
            try {
                return getApplianceStatus(LocalDateTime.now(), response);
            } catch (Throwable e) {
                logger.error("Error in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    public List<ApplianceStatus> getApplianceStatus(LocalDateTime now, HttpServletResponse response) {
        logger.debug("Received request for ApplianceStatus");
        List<ApplianceStatus> applianceStatuses = new ArrayList<>();
        if(!ApplianceManager.getInstance().isInitializationCompleted()) {
            if(response != null) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
            return applianceStatuses;
        }
        for (Appliance appliance : ApplianceManager.getInstance().getAppliances()) {
            DeviceInfo deviceInfo = getDeviceInfo(appliance.getId());
            Identification identification = null;
            if (deviceInfo != null) {
                identification = deviceInfo.getIdentification();
            }

            ApplianceStatus applianceStatus = new ApplianceStatus();
            applianceStatus.setId(appliance.getId());
            if (identification != null) {
                applianceStatus.setName(identification.getDeviceName());
                applianceStatus.setVendor(identification.getDeviceVendor());
                applianceStatus.setType(identification.getDeviceType());
            }

            if (appliance.isControllable()) {
                String applianceControlTopic = MqttClient.getApplianceTopic(appliance.getId(), Control.TOPIC);
                ControlMessage controlMessage = this.controlMessages.get(applianceControlTopic);

                applianceStatus.setControllable(true);
                Control control = appliance.getControl();
                Meter meter = appliance.getMeter();
                applianceStatus.setOn(controlMessage.on);
                TimeframeInterval nextTimeframeInterval
                        = appliance.getTimeframeIntervalHandler().getQueue().size() > 0
                        ? appliance.getTimeframeIntervalHandler().getQueue().get(0)
                        : null;
                if (nextTimeframeInterval != null) {
                    applianceStatus.setPlanningRequested(true);
                    if(nextTimeframeInterval.getRequest().isEnabled()) {
                        applianceStatus.setEarliestStart(nextTimeframeInterval.getEarliestStartSeconds(now));
                        applianceStatus.setLatestStart(nextTimeframeInterval.getLatestStartSeconds(now));
                    }
                    applianceStatus.setOptionalEnergy(nextTimeframeInterval.getRequest().isUsingOptionalEnergy(now));
                    if (nextTimeframeInterval.getState() == TimeframeIntervalState.QUEUED) {
                        applianceStatus.setRunningTime(0);
                        applianceStatus.setRemainingMinRunningTime(nextTimeframeInterval.getRequest().getMin(now));
                        applianceStatus.setRemainingMaxRunningTime(nextTimeframeInterval.getRequest().getMax(now));
                    }
                }
                if (control instanceof ElectricVehicleCharger) {
                    ElectricVehicleCharger evCharger = (ElectricVehicleCharger) control;
                    applianceStatus.setState(evCharger.getState().name());
                    if(!evCharger.isVehicleNotConnected()) {
                        applianceStatus.setEvIdCharging(evCharger.getConnectedVehicleId());
                        ZonedDateTime zdt = ZonedDateTime.of(evCharger.getStateLastChangedTimestamp(), ZoneId.systemDefault());
                        applianceStatus.setStateLastChangedTimestamp(zdt.toInstant().toEpochMilli());
                        applianceStatus.setSocInitial(evCharger.getSocInitial());
                        applianceStatus.setSocInitialTimestamp(evCharger.getSocInitialTimestamp());
                        applianceStatus.setSoc(evCharger.getSocCurrent());

                        int whAlreadyCharged = 0;
                        Integer chargePower = evCharger.getChargePower();
                        if (meter != null) {
                            String applianceMeterTopic = MqttClient.getApplianceTopic(appliance.getId(), Meter.TOPIC);
                            MeterMessage meterMessage = this.meterMessages.get(applianceMeterTopic);
                            whAlreadyCharged = Double.valueOf(meterMessage.energy * 1000.0f).intValue();
                            chargePower = meterMessage.power;
                        }
                        if (controlMessage.on) {
                            applianceStatus.setCurrentChargePower(chargePower);
                        }
                        applianceStatus.setChargedEnergyAmount(whAlreadyCharged);
                        int whRemainingToCharge = 0;
                        if(nextTimeframeInterval != null
                                && nextTimeframeInterval.getRequest() instanceof AbstractEnergyRequest) {
                            Integer max = nextTimeframeInterval.getRequest().getMax(now);
                            whRemainingToCharge = max != null ? max : 0;
                        }
                        if (nextTimeframeInterval != null && !nextTimeframeInterval.getRequest().isUsingOptionalEnergy(now)) {
                            applianceStatus.setPlannedEnergyAmount(whAlreadyCharged + whRemainingToCharge);
                            applianceStatus.setLatestEnd(nextTimeframeInterval.getLatestEndSeconds(now));
                        }
                    }
                }
                if (nextTimeframeInterval != null && nextTimeframeInterval.getState() == TimeframeIntervalState.ACTIVE) {
                    applianceStatus.setPlanningRequested(true);
                    applianceStatus.setRunningTime(nextTimeframeInterval.getRequest().getRuntime(now));
                    applianceStatus.setRemainingMinRunningTime(nextTimeframeInterval.getRequest().getMin(now));
                    applianceStatus.setRemainingMaxRunningTime(nextTimeframeInterval.getRequest().getMax(now));
                    if (! nextTimeframeInterval.getRequest().isEnabled() && nextTimeframeInterval.getRequest().isEnabledBefore()) {
                        applianceStatus.setInterruptedSince(
                            Long.valueOf(
                                    Duration.between(nextTimeframeInterval.getRequest().getControlStatusChangedAt(), now).toSeconds()
                            ).intValue());
                    }
                }
            }

            applianceStatuses.add(applianceStatus);
        }
        return applianceStatuses;
    }

    @RequestMapping(value = FILE_URL, method = RequestMethod.GET)
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Integer getFileAttributes(@RequestParam(value = "pathname") String pathname) {
        // http://localhost:8080/sae/file?pathname=%2Ftmp%2Frolling-2021-01-05.log
        try {
            logger.debug("Received request for attritbutes of file " + pathname);
            FileHandler fileHandler = new FileHandler();
            return fileHandler.getFileAttributes(pathname);
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }

    @RequestMapping(value = INFO_URL, method = RequestMethod.GET, produces = "application/json")
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public Info getInfo() {
        try {
            logger.debug("Received request for Info");
            Info info = new Info();
            info.setVersion(Application.getVersion());
            info.setBuildDate(Application.getBuildDate());
            return info;
        } catch (Throwable e) {
            logger.error("Error in " + getClass().getSimpleName(), e);
        }
        return null;
    }
}
