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

package de.avanux.smartapplianceenabler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * The GuardedTimerTask maintains parameters passed to the Timer as well as details to identify itself.
 * It also ensures, the errors during execution will be logged appropriately. Cancellation is logged as well.
 */
abstract public class GuardedTimerTask extends TimerTask {
    private transient Logger logger = LoggerFactory.getLogger(GuardedTimerTask.class);
    private String applianceId;
    private String taskName;
    private long period;

    public GuardedTimerTask(String applianceId, String taskName, long period) {
        this.applianceId = applianceId;
        this.taskName = taskName;
        this.period = period;
        logger.debug("{}: Created timer task name={} period={}ms id={}", this.applianceId != null ? this.applianceId : "",
                taskName, period, this.hashCode());
    }

    public String getTaskName() {
        return taskName;
    }

    public long getPeriod() {
        return period;
    }

    @Override
    public void run() {
        logger.debug("{}: Executing timer task name={} id={}", this.applianceId != null ? this.applianceId : "",
                this.taskName, this.hashCode());
        try  {
            runTask();
        }
        catch(Throwable e) {
            logger.error(applianceId + ": Error executing timer task name=" + taskName + " id=" + this.hashCode(), e);
        }
    }

    @Override
    public boolean cancel() {
        logger.trace("{}: Cancel timer task name={} id={}", this.applianceId != null ? this.applianceId : "",
                taskName, this.hashCode());
        return super.cancel();
    }

    /**
     * Override this method instead of #run().
     */
    abstract public void runTask();
}
