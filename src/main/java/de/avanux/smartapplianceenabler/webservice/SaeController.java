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
    private static final String BASE_URL = "/sae";
    private Logger logger = LoggerFactory.getLogger(SaeController.class);

    public SaeController() {
        logger.info("Controller ready to handle SAE requests.");
    }

    @RequestMapping(value=BASE_URL + "/timeframes", method= RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void setTimeFrames(@RequestParam(value="ApplianceId") String applianceId, @RequestBody TimeFrames timeFrames) {
        ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
        List<Schedule> timeFramesToSet = timeFrames.getSchedules();
        applianceLogger.debug("Received request to set " + (timeFramesToSet != null ? timeFramesToSet.size() : "0") + " time frame(s)");
        Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
        appliance.getRunningTimeMonitor().setSchedules(timeFramesToSet);
    }
}
