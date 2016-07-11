package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class SempControllerTest extends TestBase {

    private SempController sempController;
    private Device2EM device2EM;

    public SempControllerTest() {
        device2EM = new Device2EM();
        sempController = new SempController(device2EM);
    }

    @Test
    public void device2EM_planningRequest() {
        Identification identification = new Identification();
        identification.setDeviceId("DeviceID1");

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIdentification(identification);
        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));

        Appliance appliance = new Appliance();

        LocalDateTime now = new LocalDateTime();
        int remainingMinRunningTime = 1800;
        TimeFrame timeFrame = new TimeFrame(600, 600, new TimeOfDay(now.minusSeconds(1)), new TimeOfDay(now.plusSeconds(remainingMinRunningTime)));

        RunningTimeMonitor runningTimeMonitor = mock(RunningTimeMonitor.class);
        when(runningTimeMonitor.getTimeFrames()).thenReturn(Collections.singletonList(timeFrame));
        when(runningTimeMonitor.findAndSetCurrentTimeFrame(any())).thenReturn(timeFrame);
        when(runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame()).thenReturn(remainingMinRunningTime);
        when(runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame()).thenReturn(remainingMinRunningTime);
        appliance.setRunningTimeMonitor(runningTimeMonitor);

        Appliances appliances = new Appliances();
        appliances.setAppliances(Collections.singletonList(appliance));
        ApplianceManager.getInstance().setAppliances(appliances);

        Device2EM device2EM = sempController.createDevice2EM(now);
        List<PlanningRequest> planningRequests = device2EM.getPlanningRequest();
        Assert.assertTrue(planningRequests.size() > 0);
    }

    @Test
    public void createSempTimeFrame() {
        String deviceId = "DeviceID1";
        ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
        logger.setApplianceId(deviceId);

        LocalDateTime now = toToday(0, 30, 0);
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(1, 0, 0), new TimeOfDay(9, 0, 0));

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe sempTimeFrame = sempController
                .createSempTimeFrame(logger, deviceId, timeFrame, 0, 0, now);
        Assert.assertEquals(1800, (long) sempTimeFrame.getEarliestStart());
        Assert.assertEquals(30600, (long) sempTimeFrame.getLatestEnd());
    }

    @Test
    public void createSempTimeFrame_TimeFrameOverMidnight_BeforeMidnight() {
        String deviceId = "DeviceID1";
        ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
        logger.setApplianceId(deviceId);

        LocalDateTime dateTime = toToday(23, 30, 0);
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe sempTimeFrame = sempController
                .createSempTimeFrame(logger, deviceId, timeFrame, 0, 0, dateTime);
        Assert.assertEquals(0, (long) sempTimeFrame.getEarliestStart());
        Assert.assertEquals(16200, (long) sempTimeFrame.getLatestEnd());
    }

    @Test
    public void createSempTimeFrame_TimeFrameOverMidnight_AfterMidnight() {
        String deviceId = "DeviceID1";
        ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
        logger.setApplianceId(deviceId);

        LocalDateTime dateTime = toToday(0, 30, 0);
        TimeFrame timeFrame = new TimeFrame(7200, 7200, new TimeOfDay(20, 0, 0), new TimeOfDay(4, 0, 0));

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe sempTimeFrame = sempController
                .createSempTimeFrame(logger, deviceId, timeFrame, 0, 0, dateTime);
        Assert.assertEquals(0, (long) sempTimeFrame.getEarliestStart());
        Assert.assertEquals(12600, (long) sempTimeFrame.getLatestEnd());
    }
}
