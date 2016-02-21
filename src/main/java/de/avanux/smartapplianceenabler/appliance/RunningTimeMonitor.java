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
package de.avanux.smartapplianceenabler.appliance;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Instant;
import org.joda.time.Interval;

public class RunningTimeMonitor implements RunningTimeController {
    private List<TimeFrame> timeFrames;
    private TimeFrame currentTimeFrame;
    private Instant intervalBeginn;
    private Instant statusChangedAt;
    private Long remainingMinRunningTime;
    private boolean running;

    public void setTimeFrames(List<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
    }
    
    public List<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void setRunning(boolean running) {
        this.running = running;
        statusChangedAt = new Instant();
    }

    public long getRemainingMinRunningTime() {
        if(remainingMinRunningTime == null) {
            update();
        }
        return remainingMinRunningTime != null ? remainingMinRunningTime : 0;
    }
    
    public TimeFrame getCurrentTimeFrame() {
        return currentTimeFrame;
    }

    public void update() {
        Instant now = new Instant();
        TimeFrame timeFrameForInstant = findCurrentTimeFrame(now);
        if(timeFrameForInstant != null) {
            if(currentTimeFrame == null || ! timeFrameForInstant.equals(currentTimeFrame)) {
                // timeframe changed
                currentTimeFrame = timeFrameForInstant;
                if(currentTimeFrame.getInterval().getStart().isBefore(now)) {
                    // timeframe started in past; remaining time is less than MinRunningTime
                    remainingMinRunningTime = Double.valueOf(new Interval(now, currentTimeFrame.getInterval().getEnd()).toDurationMillis() / 1000).longValue();
                }
                else {
                    // timeframe starts in future
                    remainingMinRunningTime = timeFrameForInstant.getMinRunningTime();
                }
            }
            Interval interval = null;
            if(running) {
                if(intervalBeginn == null) {
                    // running was set to true after interval begin
                    interval = new Interval(statusChangedAt, now);
                }
                else {
                    // no status change in interval
                    interval = new Interval(intervalBeginn, now);
                }
                intervalBeginn = now;
            }
            else if (intervalBeginn != null && statusChangedAt != null) {
                // running was set to false after interval begin
                interval = new Interval(intervalBeginn, statusChangedAt);
                intervalBeginn = null;
                statusChangedAt = null;
            }
            if(interval != null) {
                remainingMinRunningTime = remainingMinRunningTime - Double.valueOf(interval.toDuration().getMillis() / 1000).longValue();
            }
        }
    }
    
    public TimeFrame findCurrentTimeFrame(Instant instant) {
        if(timeFrames != null) {
            for(TimeFrame timeFrame : timeFrames) {
                if(timeFrame.getInterval().contains(instant)) {
                    return timeFrame;
                }
            }
        }
        return null;
    }
    
    public List<TimeFrame> findFutureTimeFrames(Instant instant) {
        List<TimeFrame> futureTimeFrames = new ArrayList<TimeFrame>();
        for(TimeFrame timeFrame : timeFrames) {
            if(timeFrame.getInterval().getStart().isAfter(instant)) {
                futureTimeFrames.add(timeFrame);
            }
        }
        return futureTimeFrames;
    }
}
