package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunningTimeMonitorTest extends TestBase {

    private Logger logger = LoggerFactory.getLogger(RunningTimeMonitor.class);
    private RunningTimeMonitor runningTimeMonitor;

    public RunningTimeMonitorTest() {
        runningTimeMonitor = new RunningTimeMonitor();
        runningTimeMonitor.setApplianceId("F-00000001-000000000001-00");
    }

    @Test
    public void updateActiveTimeframeInterval_noTimeFrames() {
        runningTimeMonitor.updateActiveTimeframeInterval(new LocalDateTime());
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_noTimeFrames() {
        Assert.assertEquals(0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(new LocalDateTime()));
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame() {
        Schedule schedule = new Schedule(7200, 7200, new TimeOfDay(11, 0, 0),
                new TimeOfDay(17, 0, 0));
        runningTimeMonitor.setSchedules(Collections.singletonList(schedule));
        for(int day=1;day<3;day++) {
            logger.debug("************ Day " + day + " ************");
            Assert.assertEquals("Timeframe not yet started should return 0",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(10, 0, 0)));
            Assert.assertEquals("With timeframe started but device switched off max running time should be returned",
                    7200, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(11, 30, 0)));

            runningTimeMonitor.setRunning(true, toToday(12, 10, 0));
            Assert.assertEquals("With timeframe started and device switched on max running time should be reduced by 5 minutes",
                    6900, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(12, 15, 0)));
            runningTimeMonitor.setRunning(false, toToday(13, 10, 0));
            Assert.assertEquals("With timeframe started and device switched off running time of one hour remains",
                    3600, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(13, 15, 0)));

            runningTimeMonitor.setRunning(true, toToday(16, 10, 0));
            Assert.assertEquals("Running time may exceed LatestEnd",
                    3300, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(16, 15, 0)));
            Assert.assertEquals("Running time left right before LatestEnd",
                    601, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(16, 59, 59)));
            runningTimeMonitor.setRunning(false, toToday(17, 0, 0));
            Assert.assertEquals("No timeframe after power off should return 0",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(
                            toToday(17, 0, 0)));
        }
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_2TimeFrames() {
        List<Schedule> schedules = new ArrayList<Schedule>();
        schedules.add(new Schedule(600, 600, new TimeOfDay(11, 0, 0),
                new TimeOfDay(12, 0, 0)));
        schedules.add(new Schedule(1200, 1200, new TimeOfDay(14, 0, 0),
                new TimeOfDay(15, 0, 0)));
        runningTimeMonitor.setSchedules(schedules);
        for(int day=1;day<3;day++) {
            logger.debug("************ Day " + day + " ************");

            //
            // 1. timeframe
            //
            Assert.assertEquals("Timeframe not yet started should return 0",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(10, 0, 0)));
            Assert.assertEquals("With timeframe started but device switched off max running time should be returned",
                    600, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(11, 20, 0)));
            runningTimeMonitor.setRunning(true, toToday(11, 30, 0));
            Assert.assertEquals("With timeframe started and device switched on max running time should be reduced by 5 minutes",
                    300, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(11, 35, 0)));
            Assert.assertEquals("Running time has to be 0 at the end of the timeframe",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(11, 40, 0)));
            runningTimeMonitor.setRunning(false, toToday(11, 40, 0));
            Assert.assertEquals("Running time has to be 0 at the end of running time",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(11, 45, 0)));
            //
            // 2. timeframe
            //
            Assert.assertEquals("Timeframe not yet started should return 0",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(13, 45, 0)));
            Assert.assertEquals("With timeframe started but device switched off max running time should be returned",
                    1200, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(14, 10, 0)));
            runningTimeMonitor.setRunning(true, toToday(14, 15, 0));
            Assert.assertEquals("With timeframe started and device switched on max running time should be reduced by 5 minutes",
                    900, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(14, 20, 0)));
            Assert.assertEquals("Min running time has been exceeded by 10 minutes",
                    -600, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(14, 45, 0)));
            runningTimeMonitor.setRunning(false, toToday(14, 45, 0));
            Assert.assertEquals("Running time after power off is 0",
                    0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(14, 45, 0)));
        }
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_MinRunningTimeDifferentFromMaxRunningTime() {
        Schedule schedule = new Schedule(300, 3600, new TimeOfDay(8, 0, 0),
                new TimeOfDay(22, 59, 59));
        runningTimeMonitor.setSchedules(Collections.singletonList(schedule));
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(7, 0, 0)));
        runningTimeMonitor.setRunning(true, toToday(9, 00, 0));
        Assert.assertEquals("With timeframe started and device switched on one minute ago 4 minutes are left",
                240, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(9, 1, 0)));
        Assert.assertEquals("With timeframe started and device switched on 5 minutes ago no running time is left",
                0, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(9, 5, 0)));
        Assert.assertEquals("With timeframe started and device switched on 6 minutes ago remaining min running time has become negative",
                -60, getRemainingMinRunningTimeOfCurrentTimeFrame(toToday(9, 6, 0)));
    }

    @Test
    public void getRemainingMaxRunningTimeOfCurrentTimeFrame_MinRunningTimeDifferentFromMaxRunningTime() {
        Schedule schedule = new Schedule(300, 3600, new TimeOfDay(8, 0, 0),
                new TimeOfDay(22, 59, 59));
        runningTimeMonitor.setSchedules(Collections.singletonList(schedule));
        Assert.assertEquals("Timeframe not yet started should return 0",
                0, getRemainingMaxRunningTimeOfCurrentTimeFrame(toToday(7, 0, 0)));
        runningTimeMonitor.setRunning(true, toToday(9, 00, 0));
        Assert.assertEquals("With timeframe started and device switched on one minute ago 59 minutes are left",
                3540, getRemainingMaxRunningTimeOfCurrentTimeFrame(toToday(9, 1, 0)));
        Assert.assertEquals("With timeframe started and device switched on one hour ago no running time is left",
                0, getRemainingMaxRunningTimeOfCurrentTimeFrame(toToday(10, 0, 0)));
        Assert.assertEquals("With timeframe started and device switched on one hour plus one minute ago remaining max running time has become negative",
                -60, getRemainingMaxRunningTimeOfCurrentTimeFrame(toToday(10, 1, 0)));
    }

    private int getRemainingMinRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        runningTimeMonitor.updateActiveTimeframeInterval(now);
        return runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now);
    }

    private int getRemainingMaxRunningTimeOfCurrentTimeFrame(LocalDateTime now) {
        runningTimeMonitor.updateActiveTimeframeInterval(now);
        return runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now);
    }
}
