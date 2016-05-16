/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.Control;
import de.avanux.smartapplianceenabler.appliance.FileHandler;
import de.avanux.smartapplianceenabler.appliance.Meter;
import de.avanux.smartapplianceenabler.appliance.RunningTimeMonitor;
import de.avanux.smartapplianceenabler.appliance.TimeFrame;

@RestController
public class SempController {

    private static final String BASE_URL = "/semp";
    public static final String SCHEMA_LOCATION = "http://www.sma.de/communication/schema/SEMP/v1";
    private Logger logger = LoggerFactory.getLogger(SempController.class);
    private FileHandler fileHandler;
    private Device2EM device2EM;
    
    public SempController() {
        fileHandler = new FileHandler();
        device2EM = fileHandler.load(Device2EM.class);
        logger.info("Controller ready to handle SEMP requests.");
    }
    
    @RequestMapping(value=BASE_URL, method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String device2EM() {
        logger.debug("Device info/status/planning requested.");
        List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();
        List<PlanningRequest> planningRequests = new ArrayList<PlanningRequest>();
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            DeviceStatus deviceStatus = createDeviceStatus(appliance);
            deviceStatuses.add(deviceStatus);
            PlanningRequest planningRequest = createPlanningRequest(appliance);
            planningRequests.add(planningRequest);
        }
        device2EM.setDeviceStatus(deviceStatuses);
        if(planningRequests.size() > 0) {
            device2EM.setPlanningRequest(planningRequests);
        }
        return marshall(device2EM);
    }
    
    @RequestMapping(value=BASE_URL + "/DeviceInfo", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String deviceInfo(@RequestParam(value="DeviceId", required = false) String deviceId) {
        List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        if(deviceId != null) {
            logger.debug("Device info requested of device id=" + deviceId);
            DeviceInfo deviceInfo = findDeviceInfo(device2EM, deviceId);

            Appliance appliance = findAppliance(deviceId);
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
            logger.debug("Device status requested of device id=" + deviceId);
            Appliance appliance = findAppliance(deviceId);
            DeviceStatus deviceStatus = createDeviceStatus(appliance);
            deviceStatuses.add(deviceStatus);
        }
        else {
            logger.debug("Device status requested of all devices");
            List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
            for (Appliance appliance : appliances) {
                DeviceStatus deviceStatus = createDeviceStatus(appliance);
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
        List<PlanningRequest> planningRequests = new ArrayList<PlanningRequest>();
        if(deviceId != null) {
            logger.debug("Planning request requested of device id=" + deviceId);
            Appliance appliance = findAppliance(deviceId);
            PlanningRequest planningRequest = createPlanningRequest(appliance);
            addPlanningRequest(planningRequests, planningRequest);
        }
        else {
            logger.debug("Planning request requested of all devices");
            List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
            for (Appliance appliance : appliances) {
                PlanningRequest planningRequest = createPlanningRequest(appliance);
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
            logger.debug("Received control request for device id=" + deviceControl.getDeviceId());
            Appliance appliance = findAppliance(deviceControl.getDeviceId());
            if(appliance != null) {
                if(appliance.getControls() != null) {
                    for(Control control : appliance.getControls()) {
                        control.on(deviceControl.isOn());
                    }
                }
                else {
                    logger.warn("Appliance configuration does not contain control.");
                }
            }
            else {
                logger.warn("No appliance configured for device id " + deviceControl.getDeviceId());
            }
        }
    }
    
    private DeviceStatus createDeviceStatus(Appliance appliance) {
        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setDeviceId(appliance.getId());
        Meter meter = appliance.getMeter();

        if(appliance.getControls() != null && appliance.getControls().size() > 0) {
            for(Control control : appliance.getControls()) {
                deviceStatus.setStatus(control.isOn() ? Status.On : Status.Off);
                deviceStatus.setEMSignalsAccepted(true);
                break;
            }
        }
        else {
            // there is no control for the appliance ...
            if(meter != null) {
                // ... but we can derive the status from power consumption
                deviceStatus.setStatus(meter.isOn() ? Status.On : Status.Off);
            }
            else {
                // ... and no meter; we have to assume the appliance is switched off
                deviceStatus.setStatus(Status.Offline);
            }
            
            // an appliance without control cannot be controlled ;-)
            deviceStatus.setEMSignalsAccepted(false);
        }
        
        PowerInfo powerInfo = new PowerInfo();
        if(meter != null) {
            powerInfo.setAveragePower(meter.getAveragePower());
            powerInfo.setMinPower(meter.getMinPower());
            powerInfo.setMaxPower(meter.getMaxPower());
            powerInfo.setAveragingInterval(60); // always report 60 for SEMP regardless of real averaging interval
        }
        else {
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

    private PlanningRequest createPlanningRequest(Appliance appliance) {
        PlanningRequest planningRequest = null;
        RunningTimeMonitor runningTimeMonitor = appliance.getRunningTimeMonitor();
        if(runningTimeMonitor != null) {
            if(runningTimeMonitor.getTimeFrames() != null && runningTimeMonitor.getTimeFrames().size() > 0) {
                Instant now = new Instant();
                List<de.avanux.smartapplianceenabler.semp.webservice.Timeframe> sempTimeFrames = new ArrayList<de.avanux.smartapplianceenabler.semp.webservice.Timeframe>();
                
                TimeFrame currentTimeFrame = runningTimeMonitor.findCurrentTimeFrame(now);
                if(currentTimeFrame != null) {
                    sempTimeFrames.add(createSempTimeFrame(appliance.getId(), currentTimeFrame, runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(), now));
                }
                
                for(TimeFrame timeFrame : runningTimeMonitor.findFutureTimeFrames(now)) {
                    Long remainingMinRunningTime = timeFrame.getMinRunningTime();
                    sempTimeFrames.add(createSempTimeFrame(appliance.getId(), timeFrame, remainingMinRunningTime, now));
                }
                if(sempTimeFrames.size() > 0) {
                    planningRequest = new PlanningRequest();
                    planningRequest.setTimeframes(sempTimeFrames);
                }
                else {
                    logger.debug("No timeframes found for appliance id " + appliance.getId());
                    return null;
                }
            }
            else {
                logger.debug("No planning request configured for appliance id " + appliance.getId());
            }
        }
        return planningRequest;
    }
    
    private de.avanux.smartapplianceenabler.semp.webservice.Timeframe createSempTimeFrame(String deviceId, TimeFrame timeFrame, Long remainingMinRunningTime, Instant now) {
        Long earliestStart = 0l;
        if(timeFrame.getInterval().getStart().isAfter(now)) {
            earliestStart = Double.valueOf(new Interval(now, timeFrame.getInterval().getStart()).toDurationMillis() / 1000).longValue();
        }
        Long latestEnd = Double.valueOf(new Interval(now, timeFrame.getInterval().getEnd()).toDurationMillis() / 1000).longValue();
        Long minRunningTime = remainingMinRunningTime;
        Long maxRunningTime = remainingMinRunningTime + (timeFrame.getMaxRunningTime() - timeFrame.getMinRunningTime());
        return createSempTimeFrame(deviceId, earliestStart, latestEnd, minRunningTime, maxRunningTime);
    }
    
    private de.avanux.smartapplianceenabler.semp.webservice.Timeframe createSempTimeFrame(String deviceId, Long earliestStart, Long latestEnd, Long minRunningTime, Long maxRunningTime) {
        de.avanux.smartapplianceenabler.semp.webservice.Timeframe timeFrame = new de.avanux.smartapplianceenabler.semp.webservice.Timeframe();
        timeFrame.setDeviceId(deviceId);
        timeFrame.setEarliestStart(earliestStart);
        timeFrame.setLatestEnd(latestEnd);
        timeFrame.setMinRunningTime(minRunningTime);
        timeFrame.setMaxRunningTime(maxRunningTime);
        return timeFrame;
    }
    
    private Appliance findAppliance(String deviceId) {
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            if(appliance.getId().equals(deviceId)) {
                return appliance;
            }
        }
        return null;
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
