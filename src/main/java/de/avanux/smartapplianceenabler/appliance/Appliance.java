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
package de.avanux.smartapplianceenabler.appliance;

import java.util.*;

import javax.xml.bind.annotation.*;

import com.pi4j.io.gpio.GpioController;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import de.avanux.smartapplianceenabler.modbus.ModbusElectricityMeter;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusSwitch;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import org.slf4j.LoggerFactory;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Appliance implements ControlStateChangedListener, StartingCurrentSwitchListener {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
    @XmlAttribute
    private String id;
    @XmlElement(name = "S0ElectricityMeter")
    private S0ElectricityMeter s0ElectricityMeter;
    @XmlElement(name = "S0ElectricityMeterNetworked")
    private S0ElectricityMeterNetworked s0ElectricityMeterNetworked;
    @XmlElement(name = "ModbusElectricityMeter")
    private ModbusElectricityMeter modbusElectricityMeter;
    // Mapping interfaces in JAXB:
    // https://jaxb.java.net/guide/Mapping_interfaces.html
    // http://stackoverflow.com/questions/25374375/jaxb-wont-unmarshal-my-previously-marshalled-interface-impls
    @XmlElements({
            @XmlElement(name = "StartingCurrentSwitch", type = StartingCurrentSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class)
    })
    private List<Control> controls;
    @XmlElement(name = "Timeframe")
    private List<TimeFrame> timeFrames;
    @XmlTransient
    private RunningTimeMonitor runningTimeMonitor;

    public String getId() {
        return id;
    }

    public S0ElectricityMeter getS0ElectricityMeter() {
        return s0ElectricityMeter;
    }

    public S0ElectricityMeterNetworked getS0ElectricityMeterNetworked() {
        return s0ElectricityMeterNetworked;
    }

    public ModbusElectricityMeter getModbusElectricityMeter() {
        return modbusElectricityMeter;
    }

    public Meter getMeter() {
        return new MeterFactory().getMeter(this);
    }
    
    public List<Control> getControls() {
        return controls;
    }

    public RunningTimeMonitor getRunningTimeMonitor() {
        return runningTimeMonitor;
    }

    public void setRunningTimeMonitor(RunningTimeMonitor runningTimeMonitor) {
        this.runningTimeMonitor = runningTimeMonitor;
        this.runningTimeMonitor.setApplianceId(id);
    }

    public void init() {
        this.logger.setApplianceId(id);
        if(timeFrames != null && timeFrames.size() > 0) {
            runningTimeMonitor = new RunningTimeMonitor();
            runningTimeMonitor.setApplianceId(id);
            if(! hasStartingCurrentDetection()) {
                // in case of starting current detection timeframes are added after
                // starting current was detected
                runningTimeMonitor.setTimeFrames(timeFrames);
            }
        }

        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof ApplianceIdConsumer) {
                    ((ApplianceIdConsumer) control).setApplianceId(id);
                }
                control.addControlStateChangedListener(this);
                if(control instanceof  StartingCurrentSwitch) {
                    for(Control wrappedControl : ((StartingCurrentSwitch) control).getControls()) {
                        ((ApplianceIdConsumer) wrappedControl).setApplianceId(id);
                    }
                    ((StartingCurrentSwitch) control).addStartingCurrentSwitchListener(this);
                }
            }
        }
        Meter meter = getMeter();
        if(meter != null) {
            if(meter instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) meter).setApplianceId(id);
            }
        }

        if(s0ElectricityMeter != null) {
            if(controls != null) {
                s0ElectricityMeter.setControl(controls.get(0));
            }
        }
    }

    public void start(Timer timer, GpioController gpioController,
                      Map<String, PulseReceiver> pulseReceiverIdWithPulseReceiver,
                      Map<String, ModbusTcp> modbusIdWithModbusTcp) {
        for(GpioControllable gpioControllable : getGpioControllables()) {
            gpioControllable.setGpioController(gpioController);
            gpioControllable.start();
        }

        S0ElectricityMeterNetworked s0ElectricityMeterNetworked = getS0ElectricityMeterNetworked();
        if(s0ElectricityMeterNetworked != null) {
            String pulseReceiverId = s0ElectricityMeterNetworked.getIdref();
            PulseReceiver pulseReceiver = pulseReceiverIdWithPulseReceiver.get(pulseReceiverId);
            s0ElectricityMeterNetworked.setPulseReceiver(pulseReceiver);
            s0ElectricityMeterNetworked.start();
        }

        for(ModbusSlave modbusSlave : getModbusSlaves()) {
            modbusSlave.setApplianceId(id);
            String modbusId = modbusSlave.getIdref();
            ModbusTcp modbusTcp = modbusIdWithModbusTcp.get(modbusId);
            modbusSlave.setModbusTcp(modbusTcp);
            modbusSlave.start(timer);
        }

        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof  StartingCurrentSwitch) {
                    ((StartingCurrentSwitch) control).start(getMeter(), timer);
                }
            }
        }
    }


    private Set<GpioControllable> getGpioControllables() {
        Set<GpioControllable> controllables = new HashSet<GpioControllable>();
        if(s0ElectricityMeter != null) {
            controllables.add(s0ElectricityMeter);
        }
        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof  GpioControllable) {
                    controllables.add((GpioControllable) control);
                }
                else if(control instanceof  StartingCurrentSwitch) {
                    for(Control wrappedControl : ((StartingCurrentSwitch) control).getControls()) {
                        if(wrappedControl instanceof GpioControllable) {
                            controllables.add((GpioControllable) wrappedControl);
                        }
                    }
                }
            }
        }
        return controllables;
    }

    private Set<ModbusSlave> getModbusSlaves() {
        Set<ModbusSlave> slaves = new HashSet<ModbusSlave>();
        if(modbusElectricityMeter != null) {
            slaves.add(modbusElectricityMeter);
        }
        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof  ModbusSwitch) {
                    slaves.add((ModbusSwitch) control);
                }
                else if(control instanceof  StartingCurrentSwitch) {
                    for(Control wrappedControl : ((StartingCurrentSwitch) control).getControls()) {
                        if(wrappedControl instanceof ModbusSwitch) {
                            slaves.add((ModbusSwitch) wrappedControl);
                        }
                    }
                 }
            }
        }
        return slaves;
    }

    public boolean canConsumeOptionalEnergy() {
        if(timeFrames != null) {
            for(TimeFrame timeFrame : timeFrames) {
                if(timeFrame.getMaxRunningTime() != timeFrame.getMinRunningTime()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasStartingCurrentDetection() {
        for(Control control : controls) {
            if(control instanceof StartingCurrentSwitch) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void controlStateChanged(boolean switchOn) {
        logger.debug("Control state has changed to " + switchOn);
        runningTimeMonitor.setRunning(switchOn);
        if (!switchOn && hasStartingCurrentDetection()) {
            logger.debug("Deactivating timeframes until starting current is detected again");
            runningTimeMonitor.setTimeFrames(null);
        }
    }

    @Override
    public void startingCurrentDetected() {
        logger.debug("Activating timeframes after starting current has been detected");
        runningTimeMonitor.setTimeFrames(timeFrames);
    }
}
