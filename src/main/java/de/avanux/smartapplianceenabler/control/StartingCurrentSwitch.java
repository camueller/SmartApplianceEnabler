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
package de.avanux.smartapplianceenabler.control;

import de.avanux.smartapplianceenabler.appliance.ActiveIntervalChangedListener;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.DayTimeframeCondition;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Allows to prepare operation of an appliance while only little power is consumed.
 * Once the appliance starts the main work power consumption increases.
 * If it exceeds a threshold for a configured duration the wrapped control will be
 * switched off and energy demand will be posted. When switched on from external
 * the wrapped control of the appliance is switched on again to continue the
 * main work.
 * <p>
 * The StartingCurrentSwitch maintains a power state for the outside perspective which is
 * separate from the power state of the wrapped control.
 * The latter is only powered off after the starting current has been detected until the "on" command is received.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StartingCurrentSwitch implements Control, ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(StartingCurrentSwitch.class);
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer startingCurrentDetectionDuration; // seconds
    @XmlAttribute
    private Integer finishedCurrentDetectionDuration; // seconds
    @XmlAttribute
    private Integer minRunningTime; // seconds
    @XmlElements({
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class),
            @XmlElement(name = "MockSwitch", type = MockSwitch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "Switch", type = Switch.class)
    })
    private Control control;
    @XmlElement(name = "ForceSchedule")
    private DayTimeframeCondition dayTimeframeCondition;
    private transient String applianceId;
    private transient Integer lastAveragePowerOfPowerOnDetection;
    private transient Integer lastAveragePowerOfPowerOffDetection;
    private transient boolean on;
    private transient LocalDateTime switchOnTime;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient List<StartingCurrentSwitchListener> startingCurrentSwitchListeners = new ArrayList<>();


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public Control getControl() {
        return control;
    }

    public DayTimeframeCondition getDayTimeframeCondition() {
        return dayTimeframeCondition;
    }

    public void setDayTimeframeCondition(DayTimeframeCondition dayTimeframeCondition) {
        this.dayTimeframeCondition = dayTimeframeCondition;
    }

    public Integer getPowerThreshold() {
        return powerThreshold != null ? powerThreshold : StartingCurrentSwitchDefaults.getPowerThreshold();
    }

    protected void setPowerThreshold(Integer powerThreshold) {
        this.powerThreshold = powerThreshold;
    }

    public Integer getStartingCurrentDetectionDuration() {
        return startingCurrentDetectionDuration != null ? startingCurrentDetectionDuration
                : StartingCurrentSwitchDefaults.getStartingCurrentDetectionDuration();
    }

    protected void setStartingCurrentDetectionDuration(Integer startingCurrentDetectionDuration) {
        this.startingCurrentDetectionDuration = startingCurrentDetectionDuration;
    }

    public Integer getFinishedCurrentDetectionDuration() {
        return finishedCurrentDetectionDuration != null ? finishedCurrentDetectionDuration
                : StartingCurrentSwitchDefaults.getFinishedCurrentDetectionDuration();
    }

    protected void setFinishedCurrentDetectionDuration(Integer finishedCurrentDetectionDuration) {
        this.finishedCurrentDetectionDuration = finishedCurrentDetectionDuration;
    }

    public Integer getMinRunningTime() {
        return minRunningTime != null ? minRunningTime : StartingCurrentSwitchDefaults.getMinRunningTime();
    }

    protected void setMinRunningTime(Integer minRunningTime) {
        this.minRunningTime = minRunningTime;
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting appliance switch to {}", applianceId, (switchOn ? "on" : "off"));
        on = switchOn;
        if (switchOn) {
            // don't switch off appliance - otherwise it cannot be operated
            applianceOn(now,true);
            switchOnTime = new LocalDateTime();
        }
        for(ControlStateChangedListener listener : controlStateChangedListeners) {
            logger.debug("{}: Notifying {} {}", applianceId, ControlStateChangedListener.class.getSimpleName(),
                    listener.getClass().getSimpleName());
            listener.controlStateChanged(now, switchOn);
        }
        return on;
    }

    private boolean applianceOn(LocalDateTime now, boolean switchOn) {
        logger.debug("{}: Setting wrapped appliance switch to {}", applianceId, (switchOn ? "on" : "off"));
        return control.on(now, switchOn);
    }

    @Override
    public boolean isOn() {
        return on;
    }

    public boolean isApplianceOn() {
        if (control != null) {
            return control.isOn();
        }
        return false;
    }

    public void start(LocalDateTime now, final Meter meter, Timer timer) {
        logger.info("{}: Starting current switch: powerThreshold={}W startingCurrentDetectionDuration={}s " +
                        "finishedCurrentDetectionDuration={}s minRunningTime={}s",
                applianceId, getPowerThreshold(), getStartingCurrentDetectionDuration(),
                getFinishedCurrentDetectionDuration(), getMinRunningTime());
        applianceOn(now, true);
        if (timer != null) {
            String detectStartingCurrentTaskName = "DetectStartingCurrent";
            long detectStartingCurrentPeriod = getStartingCurrentDetectionDuration() * 1000;
            logger.debug("{}: Starting timer task name={} period={}ms", applianceId, detectStartingCurrentTaskName,
                    detectStartingCurrentPeriod);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        detectStartingCurrent(new LocalDateTime(), meter);
                    }
                    catch(Throwable e) {
                        logger.error(applianceId + ": Error executing timer task name=" + detectStartingCurrentTaskName, e);
                    }
                }
            }, 0, detectStartingCurrentPeriod);

            String detectFinishedCurrentTaskName = "DetectFinishedCurrent";
            long detectFinishedCurrentPeriod = getFinishedCurrentDetectionDuration() * 1000;
            logger.debug("{}: Starting timer task name={} period={}ms", applianceId, detectFinishedCurrentTaskName,
                    detectFinishedCurrentPeriod);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        detectFinishedCurrent(new LocalDateTime(), meter);
                    }
                    catch(Throwable e) {
                        logger.error(applianceId + ": Error executing timer task name=" + detectFinishedCurrentTaskName, e);
                    }
                }
            }, 0, detectFinishedCurrentPeriod);
        }
    }

    /**
     * Detect starting current by monitoring current power consumption.
     *
     * @param meter the meter providing current power consumption
     */
    public void detectStartingCurrent(LocalDateTime now, Meter meter) {
        if (meter != null) {
            boolean applianceOn = isApplianceOn();
            logger.debug("{}: on={} applianceOn={}", applianceId, on, applianceOn);
            if (applianceOn) {
                int averagePower = meter.getAveragePower();
                logger.debug("{}: averagePower={} lastAveragePowerOfPowerOnDetection={}", applianceId, averagePower,
                        lastAveragePowerOfPowerOnDetection);
                if (lastAveragePowerOfPowerOnDetection != null) {
                    if (! on) {
                        if (averagePower > getPowerThreshold() && lastAveragePowerOfPowerOnDetection > getPowerThreshold()) {
                            logger.debug("{}: Starting current detected.", applianceId);
                            applianceOn(now,false);
                            switchOnTime = null;
                            for (StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                                logger.debug("{}: Notifying {} {}", applianceId, StartingCurrentSwitchListener.class.getSimpleName(),
                                        listener.getClass().getSimpleName());
                                listener.startingCurrentDetected(now);
                            }
                        } else {
                            logger.debug("{}: Starting current not detected.", applianceId);
                        }
                    }
                } else {
                    logger.debug("{}: lastAveragePowerOfPowerOnDetection has not been set yet", applianceId);
                }
                lastAveragePowerOfPowerOnDetection = averagePower;
            } else {
                logger.debug("{}: Appliance not switched on.", applianceId);
            }
        } else {
            logger.debug("{}: Meter not available", applianceId);
        }
    }

    /**
     * Detect starting current by monitoring current power consumption.
     *
     * @param meter the meter providing current power consumption
     */
    protected void detectFinishedCurrent(LocalDateTime now, Meter meter) {
        if (meter != null) {
            boolean applianceOn = isApplianceOn();
            logger.debug("{}: on={} applianceOn={}", applianceId, on, applianceOn);
            if (applianceOn) {
                int averagePower = meter.getAveragePower();
                logger.debug("{}: averagePower={} lastAveragePowerOfPowerOffDetection={}", applianceId, averagePower, lastAveragePowerOfPowerOffDetection);
                if (lastAveragePowerOfPowerOffDetection != null) {
                    if (on) {
                        // requiring minimum running time avoids finish current detection right after power on
                        if (isMinRunningTimeExceeded() && averagePower < getPowerThreshold() && lastAveragePowerOfPowerOffDetection < getPowerThreshold()) {
                            logger.debug("{}: Finished current detected.", applianceId);
                            for (StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                                logger.debug("{}: Notifying {} {}", applianceId, StartingCurrentSwitchListener.class.getSimpleName(),
                                        listener.getClass().getSimpleName());
                                listener.finishedCurrentDetected();
                            }
                            on(now, false);
                        } else {
                            logger.debug("{}: Finished current not detected.", applianceId);
                        }
                    }
                } else {
                    logger.debug("{}: lastAveragePowerOfPowerOnDetection has not been set yet", applianceId);
                }
                lastAveragePowerOfPowerOffDetection = averagePower;
            } else {
                logger.debug("{}: Appliance not switched on.", applianceId);
            }
        } else {
            logger.debug("{}: Meter not available", applianceId);
        }
    }

    /**
     * Returns true, if the minimum running time has been exceeded.
     *
     * @return
     */
    protected boolean isMinRunningTimeExceeded() {
        return switchOnTime.plusSeconds(getMinRunningTime()).isBefore(new LocalDateTime());
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    public void addStartingCurrentSwitchListener(StartingCurrentSwitchListener listener) {
        this.startingCurrentSwitchListeners.add(listener);
    }
}
