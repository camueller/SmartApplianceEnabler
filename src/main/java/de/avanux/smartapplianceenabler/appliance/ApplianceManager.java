/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;


public class ApplianceManager implements Runnable {
    public static final String SCHEMA_LOCATION = "http://github.com/camueller/SmartApplianceEnabler/v1.0";
    private Logger logger = LoggerFactory.getLogger(ApplianceManager.class);
    private static ApplianceManager instance;
    private FileHandler fileHandler;
    private Appliances appliances;
    private GpioController gpioController;
    private Timer timer;
    
    private ApplianceManager() {
        fileHandler = new FileHandler();
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
        startAppliances();
    }
    
    public void startAppliances() {
        appliances = fileHandler.load(Appliances.class);
        if(appliances == null) {
            logger.error("No valid appliance configuration found. Exiting ...");
            System.exit(-1);
        }
        logger.info(getAppliances().size() + " appliance(s) configured.");

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

        for (Appliance appliance : getAppliances()) {
            appliance.init();
            appliance.start(timer, gpioController, pulseReceiverIdWithPulseReceiver, modbusIdWithModbusTcp);
        }
    }

    public List<Appliance> getAppliances() {
        if(appliances != null) {
            return appliances.getAppliances();
        }
        return Collections.EMPTY_LIST;
    }
}
