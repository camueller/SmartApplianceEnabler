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

import de.avanux.smartapplianceenabler.appliance.ActiveIntervalChangedListener;
import de.avanux.smartapplianceenabler.appliance.Appliance;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.appliance.RuntimeRequest;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.TimeframeInterval;
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
public class SempController {

    private static final String BASE_URL = "/semp";
    private static final String CROSS_ORIGIN_URL = "http://localhost:4200";
    public static final String SCHEMA_LOCATION = "http://www.sma.de/communication/schema/SEMP/v1";
    private Logger logger = LoggerFactory.getLogger(SempController.class);
    private boolean timeFrameChangedListenerRegistered;
    
    public SempController() {
        logger.info("SEMP controller created.");
    }

    @RequestMapping(value=BASE_URL, method=RequestMethod.GET, produces="application/xml")
    public String device2EM() {
        logger.debug("Device info/status/planning requested.");
        return marshall(createDevice2EM(new LocalDateTime()));
    }

    public Device2EM createDevice2EM(LocalDateTime now) {
        List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();
        List<PlanningRequest> planningRequests = new ArrayList<PlanningRequest>();
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            if(!timeFrameChangedListenerRegistered && appliance.getRunningTimeMonitor() != null) {
                timeFrameChangedListenerRegistered = true;
            }
            DeviceStatus deviceStatus = createDeviceStatus(appliance);
            deviceStatuses.add(deviceStatus);
            PlanningRequest planningRequest = createPlanningRequest(now, appliance);
            if(planningRequest != null) {
                planningRequests.add(planningRequest);
            }
        }
        Device2EM device2EM = ApplianceManager.getInstance().getDevice2EM();
        device2EM.setDeviceInfo(createDeviceInfo());
        device2EM.setDeviceStatus(deviceStatuses);
        device2EM.setPlanningRequest(planningRequests);
        return device2EM;
    }
    
    @RequestMapping(value=BASE_URL + "/DeviceInfo", method=RequestMethod.GET, produces="application/xml")
    public String deviceInfo(@RequestParam(value="DeviceId", required = false) String deviceId) {
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(Collections.singletonList(createDeviceInfo(deviceId)));
        return marshall(device2EM);
    }

    public DeviceInfo createDeviceInfo(String deviceId) {
        if(deviceId != null) {
            logger.debug("Device info requested of device id=" + deviceId);
            DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(deviceId);
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceId);
            deviceInfo.setCapabilities(createCapabilities(deviceInfo, appliance.getMeter() != null,
                    appliance.canConsumeOptionalEnergy()));
            return deviceInfo;
        }
        return null;
    }

    private List<DeviceInfo> createDeviceInfo() {
        logger.debug("Device info requested of all devices");
        List<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(appliance.getId());
            deviceInfo.setCapabilities(createCapabilities(deviceInfo, appliance.getMeter() != null,
                    appliance.canConsumeOptionalEnergy()));
            deviceInfos.add(deviceInfo);
        }
        return deviceInfos;
    }

    private Capabilities createCapabilities(DeviceInfo deviceInfo, boolean hasMeter, boolean canConsumeOptionalEnergy) {
        Capabilities capabilities = deviceInfo.getCapabilities();
        if(capabilities == null) {
            capabilities = new Capabilities();
        }
        if(hasMeter) {
            capabilities.setCurrentPowerMethod(CurrentPowerMethod.Measurement);
        }
        else {
            capabilities.setCurrentPowerMethod(CurrentPowerMethod.Estimation);
        }
        capabilities.setOptionalEnergy(canConsumeOptionalEnergy);
        return capabilities;
    }

    @RequestMapping(value=BASE_URL + "/DeviceStatus", method=RequestMethod.GET, produces="application/xml")
    public String deviceStatus(@RequestParam(value="DeviceId", required = false) String deviceId) {
        List<DeviceStatus> deviceStatuses = new ArrayList<DeviceStatus>();
        if(deviceId != null) {
            logger.debug("{}: Device status requested", deviceId);
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceId);
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
    public String planningRequest(@RequestParam(value="DeviceId", required = false) String deviceId) {
        LocalDateTime now = new LocalDateTime();
        List<PlanningRequest> planningRequests = new ArrayList<PlanningRequest>();
        if(deviceId != null) {
            logger.debug("{}: Planning request requested", deviceId);
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceId);
            PlanningRequest planningRequest = createPlanningRequest(now, appliance);
            addPlanningRequest(planningRequests, planningRequest);
        }
        else {
            logger.debug("Planning request requested of all devices");
            List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
            for (Appliance appliance : appliances) {
                PlanningRequest planningRequest = createPlanningRequest(now, appliance);
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
    @CrossOrigin(origins = CROSS_ORIGIN_URL)
    public void em2Device(@RequestBody EM2Device em2Device) {
        em2Device(new LocalDateTime(), em2Device);
    }

    public void em2Device(LocalDateTime now, EM2Device em2Device) {
        List<DeviceControl> deviceControls = em2Device.getDeviceControl();
        for(DeviceControl deviceControl : deviceControls) {
            logger.debug("{}: Received control request: {}", deviceControl.getDeviceId(), deviceControl);
            Appliance appliance = ApplianceManager.getInstance().findAppliance(deviceControl.getDeviceId());
            if(appliance != null) {
                appliance.setApplianceState(now, deviceControl.isOn(), deviceControl.getRecommendedPowerConsumption(),
                        false,
                        "Setting appliance state to " + (deviceControl.isOn() ? "ON" : "OFF"));
            }
            else {
                logger.warn("{}: No appliance configured for device id", deviceControl.getDeviceId());
            }
        }
    }

    private DeviceStatus createDeviceStatus(Appliance appliance) {
        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setDeviceId(appliance.getId());
        Meter meter = appliance.getMeter();

        Control control = appliance.getControl();
        if(control != null) {
            deviceStatus.setStatus(control.isOn() ? Status.On : Status.Off);
            deviceStatus.setEMSignalsAccepted(appliance.isAcceptControlRecommendations());
            logger.debug("{}: Reporting device status from control", appliance.getId());
        }
        else {
            // there is no control for the appliance ...
            if(meter != null) {
                // ... but we can derive the status from power consumption
                deviceStatus.setStatus(meter.isOn() ? Status.On : Status.Off);
                logger.debug("{}: Reporting device status based on power consumption", appliance.getId());
            }
            else {
                // ... and no meter; we have to assume the appliance is switched off
                deviceStatus.setStatus(Status.Offline);
                logger.debug("{}: Appliance has neither control nor meter.", appliance.getId());
            }
            
            // an appliance without control cannot be controlled ;-)
            deviceStatus.setEMSignalsAccepted(false);
        }
        logger.debug("{}: {}", appliance.getId(), deviceStatus.toString());

        PowerInfo powerInfo = new PowerInfo();
        if(meter != null) {
            logger.debug("{}: Reporting power info from meter.", appliance.getId());
            powerInfo.setAveragePower(meter.getAveragePower());
            powerInfo.setMinPower(meter.getMinPower());
            powerInfo.setMaxPower(meter.getMaxPower());
            powerInfo.setAveragingInterval(60); // always report 60 for SEMP regardless of real averaging interval
        }
        else {
            logger.debug("{}: Reporting power info from device characteristics.", appliance.getId());
            DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(appliance.getId());
            if(deviceStatus.getStatus() == Status.On) {
                powerInfo.setAveragePower(deviceInfo.getCharacteristics().getMaxPowerConsumption());
            }
            else {
                powerInfo.setAveragePower(0);
            }
            powerInfo.setAveragingInterval(60);
        }
        powerInfo.setTimestamp(0);
        logger.debug("{}: {}", appliance.getId(), powerInfo.toString());

        PowerConsumption powerConsumption = new PowerConsumption();
        powerConsumption.setPowerInfo(Collections.singletonList(powerInfo));
        
        deviceStatus.setPowerConsumption(Collections.singletonList(powerConsumption));
        return deviceStatus;
    }

    private PlanningRequest createPlanningRequest(LocalDateTime now, Appliance appliance) {
        PlanningRequest planningRequest = null;
        List<RuntimeRequest> runtimeRequests = appliance.getRuntimeRequests(now, false);
        if(runtimeRequests.size() > 0) {
            planningRequest = new PlanningRequest();
            List<de.avanux.smartapplianceenabler.semp.webservice.Timeframe> sempTimeFrames = new ArrayList<de.avanux.smartapplianceenabler.semp.webservice.Timeframe>();
            planningRequest.setTimeframes(sempTimeFrames);
            for(RuntimeRequest runtimeRequest : runtimeRequests) {
                Timeframe sempTimeFrame = createSempTimeFrame(appliance.getId(), runtimeRequest);
                sempTimeFrames.add(sempTimeFrame);
                logger.debug("{}: Timeframe added to PlanningRequest: {}", appliance.getId(), sempTimeFrame);
            }
        }
        else {
            logger.debug("{}: No planning requests created", appliance.getId());
            return null;
        }
        return planningRequest;
    }

    protected de.avanux.smartapplianceenabler.semp.webservice.Timeframe
    createSempTimeFrame(String deviceId, RuntimeRequest runtimeRequest) {
        Integer minRunningTime = runtimeRequest.getMinRunningTime();
        Integer maxRunningTime = runtimeRequest.getMaxRunningTime();
        if(minRunningTime == null) {
            minRunningTime = 0;
        }
        if(maxRunningTime == null) {
            maxRunningTime = minRunningTime;
        }
        if(minRunningTime.equals(maxRunningTime)) {
            /** WORKAROUND:
             * For unknown reason the SunnyPortal displays the scheduled times only
             * if maxRunningTime AND minRunningTime are returned and are NOT EQUAL
             * Therefore we ensure that they are not equal by reducing minRunningTime by 1 second
             */
            minRunningTime = minRunningTime >= 1 ? minRunningTime - 1 : 0;
        }
        else {
            // according to spec minRunningTime only has to be returned if different from maxRunningTime
            minRunningTime = minRunningTime >= 0 ? minRunningTime : 0;
        }
        maxRunningTime = maxRunningTime >= 0 ? maxRunningTime : 0;

        de.avanux.smartapplianceenabler.semp.webservice.Timeframe timeFrame
                = new de.avanux.smartapplianceenabler.semp.webservice.Timeframe();
        timeFrame.setDeviceId(deviceId);
        timeFrame.setEarliestStart(runtimeRequest.getEarliestStart());
        timeFrame.setLatestEnd(runtimeRequest.getLatestEnd());
        if(runtimeRequest.getMinEnergy() != null) {
            timeFrame.setMinEnergy(runtimeRequest.getMinEnergy());
            timeFrame.setMaxEnergy(runtimeRequest.getMaxEnergy());
        }
        else {
            timeFrame.setMinRunningTime(minRunningTime);
            timeFrame.setMaxRunningTime(maxRunningTime);
        }
        logger.debug("{}: Timeframe created: {}", deviceId, timeFrame);
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
