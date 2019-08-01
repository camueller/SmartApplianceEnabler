/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.schedule.Request;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
import org.joda.time.LocalDateTime;

public class RuntimeState implements Cloneable {
    public TimeframeInterval activeTimeframeInterval;
    public Request activeRequest;
    public boolean running;
    public boolean wasRunning;
    public Integer runningTime;
    public boolean interrupted;
    public LocalDateTime statusChangedAt;
    // remaining min running time while not running or when running started
    public Integer remainingMinRunningTimeWhileNotRunning;
    // remaining max running time while not running or when running started
    public Integer remainingMaxRunningTimeWhileNotRunning;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
