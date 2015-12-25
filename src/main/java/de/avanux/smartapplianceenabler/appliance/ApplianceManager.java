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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;


public class ApplianceManager implements Runnable {
    private Logger logger = LoggerFactory.getLogger(ApplianceManager.class);
    public static final String SCHEMA_LOCATION = "http://github.com/camueller/SmartApplianceEnabler/v1.0";
    private static ApplianceManager instance;
    private FileHandler fileHandler;
    private Appliances appliances;
    private GpioController gpioController;
    
    private ApplianceManager() {
        fileHandler = new FileHandler();
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
            ApplianceConfiguration configuration = appliance.getConfiguration();
            if(configuration != null) {
                if(appliance.getConfiguration() != null) {
                    for(GpioControllable gpioControllable : configuration.getGpioControllables()) {
                        gpioControllable.setGpioController(gpioController);
                        gpioControllable.start();
                    }
                }
            }
        }
    }

    public List<Appliance> getAppliances() {
        return appliances.getAppliance();
    }
}
