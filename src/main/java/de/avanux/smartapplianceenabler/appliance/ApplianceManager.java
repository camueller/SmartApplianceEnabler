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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;


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
        try {
            gpioController = GpioFactory.getInstance();
        }
        catch(UnsatisfiedLinkError e) {
            logger.error("GPIO access disabled. Probably not running on Raspberry Pi.");
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
        logger.info(getAppliances().size() + " appliance(s) configured.");
        for (Appliance appliance : getAppliances()) {
            RunningTimeMonitor runningTimeMonitor = null;
            if(appliance.getTimeFrames() != null) {
                runningTimeMonitor = new RunningTimeMonitor();
                runningTimeMonitor.setTimeFrames(appliance.getTimeFrames());
                appliance.setRunningTimeMonitor(runningTimeMonitor);
            }
            
            for(Control control : appliance.getControls()) {
                control.setRunningTimeMonitor(runningTimeMonitor);
            }
            
            for(GpioControllable gpioControllable : appliance.getGpioControllables()) {
                gpioControllable.setGpioController(gpioController);
                gpioControllable.start();
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
        return appliances.getAppliance();
    }
}
