package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

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

    private Instant toInstant(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday().toInstant();
    }
}
