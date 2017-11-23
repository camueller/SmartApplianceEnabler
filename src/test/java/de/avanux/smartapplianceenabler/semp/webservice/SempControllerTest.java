package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.matchers.Any;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class SempControllerTest extends TestBase {

    public static final String DEVICE_ID = "DeviceID1";
    private SempController sempController;

    public SempControllerTest() {
        sempController = new SempController();
    }

    @Test
    public void getPlanningRequest_intervalAlreadyActive() {
        Identification identification = new Identification();
        identification.setDeviceId(DEVICE_ID);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        setDeviceInfo(deviceInfo);

        Appliance appliance = new Appliance();
        appliance.setId(DEVICE_ID);

        LocalDateTime now = toToday(9, 30, 0);
        int remainingMaxRunningTime = 1800;
        Schedule schedule = new Schedule(600, 600, new TimeOfDay(now.minusSeconds(1)), new TimeOfDay(now.plusSeconds(remainingMaxRunningTime)));
        de.avanux.smartapplianceenabler.appliance.Timeframe timeframe = schedule.getTimeframe();
        timeframe.setSchedule(schedule);
        RunningTimeMonitor runningTimeMonitor = mock(RunningTimeMonitor.class);
        when(runningTimeMonitor.getSchedules()).thenReturn(Collections.singletonList(schedule));
        when(runningTimeMonitor.getActiveTimeframeInterval()).thenReturn(timeframe.getIntervals(now).get(0));
        when(runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame()).thenReturn(schedule.getMinRunningTime());
        when(runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame()).thenReturn(schedule.getMaxRunningTime());
        appliance.setRunningTimeMonitor(runningTimeMonitor);

        Appliances appliances = new Appliances();
        appliances.setAppliances(Collections.singletonList(appliance));
        ApplianceManager.getInstance().setAppliances(appliances);

        Device2EM device2EM = sempController.createDevice2EM(now);
        List<PlanningRequest> planningRequests = device2EM.getPlanningRequest();
        Assert.assertEquals(1, planningRequests.size());
        List<Timeframe> timeframes = planningRequests.get(0).getTimeframes();
        Assert.assertEquals(3, timeframes.size());
        assertTimeframe(timeframes.get(0), 0l, 1800l, 599l, 600l);
        assertTimeframe(timeframes.get(1), 86399l, 88200l, 599l, 600l);
        assertTimeframe(timeframes.get(2), 172799l, 174600l, 599l, 600l);
    }

    @Test
    public void getPlanningRequest_startingCurrentDetected() {
        Identification identification = new Identification();
        identification.setDeviceId(DEVICE_ID);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        setDeviceInfo(deviceInfo);

        Appliance appliance = new Appliance();
        appliance.setId(DEVICE_ID);

        LocalDateTime now = toToday(9, 0, 0);
        Schedule schedule = new Schedule(3600, 3600, new TimeOfDay(11, 0, 0), new TimeOfDay(13, 0, 0));
        de.avanux.smartapplianceenabler.appliance.Timeframe timeframe = schedule.getTimeframe();
        timeframe.setSchedule(schedule);
        RunningTimeMonitor runningTimeMonitor = mock(RunningTimeMonitor.class);
        when(runningTimeMonitor.getActiveTimeframeInterval()).thenReturn(timeframe.getIntervals(now).get(0));
        when(runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame()).thenReturn(schedule.getMinRunningTime());
        when(runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame()).thenReturn(schedule.getMaxRunningTime());
        appliance.setRunningTimeMonitor(runningTimeMonitor);

        Appliances appliances = new Appliances();
        appliances.setAppliances(Collections.singletonList(appliance));
        ApplianceManager.getInstance().setAppliances(appliances);

        appliance.startingCurrentDetected();

        // check timeframes for the first time after activation
        Device2EM device2EM = sempController.createDevice2EM(now);
        List<PlanningRequest> planningRequests = device2EM.getPlanningRequest();
        Assert.assertEquals(1, planningRequests.size());
        List<Timeframe> timeframes = planningRequests.get(0).getTimeframes();
        Assert.assertEquals(1, timeframes.size());

        // check again in order to make sure that the timeframe remains active
        device2EM = sempController.createDevice2EM(now);
        planningRequests = device2EM.getPlanningRequest();
        Assert.assertEquals(1, planningRequests.size());
        timeframes = planningRequests.get(0).getTimeframes();
        Assert.assertEquals(1, timeframes.size());

        appliance.finishedCurrentDetected();
        when(runningTimeMonitor.getActiveTimeframeInterval()).thenReturn(null);

        // check timeframes for the first time after deactivation
        device2EM = sempController.createDevice2EM(now);
        planningRequests = device2EM.getPlanningRequest();
        Assert.assertEquals(0, planningRequests.size());

        // check again in order to make sure that the timeframe remains inactive
        device2EM = sempController.createDevice2EM(now);
        planningRequests = device2EM.getPlanningRequest();
        Assert.assertEquals(0, planningRequests.size());
    }

    @Test
    public void createSempTimeFrame() {
        ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
        logger.setApplianceId(DEVICE_ID);

        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, 7200, new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe sempTimeFrame = sempController
                .createSempTimeFrame(logger, DEVICE_ID, schedule, interval, 0, 0, now);
        Assert.assertEquals(1800, (long) sempTimeFrame.getEarliestStart());
        Assert.assertEquals(30600, (long) sempTimeFrame.getLatestEnd());
    }

    @Test
    public void createSempTimeFrame_TimeFrameOverMidnight_BeforeMidnight() {
        ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
        logger.setApplianceId(DEVICE_ID);

        LocalDateTime now = toToday(23, 30, 0);
        Schedule schedule = new Schedule(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe sempTimeFrame = sempController
                .createSempTimeFrame(logger, DEVICE_ID, schedule, interval, 0, 0, now);
        Assert.assertEquals(0, (long) sempTimeFrame.getEarliestStart());
        Assert.assertEquals(16200, (long) sempTimeFrame.getLatestEnd());
    }

    @Test
    public void createSempTimeFrame_TimeFrameOverMidnight_AfterMidnight() {
        ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
        logger.setApplianceId(DEVICE_ID);

        LocalDateTime now = toToday(0, 30, 0);
        Schedule schedule = new Schedule(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));
        Interval interval = schedule.getTimeframe().getIntervals(now).get(0).getInterval();

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe sempTimeFrame = sempController
                .createSempTimeFrame(logger, DEVICE_ID, schedule, interval, 0, 0, now);
        Assert.assertEquals(0, (long) sempTimeFrame.getEarliestStart());
        Assert.assertEquals(12600, (long) sempTimeFrame.getLatestEnd());
    }

    private void assertTimeframe(Timeframe timeframe, Long earliestStart, Long latestEnd, Long minRuningTime, Long maxRunningTime) {
        Assert.assertEquals(earliestStart, timeframe.getEarliestStart());
        Assert.assertEquals(latestEnd, timeframe.getLatestEnd());
        Assert.assertEquals(latestEnd, timeframe.getLatestEnd());
        Assert.assertEquals(minRuningTime, timeframe.getMinRunningTime());
        Assert.assertEquals(maxRunningTime, timeframe.getMaxRunningTime());
    }

    private void setDeviceInfo(DeviceInfo deviceInfo) {
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));
        ApplianceManager.getInstance().setDevice2EM(device2EM);
    }
}
