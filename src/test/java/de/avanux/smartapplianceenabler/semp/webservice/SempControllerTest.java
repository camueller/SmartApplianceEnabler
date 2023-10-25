package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.ApplianceBuilder;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SempControllerTest extends TestBase {

    private SempController sempController;

    public SempControllerTest() {
        sempController = new SempController();
    }

    @Test
    public void getPlanningRequest() {
        LocalDateTime now = toToday(6, 0, 0);
        List<Appliance> appliances = new ArrayList<>();

        Appliance appliance1 = new ApplianceBuilder("F-001")
                .withMockSwitch(false)
                .withRuntimeRequest(now, now.plusHours(4), now.plusHours(8), null, 3600, true)
                .build(false);
        appliances.add(appliance1);

        Appliance appliance2 = new ApplianceBuilder("F-002")
                .withMockSwitch(false)
                .withRuntimeRequest(now, now.plusHours(3), now.plusHours(7), null, 7200, false)
                .build(false);
        appliances.add(appliance2);

        Appliance appliance3 = new ApplianceBuilder("F-003")
                .withMockSwitch(false)
                .withRuntimeRequest(now, now.plusHours(2), now.plusHours(6), 1000, 1800, true)
                .build(false);
        appliances.add(appliance3);

        Appliance appliance4 = new ApplianceBuilder("F-004")
                .withMockSwitch(false)
                .withEnergyRequest(now, now.plusHours(1), now.plusHours(8), null, 10000, true)
                .build(false);
        appliances.add(appliance4);

        Appliance appliance5 = new ApplianceBuilder("F-005")
                .withMockSwitch(false)
                .withEnergyRequest(now, now.plusHours(2), now.plusHours(10), 6000, 10000, true)
                .build(false);
        appliances.add(appliance5);

        ApplianceBuilder.init(appliances, null);

        Device2EM device2EM = sempController.createDevice2EM(now);
        List<PlanningRequest> planningRequests = device2EM.getPlanningRequest();
        assertEquals(4, planningRequests.size());

        List<Timeframe> timeframes = planningRequests.get(0).getTimeframes();
        assertEquals(1, timeframes.size());
        assertTimeframe(timeframes.get(0), 4 * 3600,  8 * 3600, 3599, 3600, false);

        timeframes = planningRequests.get(1).getTimeframes();
        assertEquals(1, timeframes.size());
        assertTimeframe(timeframes.get(0), 2 * 3600,  6 * 3600, 1000, 1800, false);

        timeframes = planningRequests.get(2).getTimeframes();
        assertEquals(1, timeframes.size());
        assertTimeframe(timeframes.get(0), 1 * 3600,  8 * 3600, 9999, 10000, true);

        timeframes = planningRequests.get(3).getTimeframes();
        assertEquals(1, timeframes.size());
        assertTimeframe(timeframes.get(0), 2 * 3600,  10 * 3600, 6000, 10000, true);
    }

    private void assertTimeframe(Timeframe timeframe, Integer earliestStart, Integer latestEnd, Integer min, Integer max, boolean energyRequest) {
        assertEquals(earliestStart, timeframe.getEarliestStart());
        assertEquals(latestEnd, timeframe.getLatestEnd());
        assertEquals(latestEnd, timeframe.getLatestEnd());
        if(energyRequest) {
            assertEquals(min, timeframe.getMinEnergy());
            assertEquals(max, timeframe.getMaxEnergy());
        } else {
            assertEquals(min, timeframe.getMinRunningTime());
            assertEquals(max, timeframe.getMaxRunningTime());
        }
    }

    private void setDeviceInfo(DeviceInfo deviceInfo) {
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));
        ApplianceManager.getInstanceWithoutTimer().setDevice2EM(device2EM);
    }
}
