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

package de.avanux.smartapplianceenabler.appliance;

public class RuntimeRequest {
    // private TimeframeInterval timeframeInterval;
    private Long earliestStart;
    private Long latestEnd;
    private Long minRunningTime;
    private Long maxRunningTime;

    public RuntimeRequest() {
    }

    public RuntimeRequest(Long earliestStart, Long latestEnd, Long minRunningTime, Long maxRunningTime) {
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
        this.minRunningTime = minRunningTime;
        this.maxRunningTime = maxRunningTime;
    }

    public Long getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(Long earliestStart) {
        this.earliestStart = earliestStart;
    }

    public Long getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(Long latestEnd) {
        this.latestEnd = latestEnd;
    }

    public Long getMinRunningTime() {
        return minRunningTime;
    }

    public void setMinRunningTime(Long minRunningTime) {
        this.minRunningTime = minRunningTime;
    }

    public Long getMaxRunningTime() {
        return maxRunningTime;
    }

    public void setMaxRunningTime(Long maxRunningTime) {
        this.maxRunningTime = maxRunningTime;
    }

    @Override
    public String toString() {
        return earliestStart
                + "s-" + latestEnd
                + "s:" + (minRunningTime != null ? minRunningTime : "-")
                + "s/" + maxRunningTime + "s";
    }
}
