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

package de.avanux.smartapplianceenabler;

import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.FileHandler;
import de.avanux.smartapplianceenabler.semp.discovery.SempDiscovery;
import de.avanux.smartapplianceenabler.semp.webservice.Device2EM;
import de.avanux.smartapplianceenabler.semp.webservice.SempController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.util.Properties;

@SpringBootApplication
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);
    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(Application.class, args);

        Application application = new Application();
        logger.info("Running version " + application.getVersionInfo());
        application.startSemp();
        application.startApplianceManager();
        application.writePidFile();
    }

    private String getVersionInfo() {
        String versionInfo = "?";
        final String versionFilename = "version.properties";
        Properties props = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream(versionFilename);
        try {
            props.load(is);
            versionInfo = props.getProperty("version") + " " + props.getProperty("build.date");
        }
        catch(IOException e) {
            logger.warn("Error reading version from " + versionFilename, e);
        }
        return versionInfo;
    }
    
    private void startSemp() {
        FileHandler fileHandler = new FileHandler();
        Device2EM device2EM = fileHandler.load(Device2EM.class);
        SempController sempController = applicationContext.getBean(SempController.class);
        sempController.setDevice2EM(device2EM);

        boolean disableDiscovery = Boolean.parseBoolean(System.getProperty("sae.discovery.disable", "false"));
        if(disableDiscovery) {
            logger.warn("SEMP discovery disabled.");
        }
        else {
            logger.debug("Starting SEMP discovery ...");
            Thread discoveryThread = new Thread(new SempDiscovery());
            discoveryThread.start();
            logger.debug("... SEMP discovery started");
        }
    }
    
    private void startApplianceManager() {
        logger.debug("Starting appliance manager ...");
        Thread managerThread = new Thread(ApplianceManager.getInstance());
        managerThread.start();
        logger.debug("... Appliance manager started");
    }
    
    private void writePidFile() {
        String pidFileName = System.getProperty("sae.pidfile");
        if(pidFileName != null) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pid = name.split("@")[0];
            try {
                Writer pidFile = new FileWriter(pidFileName);
                pidFile.write(pid);
                pidFile.close();
                logger.info("PID " + pid + " written to " + pidFileName);
            }
            catch(IOException e) {
                logger.error("Error writing PID file " + pidFileName, e);
            }
        }
    }
}
