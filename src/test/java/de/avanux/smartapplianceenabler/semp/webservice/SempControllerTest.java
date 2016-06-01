package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.appliance.*;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class SempControllerTest {

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

        Instant now = new Instant();
        long remainingMinRunningTime = 1800;
        TimeFrame timeFrame = new TimeFrame(600, 600, new TimeOfDay(now.minus(1000)), new TimeOfDay(now.plus(remainingMinRunningTime * 1000)));

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
}
