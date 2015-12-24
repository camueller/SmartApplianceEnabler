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
package de.avanux.smartapplianceenabler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.semp.discovery.SempDiscovery;

@SpringBootApplication
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class); 

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        
        logger.debug("Starting SEMP discovery ...");
        Thread discoveryThread = new Thread(new SempDiscovery());
        discoveryThread.start();
        logger.debug("... SEMP discovery started");
        
        logger.debug("Starting appliance manager ...");
        Thread managerThread = new Thread(ApplianceManager.getInstance());
        managerThread.start();
        logger.debug("... Appliance manager started");
    }
}
