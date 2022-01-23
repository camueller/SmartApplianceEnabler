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
package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * A PollPowerMeter calculates power consumption by polling.
 */
public class PollPowerMeter implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(PollPowerMeter.class);
    private String applianceId;
    private GuardedTimerTask pollTimerTask;
    private List<MeterUpdateListener> meterUpdateListeners = new ArrayList<>();

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void start(Timer timer, Integer pollInterval, PollPowerExecutor pollPowerExecutor) {
        this.pollTimerTask = new GuardedTimerTask(this.applianceId, "PollPowerMeter", pollInterval * 1000) {
            @Override
            public void runTask() {
                LocalDateTime now = LocalDateTime.now();
                Double power = pollPowerExecutor.pollPower();
                meterUpdateListeners.forEach(
                        listener -> listener.onMeterUpdate(now, power != null ? power.intValue() : 0, null));
            }
        };
        if(timer != null) {
            timer.schedule(this.pollTimerTask, 0, this.pollTimerTask.getPeriod());
        }
    }

    public void cancelTimer() {
        if(this.pollTimerTask != null) {
            this.pollTimerTask.cancel();
        }
    }

    public void addPowerUpateListener(MeterUpdateListener listener) {
        this.meterUpdateListeners.add(listener);
    }
}
