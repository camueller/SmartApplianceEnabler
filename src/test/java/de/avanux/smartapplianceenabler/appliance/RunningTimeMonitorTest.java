package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunningTimeMonitorTest {

    private RunningTimeMonitor runningTimeMonitor;

    public RunningTimeMonitorTest() {
        runningTimeMonitor = new RunningTimeMonitor();
        runningTimeMonitor.setApplianceId("F-00000001-000000000001-00");
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_noTimeFrames() {
        Assert.assertEquals(0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame());
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(11, 0, 0), new TimeOfDay(17, 0, 0));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(10, 0, 0)));
        Assert.assertEquals("With timeframe started but device switched off max running time should be returned",
                7200, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(11, 30, 0)));

        runningTimeMonitor.setRunning(true, toInstant(12, 10, 0));
        Assert.assertEquals("With timeframe started and device switched on max running time should be reduced by 5 minutes",
                6900, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(12, 15, 0)));
        runningTimeMonitor.setRunning(false, toInstant(13, 10, 0));
        Assert.assertEquals("With timeframe started and device switched off running time of one hour remains",
                3600, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(13, 15, 0)));

        runningTimeMonitor.setRunning(true, toInstant(16, 10, 0));
        Assert.assertEquals("If running time would exceed the timeframe it has to be truncated",
                2700, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(16, 15, 0)));
        Assert.assertEquals("Running time has to be 0 at the end of the timeframe",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(17, 0, 0)));
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_2TimeFrames() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        timeFrames.add(new TimeFrame(600, 600, new TimeOfDay(11, 0, 0), new TimeOfDay(12, 0, 0)));
        timeFrames.add(new TimeFrame(1200, 1200, new TimeOfDay(14, 0, 0), new TimeOfDay(15, 0, 0)));
        runningTimeMonitor.setTimeFrames(timeFrames);
        //
        // 1. timeframe
        //
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(10, 0, 0)));
        Assert.assertEquals("With timeframe started but device switched off max running time should be returned",
                600, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(11, 20, 0)));
        runningTimeMonitor.setRunning(true, toInstant(11, 30, 0));
        Assert.assertEquals("With timeframe started and device switched on max running time should be reduced by 5 minutes",
                300, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(11, 35, 0)));
        Assert.assertEquals("Running time has to be 0 at the end of the timeframe",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(11, 40, 0)));
        runningTimeMonitor.setRunning(false, toInstant(14, 40, 0));
        Assert.assertEquals("Running time has to be 0 at the end of running time",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 45, 0)));
        //
        // 2. timeframe
        //
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(13, 45, 0)));
        Assert.assertEquals("With timeframe started but device switched off max running time should be returned",
                1200, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 10, 0)));
        runningTimeMonitor.setRunning(true, toInstant(14, 15, 0));
        Assert.assertEquals("With timeframe started and device switched on max running time should be reduced by 5 minutes",
                900, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 20, 0)));
        Assert.assertEquals("Running time has to be 0 at the end of the timeframe",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 45, 0)));
        runningTimeMonitor.setRunning(false, toInstant(14, 45, 0));
        Assert.assertEquals("Running time has to be 0 at the end of running time",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 45, 0)));
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_MinRunningTimeDifferentFromMaxRunningTime() {
        TimeFrame timeFrame = new TimeFrame(300, 3600, new TimeOfDay(8, 0, 0), new TimeOfDay(22, 59, 59));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(7, 0, 0)));
        runningTimeMonitor.setRunning(true, toInstant(9, 00, 0));
        Assert.assertEquals("With timeframe started and device switched on one minute ago 4 minutes are left",
                240, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(9, 1, 0)));
        Assert.assertEquals("With timeframe started and device switched on 5 minutes ago no running time is left",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(9, 5, 0)));
        Assert.assertEquals("With timeframe started and device switched on 6 minutes ago no running time is left",
                0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(9, 6, 0)));
    }

    @Test
    public void getRemainingMaxRunningTimeOfCurrentTimeFrame_MinRunningTimeDifferentFromMaxRunningTime() {
        TimeFrame timeFrame = new TimeFrame(300, 3600, new TimeOfDay(8, 0, 0), new TimeOfDay(22, 59, 59));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(toInstant(7, 0, 0)));
        runningTimeMonitor.setRunning(true, toInstant(9, 00, 0));
        Assert.assertEquals("With timeframe started and device switched on one minute ago 59 minutes are left",
                3540, runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(toInstant(9, 1, 0)));
        Assert.assertEquals("With timeframe started and device switched on one hour ago no running time is left",
                0, runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(toInstant(10, 0, 0)));
        Assert.assertEquals("With timeframe started and device switched on one hour plus one minute ago no running time is left",
                0, runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(toInstant(10, 1, 0)));
    }

    private Instant toInstant(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday().toInstant();
    }
}
