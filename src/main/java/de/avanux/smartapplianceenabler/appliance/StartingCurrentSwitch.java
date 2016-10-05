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
@XmlRootElement
public class StartingCurrentSwitch implements Control, ApplianceIdConsumer {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(StartingCurrentSwitch.class));
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer detectionDuration = 30; // seconds
    @XmlAttribute
    private Integer minRunningTime = 600; // seconds
    @XmlElements({
            @XmlElement(name = "Switch", type = Switch.class),
            @XmlElement(name = "ModbusSwitch", type = ModbusSwitch.class),
            @XmlElement(name = "HttpSwitch", type = HttpSwitch.class)
    })
    private List<Control> controls;
    @XmlTransient
    private Integer lastAveragePower;
    @XmlTransient
    private boolean on;
    @XmlTransient
    private LocalDateTime switchOnTime;
    @XmlTransient
    private List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    @XmlTransient
    private List<StartingCurrentSwitchListener> startingCurrentSwitchListeners = new ArrayList<>();


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

    protected void setPowerThreshold(Integer powerThreshold) {
        this.powerThreshold = powerThreshold;
    }

    protected void setDetectionDuration(Integer detectionDuration) {
        this.detectionDuration = detectionDuration;
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
        logger.info("Starting current switch: powerThreshold=" + powerThreshold + "W / detectionDuration=" + detectionDuration + "s / minRunningTime=" + minRunningTime + "s");
        applianceOn(true);
        if (timer != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    analyzePowerConsumption(meter);
                }
            }, 0, detectionDuration * 1000);
        }
    }

    /**
     * Analyzes current power consumption in order to detect starting current or finish current.
     *
     * @param meter the meter providing current power consumption
     */
    protected void analyzePowerConsumption(Meter meter) {
        if (meter != null) {
            boolean applianceOn = isApplianceOn();
            logger.debug("on=" + on + " applianceOn=" + applianceOn);
            if (applianceOn) {
                int averagePower = meter.getAveragePower();
                logger.debug("averagePower=" + averagePower + " lastAveragePower=" + lastAveragePower);
                if (lastAveragePower != null) {
                    if (on) {
                        // requiring minimum running time avoids finish current detection right after power on
                        if (isMinRunningTimeExceeded() && averagePower < powerThreshold && lastAveragePower < powerThreshold) {
                            logger.debug("Finished current detected.");
                            for (StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                                listener.finishedCurrentDetected();
                            }
                            on(false);
                        } else {
                            logger.debug("Finished current not detected.");
                        }
                    } else {
                        if (averagePower > powerThreshold && lastAveragePower > powerThreshold) {
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
                    logger.debug("lastAveragePower has not been set yet");
                }
                lastAveragePower = averagePower;
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
