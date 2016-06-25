package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import de.avanux.smartapplianceenabler.modbus.ModbusSwitch;
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
 * the wrapped control of the appliance is switched on again to do continue the
 * main work.
 */
@XmlRootElement
public class StartingCurrentSwitch implements Control, ApplianceIdConsumer, TimeFrameChangedListener {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(StartingCurrentSwitch.class));
    @XmlAttribute
    private Integer powerThreshold;
    @XmlAttribute
    private Integer detectionDuration; // seconds
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
    private List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();
    @XmlTransient
    private List<StartingCurrentSwitchListener> startingCurrentSwitchListeners = new ArrayList<>();
    @XmlTransient
    private String applianceId;
    @XmlTransient
    private RunningTimeMonitor runningTimeMonitor;


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.logger.setApplianceId(applianceId);
    }

    public void setRunningTimeMonitor(RunningTimeMonitor runningTimeMonitor) {
        this.runningTimeMonitor = runningTimeMonitor;
    }

    public List<Control> getControls() {
        return controls;
    }


    @Override
    public boolean on(boolean switchOn) {
        on = switchOn;
        if(switchOn) {
            applianceOn(switchOn);
        }
        // don't switch off appliance - otherwise it cannot be operated
        return on;
    }

    private boolean applianceOn(boolean switchOn) {
        logger.debug("Setting wrapped appliance switch to " + (switchOn ? "on" : "off"));
        boolean result = false;
        for(Control control : controls) {
            result = control.on(switchOn);
        }
        return result;
    }

    @Override
    public boolean isOn() {
        return on;
    }

    private boolean isApplianceOn() {
        if(controls != null && controls.size() > 0) {
            return controls.get(0).isOn();
        }
        return false;
    }

    public void start(Meter meter, Timer timer) {
        logger.info("Starting current switch: powerThreshold=" + powerThreshold + "W / detectionDuration=" + detectionDuration + "s");
        applianceOn(true);
        if(meter != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    boolean applianceOn = isApplianceOn();
                    logger.debug("on=" + on + " applianceOn=" + applianceOn);
                    if(applianceOn && !on) {
                        int averagePower = meter.getAveragePower();
                        if(lastAveragePower != null && averagePower > powerThreshold && lastAveragePower > powerThreshold) {
                            logger.debug("Starting current detected: averagePower=" + averagePower + " lastAveragePower=" + lastAveragePower);
                            logger.debug("Switching appliance off.");
                            applianceOn(false);
                            for(StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                                listener.startingCurrentDetected();
                            }
                        }
                        lastAveragePower = averagePower;
                    }
                }
            }, 0, detectionDuration * 1000);
        }
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    public void addStartingCurrentSwitchListener(StartingCurrentSwitchListener listener) {
        this.startingCurrentSwitchListeners.add(listener);
    }

    @Override
    public void timeFrameChanged(String applianceId, TimeFrame oldTimeFrame, TimeFrame newTimeFrame) {
        logger.debug("time frame changed: this.applianceId=" + this.applianceId);
        if(applianceId.equals(this.applianceId) && newTimeFrame == null) {
            for(StartingCurrentSwitchListener listener : startingCurrentSwitchListeners) {
                logger.debug("Notifying " + listener.getClass().getSimpleName());
                listener.timeFrameExpired();
            }
        }
    }
}
