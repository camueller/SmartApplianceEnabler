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
import de.avanux.smartapplianceenabler.control.*;
import de.avanux.smartapplianceenabler.control.ev.EVControl;
import de.avanux.smartapplianceenabler.control.ev.EVModbusControl;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.meter.*;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.schedule.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Appliance implements ControlStateChangedListener, StartingCurrentSwitchListener,
        ActiveIntervalChangedListener {
    private transient Logger logger = LoggerFactory.getLogger(Appliance.class);
    @XmlAttribute
    private String id;
    // Mapping interfaces in JAXB:
    // https://jaxb.java.net/guide/Mapping_interfaces.html
    // http://stackoverflow.com/questions/25374375/jaxb-wont-unmarshal-my-previously-marshalled-interface-impls
    @XmlElements({
            @XmlElement(name = "AlwaysOnSwitch", type = AlwaysOnSwitch.class),
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class),
            @XmlElement(name = "MockSwitch", type = MockSwitch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "StartingCurrentSwitch", type = StartingCurrentSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class),
            @XmlElement(name = "ElectricVehicleCharger", type = ElectricVehicleCharger.class),
    })
    private Control control;
    @XmlElements({
            @XmlElement(name = "HttpElectricityMeter", type = HttpElectricityMeter.class),
            @XmlElement(name = "ModbusElectricityMeter", type = ModbusElectricityMeter.class),
            @XmlElement(name = "S0ElectricityMeter", type = S0ElectricityMeter.class),
            @XmlElement(name = "S0ElectricityMeterNetworked", type = S0ElectricityMeterNetworked.class)
    })
    private Meter meter;
    @XmlElement(name = "Schedule")
    private List<Schedule> schedules;
    private transient RunningTimeMonitor runningTimeMonitor;
    private transient boolean acceptControlRecommendations = true;
    private transient static final int CONSIDERATION_INTERVAL_DAYS = 2;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Meter getMeter() {
        return this.meter;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        if(control instanceof StartingCurrentSwitch) {
            if(((StartingCurrentSwitch) control).getControl() != null) {
                // only accept StartingCurrentSwitch with inner control
                this.control = control;
            }
            else {
                this.control = null;
            }
        }
        else {
            this.control = control;
        }
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public boolean isAcceptControlRecommendations() {
        return acceptControlRecommendations;
    }

    public void setAcceptControlRecommendations(boolean acceptControlRecommendations) {
        this.acceptControlRecommendations = acceptControlRecommendations;
        logger.debug("{} Set acceptControlRecommendations={}", id, acceptControlRecommendations);
    }

    public RunningTimeMonitor getRunningTimeMonitor() {
        return runningTimeMonitor;
    }

    public void setRunningTimeMonitor(RunningTimeMonitor runningTimeMonitor) {
        this.runningTimeMonitor = runningTimeMonitor;
        this.runningTimeMonitor.setApplianceId(id);
        this.runningTimeMonitor.addTimeFrameChangedListener(this);
    }

    public void init(Integer additionRunningTime) {
        if(control != null) {
            setRunningTimeMonitor(new RunningTimeMonitor());
        }
        if(schedules != null && schedules.size() > 0) {
            logger.info("{}: Schedules configured: {}", id, schedules.size());
            if(! hasStartingCurrentDetection()) {
                // in case of starting current detection timeframes are added after
                // starting current was detected
                if(runningTimeMonitor != null) {
                    runningTimeMonitor.setSchedules(schedules);
                    logger.debug("{}: Schedules passed to RunningTimeMonitor", id);
                }
            }
        }
        else {
            logger.info("{}: No schedules configured", id);
        }

        if(control != null) {
            if(control instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) control).setApplianceId(id);
            }
            if(control instanceof ElectricVehicleCharger) {
                ((ElectricVehicleCharger) control).init();
            }
            if(control instanceof StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                ((ApplianceIdConsumer) wrappedControl).setApplianceId(id);
                control.addControlStateChangedListener(this);
                logger.debug("{}: Registered as {} with {}", id, ControlStateChangedListener.class.getSimpleName(),
                        control.getClass().getSimpleName());
                ((StartingCurrentSwitch) control).addStartingCurrentSwitchListener(this);
                logger.debug("{}: Registered as {} with {}", id, StartingCurrentSwitchListener.class.getSimpleName(),
                        control.getClass().getSimpleName());
            }
            else {
                control.addControlStateChangedListener(this);
                logger.debug("{}: Registered as {} with {}", id, ControlStateChangedListener.class.getSimpleName(),
                        control.getClass().getSimpleName());
            }
        }
        Meter meter = getMeter();
        if(meter != null) {
            if(meter instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) meter).setApplianceId(id);
            }
            if(control != null) {
                if(meter instanceof S0ElectricityMeter) {
                    ((S0ElectricityMeter) meter).setControl(control);
                }
                if(meter instanceof S0ElectricityMeterNetworked) {
                    ((S0ElectricityMeterNetworked) meter).setControl(control);
                }
                logger.debug("{}: {} uses {}", id, meter.getClass().getSimpleName(), control.getClass().getSimpleName());
            }
        }
    }

    public void start(Timer timer, GpioController gpioController,
                      Map<String, PulseReceiver> pulseReceiverIdWithPulseReceiver,
                      Map<String, ModbusTcp> modbusIdWithModbusTcp) {

        if(runningTimeMonitor != null) {
            runningTimeMonitor.setTimer(timer);
        }

        for(GpioControllable gpioControllable : getGpioControllables()) {
            logger.info("{}: Starting {}", id, gpioControllable.getClass().getSimpleName());
            gpioControllable.setGpioController(gpioController);
            gpioControllable.start();
        }

        if(meter instanceof S0ElectricityMeterNetworked) {
            S0ElectricityMeterNetworked s0ElectricityMeterNetworked = (S0ElectricityMeterNetworked) meter;
            logger.info("{}: Starting {}", id, S0ElectricityMeterNetworked.class.getSimpleName());
            String pulseReceiverId = s0ElectricityMeterNetworked.getIdref();
            PulseReceiver pulseReceiver = pulseReceiverIdWithPulseReceiver.get(pulseReceiverId);
            s0ElectricityMeterNetworked.setPulseReceiver(pulseReceiver);
            s0ElectricityMeterNetworked.start();
        }

        if(meter instanceof HttpElectricityMeter) {
            ((HttpElectricityMeter) meter).start(timer);
        }

        for(ModbusSlave modbusSlave : getModbusSlaves()) {
            logger.info("{}: Starting {}", id, modbusSlave.getClass().getSimpleName());
            modbusSlave.setApplianceId(id);
            String modbusId = modbusSlave.getIdref();
            ModbusTcp modbusTcp = modbusIdWithModbusTcp.get(modbusId);
            modbusSlave.setModbusTcp(modbusTcp);
        }
        if(meter instanceof ModbusElectricityMeter) {
            ((ModbusElectricityMeter) meter).start(timer);
        }
        if(control instanceof ElectricVehicleCharger) {
            ((ElectricVehicleCharger) control).start(timer);
        }

        if(control instanceof  StartingCurrentSwitch) {
            logger.info("{}: Starting {}", id, StartingCurrentSwitch.class.getSimpleName());
            ((StartingCurrentSwitch) control).start(new LocalDateTime(), getMeter(), timer);
        }
    }

    public void stop() {
        if(control instanceof GpioControllable) {
            ((GpioControllable) control).stop();
        }
        if(meter instanceof GpioControllable) {
            ((GpioControllable) meter).stop();
        }
        if(runningTimeMonitor != null) {
            runningTimeMonitor.cancelTimer();
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
        if(control != null) {
            if(control instanceof  GpioControllable) {
                controllables.add((GpioControllable) control);
            }
            else if(control instanceof  StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                if(wrappedControl instanceof GpioControllable) {
                    controllables.add((GpioControllable) wrappedControl);
                }
            }
        }
        return controllables;
    }

    public boolean isControllable() {
        return control != null &&
            (
                control instanceof Switch
                || control instanceof HttpSwitch
                || control instanceof ModbusSwitch
                || control instanceof MockSwitch
                || control instanceof StartingCurrentSwitch
                || control instanceof ElectricVehicleCharger
            );
    }

    public void setApplianceState(LocalDateTime now, boolean switchOn, Integer recommendedPowerConsumption,
                                  boolean deactivateTimeframe, String logMessage) {
        if(control != null) {
            if(control instanceof ElectricVehicleCharger && recommendedPowerConsumption != null) {
                ((ElectricVehicleCharger) control).setChargePower(recommendedPowerConsumption);
            }

            boolean stateChanged = false;
            // only change state if requested state differs from actual state
            if(control.isOn() ^ switchOn) {
                control.on(now, switchOn);
                if(! switchOn && deactivateTimeframe) {
                    this.runningTimeMonitor.activateTimeframeInterval(now, (TimeframeInterval) null);
                }
                stateChanged = true;
                this.runningTimeMonitor.updateActiveTimeframeInterval(now);
            }
            if(stateChanged) {
                logger.debug("{}: {}", id, logMessage);
            }
        }
        else {
            logger.warn("{}: Appliance configuration does not contain control.", id);
        }
    }

    private Set<ModbusSlave> getModbusSlaves() {
        Set<ModbusSlave> slaves = new HashSet<ModbusSlave>();
        if(meter != null && meter instanceof  ModbusElectricityMeter) {
            slaves.add((ModbusElectricityMeter) meter);
        }
        if(control != null) {
            if(control instanceof  ModbusSwitch) {
                slaves.add((ModbusSwitch) control);
            }
            else if(control instanceof  StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                if(wrappedControl instanceof ModbusSwitch) {
                    slaves.add((ModbusSwitch) wrappedControl);
                }
            }
            else if(control instanceof  ElectricVehicleCharger) {
                EVControl evControl = ((ElectricVehicleCharger) control).getEvControl();
                if(evControl instanceof EVModbusControl) {
                    slaves.add((EVModbusControl) evControl);
                }
            }
        }
        return slaves;
    }

    public boolean canConsumeOptionalEnergy() {
        if(this.control instanceof ElectricVehicleCharger) {
            return ((ElectricVehicleCharger) this.control).isUseOptionalEnergy();
        }
        else if(schedules != null) {
            for (Schedule schedule : schedules) {
                if (schedule.getRequest() != null
                        && schedule.getRequest().getMax() != null
                        && schedule.getRequest().getMax() > schedule.getRequest().getMin()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasStartingCurrentDetection() {
        return control != null && control instanceof StartingCurrentSwitch;
    }

    /**
     * Returns a forced schedule if there is one.
     * @param now
     * @return
     */
    private Schedule getForcedSchedule(LocalDateTime now) {
        String scheduleId = null;
        if(control instanceof StartingCurrentSwitch) {
            DayTimeframeCondition dayTimeframeCondition = ((StartingCurrentSwitch) control).getDayTimeframeCondition();
            if(dayTimeframeCondition != null) {
                if(dayTimeframeCondition.isMet(now)) {
                    scheduleId = dayTimeframeCondition.getIdref();
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

    public List<RuntimeInterval> getRuntimeIntervals(LocalDateTime now, boolean onlySufficient) {
        List<RuntimeInterval> runtimeIntervals = new ArrayList<>();
        if(this.control instanceof ElectricVehicleCharger &&
                (runningTimeMonitor == null || runningTimeMonitor.getActiveTimeframeInterval() == null)) {
            if(((ElectricVehicleCharger) this.control).isVehicleConnected()) {
                RuntimeInterval evOptionalEnergy = getRuntimeIntervalForEVUsingOptionalEnergy();
                logger.debug("{}: requesting optional energy for electric vehicle: {}", id, evOptionalEnergy);
                runtimeIntervals.add(evOptionalEnergy);
            }
        }
        if(runningTimeMonitor != null) {
            runtimeIntervals.addAll(getRuntimeIntervals(now,
                    runningTimeMonitor.getSchedules(),
                    runningTimeMonitor.getActiveTimeframeInterval(),
                    onlySufficient,
                    runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now),
                    runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now)));
        }
        return runtimeIntervals;
    }

    protected List<RuntimeInterval> getRuntimeIntervals(LocalDateTime now, List<Schedule> schedules,
                                                        TimeframeInterval activeTimeframeInterval, boolean onlySufficient,
                                                        Integer remainingMinRunningTime, Integer remainingMaxRunningTime) {
        List<RuntimeInterval> runtimeIntervals = new ArrayList<>();
        if(schedules != null && schedules.size() > 0) {
            logger.debug("{}: Active schedules: {}", id, schedules.size());
            if(activeTimeframeInterval != null) {
                // active timeframe interval has always to be added even if not sufficient
                addRuntimeInterval(now, activeTimeframeInterval, runtimeIntervals,
                        remainingMinRunningTime, remainingMaxRunningTime);
            }

            Interval considerationInterval = new Interval(now.toDateTime(), now.plusDays(CONSIDERATION_INTERVAL_DAYS).toDateTime());
            List<TimeframeInterval> timeFrameIntervals = Schedule.findTimeframeIntervals(now, considerationInterval,
                    schedules, false, onlySufficient);
            for(TimeframeInterval timeframeIntervalOfSchedule : timeFrameIntervals) {
                Schedule schedule = timeframeIntervalOfSchedule.getTimeframe().getSchedule();
                Interval interval = timeframeIntervalOfSchedule.getInterval();
                Integer minRunningTime = schedule.getRequest() instanceof RuntimeRequest ? schedule.getRequest().getMin() : null;
                if(minRunningTime != null) {
                    if(interval.contains(now.toDateTime()) && remainingMinRunningTime != null) {
                        minRunningTime = remainingMinRunningTime;
                    }
                    addRuntimeInterval(runtimeIntervals, interval, minRunningTime, schedule.getRequest().getMax(), now);
                }
            }
        }
        else if(activeTimeframeInterval != null) {
            logger.debug("{}: Active timeframe interval found", id);
            addRuntimeInterval(now, activeTimeframeInterval, runtimeIntervals, remainingMinRunningTime, remainingMaxRunningTime);
        }
        else {
            logger.debug("{}: No timeframes found", id);
        }
        return runtimeIntervals;
    }

    private RuntimeInterval getRuntimeIntervalForEVUsingOptionalEnergy() {
        float energy = meter.getEnergy();
        logger.debug("{}: energy metered: {} kWh", id, energy);

        RuntimeInterval runtimeInterval = new RuntimeInterval();
        runtimeInterval.setEarliestStart(0);
        runtimeInterval.setLatestEnd(CONSIDERATION_INTERVAL_DAYS * 24 * 3600);
        runtimeInterval.setMinEnergy(0);
        // FIXME use battery capacity
        runtimeInterval.setMaxEnergy(40000 - Float.valueOf(energy * 1000).intValue());
        return runtimeInterval;
    }

    private void addRuntimeInterval(LocalDateTime now, TimeframeInterval timeframeInterval, List<RuntimeInterval> runtimeIntervals,
                                    Integer remainingMinRunningTime, Integer remainingMaxRunningTime) {
        addRuntimeInterval(runtimeIntervals, timeframeInterval.getInterval(),
                remainingMinRunningTime, remainingMaxRunningTime, now);
        if((remainingMinRunningTime != null && remainingMaxRunningTime == null && remainingMinRunningTime < 0)) {
            setApplianceState(now,false, null, true,"Switching off due to minRunningTime < 0");
        }
        else if(remainingMaxRunningTime != null && remainingMaxRunningTime < 0) {
            setApplianceState(now,false, null, true,"Switching off due to maxRunningTime < 0");
        }
    }

    private void addRuntimeInterval(List<RuntimeInterval> runtimeIntervals, Interval interval, Integer remainingMinRunningTime,
                                    Integer remainingMaxRunningTime, LocalDateTime now) {
        RuntimeInterval runtimeInterval = createRuntimeInterval(interval, remainingMinRunningTime, remainingMaxRunningTime, now);
        if(runtimeInterval != null) {
            // we already might have a runtime request obtained from RunningTimeMonitor which might have been added
            // manually (setRuntime() aka click green traffic light). It would be not equal to any of the runtime requests
            // derived from the schedule, e.g. (from IntegrationTest.testClickGoLight):
            // RuntimeInterval created: 0s-1800s:1800s/nulls
            // RuntimeInterval created: 0s-25200s:1800s/nulls <--- has to be excluded
            // RuntimeInterval created: 82800s-111600s:3600s/nulls
            // RuntimeInterval created: 169200s-198000s:3600s/nulls
            boolean overlap = false;
            for(RuntimeInterval existingRuntimeInterval : runtimeIntervals) {
                if(runtimeInterval.getEarliestStart()>= existingRuntimeInterval.getEarliestStart()
                    && runtimeInterval.getEarliestStart() < existingRuntimeInterval.getLatestEnd()) {
                    overlap = true;
                }
            }
            if(! overlap) {
                runtimeIntervals.add(runtimeInterval);
            }
            else {
                logger.debug("{} Ignore overlapping RuntimeInterval: {}", id, runtimeInterval);
            }
        }
    }

    protected RuntimeInterval createRuntimeInterval(Interval interval, Integer minRunningTime,
                                                    Integer maxRunningTime, LocalDateTime now) {
        Integer earliestStart = 0;
        DateTime start = interval.getStart();
        DateTime end = interval.getEnd();
        if(start.isAfter(now.toDateTime())) {
            earliestStart = Double.valueOf(new Interval(now.toDateTime(), start).toDurationMillis() / 1000).intValue();
        }
        LocalDateTime nowBeforeEnd = new LocalDateTime(now);
        if(now.toDateTime().isAfter(end)) {
            nowBeforeEnd = now.minusHours(24);
        }
        Integer latestEnd = Double.valueOf(new Interval(nowBeforeEnd.toDateTime(), end).toDurationMillis() / 1000).intValue();
        if(maxRunningTime != null && maxRunningTime > latestEnd) {
            maxRunningTime = latestEnd;
        }
        return createRuntimeInterval(earliestStart, latestEnd, minRunningTime, maxRunningTime);
    }

    protected RuntimeInterval createRuntimeInterval(Integer earliestStart, Integer latestEnd, Integer minRunningTime,
                                                    Integer maxRunningTime) {
        if(minRunningTime != null && (minRunningTime > 0 || (maxRunningTime != null && maxRunningTime > 0))) {
            RuntimeInterval runtimeInterval = new RuntimeInterval();
            runtimeInterval.setEarliestStart(earliestStart);
            runtimeInterval.setLatestEnd(latestEnd);
            runtimeInterval.setMinRunningTime(minRunningTime);
            runtimeInterval.setMaxRunningTime(maxRunningTime);
            logger.debug("{}: RuntimeInterval created: {}", id, runtimeInterval);
            return runtimeInterval;
        }
        return null;
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Control state has changed to {}", id, (switchOn ? "on" : "off"));
        if (runningTimeMonitor != null) {
            runningTimeMonitor.setRunning(switchOn, now);
        }
        if (meter != null) {
            if (switchOn) {
                meter.startEnergyMeter();
            } else {
                meter.stopEnergyMeter();
            }
        }
    }

    @Override
    public void startingCurrentDetected(LocalDateTime now) {
        logger.debug("{}: Activating next sufficient timeframe interval after starting current has been detected", id);
        TimeframeInterval timeframeInterval;
        Schedule forcedSchedule = getForcedSchedule(now);
        if(forcedSchedule != null) {
            logger.debug("{}: Forcing schedule {}", id, forcedSchedule);
            timeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, Collections.singletonList(forcedSchedule), false, true);
        }
        else {
            timeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, schedules, false, true);
        }
        runningTimeMonitor.activateTimeframeInterval(now, timeframeInterval);
    }

    @Override
    public void finishedCurrentDetected() {
        if(runningTimeMonitor != null) {
            logger.debug("{}: Deactivating timeframe interval until starting current is detected again", id);
            runningTimeMonitor.activateTimeframeInterval(new LocalDateTime(), (TimeframeInterval) null);
        }
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval,
                                      TimeframeInterval activatedInterval) {
        if(activatedInterval == null) {
            setApplianceState(now, false, null,
                    true,"Switching off due to end of time frame");
            if(meter != null) {
                meter.resetEnergyMeter();
            }
            acceptControlRecommendations = true;
            logger.debug("{}: Set acceptControlRecommendations={}", id, acceptControlRecommendations);
        }
    }
}
