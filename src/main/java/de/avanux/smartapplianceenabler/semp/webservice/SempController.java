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
        List<Appliance> appliances = ApplianceManager.getInstance().getAppliances();
        for (Appliance appliance : appliances) {
            DeviceStatus deviceStatus = createDeviceStatus(appliance);
            deviceStatuses.add(deviceStatus);
        }
        device2EM.setDeviceStatus(deviceStatuses);
        return marshall(device2EM);
    }
    
    @RequestMapping(value=BASE_URL + "/DeviceInfo", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String deviceInfo(@RequestParam(value="DeviceId") String deviceId) {
        logger.debug("Device info requested for device id=" + deviceId);
        DeviceInfo deviceInfo = findDeviceInfo(device2EM, deviceId);
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceInfo(Collections.singletonList(deviceInfo));
        return marshall(device2EM);
    }

    @RequestMapping(value=BASE_URL + "/DeviceStatus", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String deviceStatus(@RequestParam(value="DeviceId") String deviceId) {
        logger.debug("Device status requested for device id=" + deviceId);
        Appliance appliance = findAppliance(deviceId);
        DeviceStatus deviceStatus = createDeviceStatus(appliance);
        Device2EM device2EM = new Device2EM();
        device2EM.setDeviceStatus(Collections.singletonList(deviceStatus));
        return marshall(device2EM);
    }
    
    @RequestMapping(value=BASE_URL + "/PlanningRequest", method=RequestMethod.GET, produces="application/xml")
    @ResponseBody
    public String planningRequest(@RequestParam(value="DeviceId") String deviceId) {
        logger.debug("Planning request requested for device id=" + deviceId);
        PlanningRequest planningRequest = findPlanningRequest(device2EM, deviceId);
        Device2EM device2EM = new Device2EM();
        if(planningRequest != null) {
            device2EM.setPlanningRequest(Collections.singletonList(planningRequest));
        }
        else {
            logger.debug("No planning request configured for device id " + deviceId);
        }
        return marshall(device2EM);
    }

    @RequestMapping(value=BASE_URL, method=RequestMethod.POST, consumes="application/xml")
    @ResponseBody
    public void em2Device(@RequestBody EM2Device em2Device) {
        List<DeviceControl> deviceControls = em2Device.getDeviceControl();
        for(DeviceControl deviceControl : deviceControls) {
            logger.debug("Received control request for device id=" + deviceControl.getDeviceId());
            Appliance appliance = findAppliance(deviceControl.getDeviceId());
            if(appliance != null) {
                if(appliance.getControl() != null) {
                    appliance.getControl().on(deviceControl.isOn());
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
        
        Control control = appliance.getControl();
        if(control != null) {
            deviceStatus.setStatus(control.isOn() ? Status.On : Status.Off);
        }
        else {
            deviceStatus.setStatus(Status.Offline);
        }
        deviceStatus.setEMSignalsAccepted(control != null);
        
        Meter meter = appliance.getMeter();
        PowerConsumption powerConsumption = new PowerConsumption();
        powerConsumption.setAveragePower(meter.getAveragePower());
        powerConsumption.setAveragingInterval(meter.getMeasurementInterval());
        powerConsumption.setTimestamp(0);
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

    private PlanningRequest findPlanningRequest(Device2EM device2EM, String deviceId) {
        if(device2EM.getPlanningRequest() != null) {
            for(PlanningRequest planningRequest : device2EM.getPlanningRequest()) {
                if(planningRequest.getTimeframe().getDeviceId().equals(deviceId)) {
                    return planningRequest;
                }
            }
        }
        return null;
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
