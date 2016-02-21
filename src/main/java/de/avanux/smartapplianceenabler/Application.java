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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.semp.discovery.SempDiscovery;

@SpringBootApplication
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class); 

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        Application application = new Application();
        application.configureLogging();
        application.startSemp();
        application.startApplianceManager();
        application.writePidFile();
    }
    
    private void startSemp() {
        logger.debug("Starting SEMP discovery ...");
        Thread discoveryThread = new Thread(new SempDiscovery());
        discoveryThread.start();
        logger.debug("... SEMP discovery started");
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
    
    private void configureLogging() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        ple.setContext(lc);
        ple.start();

        String logfileString = System.getProperty("sae.logfile");
        FileAppender<ILoggingEvent> fileAppender = null;
        if(logfileString != null) {
            fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setFile(logfileString);
            fileAppender.setEncoder(ple);
            fileAppender.setContext(lc);
            fileAppender.start();
        }

        ch.qos.logback.classic.Level level = ch.qos.logback.classic.Level.INFO;
        String levelString = System.getProperty("sae.loglevel");
        if(levelString != null) {
            level = ch.qos.logback.classic.Level.valueOf(levelString);    
        }
        logger.info("Logging configured with log level " + level);
        
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("de.avanux");
        if(fileAppender != null) {
            logger.addAppender(fileAppender);
        }
        logger.setLevel(level);
        logger.setAdditive(false); /* set to true if logging not already configured by Spring */
        
        if(fileAppender != null) {
            ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            rootLogger.addAppender(fileAppender);
        }
    }
}
