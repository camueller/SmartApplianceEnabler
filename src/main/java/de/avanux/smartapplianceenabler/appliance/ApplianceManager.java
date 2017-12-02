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
import de.avanux.smartapplianceenabler.configuration.Connectivity;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.GpioControllable;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.meter.PulseReceiver;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.semp.webservice.Device2EM;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceStatus;
import de.avanux.smartapplianceenabler.util.FileHandler;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ApplianceManager implements Runnable {
    public static final String SCHEMA_LOCATION = "http://github.com/camueller/SmartApplianceEnabler/v1.2";
    private Logger logger = LoggerFactory.getLogger(ApplianceManager.class);
    private static ApplianceManager instance;
    private FileHandler fileHandler = new FileHandler();
    private Device2EM device2EM;
    private Appliances appliances;
    private GpioController gpioController;
    private Timer timer;
    
    private ApplianceManager() {
        timer = new Timer();
        if(System.getProperty("os.arch").equals("arm")) {
            gpioController = GpioFactory.getInstance();
        }
        else {
            logger.error("GPIO access disabled - not running on Raspberry Pi.");
        }
    }
    
    public static ApplianceManager getInstance() {
        if(instance == null) {
            instance = new ApplianceManager();
        }
        return instance;
    }
    
    public void run() {
        try {
            startAppliances();
        }
        catch(Exception e) {
            logger.error("Error starting appliances", e);
        }
    }

    private void startAppliances() {
        if(appliances == null) {
            appliances = fileHandler.load(Appliances.class);
            if(appliances == null) {
                this.appliances = new Appliances();
                this.appliances.setAppliances(new ArrayList<>());
            }
        }
        if(this.device2EM == null) {
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
        if(this.appliances != null) {
            Connectivity connectivity = this.appliances.getConnectivity();
            if(connectivity != null) {
                // make PulseReceiver accessible by id
                if (connectivity.getPulseReceivers() != null) {
                    for (PulseReceiver pulseReceiver : connectivity.getPulseReceivers()) {
                        logger.info("Stopping PulseReceiver (" + pulseReceiver.getId() + ") configured for port " + pulseReceiver.getPort());
                        pulseReceiver.stop();
                    }
                }
            }
            for(Appliance appliance : appliances.getAppliances()) {
                Control control = appliance.getControl();
                if(control instanceof GpioControllable) {
                    ((GpioControllable) control).stop();
                }
            }
        }
        logger.info("Restarting appliances ...");
        this.appliances = null;
        this.device2EM = null;
        startAppliances();
    }

    private void init() {
        Map<String,PulseReceiver> pulseReceiverIdWithPulseReceiver = new HashMap<String,PulseReceiver>();
        Map<String,ModbusTcp> modbusIdWithModbusTcp = new HashMap<String,ModbusTcp>();
        Connectivity connectivity = appliances.getConnectivity();
        if(connectivity != null) {
            // make PulseReceiver accessible by id
            if(connectivity.getPulseReceivers() != null) {
                for(PulseReceiver pulseReceiver : connectivity.getPulseReceivers()) {
                    logger.info("PulseReceiver (" + pulseReceiver.getId() + ") configured for port " + pulseReceiver.getPort());
                    pulseReceiverIdWithPulseReceiver.put(pulseReceiver.getId(), pulseReceiver);
                    pulseReceiver.start();
                }
            }
            // make ModbusTcp accessible by id
            if(connectivity.getModbusTCPs() != null) {
                for(ModbusTcp modbusTCP : connectivity.getModbusTCPs()) {
                    logger.info("ModBus (" + modbusTCP.getId() + ") configured for " + modbusTCP.toString());
                    modbusIdWithModbusTcp.put(modbusTCP.getId(), modbusTCP);
                }
            }
        }

        Integer additionRunningTime = null;
        String additionRunningTimeString = appliances.getConfigurationValue("TimeframeIntervalAdditionalRunningTime");
        if(additionRunningTimeString != null) {
            additionRunningTime = Integer.valueOf(additionRunningTimeString);
        }

        boolean holidaysUsed = false;
        for (Appliance appliance : getAppliances()) {
            if(appliance.hasTimeframeForHolidays()) {
                holidaysUsed = true;
            }
            appliance.init(additionRunningTime);
            appliance.start(timer, gpioController, pulseReceiverIdWithPulseReceiver, modbusIdWithModbusTcp);
        }

        if(holidaysUsed) {
            logger.debug("Holidays are used.");
            /**
             * Once a day check availability of holidays file - the year might have changed!
             * Download it if it is not available. If it is available (either downloaded or just placed there)
             * load holidays from the file pass them on to all appliances.
             */
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
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
            }, 0,24 * 60 * 60 * 1000);
        }
        else {
            logger.debug("Holidays are NOT used.");
        }
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
        for(DeviceStatus deviceStatus : this.device2EM.getDeviceStatus()) {
            if(deviceStatus.getDeviceId().equals(applianceId)) {
                return deviceStatus;
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
        if(appliances != null) {
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
        appliances.getAppliances().add(appliance);
        save(true, true);
    }

    public void updateAppliance(DeviceInfo deviceInfo) {
        logger.debug("{}: Update appliance", deviceInfo.getIdentification().getDeviceId());
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

    public void deleteAppliance(String applianceId) {
        logger.debug("{}: Delete appliance", applianceId);

        DeviceInfo deviceInfoToBeDeleted = getDeviceInfo(applianceId);
        device2EM.getDeviceInfo().remove(deviceInfoToBeDeleted);

        DeviceStatus deviceStatus = getDeviceStatus(applianceId);
        device2EM.getDeviceStatus().remove(deviceStatus);

        Appliance applianceToBeDeleted = getAppliance(applianceId);
        if(applianceToBeDeleted != null) {
            appliances.getAppliances().remove(applianceToBeDeleted);
            save(true, true);
        }
        else {
            logger.error("{}: Appliance not found", applianceId);
        }
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

    public void setControl(String applianceId, Control control) {
        logger.debug("{}: Set control", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.setControl(control);
            save(false, true);
        }
        else {
            logger.error("{}: Appliance not found", applianceId);
        }
    }

    public void deleteControl(String applianceId) {
        logger.debug("{}: Delete control", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.setControl(null);
            save(false, true);
        }
        else {
            logger.error("{}: Appliance not found", applianceId);
        }
    }

    public void setMeter(String applianceId, Meter meter) {
        logger.debug("{}: Set meter", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.setMeter(meter);
            save(false, true);
        }
        else {
            logger.error("{}: Appliance not found", applianceId);
        }
    }

    public void deleteMeter(String applianceId) {
        logger.debug("{}: Delete meter", applianceId);
        Appliance appliance = getAppliance(applianceId);
        if(appliance != null) {
            appliance.setMeter(null);
            save(false, true);
        }
        else {
            logger.error("{}: Appliance not found", applianceId);
        }
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
