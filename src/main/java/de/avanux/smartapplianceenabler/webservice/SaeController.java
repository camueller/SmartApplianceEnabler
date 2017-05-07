/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
