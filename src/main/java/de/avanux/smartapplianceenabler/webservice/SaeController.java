package de.avanux.smartapplianceenabler.webservice;

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.Schedule;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SaeController {
    protected static final String BASE_URL = "/sae";
    protected static final String SCHEDULES_URL = BASE_URL + "/schedules";

    private Logger logger = LoggerFactory.getLogger(SaeController.class);

    public SaeController() {
        logger.info("SAE controller created.");
    }

    @RequestMapping(value=SCHEDULES_URL, method= RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void setSchedules(@RequestParam(value="ApplianceId") String applianceId, @RequestBody Schedules schedules) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        List<Schedule> schedulesToSet = schedules.getSchedules();
        applianceLogger.debug("Received request to set " + (schedulesToSet != null ? schedulesToSet.size() : "0") + " schedule(s)");
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        appliance.getRunningTimeMonitor().setSchedules(schedulesToSet);
    }
}
