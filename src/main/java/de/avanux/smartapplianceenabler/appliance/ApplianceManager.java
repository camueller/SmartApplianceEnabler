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
    private static final int RUNNING_TIME_UPDATE_INTERVAL_SECONDS = 60;
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
            RunningTimeMonitor runningTimeMonitor = null;
            if(appliance.getTimeFrames() != null) {
                runningTimeMonitor = new RunningTimeMonitor();
                runningTimeMonitor.setTimeFrames(appliance.getTimeFrames());
                appliance.setRunningTimeMonitor(runningTimeMonitor);
            }
            
            for(Control control : appliance.getControls()) {
                control.setRunningTimeController(runningTimeMonitor);
            }
            
            for(GpioControllable gpioControllable : appliance.getGpioControllables()) {
                gpioControllable.setGpioController(gpioController);
                gpioControllable.start();
            }

            List<Switch> switches = appliance.getSwitches();

            S0ElectricityMeter s0ElectricityMeter = appliance.getS0ElectricityMeter();
            if(s0ElectricityMeter != null) {
                s0ElectricityMeter.setApplianceId(appliance.getId());
                if(switches != null && switches.size() > 0) {
                    s0ElectricityMeter.setControl(switches.get(0));
                }
            }

            S0ElectricityMeterNetworked s0ElectricityMeterNetworked = appliance.getS0ElectricityMeterNetworked();
            if(s0ElectricityMeterNetworked != null) {
                String pulseReceiverId = s0ElectricityMeterNetworked.getIdref();
                PulseReceiver pulseReceiver = pulseReceiverIdWithPulseReceiver.get(pulseReceiverId);
                s0ElectricityMeterNetworked.setApplianceId(appliance.getId());
                s0ElectricityMeterNetworked.setPulseReceiver(pulseReceiver);
                s0ElectricityMeterNetworked.start();
            }

            for(ModbusSlave modbusSlave : appliance.getModbusSlaves()) {
                String modbusId = modbusSlave.getIdref();
                ModbusTcp modbusTcp = modbusIdWithModbusTcp.get(modbusId);
                modbusSlave.setModbusTcp(modbusTcp);
                modbusSlave.start(timer);
            }
        }
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Appliance appliance : getAppliances()) {
                    RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
                    if(runningTimeMonitor != null) {
                        runningTimeMonitor.update();
                        logger.debug("Appliance " + appliance.getId() + ": Remaining running time " + runningTimeMonitor.getRemainingMinRunningTime() + " s (" + runningTimeMonitor.getCurrentTimeFrame() + ")");
                    }
                }
            }
        }, RUNNING_TIME_UPDATE_INTERVAL_SECONDS * 1000, RUNNING_TIME_UPDATE_INTERVAL_SECONDS * 1000);
    }

    public List<Appliance> getAppliances() {
        if(appliances != null) {
            return appliances.getAppliances();
        }
        return Collections.EMPTY_LIST;
    }
}
