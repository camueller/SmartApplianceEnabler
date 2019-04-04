package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.schedule.Schedule;
import de.avanux.smartapplianceenabler.schedule.TimeOfDay;
import de.avanux.smartapplianceenabler.test.TestBuilder;
import de.avanux.smartapplianceenabler.util.DateTimeProvider;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SempControllerTest extends TestBase {

    public static final String DEVICE_ID = "DeviceID1";
    String applianceId = "F-001";
    private SempController sempController;
    private DateTimeProvider dateTimeProvider = Mockito.mock(DateTimeProvider.class);

    public SempControllerTest() {
        sempController = new SempController();
    }

    @Test
    public void createDeviceInfo_noOptionalEnergy() {
        LocalDateTime now = toToday(9, 30, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, now)
                .withMockSwitch(false)
                .withSchedule(10, 0, 18, 0, 3600, null)
                .init();
        DeviceInfo deviceInfo = sempController.createDeviceInfo(applianceId);
        Assert.assertFalse("No value for maxRunningTime indicates no ability to consume optional energy",
                deviceInfo.getCapabilities().getOptionalEnergy());
    }

    @Test
    public void createDeviceInfo_optionalEnergy() {
        LocalDateTime now = toToday(9, 30, 0);
        TestBuilder builder = new TestBuilder()
                .appliance(applianceId, dateTimeProvider, now)
                .withMockSwitch(false)
                .withSchedule(10, 0, 18, 0, 3600, 7200)
                .init();
        DeviceInfo deviceInfo = sempController.createDeviceInfo(applianceId);
        Assert.assertTrue("Different values for minRunningTime and maxRunningTime indicate consumption of " +
                "optional energy", deviceInfo.getCapabilities().getOptionalEnergy());
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
        Schedule schedule = new Schedule(600, null, new TimeOfDay(now.minusSeconds(1)),
                new TimeOfDay(now.plusSeconds(remainingMaxRunningTime)));
        de.avanux.smartapplianceenabler.schedule.Timeframe timeframe = schedule.getTimeframe();
        timeframe.setSchedule(schedule);
        RunningTimeMonitor runningTimeMonitor = mock(RunningTimeMonitor.class);
        when(runningTimeMonitor.getSchedules()).thenReturn(Collections.singletonList(schedule));
        when(runningTimeMonitor.getActiveTimeframeInterval()).thenReturn(timeframe.getIntervals(now).get(0));
        when(runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now)).thenReturn(schedule.getRequest().getMin());
        when(runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now)).thenReturn(schedule.getRequest().getMax());
        appliance.setRunningTimeMonitor(runningTimeMonitor);

        Appliances appliances = new Appliances();
        appliances.setAppliances(Collections.singletonList(appliance));
        ApplianceManager.getInstanceWithoutTimer().setAppliances(appliances);

        Device2EM device2EM = sempController.createDevice2EM(now);
        List<PlanningRequest> planningRequests = device2EM.getPlanningRequest();
        Assert.assertEquals(1, planningRequests.size());
        List<Timeframe> timeframes = planningRequests.get(0).getTimeframes();
        Assert.assertEquals(3, timeframes.size());
        assertTimeframe(timeframes.get(0), 0, 1800, 599, 600);
        assertTimeframe(timeframes.get(1), 86399, 88200, 599, 600);
        assertTimeframe(timeframes.get(2), 172799, 174600, 599, 600);
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
        Schedule schedule = new Schedule(3600, null,
                new TimeOfDay(11, 0, 0), new TimeOfDay(13, 0, 0));
        de.avanux.smartapplianceenabler.schedule.Timeframe timeframe = schedule.getTimeframe();
        timeframe.setSchedule(schedule);
        RunningTimeMonitor runningTimeMonitor = mock(RunningTimeMonitor.class);
        when(runningTimeMonitor.getActiveTimeframeInterval()).thenReturn(timeframe.getIntervals(now).get(0));
        when(runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now)).thenReturn(schedule.getRequest().getMin());
        when(runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now)).thenReturn(schedule.getRequest().getMax());
        appliance.setRunningTimeMonitor(runningTimeMonitor);

        Appliances appliances = new Appliances();
        appliances.setAppliances(Collections.singletonList(appliance));
        ApplianceManager.getInstanceWithoutTimer().setAppliances(appliances);

        appliance.startingCurrentDetected(now);

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

    private void assertTimeframe(Timeframe timeframe, Integer earliestStart, Integer latestEnd, Integer minRuningTime, Integer maxRunningTime) {
        Assert.assertEquals(earliestStart, timeframe.getEarliestStart());
        Assert.assertEquals(latestEnd, timeframe.getLatestEnd());
        Assert.assertEquals(latestEnd, timeframe.getLatestEnd());
        Assert.assertEquals(minRuningTime, timeframe.getMinRunningTime());
        Assert.assertEquals(maxRunningTime, timeframe.getMaxRunningTime());
    }

    private void setDeviceInfo(DeviceInfo deviceInfo) {
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);
    }
}
