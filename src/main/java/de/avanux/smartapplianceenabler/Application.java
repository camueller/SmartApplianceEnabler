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
import de.avanux.smartapplianceenabler.semp.discovery.SempDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);
    private static ConfigurableApplicationContext applicationContext;
    private static Properties versionProperties;

    public static void main(String[] args) {
        try {
            applicationContext = SpringApplication.run(Application.class, args);

            loadVersionProperties();

            Application application = new Application();
            logger.info("Running version " + application.getVersionInfo());
            application.startSemp();
            application.startApplianceManager();
            application.writePidFile();
        }
        catch(Throwable e) {
            logger.error("Fatal error", e);
        }
    }

    private static void loadVersionProperties() {
        final String versionFilename = "version.properties";
        versionProperties = new Properties();
        InputStream is = Application.class.getClassLoader().getResourceAsStream(versionFilename);
        try {
            versionProperties.load(is);
        }
        catch(IOException e) {
            logger.warn("Error reading version from " + versionFilename, e);
        }
    }

    private String getVersionInfo() {
        String versionInfo = "?";
        if(versionProperties != null && versionProperties.size() > 0) {
            versionInfo = versionProperties.getProperty("version") + " " + versionProperties.getProperty("build.date");
        }
        return versionInfo;
    }

    public static String getVersion() {
        String version = "";
        if(versionProperties != null && versionProperties.size() > 0) {
            version = versionProperties.getProperty("version");
        }
        return version;
    }

    public static String getBuildDate() {
        String buildDate = "";
        if(versionProperties != null && versionProperties.size() > 0) {
            buildDate = versionProperties.getProperty("build.date");
        }
        return buildDate;
    }

    private void startSemp() {
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
