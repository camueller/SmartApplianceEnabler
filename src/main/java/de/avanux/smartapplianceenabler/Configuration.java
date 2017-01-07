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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages configuration values.
 */
public class Configuration {
    private static Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final String TIMEFRAME_INTERVAL_ADDITIONAL_RUNNING_TIME = "TIMEFRAME_INTERVAL_ADDITIONAL_RUNNING_TIME";
    private static Configuration instance;
    private Integer timeframeIntervalAdditionalSeconds;

    public synchronized static Configuration getInstance() {
        if(instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * Returns the running time to be added to the maximum running time of the schedule during search for the next sufficient timeframe interval.
     * This additional time can be used for processing by the Sunny Home Manager while still being able to fit running time into the timeframe.
     * @return the number of seconds to be added
     */
    public int getTimeframeIntervalAdditionalRunningTime() {
        if(timeframeIntervalAdditionalSeconds == null) {
            String additionalSecondsString = System.getProperty(TIMEFRAME_INTERVAL_ADDITIONAL_RUNNING_TIME);
            if(additionalSecondsString != null) {
                timeframeIntervalAdditionalSeconds = Integer.valueOf(additionalSecondsString);
                logger.debug("Using configured value for timeframeIntervalAdditionalSeconds=" + timeframeIntervalAdditionalSeconds);
            }
            else {
                timeframeIntervalAdditionalSeconds = 900;
                logger.debug("Using default value for timeframeIntervalAdditionalSeconds=" + timeframeIntervalAdditionalSeconds);
            }
        }
        return timeframeIntervalAdditionalSeconds;
    }

    /**
     * Set the running time to be added to the maximum running time of the schedule during search for the next sufficient timeframe interval.
     * @param timeframeIntervalAdditionalRunningTime the number of seconds to be added
     */
    public void setTimeframeIntervalAdditionalRunningTime(Integer timeframeIntervalAdditionalRunningTime) {
        this.timeframeIntervalAdditionalSeconds = timeframeIntervalAdditionalRunningTime;
    }
}
