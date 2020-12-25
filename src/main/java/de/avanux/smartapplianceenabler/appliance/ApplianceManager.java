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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import de.avanux.smartapplianceenabler.HolidaysDownloader;
import de.avanux.smartapplianceenabler.configuration.Configuration;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Connectivity;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.semp.webservice.Device2EM;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceStatus;
import de.avanux.smartapplianceenabler.util.FileHandler;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ApplianceManager implements Runnable {
    public static final String SCHEMA_LOCATION = "http://github.com/camueller/SmartApplianceEnabler/v1.6";
    private Logger logger = LoggerFactory.getLogger(ApplianceManager.class);
    private static ApplianceManager instance;
    private FileHandler fileHandler = new FileHandler();
    private Device2EM device2EM;
    private Appliances appliances;
    private Timer timer;
    private GuardedTimerTask holidaysDownloaderTimerTask;
    private Integer autoclearSeconds;
    private boolean initializationCompleted;
    
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

    private GpioController getGpioController() {
        if(System.getProperty("os.arch").equals("arm")) {
            try {
                return GpioFactory.getInstance();
            }
            catch(Error e) {
                // warning will be logged later on only if GPIO access is required
            }
        }
        else {
            logger.warn("GPIO access disabled - not running on Raspberry Pi.");
        }
        return null;
    }

    public void run() {
        try {
            startAppliances();
        }
        catch(Throwable e) {
            logger.error("Error starting appliances", e);
        }
    }

    private void startAppliances() {
        logger.info("Starting appliances ...");
        if(appliances == null) {
            logger.debug("Loading Appliances.xml ...");
            appliances = fileHandler.load(Appliances.class);
            if(appliances == null) {
                this.appliances = new Appliances();
                this.appliances.setAppliances(new ArrayList<>());
            }
        }
        if(this.device2EM == null) {
            logger.debug("Loading Appliances.xml ...");
            this.device2EM = fileHandler.load(Device2EM.class);
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

    private void restartAppliances() {
        logger.debug("Restarting ...");
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
        logger.info("Restarting appliances ...");
        this.appliances = null;
        this.device2EM = null;
        startAppliances();
    }

    public void init() {
        logger.debug("Initializing ...");
        Map<String,ModbusTcp> modbusIdWithModbusTcp = new HashMap<String,ModbusTcp>();
        Connectivity connectivity = appliances.getConnectivity();
        if(connectivity != null) {
            // make ModbusTcp accessible by id
            if(connectivity.getModbusTCPs() != null) {
                for(ModbusTcp modbusTCP : connectivity.getModbusTCPs()) {
                    logger.info("ModBus " + modbusTCP.getId() + " configured for " + modbusTCP.toString());
                    modbusIdWithModbusTcp.put(modbusTCP.getId(), modbusTCP);
                }
            }
        }

        boolean holidaysUsed = false;
        for (Appliance appliance : getAppliances()) {
            if(appliance.hasTimeframeForHolidays()) {
                holidaysUsed = true;
            }
            logger.debug("{}: Initializing appliance ...", appliance.getId());
            try {
                appliance.init(getGpioController(), modbusIdWithModbusTcp,
                        appliances.getConfigurationValue(NotificationHandler.CONFIGURATION_KEY_NOTIFICATION_COMMAND));
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
                        String downloadUrl = appliances.getConfigurationValue(HolidaysDownloader.urlConfigurationParamName);
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
        initializationCompleted = true;
    }

    public void save(boolean writeDevice2EM, boolean writeAppliances) {
        logger.debug("Saving to file: writeDevice2EM=" + writeDevice2EM + " writeAppliances=" + writeAppliances);
        if(writeDevice2EM) {
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

    public void setConnectivity(Connectivity connectivity) {
        logger.debug("Set connectivity");
        this.appliances.setConnectivity(connectivity);
        save(false, true);
    }

    public void setConfiguration(List<Configuration> configurations) {
        logger.debug("Set configuration");
        this.appliances.setConfigurations(configurations);
        save(false, true);
    }
}
