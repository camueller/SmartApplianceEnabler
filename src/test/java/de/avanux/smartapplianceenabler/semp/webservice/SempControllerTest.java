package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.ApplianceBuilder;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SempControllerTest extends TestBase {

    public static final String DEVICE_ID = "DeviceID1";
    public static final String DEVICE_ID2 = "DeviceID2";
    public static final String DEVICE_ID3 = "DeviceID3";
    private SempController sempController;

    public SempControllerTest() {
        sempController = new SempController();
    }

    @Test
    public void getPlanningRequest() {
        LocalDateTime now = toToday(6, 0, 0);
        List<Appliance> appliances = new ArrayList<>();

        Appliance appliance1 = new ApplianceBuilder(DEVICE_ID)
                .withMockSwitch(false)
                .withRuntimeRequest(now, now.plusHours(4), now.plusHours(8), null, 3600, true)
                .build(false);
        appliances.add(appliance1);

        Appliance appliance2 = new ApplianceBuilder(DEVICE_ID2)
                .withMockSwitch(false)
                .withRuntimeRequest(now, now.plusHours(3), now.plusHours(7), null, 7200, false)
                .build(false);
        appliances.add(appliance2);

        Appliance appliance3 = new ApplianceBuilder(DEVICE_ID3)
                .withMockSwitch(false)
                .withRuntimeRequest(now, now.plusHours(2), now.plusHours(6), 1000, 1800, true)
                .build(false);
        appliances.add(appliance3);

        ApplianceBuilder.init(appliances, null);

        Device2EM device2EM = sempController.createDevice2EM(now);
        List<PlanningRequest> planningRequests = device2EM.getPlanningRequest();
        assertEquals(2, planningRequests.size());

        List<Timeframe> timeframes = planningRequests.get(0).getTimeframes();
        assertEquals(1, timeframes.size());
        assertTimeframe(timeframes.get(0), 4 * 3600,  8 * 3600, 3599, 3600);

        timeframes = planningRequests.get(1).getTimeframes();
        assertEquals(1, timeframes.size());
        assertTimeframe(timeframes.get(0), 2 * 3600,  6 * 3600, 1000, 1800);
    }

    private void assertTimeframe(Timeframe timeframe, Integer earliestStart, Integer latestEnd, Integer minRuningTime, Integer maxRunningTime) {
        assertEquals(earliestStart, timeframe.getEarliestStart());
        assertEquals(latestEnd, timeframe.getLatestEnd());
        assertEquals(latestEnd, timeframe.getLatestEnd());
        assertEquals(minRuningTime, timeframe.getMinRunningTime());
        assertEquals(maxRunningTime, timeframe.getMaxRunningTime());
    }

    private void setDeviceInfo(DeviceInfo deviceInfo) {
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);
    }
}
