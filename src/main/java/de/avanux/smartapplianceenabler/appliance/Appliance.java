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
import de.avanux.smartapplianceenabler.control.ev.EVChargerControl;
import de.avanux.smartapplianceenabler.control.ev.EVChargerState;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicle;
import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import de.avanux.smartapplianceenabler.meter.HttpElectricityMeter;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.meter.ModbusElectricityMeter;
import de.avanux.smartapplianceenabler.meter.S0ElectricityMeter;
import de.avanux.smartapplianceenabler.modbus.EVModbusControl;
import de.avanux.smartapplianceenabler.modbus.ModbusSlave;
import de.avanux.smartapplianceenabler.modbus.ModbusTcp;
import de.avanux.smartapplianceenabler.schedule.*;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.util.DateTimeProvider;
import de.avanux.smartapplianceenabler.util.DateTimeProviderImpl;
import de.avanux.smartapplianceenabler.util.Validateable;
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
public class Appliance implements Validateable, ControlStateChangedListener,
        StartingCurrentSwitchListener, ActiveIntervalChangedListener, TimeframeIntervalStateChangedListener {

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
    })
    private Meter meter;
    @XmlElement(name = "Schedule")
    private List<Schedule> schedules;
    private transient DateTimeProvider dateTimeProvider = new DateTimeProviderImpl();
    private transient RunningTimeMonitor runningTimeMonitor;
    private transient TimeframeIntervalHandler timeframeIntervalHandler;
    private transient Stack<Boolean> acceptControlRecommendations;
    private transient static final int CONSIDERATION_INTERVAL_DAYS = 2;

    public Appliance() {
        this.initAcceptControlRecommendations();
    }

    public void setDateTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

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

    public void deleteMeter() {
        logger.debug("{}: Delete meter", id);
        if(meter != null) {
            meter.stop(new LocalDateTime());
        }
        setMeter(null);
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

    public void deleteControl() {
        logger.debug("{}: Delete control", id);
        if(control != null) {
            control.stop(new LocalDateTime());
        }
        setControl(null);
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public void initAcceptControlRecommendations() {
        this.acceptControlRecommendations = new Stack<>();
        setAcceptControlRecommendations(true);
    }

    public boolean isAcceptControlRecommendations() {
        if(this.control instanceof AlwaysOnSwitch) {
            return false;
        }
        // FIXME delegate to EVCharger
        return acceptControlRecommendations.peek();
    }

    public void setAcceptControlRecommendations(boolean acceptControlRecommendations) {
        this.acceptControlRecommendations.push(acceptControlRecommendations);
        logger.debug("{} Set acceptControlRecommendations={}", id, isAcceptControlRecommendations());
    }

    public void resetAcceptControlRecommendations() {
        if(this.acceptControlRecommendations.size() > 1) {
            this.acceptControlRecommendations.pop();
        }
        logger.debug("{} Reset acceptControlRecommendations to {}", id, isAcceptControlRecommendations());
    }

    public RunningTimeMonitor getRunningTimeMonitor() {
        return runningTimeMonitor;
    }

    public void setRunningTimeMonitor(RunningTimeMonitor runningTimeMonitor) {
        this.runningTimeMonitor = runningTimeMonitor;
        this.runningTimeMonitor.setApplianceId(id);
        this.runningTimeMonitor.init();
        this.runningTimeMonitor.addTimeFrameChangedListener(this);
    }

    public TimeframeIntervalHandler getTimeframeIntervalHandler() {
        return timeframeIntervalHandler;
    }

    public void setTimeframeIntervalHandler(TimeframeIntervalHandler timeframeIntervalHandler) {
        this.timeframeIntervalHandler = timeframeIntervalHandler;
        this.timeframeIntervalHandler.setApplianceId(id);
        this.timeframeIntervalHandler.addTimeFrameIntervalChangedListener(this);
        this.timeframeIntervalHandler.addTimeframeIntervalStateChangedListener(this);
    }

    public void init(GpioController gpioController, Map<String, ModbusTcp> modbusIdWithModbusTcp) {
        logger.debug("{}: Initializing appliance", id);
        if(control != null) {
            setRunningTimeMonitor(new RunningTimeMonitor());
            setTimeframeIntervalHandler(new TimeframeIntervalHandler(this.schedules, this.control));
            if(control instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) control).setApplianceId(id);
            }
            if(isEvCharger()) {
                ((ElectricVehicleCharger) control).setAppliance(this);
            }
            if(control instanceof StartingCurrentSwitch) {
                Control wrappedControl = ((StartingCurrentSwitch) control).getControl();
                ((ApplianceIdConsumer) wrappedControl).setApplianceId(id);
//                control.addControlStateChangedListener(this);
//                logger.debug("{}: Registered as {} with {}", id, ControlStateChangedListener.class.getSimpleName(),
//                        control.getClass().getSimpleName());
//                ((StartingCurrentSwitch) control).addStartingCurrentSwitchListener(this);
//                logger.debug("{}: Registered as {} with {}", id, StartingCurrentSwitchListener.class.getSimpleName(),
//                        control.getClass().getSimpleName());
            }
            else {
                control.addControlStateChangedListener(this);
                logger.debug("{}: Registered as {} with {}", id, ControlStateChangedListener.class.getSimpleName(),
                        control.getClass().getSimpleName());
            }
            if(control instanceof ApplianceLifeCycle) {
                ((ApplianceLifeCycle) control).init();
            }
        }
        Meter meter = getMeter();
        if(meter != null) {
            if(meter instanceof ApplianceIdConsumer) {
                ((ApplianceIdConsumer) meter).setApplianceId(id);
            }
            if(meter instanceof ApplianceLifeCycle) {
                ((ApplianceLifeCycle) meter).init();
            }
            if(control != null) {
                if(meter instanceof S0ElectricityMeter) {
                    ((S0ElectricityMeter) meter).setControl(control);
                }
                logger.debug("{}: {} uses {}", id, meter.getClass().getSimpleName(), control.getClass().getSimpleName());
            }
        }

        if(control instanceof  StartingCurrentSwitch) {
            ((StartingCurrentSwitch) control).setMeter(meter);
            logger.debug("{}: {} uses {}", id, control.getClass().getSimpleName(), meter.getClass().getSimpleName());
        }

        if(getGpioControllables().size() > 0) {
            if(gpioController != null) {
                for(GpioControllable gpioControllable : getGpioControllables()) {
                    logger.info("{}: Configuring {}", id, gpioControllable.getClass().getSimpleName());
                    gpioControllable.setGpioController(gpioController);
                }
            }
            else {
                logger.error("Error initializing pi4j. Most likely libwiringPi.so is missing. In order to install it use the following command: sudo apt-get install wiringpi");
            }
        }

        for(ModbusSlave modbusSlave : getModbusSlaves()) {
            logger.info("{}: Configuring {}", id, modbusSlave.getClass().getSimpleName());
            String modbusId = modbusSlave.getIdref();
            ModbusTcp modbusTcp = modbusIdWithModbusTcp.get(modbusId);
            modbusSlave.setModbusTcp(modbusTcp);
        }

        if(schedules != null && schedules.size() > 0) {
            logger.info("{}: Schedules configured: {}", id, schedules.size());
            if(! hasScheduleHandling()) {
                activateSchedules();
            }
        }
        else {
            logger.info("{}: No schedules configured", id);
        }
    }

    @Override
    public void validate() {
        logger.info("{}: Validating appliance configuration", id);
        if(control instanceof Validateable) {
            ((Validateable) control).validate();
        }
        if(meter instanceof Validateable) {
            ((Validateable) meter).validate();
        }
    }

    public void start(Timer timer) {
        logger.info("{}: Starting appliance", id);
        LocalDateTime now = new LocalDateTime();

        if(runningTimeMonitor != null) {
            runningTimeMonitor.setTimer(timer);
        }
        if(timeframeIntervalHandler != null) {
            timeframeIntervalHandler.setTimer(timer);
        }

        if(meter != null) {
            logger.info("{}: Starting {}", id, meter.getClass().getSimpleName());
            meter.start(now, timer);
        }

        if(control != null) {
            logger.info("{}: Starting {}", id, control.getClass().getSimpleName());
            control.start(new LocalDateTime(), timer);
            logger.info("{}: Switch off appliance initially", id);
            control.on(now, false);
        }
    }

    public void stop() {
        logger.info("{}: Stopping appliance ...", id);
        LocalDateTime now = new LocalDateTime();

        if(control != null) {
            logger.info("{}: Stopping {}", id, control.getClass().getSimpleName());
            control.stop(now);
        }

        if(meter != null) {
            logger.info("{}: Stopping meter {}", id, meter.getClass().getSimpleName());
            meter.stop(now);
        }

        if(runningTimeMonitor != null) {
            logger.info("{}: Cancel runningTimeMonitor timer", id);
            runningTimeMonitor.cancelTimer();
        }
        if(timeframeIntervalHandler != null) {
            timeframeIntervalHandler.cancelTimer();
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
            if(control instanceof GpioControllable) {
                controllables.add((GpioControllable) control);
            }
            else if(control instanceof StartingCurrentSwitch) {
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
                || isEvCharger()
            );
    }

    private boolean isEvCharger() {
        return this.control instanceof ElectricVehicleCharger;
    }

    public void setApplianceState(LocalDateTime now, boolean switchOn, Integer recommendedPowerConsumption,
                                  String logMessage) {
        if(control != null) {
            logger.debug("{}: {}", id, logMessage);
            if(switchOn && isEvCharger()) {
                int chargePower = 0;
//                if(this.runningTimeMonitor.getActiveTimeframeInterval() != null) {
//                    // if we receive a switch recommendation with an active timeframe interval
//                    // we are NOT using excess energy and should be charging with maximum power
//                    DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(this.id);
//                    chargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();
//                    logger.debug("{}: setting charge power to maximum: {}W", id, chargePower);
//                }
//                else
                if(recommendedPowerConsumption != null) {
                    chargePower = recommendedPowerConsumption;
                    logger.debug("{}: setting charge power to recommendation: {}W", id, chargePower);
                }
                else {
                    DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(this.id);
                    chargePower = deviceInfo.getCharacteristics().getMaxPowerConsumption();
                    logger.debug("{}: setting charge power to maximum: {}W", id, chargePower);
                }
                ((ElectricVehicleCharger) control).setChargePower(chargePower);
            }

            // only change state if requested state differs from actual state
            if(control.isOn() ^ switchOn) {
                control.on(now, switchOn);
            }
            else {
                logger.debug("{}: Requested appliance state already set.", id);
            }
        }
        else {
            logger.warn("{}: Appliance configuration does not contain control.", id);
        }
    }

    public void setEnergyDemand(LocalDateTime now, Integer evId, Integer socCurrent, Integer socRequested, LocalDateTime chargeEnd) {
        if (isEvCharger()) {
            Meter meter = getMeter();
            if(meter != null) {
                meter.resetEnergyMeter();
            }

            ElectricVehicleCharger evCharger = (ElectricVehicleCharger) this.control;
            evCharger.setEnergyDemand(now, evId, socCurrent, socRequested, chargeEnd);

            if(chargeEnd == null) {
                // if no charge end is provided we switch on immediatly with full power and don't accept
                // any control recommendations
                setApplianceState(now, true, null,"Switching on charger");
                setAcceptControlRecommendations(false);
            }
            else {
                setAcceptControlRecommendations(true);
            }
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
            else if(isEvCharger()) {
                EVChargerControl evChargerControl = ((ElectricVehicleCharger) control).getControl();
                if(evChargerControl instanceof EVModbusControl) {
                    slaves.add((EVModbusControl) evChargerControl);
                }
            }
        }
        return slaves;
    }

    public boolean canConsumeOptionalEnergy(LocalDateTime now) {
        if(isEvCharger()) {
            return ((ElectricVehicleCharger) this.control).isUseOptionalEnergy();
        }
        else if(schedules != null) {
            for (Schedule schedule : schedules) {
                if (schedule.getRequest() != null
                        && schedule.getRequest().getMin(now) != null
                        && schedule.getRequest().getMax(now) > schedule.getRequest().getMin(now)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true, if Schedules are handled.
     * @return
     */
    private boolean hasScheduleHandling() {
        // in case of starting current detection timeframes are added after
        // starting current was detected
        return hasStartingCurrentDetection();
    }

    private boolean hasStartingCurrentDetection() {
        return control != null && control instanceof StartingCurrentSwitch;
    }


    public void activateSchedules() {
        if(runningTimeMonitor != null) {
//            logger.debug("{}: Activating schedules", id);
//            runningTimeMonitor.setSchedules(schedules, this.dateTimeProvider.now());
        }
    }

    public void deactivateSchedules() {
        if(runningTimeMonitor != null) {
//            logger.debug("{}: Deactivating schedules", id);
//            runningTimeMonitor.setSchedules(new ArrayList<>(), this.dateTimeProvider.now());
        }
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
        List<RuntimeInterval> nonEvOptionalEnergyRuntimeIntervals = null;
        if(runningTimeMonitor != null) {
            nonEvOptionalEnergyRuntimeIntervals = getRuntimeIntervals(now,
                    runningTimeMonitor.getSchedules(),
                    runningTimeMonitor.getActiveTimeframeInterval(),
                    onlySufficient,
                    runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now),
                    runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now));
        }
        return getRuntimeIntervals(nonEvOptionalEnergyRuntimeIntervals);
    }

    public List<RuntimeInterval> getRuntimeIntervals(List<RuntimeInterval> nonEvOptionalEnergyRuntimeIntervals) {
        List<RuntimeInterval> runtimeIntervals = new ArrayList<>();
        if (isEvCharger()) {
            logger.debug("{}: control is ev charger", id);
            ElectricVehicleCharger electricVehicleCharger = (ElectricVehicleCharger) this.control;
            if (electricVehicleCharger.isInErrorState()) {
                logger.warn("{}: skipping runtime intervals because of charger error state", id);
            } else {
                logger.debug("{}: checking ev charger state", id);
                if (electricVehicleCharger.isVehicleConnected() || electricVehicleCharger.isCharging()) {
                    logger.debug("{}: connected={} charging={}", id,
                            electricVehicleCharger.isVehicleConnected(), electricVehicleCharger.isCharging());
                    if (runningTimeMonitor == null || runningTimeMonitor.getActiveTimeframeInterval() == null) {
                        logger.debug("{}: no active timeframe interval found", id);
                        RuntimeInterval evOptionalEnergy = getRuntimeIntervalForEVUsingOptionalEnergy(electricVehicleCharger);
                        if (evOptionalEnergy != null) {
                            logger.warn("{}: request for optional energy was created", id);
                            if (nonEvOptionalEnergyRuntimeIntervals != null && nonEvOptionalEnergyRuntimeIntervals.size() > 0) {
                                // evOptionalEnergy runtime interval must not overlap with other intervals
                                Integer firstNonEvOptionalEnergyRuntimeIntervalEarliestStart
                                        = nonEvOptionalEnergyRuntimeIntervals.get(0).getEarliestStart();
                                if(firstNonEvOptionalEnergyRuntimeIntervalEarliestStart > 0) {
                                    evOptionalEnergy.setLatestEnd(firstNonEvOptionalEnergyRuntimeIntervalEarliestStart - 1);
                                }
                            }
                            logger.debug("{}: requesting optional energy for electric vehicle: {}", id, evOptionalEnergy);
                            runtimeIntervals.add(evOptionalEnergy);
                        }
                        else {
                            logger.warn("{}: no request for optional energy was created", id);
                        }
                    }
                    else {
                        logger.debug("{}: active timeframe interval found - not requesting optional energy", id);
                    }
                    if (nonEvOptionalEnergyRuntimeIntervals != null && nonEvOptionalEnergyRuntimeIntervals.size() > 0) {
                        runtimeIntervals.addAll(nonEvOptionalEnergyRuntimeIntervals);
                    }
                }
                else {
                    logger.debug("{}: ignoring runtime intervals due to vehicle state {}", id, electricVehicleCharger.getState());
                }
            }
        }
        else {
            if (nonEvOptionalEnergyRuntimeIntervals != null && nonEvOptionalEnergyRuntimeIntervals.size() > 0) {
                runtimeIntervals.addAll(nonEvOptionalEnergyRuntimeIntervals);
            }
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
                Schedule schedule = activeTimeframeInterval.getTimeframe().getSchedule();
                Request request = schedule.getRequest();
                if(request instanceof RuntimeRequest) {
                    addRuntimeRequestInterval(now, activeTimeframeInterval.getInterval(), runtimeIntervals,
                            remainingMinRunningTime, remainingMaxRunningTime);
                }
                else if(request instanceof EnergyRequest) {
                    EnergyRequest remainingEnergy = calculateRemainingEnergy(now, schedule);
                    if(remainingEnergy != null) {
                        addEnergyRequestInterval(now, runtimeIntervals, activeTimeframeInterval.getInterval(),
                                remainingEnergy.getMin(now), remainingEnergy.getMax(now));
                    }
                }
                else if(request instanceof SocRequest) {
                    EnergyRequest remainingEnergy = calculateRemainingEnergy(now, (SocRequest) request, true);
                    if(remainingEnergy != null) {
                        addEnergyRequestInterval(now, runtimeIntervals, activeTimeframeInterval.getInterval(),
                                remainingEnergy.getMin(now), remainingEnergy.getMax(now));
                    }
                }
            }

            Interval considerationInterval = new Interval(now.toDateTime(), now.plusDays(CONSIDERATION_INTERVAL_DAYS).toDateTime());
            List<TimeframeInterval> timeFrameIntervals = Schedule.findTimeframeIntervals(now, considerationInterval,
                    schedules, false, onlySufficient);
            for(TimeframeInterval timeframeIntervalOfSchedule : timeFrameIntervals) {
                Schedule schedule = timeframeIntervalOfSchedule.getTimeframe().getSchedule();
                Interval interval = timeframeIntervalOfSchedule.getInterval();
                Request request = schedule.getRequest();
                if(request instanceof RuntimeRequest) {
                    Integer minRunningTime = request.getMin(now);
                    if(interval.contains(now.toDateTime()) && remainingMinRunningTime != null) {
                        minRunningTime = remainingMinRunningTime;
                    }
                    addRuntimeRequestInterval(now, interval, runtimeIntervals, minRunningTime, request.getMax(now));
                }
                else if(request instanceof EnergyRequest) {
                    addEnergyRequestInterval(now, runtimeIntervals, interval, request.getMin(now), request.getMax(now));
                }
                else if(request instanceof SocRequest) {
                    EnergyRequest remainingEnergy = calculateRemainingEnergy(now, (SocRequest) request, false);
                    if(remainingEnergy != null) {
                        addEnergyRequestInterval(now, runtimeIntervals, interval,
                                remainingEnergy.getMin(now), remainingEnergy.getMax(now));
                    }
                }
            }
        }
        else if(activeTimeframeInterval != null) {
            logger.debug("{}: Active timeframe interval found", id);
            Schedule schedule = activeTimeframeInterval.getTimeframe().getSchedule();
            Request request = schedule.getRequest();
            if(request instanceof RuntimeRequest) {
                addRuntimeRequestInterval(now, activeTimeframeInterval.getInterval(), runtimeIntervals,
                        remainingMinRunningTime, remainingMaxRunningTime);
            }
            else if(request instanceof EnergyRequest) {
                EnergyRequest remainingEnergy = calculateRemainingEnergy(now, schedule);
                addEnergyRequestInterval(now, runtimeIntervals, activeTimeframeInterval.getInterval(),
                        remainingEnergy.getMin(now), remainingEnergy.getMax(now));
            }
        }
        else {
            logger.debug("{}: No timeframes found", id);
        }
        return runtimeIntervals;
    }

    public EnergyRequest calculateRemainingEnergy(LocalDateTime now, Schedule schedule) {
        return buildEnergyRequestConsideringEnergyAlreadyCharged(now,
                schedule.getRequest().getMin(now), schedule.getRequest().getMax(now));
    }

    public EnergyRequest calculateRemainingEnergy(LocalDateTime now, SocRequest socRequest, boolean considerEnergyAlreadyCharged) {
        if(isEvCharger()) {
            ElectricVehicleCharger charger = (ElectricVehicleCharger) this.control;
            ElectricVehicle ev = charger.getVehicle(socRequest.getEvId());
            if(ev == null) {
                return buildEnergyRequest(0, 0);
            }
            Integer socStart = charger.getConnectedVehicleSoc() != null ? charger.getConnectedVehicleSoc() : 0;
            int energyToBeCharged = 0;
            if(socRequest.getSoc() > socStart) {
                energyToBeCharged = Float.valueOf((socRequest.getSoc() - socStart)/100.0f
                        * (100 + ev.getChargeLoss())/100.0f * ev.getBatteryCapacity()).intValue();
            }
            logger.debug("{}: Energy to be charged: {} Wh (batteryCapacity={}Wh chargeLoss={}% socStart={} socRequested={})",
                    id, energyToBeCharged, ev.getBatteryCapacity(), ev.getChargeLoss(), socStart, socRequest.getSoc());
            if(considerEnergyAlreadyCharged) {
                return buildEnergyRequestConsideringEnergyAlreadyCharged(now, energyToBeCharged, energyToBeCharged);
            }
            return buildEnergyRequest(energyToBeCharged, energyToBeCharged);
        }
        return null;
    }

    private EnergyRequest buildEnergyRequestConsideringEnergyAlreadyCharged(LocalDateTime now, int min, int max) {
        EnergyRequest remainingEnergy = buildEnergyRequest(min, max);
        if(remainingEnergy != null) {
            if(meter != null) {
                int whAlreadyCharged = Float.valueOf(meter.getEnergy() * 1000.0f).intValue();
                remainingEnergy.setMin(min - whAlreadyCharged);
                if(remainingEnergy.getMax(now) != null) {
                    remainingEnergy.setMax(max - whAlreadyCharged);
                }
            }
            logger.debug("{}: Remaining energy calculated: {}", id, remainingEnergy);
        }
        return remainingEnergy;
    }

    private EnergyRequest buildEnergyRequest(int min, int max) {
        if(max == 0) {
            logger.debug("{}: Skip creation of energy request with remaining max energy = 0", id);
            return null;
        }
        EnergyRequest request = new EnergyRequest();
        request.setMin(min);
        request.setMax(max);
        return request;
    }

    private RuntimeInterval getRuntimeIntervalForEVUsingOptionalEnergy(ElectricVehicleCharger evCharger) {
        float energy = 0.0f;
        if(meter != null) {
            energy = meter.getEnergy();
            logger.debug("{}: energy metered: {} kWh", id, energy);
        }
        else {
            logger.debug("{}: No energy meter configured - cannot calculate maxEnergy", id);
        }
        RuntimeInterval runtimeInterval = null;
        ElectricVehicle vehicle = evCharger.getConnectedVehicle();
        if(vehicle != null) {
            logger.warn("{}: creating optional energy request for vehicleId={}", id, vehicle.getId());
            int batteryCapacity = vehicle.getBatteryCapacity();
            Integer initialSoc = evCharger.getConnectedVehicleSoc();
            Integer targetSoc = vehicle.getDefaultSocOptionalEnergy();
            logger.debug("{}: calculating optional energy evId={} batteryCapactiy={} chargeLoss={}% initialSoc={} targetSoc={}",
                    id, vehicle.getId(), batteryCapacity, vehicle.getChargeLoss(), initialSoc, targetSoc);
            if(initialSoc == null) {
                initialSoc = 0;
            }
            if(targetSoc == null) {
                targetSoc = 100;
            }
            Integer maxEnergy = Float.valueOf((targetSoc - initialSoc)/100.0f
                    * (100 + vehicle.getChargeLoss())/100.0f * batteryCapacity).intValue()
                    - Float.valueOf(energy * 1000).intValue();
            logger.debug("{}: optional energy calculated={}Wh", id, maxEnergy);
            if (maxEnergy > 0) {
                runtimeInterval = new RuntimeInterval();
                runtimeInterval.setEarliestStart(0);
                runtimeInterval.setLatestEnd(CONSIDERATION_INTERVAL_DAYS * 24 * 3600);
                runtimeInterval.setMinEnergy(0);
                runtimeInterval.setMaxEnergy(maxEnergy);
            }
        }
        else {
            logger.warn("{}: no connected vehicle was found", id);
        }
        return runtimeInterval;
    }

    private void addRuntimeRequestInterval(LocalDateTime now, Interval interval, List<RuntimeInterval> runtimeIntervals,
                                           Integer remainingMinRunningTime, Integer remainingMaxRunningTime) {
        RuntimeInterval runtimeInterval = createRuntimeRequestInterval(interval, remainingMinRunningTime, remainingMaxRunningTime, now);
        if(runtimeInterval != null) {
            if(! isOverlappingRuntimeInterval(runtimeInterval, runtimeIntervals)) {
                runtimeIntervals.add(runtimeInterval);
            }
            else {
                logger.debug("{} Ignore overlapping RuntimeInterval: {}", id, runtimeInterval);
            }
        }
    }

    private void addEnergyRequestInterval(LocalDateTime now, List<RuntimeInterval> runtimeIntervals, Interval interval, Integer minEnergy,
                                          Integer maxEnergy) {
        RuntimeInterval runtimeInterval = createEnergyRequestInterval(interval, minEnergy, maxEnergy, now);
        if(runtimeInterval != null) {
            if(!isFutureEmptyEnergyRequestInterval(runtimeInterval)) {
                if(! isOverlappingRuntimeInterval(runtimeInterval, runtimeIntervals)) {
                    runtimeIntervals.add(runtimeInterval);
                }
                else {
                    logger.debug("{} Ignore overlapping request Interval: {}", id, runtimeInterval);
                }
            }
            else {
                logger.debug("{} Ignore future empty energy request Interval: {}", id, runtimeInterval);
            }
        }
    }

    protected boolean isFutureEmptyEnergyRequestInterval(RuntimeInterval runtimeInterval) {
        return runtimeInterval.getEarliestStart() > 0
                && runtimeInterval.getMinEnergy() == 0
                && runtimeInterval.getMaxEnergy() == 0;
    }

    protected boolean isOverlappingRuntimeInterval(RuntimeInterval runtimeInterval, List<RuntimeInterval> runtimeIntervals) {
        // we already might have a runtime request obtained from RunningTimeMonitor which might have been added
        // manually (setRuntime() aka click green traffic light). It would be not equal to any of the runtime requests
        // derived from the schedule, e.g. (from IntegrationTest.testClickGoLight):
        // RuntimeInterval created: 0s-1800s:1800s/nulls
        // RuntimeInterval created: 0s-25200s:1800s/nulls <--- has to be excluded
        // RuntimeInterval created: 82800s-111600s:3600s/nulls
        // RuntimeInterval created: 169200s-198000s:3600s/nulls
        for(RuntimeInterval existingRuntimeInterval : runtimeIntervals) {
            if(runtimeInterval.getEarliestStart()>= existingRuntimeInterval.getEarliestStart()
                    && runtimeInterval.getEarliestStart() < existingRuntimeInterval.getLatestEnd()) {
                return true;
            }
        }
        return false;
    }

    protected RuntimeInterval createRuntimeRequestInterval(Interval interval, Integer minRunningTime,
                                                           Integer maxRunningTime, LocalDateTime now) {
        Integer earliestStart = calculateEarliestStart(now, interval.getStart());
        Integer latestEnd = calculateLatestEnd(now, interval.getEnd());
        if(minRunningTime != null && minRunningTime > latestEnd) {
            minRunningTime = latestEnd;
        }
        if(maxRunningTime != null && maxRunningTime > latestEnd) {
            maxRunningTime = latestEnd;
        }
        return createRuntimeRequestInterval(earliestStart, latestEnd, minRunningTime, maxRunningTime);
    }

    protected RuntimeInterval createEnergyRequestInterval(Interval interval, Integer minEnergy,
                                                          Integer maxEnergy, LocalDateTime now) {
        Integer earliestStart = calculateEarliestStart(now, interval.getStart());
        Integer latestEnd = calculateLatestEnd(now, interval.getEnd());
        return createEnergyRequestInterval(earliestStart, latestEnd, minEnergy, maxEnergy);
    }

    protected Integer calculateEarliestStart(LocalDateTime now, DateTime start) {
        Integer earliestStart = 0;
        if(start.isAfter(now.toDateTime())) {
            earliestStart = Double.valueOf(new Interval(now.toDateTime(), start).toDurationMillis() / 1000).intValue();
        }
        return earliestStart;
    }

    protected Integer calculateLatestEnd(LocalDateTime now, DateTime end) {
        LocalDateTime nowBeforeEnd = new LocalDateTime(now);
        if(now.toDateTime().isAfter(end)) {
            nowBeforeEnd = now.minusHours(24);
        }
        return Double.valueOf(new Interval(nowBeforeEnd.toDateTime(), end).toDurationMillis() / 1000).intValue();
    }


    protected RuntimeInterval createRuntimeRequestInterval(Integer earliestStart, Integer latestEnd, Integer minRunningTime,
                                                           Integer maxRunningTime) {
        RuntimeInterval runtimeInterval = new RuntimeInterval();
        runtimeInterval.setEarliestStart(earliestStart);
        runtimeInterval.setLatestEnd(latestEnd);
        runtimeInterval.setMinRunningTime(minRunningTime);
        runtimeInterval.setMaxRunningTime(maxRunningTime);
        logger.debug("{}: RuntimeInterval created: {}", id, runtimeInterval);
        return runtimeInterval;
    }

    protected RuntimeInterval createEnergyRequestInterval(Integer earliestStart, Integer latestEnd, Integer minEnergy,
                                                          Integer maxEnergy) {
        if(maxEnergy != null && maxEnergy == 0) {
            logger.debug("{}: Skip creation of energy request with remaining max energy = 0", id);
            return null;
        }
        RuntimeInterval energyRequestInterval = new RuntimeInterval();
        energyRequestInterval.setEarliestStart(earliestStart);
        energyRequestInterval.setLatestEnd(latestEnd);
        energyRequestInterval.setMinEnergy(minEnergy);
        energyRequestInterval.setMaxEnergy(maxEnergy);
        logger.debug("{}: Energy request created: {}", id, energyRequestInterval);
        return energyRequestInterval;
    }

    public void resetActiveTimeframInterval() {
        if(runningTimeMonitor != null) {
            logger.debug("{}: Reset active timeframe interval", id);
            runningTimeMonitor.activateTimeframeInterval(new LocalDateTime(), (TimeframeInterval) null);
        }
    }

    @Override
    public void controlStateChanged(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Control state has changed to {}", id, (switchOn ? "on" : "off"));
        if (runningTimeMonitor != null) {
//            runningTimeMonitor.setRunning(switchOn, now);
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
    public void onEVChargerStateChanged(LocalDateTime now, EVChargerState previousState, EVChargerState newState,
                                        ElectricVehicle ev) {
        if(newState == EVChargerState.VEHICLE_CONNECTED) {
            initAcceptControlRecommendations();
        }
    }

    @Override
    public void onEVChargerSocChanged(LocalDateTime now, Float soc) {

    }

    @Override
    public void startingCurrentDetected(LocalDateTime now) {
        logger.debug("{}: Activating next sufficient timeframe interval for starting current controlled appliance", id);
        TimeframeInterval timeframeInterval;
        Schedule forcedSchedule = getForcedSchedule(now);
        if(forcedSchedule != null) {
            logger.debug("{}: Forcing schedule {}", id, forcedSchedule);
            timeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, Collections.singletonList(forcedSchedule), false, true);
        }
        else {
            timeframeInterval = Schedule.getCurrentOrNextTimeframeInterval(now, schedules, false, true);
        }
        if(timeframeInterval != null) {
            timeframeInterval.setTriggeredByStartingCurrent(true);
        }
        runningTimeMonitor.activateTimeframeInterval(now, timeframeInterval);
    }

    @Override
    public void finishedCurrentDetected() {
        logger.debug("{}: Deactivating timeframe interval until starting current is detected again", id);
        resetActiveTimeframInterval();
    }

    @Override
    public void activeIntervalChanged(LocalDateTime now, String applianceId, TimeframeInterval deactivatedInterval,
                                      TimeframeInterval activatedInterval, boolean wasRunning) {
        if(activatedInterval != null && deactivatedInterval != null) {
            endOfTimeFrame(now);
        }
        else if(activatedInterval != null) {
            if(isEvCharger()) {
                ElectricVehicleCharger charger = (ElectricVehicleCharger) this.control;
                if(charger.isVehicleConnected()) {
                    Request request = activatedInterval.getTimeframe().getSchedule().getRequest();
                    if(request instanceof EnergyRequest) {
                        charger.setChargeAmount(request.getMin(now));
                    }
                    else if(request instanceof SocRequest) {
                        EnergyRequest remainingEnergy = calculateRemainingEnergy(now, (SocRequest) request, false);
                        charger.setChargeAmount(remainingEnergy != null ? remainingEnergy.getMin(now) : 0);
                    }
                }
            }
        }
        else {
            endOfTimeFrame(now);
            if(! wasRunning) {
                if(runningTimeMonitor.getRunningTimeOfCurrentTimeFrame(now) == null) {
                    logger.debug("{}: Rescheduling timeframe interval for starting current controlled appliance with no running time", id);
                    startingCurrentDetected(now);
                }
            }
        }
    }

    @Override
    public void timeframeIntervalCreated(LocalDateTime now, TimeframeInterval timeframeInterval) {
        timeframeInterval.setApplianceId(id);
        timeframeInterval.getRequest().setApplianceId(id);
        timeframeInterval.getRequest().setMeter(meter);
        timeframeInterval.getRequest().setControl(control);
        control.addControlStateChangedListener(timeframeInterval.getRequest());

    }

    @Override
    public void onTimeframeIntervalStateChanged(LocalDateTime now, TimeframeIntervalState previousState, TimeframeIntervalState newState) {
    }

    /**
     * This method should not update RunningTimeMonitor (since it is already triggered by it).
     *
     * @param now
     */
    private void endOfTimeFrame(LocalDateTime now) {
        setApplianceState(now, false, null, "Switching off due to end of time frame");
        if(meter != null && ! isEvCharger()) {
            meter.resetEnergyMeter();
        }
        initAcceptControlRecommendations();
    }

    @Override
    public void activeIntervalChecked(LocalDateTime now, String applianceId, TimeframeInterval activeInterval) {
        Integer remainingMinRunningTime = runningTimeMonitor.getRemainingMinRunningTimeOfCurrentTimeFrame(now);
        Integer remainingMaxRunningTime = runningTimeMonitor.getRemainingMaxRunningTimeOfCurrentTimeFrame(now);
        logger.debug("{}: Checking running time: remainingMinRunningTime={} remainingMaxRunningTime={}", id, remainingMinRunningTime, remainingMaxRunningTime);
        if((remainingMinRunningTime !=null && remainingMinRunningTime <= 0
                && remainingMaxRunningTime != null && remainingMaxRunningTime <= 0)) {
            // Timeframe must not be deactivated in order to avoid recreation of the same timeframe!
            // Only for StartingCurrentSwitch timeframe should be deactivated
            setApplianceState(now,false, null, "Switching off because runningTime finished");
        }
    }
}
