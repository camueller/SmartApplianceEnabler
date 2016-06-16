package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RunningTimeMonitorTest {

    private Logger logger = LoggerFactory.getLogger(RunningTimeMonitor.class);
    private RunningTimeMonitor runningTimeMonitor;

    public RunningTimeMonitorTest() {
        runningTimeMonitor = new RunningTimeMonitor();
        runningTimeMonitor.setApplianceId("F-00000001-000000000001-00");
    }

    @Test
    public void findAndSetCurrentTimeFrame_noTimeFrames() {
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(12, 0, 0)));
    }

    @Test
    public void findAndSetCurrentTimeFrame_oneTimeFrame() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(11, 0, 0), new TimeOfDay(17, 0, 0));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(10, 0, 0)));
        Assert.assertEquals(timeFrame, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(12, 0, 0)));
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(18, 0, 0)));
    }

    @Test
    public void findAndSetCurrentTimeFrame_oneTimeFrameOverMidnight() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(23, 0, 0), new TimeOfDay(1, 0, 0));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(22, 30, 0)));
        Assert.assertEquals(timeFrame, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(23, 30, 0)));
        Assert.assertEquals(timeFrame, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(0, 30, 0)));
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(1, 30, 0)));
    }

    @Test
    public void findAndSetCurrentTimeFrame_twoTimeFrames() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        TimeFrame timeFrame1 = new TimeFrame(600, 600, new TimeOfDay(11, 0, 0), new TimeOfDay(12, 0, 0));
        timeFrames.add(timeFrame1);
        TimeFrame timeFrame2 = new TimeFrame(1200, 1200, new TimeOfDay(14, 0, 0), new TimeOfDay(15, 0, 0));
        timeFrames.add(timeFrame2);
        runningTimeMonitor.setTimeFrames(timeFrames);
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(10, 0, 0)));
        Assert.assertEquals(timeFrame1, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(11, 30, 0)));
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(12, 30, 0)));
        Assert.assertEquals(timeFrame2, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(14, 30, 0)));
        Assert.assertNull(runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(15, 30, 0)));
    }

    @Test
    public void findAndSetCurrentTimeFrame_multipleTimeFrames_differentDaysOfWeek() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        TimeFrame timeFrame1 = new TimeFrame(3600, 3600, new TimeOfDay(11, 0, 0), new TimeOfDay(12, 0, 0), Arrays.asList(new Integer[]{1, 5}));
        timeFrames.add(timeFrame1);
        TimeFrame timeFrame2 = new TimeFrame(3600, 3600, new TimeOfDay(14, 0, 0), new TimeOfDay(15, 0, 0), Arrays.asList(new Integer[]{2, 3, 4}));
        timeFrames.add(timeFrame2);
        TimeFrame timeFrame3 = new TimeFrame(3600, 3600, new TimeOfDay(16, 0, 0), new TimeOfDay(17, 0, 0), Arrays.asList(new Integer[]{1, 5, 6, 7}));
        timeFrames.add(timeFrame3);
        runningTimeMonitor.setTimeFrames(timeFrames);
        assert_findAndSetCurrentTimeFrame(1, null, timeFrame1, null, null, timeFrame3, null);
        assert_findAndSetCurrentTimeFrame(2, null, null, null, timeFrame2, null, null);
        assert_findAndSetCurrentTimeFrame(3, null, null, null, timeFrame2, null, null);
        assert_findAndSetCurrentTimeFrame(4, null, null, null, timeFrame2, null, null);
        assert_findAndSetCurrentTimeFrame(5, null, timeFrame1, null, null, timeFrame3, null);
        assert_findAndSetCurrentTimeFrame(6, null, null, null, null, timeFrame3, null);
        assert_findAndSetCurrentTimeFrame(7, null, null, null, null, timeFrame3, null);
    }

    private void assert_findAndSetCurrentTimeFrame(int dayOfWeek, TimeFrame timeFrameTime1, TimeFrame timeFrameTime2, TimeFrame timeFrameTime3, TimeFrame timeFrameTime4, TimeFrame timeFrameTime5, TimeFrame timeFrameTime6) {
        Assert.assertEquals(timeFrameTime1, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(dayOfWeek, 10, 30, 0)));
        Assert.assertEquals(timeFrameTime2, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(dayOfWeek, 11, 30, 0)));
        Assert.assertEquals(timeFrameTime3, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(dayOfWeek, 12, 30, 0)));
        Assert.assertEquals(timeFrameTime4, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(dayOfWeek, 14, 30, 0)));
        Assert.assertEquals(timeFrameTime5, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(dayOfWeek, 16, 30, 0)));
        Assert.assertEquals(timeFrameTime6, runningTimeMonitor.findAndSetCurrentTimeFrame(toInstant(dayOfWeek, 17, 30, 0)));
    }

    @Test
    public void findFutureTimeFrames_noTimeFrames() {
        Assert.assertEquals(0, runningTimeMonitor.findFutureTimeFrames(toInstant(12, 0, 0)).size());
    }

    @Test
    public void findFutureTimeFrames_oneTimeFrame() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(11, 0, 0), new TimeOfDay(17, 0, 0));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        List<TimeFrame> futureTimeFrames = runningTimeMonitor.findFutureTimeFrames(toInstant(10, 30, 0));
        Assert.assertEquals(1, futureTimeFrames.size());
        Assert.assertEquals(timeFrame, futureTimeFrames.get(0));
        Assert.assertEquals(0, runningTimeMonitor.findFutureTimeFrames(toInstant(11, 30, 0)).size());
        Assert.assertEquals(0, runningTimeMonitor.findFutureTimeFrames(toInstant(17, 30, 0)).size());
    }

    @Test
    public void findFutureTimeFrames_twoTimeFrames() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        TimeFrame timeFrame1 = new TimeFrame(600, 600, new TimeOfDay(11, 0, 0), new TimeOfDay(12, 0, 0));
        timeFrames.add(timeFrame1);
        TimeFrame timeFrame2 = new TimeFrame(1200, 1200, new TimeOfDay(14, 0, 0), new TimeOfDay(15, 0, 0));
        timeFrames.add(timeFrame2);
        runningTimeMonitor.setTimeFrames(timeFrames);
        List<TimeFrame> futureTimeFrames = runningTimeMonitor.findFutureTimeFrames(toInstant(10, 30, 0));
        Assert.assertEquals(2, futureTimeFrames.size());
        Assert.assertEquals(timeFrame1, futureTimeFrames.get(0));
        Assert.assertEquals(timeFrame2, futureTimeFrames.get(1));
        futureTimeFrames = runningTimeMonitor.findFutureTimeFrames(toInstant(11, 30, 0));
        Assert.assertEquals(1, futureTimeFrames.size());
        Assert.assertEquals(timeFrame2, futureTimeFrames.get(0));
        futureTimeFrames = runningTimeMonitor.findFutureTimeFrames(toInstant(12, 30, 0));
        Assert.assertEquals(1, futureTimeFrames.size());
        Assert.assertEquals(timeFrame2, futureTimeFrames.get(0));
        futureTimeFrames = runningTimeMonitor.findFutureTimeFrames(toInstant(14, 30, 0));
        Assert.assertEquals(0, futureTimeFrames.size());
        futureTimeFrames = runningTimeMonitor.findFutureTimeFrames(toInstant(15, 30, 0));
        Assert.assertEquals(0, futureTimeFrames.size());
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_noTimeFrames() {
        Assert.assertEquals(0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame());
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame() {
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(11, 0, 0), new TimeOfDay(17, 0, 0));
        runningTimeMonitor.setTimeFrames(Collections.singletonList(timeFrame));
        for(int day=1;day<3;day++) {
            logger.debug("************ Day " + day + " ************");
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
            Assert.assertEquals("Running time may exceed LatestEnd",
                    3300, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(16, 15, 0)));
            Assert.assertEquals("Running time left right before LatestEnd",
                    601, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(16, 59, 59)));
            runningTimeMonitor.setRunning(false, toInstant(17, 0, 0));
            Assert.assertEquals("Running time remains unchanged after power off",
                    601, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(17, 0, 0)));
        }
    }

    @Test
    public void getRemainingMinRunningTimeOfCurrentTimeFrame_2TimeFrames() {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();
        timeFrames.add(new TimeFrame(600, 600, new TimeOfDay(11, 0, 0), new TimeOfDay(12, 0, 0)));
        timeFrames.add(new TimeFrame(1200, 1200, new TimeOfDay(14, 0, 0), new TimeOfDay(15, 0, 0)));
        runningTimeMonitor.setTimeFrames(timeFrames);
        for(int day=1;day<3;day++) {
            logger.debug("************ Day " + day + " ************");

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
            runningTimeMonitor.setRunning(false, toInstant(11, 40, 0));
            Assert.assertEquals("Running time has to be 0 at the end of running time",
                    0, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(11, 45, 0)));
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
            Assert.assertEquals("Min running time has been exceeded by 10 minutes",
                    -600, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 45, 0)));
            runningTimeMonitor.setRunning(false, toInstant(14, 45, 0));
            Assert.assertEquals("Running time remains negative after power off",
                    -600, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(14, 45, 0)));
        }
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
        Assert.assertEquals("With timeframe started and device switched on 6 minutes ago remaining min running time has become negative",
                -60, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(toInstant(9, 6, 0)));
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
        Assert.assertEquals("With timeframe started and device switched on one hour plus one minute ago remaining max running time has become negative",
                -60, runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(toInstant(10, 1, 0)));
    }

    private Instant toInstant(Integer hour, Integer minute, Integer second) {
        return new TimeOfDay(hour, minute, second).toLocalTime().toDateTimeToday().toInstant();
    }

    private ReadableInstant toInstant(int dayOfWeek, Integer hour, Integer minute, Integer second) {
        DateTime instant = new DateTime(2016, 6, 13, hour, minute, second);
        instant = instant.plusHours((dayOfWeek - 1) * 24);
        return instant;
    }

}
