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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.avanux.smartapplianceenabler.HolidaysDownloader;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import de.avanux.smartapplianceenabler.modbus.ModbusTcp;


public class ApplianceManager implements Runnable {
    public static final String SCHEMA_LOCATION = "http://github.com/camueller/SmartApplianceEnabler/v1.1";
    private Logger logger = LoggerFactory.getLogger(ApplianceManager.class);
    private static ApplianceManager instance;
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
    
    public void startAppliances() {
        FileHandler fileHandler = new FileHandler();
        if(appliances == null) {
            appliances = fileHandler.load(Appliances.class);
        }
        if(appliances != null) {
            init();
        }
        else {
            logger.warn("No valid appliance configuration found. Exiting ...");
        }
        logger.info(getAppliances().size() + " appliance(s) configured.");
    }

    public void restartAppliances() {
        Connectivity connectivity = this.appliances.getConnectivity();
        if(connectivity != null) {
            if(connectivity != null) {
                // make PulseReceiver accessible by id
                if (connectivity.getPulseReceivers() != null) {
                    for (PulseReceiver pulseReceiver : connectivity.getPulseReceivers()) {
                        logger.info("Stopping PulseReceiver (" + pulseReceiver.getId() + ") configured for port " + pulseReceiver.getPort());
                        pulseReceiver.stop();
                    }
                }
            }
        }
        logger.info("Restarting appliances ...");
        this.appliances = null;
        startAppliances();
    }

    public List<Appliance> getAppliances() {
        if(appliances != null) {
            return appliances.getAppliances();
        }
        return Collections.EMPTY_LIST;
    }

    public void setAppliances(Appliances appliances) {
        this.appliances = appliances;
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
}
