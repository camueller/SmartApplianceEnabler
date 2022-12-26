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

import de.avanux.smartapplianceenabler.HolidaysDownloader;
import de.avanux.smartapplianceenabler.configuration.Configuration;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.ConfigurationParam;
import de.avanux.smartapplianceenabler.configuration.Connectivity;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ev.EvChargerTemplatesDownloader;
import de.avanux.smartapplianceenabler.http.HttpRead;
import de.avanux.smartapplianceenabler.meter.HttpElectricityMeter;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.meter.MeterValueName;
import de.avanux.smartapplianceenabler.meter.ModbusElectricityMeter;
import de.avanux.smartapplianceenabler.modbus.ModbusRead;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.mqtt.ApplianceInfoMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.semp.webservice.*;
import de.avanux.smartapplianceenabler.util.FileHandler;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import de.avanux.smartapplianceenabler.webservice.ApplianceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.pigpioj.PigpioInterface;
import uk.pigpioj.PigpioJ;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class ApplianceManager implements Runnable {
    public static final String SCHEMA_LOCATION = "http://github.com/camueller/SmartApplianceEnabler/v2.0";
    private String TOPIC = "ApplianceInfo";
    private Logger logger = LoggerFactory.getLogger(ApplianceManager.class);
    private static ApplianceManager instance;
    private FileHandler fileHandler = new FileHandler();
    private Device2EM device2EM;
    private Appliances appliances;
    private Timer timer;
    private GuardedTimerTask holidaysDownloaderTimerTask;
    private Integer autoclearSeconds;
    private boolean initializationCompleted;
    private transient MqttClient mqttClient;

    private ApplianceManager() {
    }
    
    public static ApplianceManager getInstance() {
        if(instance == null) {
            instance = new ApplianceManager();
            // creating the timer here disables the it during unit tests
            instance.timer = new Timer();
            String autoClear = System.getProperty("sae.autoclear", null);
            if(autoClear != null) {
                instance.autoclearSeconds = Integer.parseInt(autoClear);
                instance.logger.info("*** AUTO CLEAR ENABLED ({} s) ***", instance.autoclearSeconds);
            }
        }
        return instance;
    }

    public boolean isInitializationCompleted() {
        return initializationCompleted;
    }

    public static ApplianceManager getInstanceWithoutTimer() {
        if(instance == null) {
            instance = new ApplianceManager();
            instance.logger.warn("Timer disabled");
        }
        return instance;
    }

    public void run() {
        try {
            MqttClient.start();
            startAppliances();
        }
        catch(Throwable e) {
            logger.error("Error starting appliances", e);
        }
    }

    private Appliances loadAppliances() {
        // FIXME: use FileContentPreProcessor
        return fileHandler.load(Appliances.class, content -> {
            if(content.contains("SmartApplianceEnabler/v1.5") || content.contains("SmartApplianceEnabler/v1.6")) {
                content = content.replaceAll("gpio=\"0\"", "gpioNew=\"17\"");
                content = content.replaceAll("gpio=\"1\"", "gpioNew=\"18\"");
                content = content.replaceAll("gpio=\"2\"", "gpioNew=\"27\"");
                content = content.replaceAll("gpio=\"3\"", "gpioNew=\"22\"");
                content = content.replaceAll("gpio=\"4\"", "gpioNew=\"23\"");
                content = content.replaceAll("gpio=\"5\"", "gpioNew=\"24\"");
                content = content.replaceAll("gpio=\"6\"", "gpioNew=\"25\"");
                content = content.replaceAll("gpio=\"7\"", "gpioNew=\"4\"");
                content = content.replaceAll("gpio=\"8\"", "gpioNew=\"2\"");
                content = content.replaceAll("gpio=\"9\"", "gpioNew=\"3\"");
                content = content.replaceAll("gpio=\"10\"", "gpioNew=\"8\"");
                content = content.replaceAll("gpio=\"11\"", "gpioNew=\"7\"");
                content = content.replaceAll("gpio=\"12\"", "gpioNew=\"10\"");
                content = content.replaceAll("gpio=\"13\"", "gpioNew=\"9\"");
                content = content.replaceAll("gpio=\"14\"", "gpioNew=\"11\"");
                content = content.replaceAll("gpio=\"15\"", "gpioNew=\"14\"");
                content = content.replaceAll("gpio=\"16\"", "gpioNew=\"15\"");
                content = content.replaceAll("gpio=\"21\"", "gpioNew=\"5\"");
                content = content.replaceAll("gpio=\"22\"", "gpioNew=\"6\"");
                content = content.replaceAll("gpio=\"23\"", "gpioNew=\"13\"");
                content = content.replaceAll("gpio=\"24\"", "gpioNew=\"19\"");
                content = content.replaceAll("gpio=\"25\"", "gpioNew=\"26\"");
                content = content.replaceAll("gpio=\"26\"", "gpioNew=\"12\"");
                content = content.replaceAll("gpio=\"27\"", "gpioNew=\"16\"");
                content = content.replaceAll("gpio=\"28\"", "gpioNew=\"20\"");
                content = content.replaceAll("gpio=\"29\"", "gpioNew=\"21\"");
                content = content.replaceAll("gpio=\"31\"", "gpioNew=\"1\"");
                content = content.replaceAll("gpioNew", "gpio");
            }
            content = content.replaceAll("http://github.com/camueller/SmartApplianceEnabler/v[0-9.]*", ApplianceManager.SCHEMA_LOCATION);
            content = content.replaceAll("type=\"InputString\"", "type=\"Input\" valueType=\"String\"");
            content = content.replaceAll("type=\"InputFloat\"", "type=\"Input\" valueType=\"Float\"");
            content = content.replaceAll("type=\"InputDecimal\"", "type=\"Input\" valueType=\"Integer2Float\"");
            content = content.replaceAll("bytes=", "words=");
            return content;
        });
    }

    private void startAppliances() {
        logger.info("Starting appliances ...");
        if(appliances == null) {
            logger.debug("Loading Appliances.xml ...");
            appliances = loadAppliances();
            if(appliances == null) {
                this.appliances = new Appliances();
                this.appliances.setAppliances(new ArrayList<>());
            }

            if(appliances.getAppliances() != null) {
                // FIXME remove migration code to one Read/Read value for Meter
                appliances.getAppliances().stream().forEach(appliance -> {
                    Meter meter = appliance.getMeter();
                    if(meter instanceof HttpElectricityMeter) {
                        HttpElectricityMeter httpMeter = ((HttpElectricityMeter) meter);
                        List<HttpRead> httpReads = httpMeter.getHttpReads();
                        if(httpReads != null) {
                            if(httpReads.size() == 1) {
                                HttpRead httpRead = httpReads.get(0);
                                if(httpRead.getReadValues() != null && httpRead.getReadValues().size() == 2) {
                                    httpRead.setReadValues(httpRead.getReadValues().stream().filter(readValue -> readValue.getName().equals(MeterValueName.Energy.name()))
                                            .collect(Collectors.toList()));
                                }
                            }
                            else if(httpReads.size() == 2) {
                                httpMeter.setHttpReads(httpReads.stream()
                                        .filter(httpRead -> httpRead.getReadValues().stream().anyMatch(readValue -> readValue.getName().equals(MeterValueName.Energy.name())))
                                        .collect(Collectors.toList()));
                            }
                        }
                    }
                    if(meter instanceof ModbusElectricityMeter) {
                        ModbusElectricityMeter modbusMeter = ((ModbusElectricityMeter) meter);
                        List<ModbusRead> modbusReads = modbusMeter.getModbusReads();
                        if(modbusReads.size() == 2) {
                            modbusMeter.setModbusReads(modbusReads.stream()
                                    .filter(modbusRead -> modbusRead.getReadValues().stream().anyMatch(readValue -> readValue.getName().equals(MeterValueName.Energy.name())))
                                    .collect(Collectors.toList()));
                        }
                    }
                });
            }
        }
        if(this.device2EM == null) {
            logger.debug("Loading Appliances.xml ...");
            this.device2EM = fileHandler.load(Device2EM.class, null);
            if(device2EM == null) {
                this.device2EM = new Device2EM();
            }
        }
        List<DeviceInfo> deviceInfos = device2EM.getDeviceInfo();
        if(deviceInfos == null) {
            deviceInfos = new ArrayList<>();
            device2EM.setDeviceInfo(deviceInfos);
        }
        if(appliances != null) {
            init();
        }
        else {
            logger.warn("No valid appliance configuration found.");
        }
        logger.info(getAppliances().size() + " appliance(s) configured.");
    }

    private void stopAppliances() {
        logger.info("Stopping appliances ...");
        initializationCompleted = false;
        if(this.appliances != null) {
            if(appliances.getAppliances() != null) {
                for(Appliance appliance : appliances.getAppliances()) {
                    logger.info("{}: Stopping appliance ...", appliance.getId());
                    try {
                        appliance.stop();
                    }
                    catch(Exception e) {
                        logger.error("{}: Error stopping appliance", appliance.getId(), e);
                    }
                }
            }
        }
    }

    private void restartAppliances() {
        logger.info("Restarting appliances ...");
        stopAppliances();
        if(mqttClient != null) {
            mqttClient.disconnect();
        }
        MqttClient.stop();
        this.appliances = null;
        this.device2EM = null;
        MqttClient.start();
        startAppliances();
    }

    public void init() {
        logger.debug("Initializing ...");
        Map<String,ModbusTcp> modbusIdWithModbusTcp = new HashMap<String,ModbusTcp>();
        Connectivity connectivity = appliances.getConnectivity();
        if(connectivity != null) {
           if(connectivity.getMqttBroker() != null) {
               MqttClient.setMqttBroker(connectivity.getMqttBroker());
           }
            // make ModbusTcp accessible by id
            if(connectivity.getModbusTCPs() != null) {
                for(ModbusTcp modbusTCP : connectivity.getModbusTCPs()) {
                    logger.info("ModBus " + modbusTCP.getId() + " configured for " + modbusTCP.toString());
                    modbusIdWithModbusTcp.put(modbusTCP.getId(), modbusTCP);
                }
            }
        }
        mqttClient = new MqttClient("", getClass());

        boolean holidaysUsed = false;
        for (Appliance appliance : getAppliances()) {
            publishApplianceInfoMessage(appliance.getId());
            if(appliance.hasTimeframeForHolidays()) {
                holidaysUsed = true;
            }
            logger.debug("{}: Initializing appliance ...", appliance.getId());
            try {
                appliance.init(modbusIdWithModbusTcp,
                        appliances.getConfigurationValue(ConfigurationParam.NOTIFICATION_COMMAND.getVal()));
            }
            catch (Exception e) {
                logger.error("{}: Error initializing appliance", appliance.getId(), e);
            }
            logger.debug("{}: Validating appliance ...", appliance.getId());
            try {
                appliance.validate();
            } catch (ConfigurationException e) {
                logger.error("{}: Terminating because of incorrect configuration", appliance.getId());
                System.exit(-1);
            }
        }

        for (Appliance appliance : getAppliances()) {
            logger.debug("{}: Starting appliance ...", appliance.getId());
            try {
                appliance.start(timer);
            }
            catch(Exception e) {
                logger.error("{}: Error starting appliance", appliance.getId(), e);
            }
        }

        if(holidaysUsed) {
            logger.debug("Holidays are used.");
            /**
             * Once a day check availability of holidays file - the year might have changed!
             * Download it if it is not available. If it is available (either downloaded or just placed there)
             * load holidays from the file pass them on to all appliances.
             */
            this.holidaysDownloaderTimerTask = new GuardedTimerTask(null,
                    "HolidaysDownloader", 24 * 60 * 60 * 1000) {
                @Override
                public void runTask() {
                    FileHandler fileHandler = new FileHandler();
                    if(! fileHandler.isHolidayFileAvailable()) {
                        HolidaysDownloader downloader = new HolidaysDownloader();
                        String downloadUrl = appliances.getConfigurationValue(ConfigurationParam.HOLIDAYS_URL.getVal());
                        if(downloadUrl != null) {
                            downloader.setUrl(downloadUrl);
                        }
                        Map<LocalDate, String> holidayWithName = downloader.downloadHolidays();
                        fileHandler.saveHolidays(holidayWithName);
                    }

                    List<LocalDate> holidays = fileHandler.loadHolidays();
                    if(holidays != null) {
                        for (Appliance appliance : getAppliances()) {
                            appliance.setHolidays(holidays);
                        }
                    }
                }
            };
            timer.schedule(this.holidaysDownloaderTimerTask, 0, this.holidaysDownloaderTimerTask.getPeriod());
        }
        else {
            logger.debug("Holidays are NOT used.");
        }

        var evChargerTemplatesDownloaderTimerTask = new TimerTask() {
            @Override
            public void run() {
                var evChargerTemplates = new EvChargerTemplatesDownloader().download();
                if(evChargerTemplates != null) {
                    FileHandler fileHandler = new FileHandler();
                    fileHandler.saveEVChargerTemplates(evChargerTemplates);
                }
            }
        };
        if(timer != null) {
            timer.schedule(evChargerTemplatesDownloaderTimerTask, 0);
        }

        initializationCompleted = true;
    }

    public void save(boolean writeDevice2EM, boolean writeAppliances) {
        logger.debug("Saving to file: writeDevice2EM=" + writeDevice2EM + " writeAppliances=" + writeAppliances);
        if(writeDevice2EM) {
            this.device2EM.setPlanningRequest(null);
            fileHandler.save(this.device2EM);
        }
        if(writeAppliances) {
            fileHandler.save(this.appliances);
        }
        if(writeDevice2EM || writeAppliances) {
            restartAppliances();
            if(this.autoclearSeconds != null) {
                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logger.info("*** AUTO CLEAR ENABLED ***");
                        if(device2EM.getDeviceInfo() != null) {
                            device2EM.getDeviceInfo().clear();
                        }
                        if(appliances.getAppliances() != null) {
                            appliances.getAppliances().clear();
                        }
                        fileHandler.save(device2EM);
                        fileHandler.save(appliances);
                        restartAppliances();
                    }
                }, this.autoclearSeconds * 1000);
            }
        }
    }

    public Device2EM getDevice2EM() {
        return this.device2EM;
    }

    /**
     * Should only be used for testing
     * @param device2EM
     */
    public void setDevice2EM(Device2EM device2EM) {
        this.device2EM = device2EM;
    }

    /**
     * Return the corresponding DeviceInfo for an appliance.
     * @param applianceId
     * @return
     */
    public DeviceInfo getDeviceInfo(String applianceId) {
        for(DeviceInfo deviceInfo : this.device2EM.getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(applianceId)) {
                return deviceInfo;
            }
        }
        return null;
    }

    /**
     * Return the corresponding DeviceStatus for an appliance.
     * @param applianceId
     * @return
     */
    private DeviceStatus getDeviceStatus(String applianceId) {
        List<DeviceStatus> deviceStatuses = this.device2EM.getDeviceStatus();
        if(deviceStatuses != null) {
            for(DeviceStatus deviceStatus : deviceStatuses) {
                if(deviceStatus.getDeviceId().equals(applianceId)) {
                    return deviceStatus;
                }
            }
        }
        return null;
    }

    public Appliance getAppliance(String applianceId) {
        for (Appliance appliance: this.appliances.getAppliances()) {
            if(appliance.getId().equals(applianceId)) {
                return appliance;
            }
        }
        return null;
    }

    public List<Appliance> getAppliances() {
        if(appliances != null && appliances.getAppliances() != null) {
            return appliances.getAppliances();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Should only be used for testing
     * @param appliances
     */
    public void setAppliances(Appliances appliances) {
        this.appliances = appliances;
    }

    public void addAppliance(Appliance appliance, DeviceInfo deviceInfo) {
        logger.debug("{}: Add appliance", appliance.getId());
        List<DeviceInfo> deviceInfos = device2EM.getDeviceInfo();
        deviceInfos.add(deviceInfo);
        List<Appliance> appliances = this.appliances.getAppliances();
        if(appliances == null) {
            appliances = new ArrayList<>();
            this.appliances.setAppliances(appliances);
        }
        appliances.add(appliance);
        save(true, true);
    }

    /**
     * Update an appliance.
     * @param deviceInfo
     * @return true, if the update was successful; false, if the appliance with the given id was not found
     */
    public boolean updateAppliance(Appliance appliance, DeviceInfo deviceInfo) {
        logger.debug("{}: Update appliance", appliance.getId());

        Integer applianceReplaceIndex = null;
        for(int i=0;i<this.appliances.getAppliances().size();i++) {
            if(appliance.getId().equals(this.appliances.getAppliances().get(i).getId())) {
                applianceReplaceIndex = i;
                break;
            }
        }
        if(applianceReplaceIndex != null) {
            this.appliances.getAppliances().remove(applianceReplaceIndex.intValue());
            this.appliances.getAppliances().add(applianceReplaceIndex, appliance);
        }

        Integer device2EMReplaceIndex = null;
        for(int i=0;i<device2EM.getDeviceInfo().size();i++) {
            if(appliance.getId().equals(device2EM.getDeviceInfo().get(i).getIdentification().getDeviceId())) {
                device2EMReplaceIndex = i;
                break;
            }
        }
        if(device2EMReplaceIndex != null) {
            device2EM.getDeviceInfo().remove(device2EMReplaceIndex.intValue());
            device2EM.getDeviceInfo().add(device2EMReplaceIndex, deviceInfo);
        }

        // handle change of appliance ID
        appliance.setId(deviceInfo.getIdentification().getDeviceId());

        if(applianceReplaceIndex != null || device2EMReplaceIndex != null) {
            save(device2EMReplaceIndex != null, applianceReplaceIndex != null);
            return true;
        }
        return false;
    }

    /**
     * Delete an appliance.
     * @param applianceId
     * @return true, if the deletion was successful; false, if the appliance with the given id was not found
     */
    public boolean deleteAppliance(String applianceId) {
        logger.debug("{}: Delete appliance", applianceId);

        DeviceInfo deviceInfoToBeDeleted = getDeviceInfo(applianceId);
        device2EM.getDeviceInfo().remove(deviceInfoToBeDeleted);

        DeviceStatus deviceStatus = getDeviceStatus(applianceId);
        if(device2EM.getDeviceStatus() != null) {
            device2EM.getDeviceStatus().remove(deviceStatus);
        }

        Appliance applianceToBeDeleted = getAppliance(applianceId);
        if(applianceToBeDeleted != null) {
            appliances.getAppliances().remove(applianceToBeDeleted);
            save(true, true);
            return true;
        }
        return false;
    }

    public Appliance findAppliance(String applianceId) {
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            if(appliance.getId().equals(applianceId)) {
                return appliance;
            }
        }
        return null;
    }

    /**
     * Set the control of an appliance.
     * @param applianceId
     * @param control
     * @return true, if the control was set; false, if the appliance with the given id was not found
     */
    public boolean setControl(String applianceId, Control control) {
        logger.debug("{}: Set control", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            Control oldControl = appliance.getControl();
            if(oldControl != null) {
                oldControl.stop(LocalDateTime.now());
            }
            if(control instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) control).setApplianceId(applianceId);
            }
            appliance.setControl(control);
            save(false, true);
            return true;
        }
        return false;
    }

    /**
     * Delete the control of an appliance.
     * @param applianceId
     * @return true, if the control was set; false, if the appliance with the given id was not found
     */
    public boolean deleteControl(String applianceId) {
        logger.debug("{}: Delete control", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.deleteControl();
            save(false, true);
            return true;
        }
        return false;
    }

    /**
     * Set the mether of an appliance.
     * @param applianceId
     * @param meter
     * @return true, if the meter was set; false, if the appliance with the given id was not found
     */
    public boolean setMeter(String applianceId, Meter meter) {
        logger.debug("{}: Set meter", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            Meter oldMeter = appliance.getMeter();
            if(oldMeter != null) {
                oldMeter.stop(LocalDateTime.now());
            }
            if(meter instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) meter).setApplianceId(applianceId);
            }
            appliance.setMeter(meter);
            save(false, true);
            return true;
        }
        return false;
    }

    /**
     * Delete the meter of an appliance.
     * @param applianceId
     * @return true, if the meter was set; false, if the appliance with the given id was not found
     */
    public boolean deleteMeter(String applianceId) {
        logger.debug("{}: Delete meter", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.deleteMeter();
            save(false, true);
            return true;
        }
        return false;
    }

    public void setSchedules(String applianceId, List<Schedule> schedules) {
        logger.debug("{}: Set schedules", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.setSchedules(schedules);
            save(false, true);
        }
        else {
            logger.error("Appliance not found", applianceId);
        }
    }

    public Appliances getAppliancesRoot() {
        return this.appliances;
    }

    public void setConfiguration(List<Configuration> configurations, Connectivity connectivity) {
        logger.debug("Set configuration and connectivity");
        this.appliances.setConfigurations(configurations);
        this.appliances.setConnectivity(connectivity);
        save(false, true);
    }

    private void publishApplianceInfoMessage(String applianceId) {
        ApplianceInfo applianceInfo = getApplianceInfo(applianceId);
        ApplianceInfoMessage message = new ApplianceInfoMessage(LocalDateTime.now(), applianceInfo);
        String topic = MqttClient.getApplianceTopic(applianceId, TOPIC);
        mqttClient.publish(topic, false, message, false, true);
    }

    public ApplianceInfo getApplianceInfo(String applianceId) {
        DeviceInfo deviceInfo = getDeviceInfo(applianceId);
        ApplianceInfo applianceInfo = new ApplianceInfo();
        if(deviceInfo.getIdentification() != null) {
            applianceInfo.setId(deviceInfo.getIdentification().getDeviceId());
            applianceInfo.setName(deviceInfo.getIdentification().getDeviceName());
            applianceInfo.setVendor(deviceInfo.getIdentification().getDeviceVendor());
            applianceInfo.setSerial(deviceInfo.getIdentification().getDeviceSerial());
            applianceInfo.setType(deviceInfo.getIdentification().getDeviceType());
        }
        if(deviceInfo.getCharacteristics() != null) {
            applianceInfo.setMinPowerConsumption(deviceInfo.getCharacteristics().getMinPowerConsumption());
            applianceInfo.setMaxPowerConsumption(deviceInfo.getCharacteristics().getMaxPowerConsumption());
            applianceInfo.setMinOnTime(deviceInfo.getCharacteristics().getMinOnTime());
            applianceInfo.setMaxOnTime(deviceInfo.getCharacteristics().getMaxOnTime());
            applianceInfo.setMinOffTime(deviceInfo.getCharacteristics().getMinOffTime());
            applianceInfo.setMaxOffTime(deviceInfo.getCharacteristics().getMaxOffTime());
        }
        if(deviceInfo.getCapabilities() != null) {
            if (deviceInfo.getCapabilities().getCurrentPowerMethod() != null) {
                applianceInfo.setCurrentPowerMethod(deviceInfo.getCapabilities().getCurrentPowerMethod().name());
            }
            applianceInfo.setInterruptionsAllowed(deviceInfo.getCapabilities().getInterruptionsAllowed());
        }
        return applianceInfo;
    }

    public DeviceInfo getDeviceInfo(ApplianceInfo applianceInfo) {
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
}
