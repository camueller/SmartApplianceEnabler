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
package de.avanux.smartapplianceenabler.semp.webservice;

import de.avanux.smartapplianceenabler.appliance.*;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class SempController implements ActiveIntervalChangedListener {

    private static final String BASE_URL = "/semp";
    public static final String SCHEMA_LOCATION = "http://www.sma.de/communication/schema/SEMP/v1";
    private Logger logger = LoggerFactory.getLogger(SempController.class);
    private Device2EM device2EM;
    private boolean timeFrameChangedListenerRegistered;
    
    public SempController() {
        logger.info("SEMP controller created.");
    }

    protected SempController(Device2EM device2EM) {
        setDevice2EM(device2EM);
    }

    public void setDevice2EM(Device2EM device2EM) {
        this.device2EM = device2EM;
        logger.debug("Device2EM configured");
    }

    @RequestMapping(value=BASE_URL, method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String device2EM() {
        logger.debug("Device info/status/planning requested.");
        return marshall(createDevice2EM(new LocalDateTime()));
    }

    protected Device2EM createDevice2EM(LocalDateTime now) {
        List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();
        List<PlanningRequest> planningRequests = new ArrayList<PlanningRequest>();
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, appliance.getId());
            if(!timeFrameChangedListenerRegistered && appliance.getRunningTimeMonitor() != null) {
                appliance.getRunningTimeMonitor().addTimeFrameChangedListener(this);
                timeFrameChangedListenerRegistered = true;
            }
            DeviceStatus deviceStatus = createDeviceStatus(applianceLogger, appliance);
            deviceStatuses.add(deviceStatus);
            PlanningRequest planningRequest = createPlanningRequest(applianceLogger, now, appliance);
            if(planningRequest != null) {
                planningRequests.add(planningRequest);
            }
        }
        device2EM.setDeviceStatus(deviceStatuses);
        device2EM.setPlanningRequest(planningRequests);
        return device2EM;
    }
    
    @RequestMapping(value=BASE_URL + "/DeviceInfo", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String deviceInfo(@RequestParam(value="DeviceId", required = false) String deviceId) {
        List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        if(deviceId != null) {
            logger.debug("Device info requested of device id=" + deviceId);
            DeviceInfo deviceInfo = findDeviceInfo(device2EM, deviceId);

            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceId);
            if(appliance.getMeter() != null) {
                deviceInfo.getCapabilities().setCurrentPowerMethod(CurrentPowerMethod.Measurement);
            }
            else {
                deviceInfo.getCapabilities().setCurrentPowerMethod(CurrentPowerMethod.Estimation);
            }

            deviceInfo.getCapabilities().setOptionalEnergy(appliance.canConsumeOptionalEnergy());
            deviceInfos.add(deviceInfo);
        }
        else {
            logger.debug("Device info requested of all devices");
            List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
            for (Appliance appliance : appliances) {
                DeviceInfo deviceInfo = findDeviceInfo(device2EM, appliance.getId());
                if(appliance.getMeter() != null) {
                    deviceInfo.getCapabilities().setCurrentPowerMethod(CurrentPowerMethod.Measurement);
                }
                else {
                    deviceInfo.getCapabilities().setCurrentPowerMethod(CurrentPowerMethod.Estimation);
                }
                deviceInfo.getCapabilities().setOptionalEnergy(appliance.canConsumeOptionalEnergy());
                deviceInfos.add(deviceInfo);
            }
        }
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(deviceInfos);
        return marshall(device2EM);
    }

    @RequestMapping(value=BASE_URL + "/DeviceStatus", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String deviceStatus(@RequestParam(value="DeviceId", required = false) String deviceId) {
        List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();
        if(deviceId != null) {
            ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, deviceId);
            applianceLogger.debug("Device status requested");
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceId);
            DeviceStatus deviceStatus = createDeviceStatus(applianceLogger, appliance);
            deviceStatuses.add(deviceStatus);
        }
        else {
            logger.debug("Device status requested of all devices");
            List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
            for (Appliance appliance : appliances) {
                ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, appliance.getId());
                DeviceStatus deviceStatus = createDeviceStatus(applianceLogger, appliance);
                deviceStatuses.add(deviceStatus);
            }
        }
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceStatus(deviceStatuses);
        return marshall(device2EM);
    }
    
    @RequestMapping(value=BASE_URL + "/PlanningRequest", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String planningRequest(@RequestParam(value="DeviceId", required = false) String deviceId) {
        LocalDateTime now = new LocalDateTime();
        List<PlanningRequest> planningRequests = new ArrayList<PlanningRequest>();
        if(deviceId != null) {
            ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, deviceId);
            applianceLogger.debug("Planning request requested");
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceId);
            PlanningRequest planningRequest = createPlanningRequest(applianceLogger, now, appliance);
            addPlanningRequest(planningRequests, planningRequest);
        }
        else {
            logger.debug("Planning request requested of all devices");
            List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
            for (Appliance appliance : appliances) {
                ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, appliance.getId());
                PlanningRequest planningRequest = createPlanningRequest(applianceLogger, now, appliance);
                addPlanningRequest(planningRequests, planningRequest);
            }
        }
        Device2EM device2EM = new Device2EM();
        if(planningRequests.size() > 0) {
            device2EM.setPlanningRequest(planningRequests);
        }
        return marshall(device2EM);
    }

    private void addPlanningRequest(List<PlanningRequest> planningRequests, PlanningRequest planningRequest) {
        if(planningRequest != null) {
            planningRequests.add(planningRequest);
        }
    }

    @RequestMapping(value=BASE_URL, method=RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void em2Device(@RequestBody EM2Device em2Device) {
        List<DeviceControl> deviceControls = em2Device.getDeviceControl();
        for(DeviceControl deviceControl : deviceControls) {
            ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, deviceControl.getDeviceId());
            applianceLogger.debug("Received control request");
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceControl.getDeviceId());
            if(appliance != null) {
                setApplianceState(applianceLogger, appliance, deviceControl.isOn(), "Setting appliance state to " + (deviceControl.isOn() ? "ON" : "OFF"));
            }
            else {
                applianceLogger.warn("No appliance configured for device id " + deviceControl.getDeviceId());
            }
        }
    }

    private void setApplianceState(ApplianceLogger applianceLogger, Appliance appliance, boolean switchOn, String logMessage) {
        Control control = appliance.getControl();
        if(control != null) {
            boolean stateChanged = false;
            // only change state if requested state differs from actual state
            if(control.isOn() ^ switchOn) {
                control.on(switchOn);
                stateChanged = true;
            }
            if(stateChanged) {
                applianceLogger.debug(logMessage);
            }
        }
        else {
            applianceLogger.warn("Appliance configuration does not contain control.");
        }
    }

    @Override
    public void activeIntervalChanged(String applianceId, TimeframeInterval deactivatedInterval, TimeframeInterval activatedInterval) {
        if(activatedInterval == null) {
            Appliance appliance = ApplianceManager.getInstance().findAppliance(applianceId);
            ApplianceLogger applianceLogger = ApplianceLogger.createForAppliance(logger, applianceId);
            setApplianceState(applianceLogger, appliance, false, "Switching off due to end of time frame");
        }
    }

    private DeviceStatus createDeviceStatus(ApplianceLogger applianceLogger, Appliance appliance) {
        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setDeviceId(appliance.getId());
        Meter meter = appliance.getMeter();

        Control control = appliance.getControl();
        if(control != null) {
            deviceStatus.setStatus(control.isOn() ? Status.On : Status.Off);
            deviceStatus.setEMSignalsAccepted(true);
            applianceLogger.debug("Reporting device status from control");
        }
        else {
            // there is no control for the appliance ...
            if(meter != null) {
                // ... but we can derive the status from power consumption
                deviceStatus.setStatus(meter.isOn() ? Status.On : Status.Off);
                applianceLogger.debug("Reporting device status based on power consumption");
            }
            else {
                // ... and no meter; we have to assume the appliance is switched off
                deviceStatus.setStatus(Status.Offline);
                applianceLogger.debug("Appliance has neither control nor meter.");
            }
            
            // an appliance without control cannot be controlled ;-)
            deviceStatus.setEMSignalsAccepted(false);
        }
        applianceLogger.debug(deviceStatus.toString());

        PowerInfo powerInfo = new PowerInfo();
        if(meter != null) {
            applianceLogger.debug("Reporting power info from meter.");
            powerInfo.setAveragePower(meter.getAveragePower());
            powerInfo.setMinPower(meter.getMinPower());
            powerInfo.setMaxPower(meter.getMaxPower());
            powerInfo.setAveragingInterval(60); // always report 60 for SEMP regardless of real averaging interval
        }
        else {
            applianceLogger.debug("Reporting power info from device characteristics.");
            DeviceInfo deviceInfo = findDeviceInfo(device2EM, appliance.getId());
            if(deviceStatus.getStatus() == Status.On) {
                powerInfo.setAveragePower(deviceInfo.getCharacteristics().getMaxPowerConsumption());
            }
            else {
                powerInfo.setAveragePower(0);
            }
            powerInfo.setAveragingInterval(60);
        }
        powerInfo.setTimestamp(0);
        applianceLogger.debug(powerInfo.toString());

        PowerConsumption powerConsumption = new PowerConsumption();
        powerConsumption.setPowerInfo(Collections.singletonList(powerInfo));
        
        deviceStatus.setPowerConsumption(Collections.singletonList(powerConsumption));
        return deviceStatus;
    }

    private DeviceInfo findDeviceInfo(Device2EM device2EM, String deviceId) {
        for(DeviceInfo deviceInfo : device2EM.getDeviceInfo()) {
            if(deviceInfo.getIdentification().getDeviceId().equals(deviceId)) {
                return deviceInfo;
            }
        }
        return null;
    }

    private PlanningRequest createPlanningRequest(ApplianceLogger applianceLogger, LocalDateTime now, Appliance appliance) {
        PlanningRequest planningRequest = null;
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
        if(runningTimeMonitor != null) {
            List<de.avanux.smartapplianceenabler.semp.webservice.Timeframe> sempTimeFrames = new ArrayList<de.avanux.smartapplianceenabler.semp.webservice.Timeframe>();
            List<Schedule> schedules = runningTimeMonitor.getSchedules();
            TimeframeInterval timeframeInterval = runningTimeMonitor.getActiveTimeframeInterval();
            if(schedules != null && schedules.size() > 0) {
                applianceLogger.debug("Active schedules: " + schedules.size());
                TimeframeInterval activeTimeframeInterval = runningTimeMonitor.getActiveTimeframeInterval();
                addSempTimeFrame(applianceLogger, runningTimeMonitor, timeframeInterval, appliance, sempTimeFrames, now);

                Interval considerationInterval = new Interval(now.toDateTime(), now.plusDays(2).toDateTime());
                List<TimeframeInterval> timeFrameIntervals = Schedule.findTimeframeIntervals(now, considerationInterval, runningTimeMonitor.getSchedules());
                for(TimeframeInterval timeframeIntervalOfSchedule : timeFrameIntervals) {
                    Schedule schedule = timeframeIntervalOfSchedule.getTimeframe().getSchedule();
                    addSempTimeFrame(applianceLogger, appliance, sempTimeFrames, schedule, timeframeIntervalOfSchedule.getInterval(), schedule.getMinRunningTime(),
                            schedule.getMaxRunningTime(), now);
                }
            }
            else if(timeframeInterval != null) {
                applianceLogger.debug("Active timeframe interval found");
                addSempTimeFrame(applianceLogger, runningTimeMonitor, timeframeInterval, appliance, sempTimeFrames, now);
            }
            else {
                applianceLogger.debug("No timeframes found");
            }

            if(sempTimeFrames.size() > 0) {
                planningRequest = new PlanningRequest();
                planningRequest.setTimeframes(sempTimeFrames);
            }
            else {
                applianceLogger.debug("No planning requests created");
                return null;
            }
        }
        return planningRequest;
    }

    private void addSempTimeFrame(ApplianceLogger applianceLogger, RunningTimeMonitor runningTimeMonitor, TimeframeInterval timeframeInterval, Appliance appliance,
                                  List<de.avanux.smartapplianceenabler.semp.webservice.Timeframe> sempTimeFrames, LocalDateTime now) {
        if(timeframeInterval != null) {
            final long remainingMaxRunningTime = runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame();
            addSempTimeFrame(applianceLogger, appliance, sempTimeFrames,
                    timeframeInterval.getTimeframe().getSchedule(), timeframeInterval.getInterval(),
                    runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(), remainingMaxRunningTime, now);
            if(remainingMaxRunningTime < 0) {
                setApplianceState(applianceLogger, appliance, false, "Switching off due to maxRunningTime < 0");
            }
        }
    }

    private void addSempTimeFrame(ApplianceLogger applianceLogger, Appliance appliance, List<de.avanux.smartapplianceenabler.semp.webservice.Timeframe> sempTimeFrames,
                                  Schedule currentSchedule, Interval interval, long remainingMinRunningTime, long remainingMaxRunningTime, LocalDateTime now) {
        sempTimeFrames.add(createSempTimeFrame(applianceLogger, appliance.getId(), currentSchedule, interval, remainingMinRunningTime, remainingMaxRunningTime, now));
    }
    
    protected de.avanux.smartapplianceenabler.semp.webservice.Timeframe createSempTimeFrame(ApplianceLogger applianceLogger, String deviceId, Schedule schedule,
                                                                                            Interval interval, long minRunningTime, long maxRunningTime, LocalDateTime now) {
        Long earliestStart = 0l;
        DateTime start = interval.getStart();
        DateTime end = interval.getEnd();
        if(start.isAfter(now.toDateTime())) {
            earliestStart = Double.valueOf(new Interval(now.toDateTime(), start).toDurationMillis() / 1000).longValue();
        }
        LocalDateTime nowBeforeEnd = new LocalDateTime(now);
        if(now.toDateTime().isAfter(end)) {
            nowBeforeEnd = now.minusHours(24);
        }
        Long latestEnd = Double.valueOf(new Interval(nowBeforeEnd.toDateTime(), end).toDurationMillis() / 1000).longValue();
        return createSempTimeFrame(applianceLogger, deviceId, earliestStart, latestEnd, minRunningTime, maxRunningTime);
    }
    
    protected de.avanux.smartapplianceenabler.semp.webservice.Timeframe createSempTimeFrame(ApplianceLogger applianceLogger, String deviceId,
                                                                                            Long earliestStart, Long latestEnd, long minRunningTime, long maxRunningTime) {
        de.avanux.smartapplianceenabler.semp.webservice.Timeframe timeFrame = new de.avanux.smartapplianceenabler.semp.webservice.Timeframe();
        timeFrame.setDeviceId(deviceId);
        timeFrame.setEarliestStart(earliestStart);
        timeFrame.setLatestEnd(latestEnd);
        if(minRunningTime == maxRunningTime) {
            /** WORKAROUND:
             * For unknown reason the SunnyPortal displays the scheduled times only
             * if maxRunningTime AND minRunningTime are returned and are NOT EQUAL
             * Therefore we ensure that they are not equal by reducing minRunningTime by 1 second
             */
            timeFrame.setMinRunningTime(minRunningTime >= 1 ? minRunningTime - 1 : 0);
        }
        else {
            // according to spec minRunningTime only has to be returned if different from maxRunningTime
            timeFrame.setMinRunningTime(minRunningTime >= 0 ? minRunningTime : 0);
        }
        timeFrame.setMaxRunningTime(maxRunningTime >= 0 ? maxRunningTime : 0);
        applianceLogger.debug("Timeframe added to PlanningRequest: " + timeFrame.toString());
        return timeFrame;
    }

    private String marshall(Device2EM device2EM) {
        StringWriter writer = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(Device2EM.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(device2EM, writer);        
            return writer.toString();
        }
        catch(JAXBException e) {
            logger.error("Error marshalling", e);
        }
        return null;
    }
}
