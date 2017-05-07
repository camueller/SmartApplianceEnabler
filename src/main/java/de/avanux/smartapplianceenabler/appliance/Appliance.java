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
package de.avanux.smartapplianceenabler.appliance;

import com.pi4j.io.gpio.GpioController;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import de.avanux.smartapplianceenabler.modbus.ModbusElectricityMeter;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusSwitch;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Appliance implements ControlStateChangedListener, StartingCurrentSwitchListener {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(Appliance.class));
    @XmlAttribute
    private String id;
    @XmlElements({
            @XmlElement(name = "S0ElectricityMeter", type = S0ElectricityMeter.class),
            @XmlElement(name = "S0ElectricityMeterNetworked", type = S0ElectricityMeterNetworked.class),
            @XmlElement(name = "ModbusElectricityMeter", type = ModbusElectricityMeter.class),
            @XmlElement(name = "HttpElectricityMeter", type = HttpElectricityMeter.class),
    })
    private Meter meter;
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
    @XmlElement(name = "Schedule")
    private List<Schedule> schedules;
    @XmlTransient
    private RunningTimeMonitor runningTimeMonitor;

    public void setId(String id) {
        this.id = id;
        this.logger.setApplianceId(id);
    }

    public String getId() {
        return id;
    }

    public Meter getMeter() {
        return this.meter;
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

    public void init(Integer additionRunningTime) {
        this.logger.setApplianceId(id);
        if(schedules != null && schedules.size() > 0) {
            logger.info("Schedules configured: " + schedules.size());
            runningTimeMonitor = new RunningTimeMonitor();
            runningTimeMonitor.setApplianceId(id);
            if(! hasStartingCurrentDetection()) {
                // in case of starting current detection timeframes are added after
                // starting current was detected
                runningTimeMonitor.setSchedules(schedules);
                logger.debug("Schedules passed to RunningTimeMonitor");
            }
        }
        else {
            logger.info("No schedules configured");
        }

        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof ApplianceIdConsumer) {
                    ((ApplianceIdConsumer) control).setApplianceId(id);
                }
                if(control instanceof StartingCurrentSwitch) {
                    for(Control wrappedControl : ((StartingCurrentSwitch) control).getControls()) {
                        ((ApplianceIdConsumer) wrappedControl).setApplianceId(id);
                        wrappedControl.addControlStateChangedListener(this);
                        logger.debug("Registered as " + ControlStateChangedListener.class.getSimpleName() + " with " + wrappedControl.getClass().getSimpleName());
                    }
                    ((StartingCurrentSwitch) control).addStartingCurrentSwitchListener(this);
                    logger.debug("Registered as " + StartingCurrentSwitchListener.class.getSimpleName() + " with " + control.getClass().getSimpleName());
                }
                else {
                    control.addControlStateChangedListener(this);
                    logger.debug("Registered as " + ControlStateChangedListener.class.getSimpleName() + " with " + control.getClass().getSimpleName());
                }
            }
        }
        Meter meter = getMeter();
        if(meter != null) {
            if(meter instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) meter).setApplianceId(id);
            }
        }

        if(meter != null && meter instanceof S0ElectricityMeter) {
            if(controls != null) {
                Control control = controls.get(0);
                ((S0ElectricityMeter) meter).setControl(control);
                logger.debug(S0ElectricityMeter.class.getSimpleName() + " uses " + control.getClass().getSimpleName());
            }
        }

        if(schedules != null) {
            if(additionRunningTime != null) {
                Schedule.setAdditionalRunningTime(additionRunningTime);
            }
            for(Schedule schedule : schedules) {
                schedule.getTimeframe().setSchedule(schedule);
            }
        }
    }

    public void start(Timer timer, GpioController gpioController,
                      Map<String, PulseReceiver> pulseReceiverIdWithPulseReceiver,
                      Map<String, ModbusTcp> modbusIdWithModbusTcp) {

        for(GpioControllable gpioControllable : getGpioControllables()) {
            logger.info("Starting " + gpioControllable.getClass().getSimpleName());
            gpioControllable.setGpioController(gpioController);
            gpioControllable.start();
        }

        if(meter != null && meter instanceof S0ElectricityMeterNetworked) {
            S0ElectricityMeterNetworked s0ElectricityMeterNetworked = (S0ElectricityMeterNetworked) meter;
            logger.info("Starting " + S0ElectricityMeterNetworked.class.getSimpleName());
            String pulseReceiverId = s0ElectricityMeterNetworked.getIdref();
            PulseReceiver pulseReceiver = pulseReceiverIdWithPulseReceiver.get(pulseReceiverId);
            s0ElectricityMeterNetworked.setPulseReceiver(pulseReceiver);
            s0ElectricityMeterNetworked.start();
        }

        if(meter != null && meter instanceof HttpElectricityMeter) {
            ((HttpElectricityMeter) meter).start(timer);
        }

        for(ModbusSlave modbusSlave : getModbusSlaves()) {
            logger.info("Starting " + modbusSlave.getClass().getSimpleName());
            modbusSlave.setApplianceId(id);
            String modbusId = modbusSlave.getIdref();
            ModbusTcp modbusTcp = modbusIdWithModbusTcp.get(modbusId);
            modbusSlave.setModbusTcp(modbusTcp);
        }
        if(meter != null && meter instanceof ModbusElectricityMeter) {
            ((ModbusElectricityMeter) meter).start(timer);
        }

        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof  StartingCurrentSwitch) {
                    logger.info("Starting " + StartingCurrentSwitch.class.getSimpleName());
                    ((StartingCurrentSwitch) control).start(getMeter(), timer);
                }
            }
        }
    }

    public void setHolidays(List<LocalDate> holidays) {
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                final Timeframe timeframe = schedule.getTimeframe();
                if(timeframe instanceof DayTimeframe) {
                    ((DayTimeframe) timeframe).setHolidays(holidays);
                }
            }
        }
    }

    public boolean hasTimeframeForHolidays() {
        if(schedules != null) {
            for (Schedule schedule : schedules) {
                Timeframe timeframe = schedule.getTimeframe();
                if(timeframe instanceof  DayTimeframe) {
                    List<Integer> daysOfWeekValues = ((DayTimeframe) timeframe).getDaysOfWeekValues();
                    if(daysOfWeekValues != null && daysOfWeekValues.contains(DayTimeframe.DOW_HOLIDAYS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<GpioControllable> getGpioControllables() {
        Set<GpioControllable> controllables = new HashSet<GpioControllable>();
        if(meter != null && meter instanceof S0ElectricityMeter) {
            controllables.add((S0ElectricityMeter) meter);
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
        if(meter != null && meter instanceof  ModbusElectricityMeter) {
            slaves.add((ModbusElectricityMeter) meter);
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
        if(schedules != null) {
            for(Schedule schedule : schedules) {
                if(schedule.getMaxRunningTime() != schedule.getMinRunningTime()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasStartingCurrentDetection() {
        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof StartingCurrentSwitch) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a forced schedule if there is one.
     * @param now
     * @return
     */
    private Schedule getForcedSchedule(LocalDateTime now) {
        String scheduleId = null;
        if(controls != null) {
            for(Control control : controls) {
                if(control instanceof StartingCurrentSwitch) {
                    DayTimeframeCondition dayTimeframeCondition = ((StartingCurrentSwitch) control).getDayTimeframeCondition();
                    if(dayTimeframeCondition != null) {
                        if(dayTimeframeCondition.isMet(now)) {
                            scheduleId = dayTimeframeCondition.getIdref();
                            break;
                        }
                    }
                }
            }
        }
        if(scheduleId != null) {
            for(Schedule schedule : schedules) {
                if(scheduleId.equals(schedule.getId())) {
                    return schedule;
                }
            }
        }
        return null;
    }

    @Override
    public void controlStateChanged(boolean switchOn) {
        logger.debug("Control state has changed to " + (switchOn ? "on" : "off") + ": runningTimeMonitor=" + (runningTimeMonitor != null ? "not null" : "null"));
        if(runningTimeMonitor != null) {
            runningTimeMonitor.setRunning(switchOn);
        }
    }

    @Override
    public void startingCurrentDetected() {
        logger.debug("Activating next sufficient timeframe interval after starting current has been detected");
        LocalDateTime now = new LocalDateTime();
        TimeframeInterval timeframeInterval;
        Schedule forcedSchedule = getForcedSchedule(now);
        if(forcedSchedule != null) {
            logger.debug("Forcing schedule " + forcedSchedule);
            timeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, Collections.singletonList(forcedSchedule), false, true);
        }
        else {
            timeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, schedules, false, true);
        }
        runningTimeMonitor.activateTimeframeInterval(now, timeframeInterval);
    }

    @Override
    public void finishedCurrentDetected() {
        logger.debug("Deactivating timeframe interval until starting current is detected again: runningTimeMonitor=" + (runningTimeMonitor != null ? "not null" : "null"));
        if(runningTimeMonitor != null) {
            runningTimeMonitor.activateTimeframeInterval(new LocalDateTime(), (TimeframeInterval) null);
        }
    }
}
