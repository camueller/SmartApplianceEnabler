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

package de.avanux.smartapplianceenabler.control;

public class StartingCurrentSwitchDefaults {
    // static members won't be serialized but we need those values on the client
    private Integer powerThreshold = 15;
    private Integer startingCurrentDetectionDuration = 30; // seconds
    private Integer finishedCurrentDetectionDuration = 300; // seconds
    private Integer minRunningTime = 600; // seconds
    private Integer pollInterval = 20; // seconds
    private static StartingCurrentSwitchDefaults instance = new StartingCurrentSwitchDefaults();

    public static Integer getPowerThreshold() {
        return instance.powerThreshold;
    }

    public static Integer getStartingCurrentDetectionDuration() {
        return instance.startingCurrentDetectionDuration;
    }

    public static Integer getFinishedCurrentDetectionDuration() {
        return instance.finishedCurrentDetectionDuration;
    }

    public static Integer getMinRunningTime() {
        return instance.minRunningTime;
    }

    public static Integer getPollInterval() {
        return instance.pollInterval;
    }
}
