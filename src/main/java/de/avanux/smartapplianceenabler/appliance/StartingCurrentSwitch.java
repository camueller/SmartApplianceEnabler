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

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import de.avanux.smartapplianceenabler.modbus.ModbusSwitch;
import org.joda.time.LocalDateTime;
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
    private transient ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(StartingCurrentSwitch.class));
    @XmlAttribute
    private Integer powerThreshold = 15;
    @XmlAttribute
    private Integer startingCurrentDetectionDuration = 30; // seconds
    @XmlAttribute
    private Integer finishedCurrentDetectionDuration = 300; // seconds
    @XmlAttribute
    private Integer minRunningTime = 600; // seconds
    @XmlElements({
            @XmlElement(name = "Switch", type = Switch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class)
    })
    private List<Control> controls;
    @XmlElement(name = "ForceSchedule")
    private DayTimeframeCondition dayTimeframeCondition;
    private transient Integer lastAveragePowerOfPowerOnDetection;
    private transient Integer lastAveragePowerOfPowerOffDetection;
    private transient boolean on;
    private transient LocalDateTime switchOnTime;
    private transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    private transient List<StartingCurrentSwitchListener> startingCurrentSwitchListeners = new ArrayList<>();


    @Override
    public void setApplianceId(String applianceId) {
        this.logger.setApplianceId(applianceId);
    }

    protected void setControls(List<Control> controls) {
        this.controls = controls;
    }

    public List<Control> getControls() {
        return controls;
    }

    public DayTimeframeCondition getDayTimeframeCondition() {
        return dayTimeframeCondition;
    }

    public void setDayTimeframeCondition(DayTimeframeCondition dayTimeframeCondition) {
        this.dayTimeframeCondition = dayTimeframeCondition;
    }

    protected void setPowerThreshold(Integer powerThreshold) {
        this.powerThreshold = powerThreshold;
    }

    protected void setStartingCurrentDetectionDuration(Integer startingCurrentDetectionDuration) {
        this.startingCurrentDetectionDuration = startingCurrentDetectionDuration;
    }

    protected void setFinishedCurrentDetectionDuration(Integer finishedCurrentDetectionDuration) {
        this.finishedCurrentDetectionDuration = finishedCurrentDetectionDuration;
    }

    protected void setMinRunningTime(Integer minRunningTime) {
        this.minRunningTime = minRunningTime;
    }

    @Override
    public boolean on(boolean switchOn) {
        logger.debug("Setting appliance switch to " + (switchOn ? "on" : "off"));
        on = switchOn;
        if (switchOn) {
            applianceOn(true);
            switchOnTime = new LocalDateTime();
        }
        // don't switch off appliance - otherwise it cannot be operated
        return on;
    }

    private boolean applianceOn(boolean switchOn) {
        logger.debug("Setting wrapped appliance switch to " + (switchOn ? "on" : "off"));
        boolean result = false;
        for (Control control : controls) {
            result = control.on(switchOn);
        }
        return result;
    }

    @Override
    public boolean isOn() {
        return on;
    }

    protected boolean isApplianceOn() {
        if (controls != null && controls.size() > 0) {
            return controls.get(0).isOn();
        }
        return false;
    }

    public void start(final Meter meter, Timer timer) {
        logger.info("Starting current switch: powerThreshold=" + powerThreshold + "W"
                + " / startingCurrentDetectionDuration=" + startingCurrentDetectionDuration + "s"
                + " / finishedCurrentDetectionDuration=" + finishedCurrentDetectionDuration + "s"
                + " / minRunningTime=" + minRunningTime + "s");
        applianceOn(true);
        if (timer != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    detectStartingCurrent(meter);
                }
            }, 0, startingCurrentDetectionDuration * 1000);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    detectFinishedCurrent(meter);
                }
            }, 0, finishedCurrentDetectionDuration * 1000);
        }
    }

    /**
     * Detect starting current by monitoring current power consumption.
     *
     * @param meter the meter providing current power consumption
     */
    protected void detectStartingCurrent(Meter meter) {
        if (meter != null) {
            boolean applianceOn = isApplianceOn();
            logger.debug("on=" + on + " applianceOn=" + applianceOn);
            if (applianceOn) {
                int averagePower = meter.getAveragePower();
                logger.debug("averagePower=" + averagePower + " lastAveragePowerOfPowerOnDetection=" + lastAveragePowerOfPowerOnDetection);
                if (lastAveragePowerOfPowerOnDetection != null) {
                    if (! on) {
                        if (averagePower > powerThreshold && lastAveragePowerOfPowerOnDetection > powerThreshold) {
                            logger.debug("Starting current detected.");
                            applianceOn(false);
                            switchOnTime = null;
                            for (StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                                listener.startingCurrentDetected();
                            }
                        } else {
                            logger.debug("Starting current not detected.");
                        }
                    }
                } else {
                    logger.debug("lastAveragePowerOfPowerOnDetection has not been set yet");
                }
                lastAveragePowerOfPowerOnDetection = averagePower;
            } else {
                logger.debug("Appliance not switched on.");
            }
        } else {
            logger.debug("Meter not available");
        }
    }

    /**
     * Detect starting current by monitoring current power consumption.
     *
     * @param meter the meter providing current power consumption
     */
    protected void detectFinishedCurrent(Meter meter) {
        if (meter != null) {
            boolean applianceOn = isApplianceOn();
            logger.debug("on=" + on + " applianceOn=" + applianceOn);
            if (applianceOn) {
                int averagePower = meter.getAveragePower();
                logger.debug("averagePower=" + averagePower + " lastAveragePowerOfPowerOffDetection=" + lastAveragePowerOfPowerOffDetection);
                if (lastAveragePowerOfPowerOffDetection != null) {
                    if (on) {
                        // requiring minimum running time avoids finish current detection right after power on
                        if (isMinRunningTimeExceeded() && averagePower < powerThreshold && lastAveragePowerOfPowerOffDetection < powerThreshold) {
                            logger.debug("Finished current detected.");
                            for (StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                                listener.finishedCurrentDetected();
                            }
                            on(false);
                        } else {
                            logger.debug("Finished current not detected.");
                        }
                    }
                } else {
                    logger.debug("lastAveragePowerOfPowerOnDetection has not been set yet");
                }
                lastAveragePowerOfPowerOffDetection = averagePower;
            } else {
                logger.debug("Appliance not switched on.");
            }
        } else {
            logger.debug("Meter not available");
        }
    }

    /**
     * Returns true, if the minimum running time has been exceeded.
     *
     * @return
     */
    protected boolean isMinRunningTimeExceeded() {
        return switchOnTime.plusSeconds(minRunningTime).isBefore(new LocalDateTime());
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    public void addStartingCurrentSwitchListener(StartingCurrentSwitchListener listener) {
        this.startingCurrentSwitchListeners.add(listener);
    }
}
