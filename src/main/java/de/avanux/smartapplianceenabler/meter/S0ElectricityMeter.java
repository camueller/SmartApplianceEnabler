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
package de.avanux.smartapplianceenabler.meter;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import de.avanux.smartapplianceenabler.control.GpioControllable;
import java.time.LocalDateTime;

import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import de.avanux.smartapplianceenabler.notification.NotificationProvider;
import de.avanux.smartapplianceenabler.notification.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class S0ElectricityMeter extends GpioControllable implements Meter, NotificationProvider, DigitalStateChangeListener {

    private transient Logger logger = LoggerFactory.getLogger(S0ElectricityMeter.class);
    @XmlAttribute
    private Integer impulsesPerKwh;
    @XmlAttribute
    private Integer minPulseDuration; // milliseconds
    @XmlElement(name = "Notifications")
    private Notifications notifications;
    private transient Long pulseTimestamp;
    private transient DigitalInput input;
    private transient PulsePowerMeter pulsePowerMeter = new PulsePowerMeter();
    private transient PulseEnergyMeter pulseEnergyMeter = new PulseEnergyMeter();
    private transient List<PowerUpdateListener> powerMeterListeners = new ArrayList<>();
    private transient NotificationHandler notificationHandler;

    @Override
    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(this.notificationHandler != null) {
            this.notificationHandler.setRequestedNotifications(notifications);
        }
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    public Integer getImpulsesPerKwh() {
        return impulsesPerKwh;
    }

    public Integer getMinPulseDuration() {
        return minPulseDuration != null ? minPulseDuration : S0ElectricityMeterDefaults.getMinPulseDuration();
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.pulsePowerMeter.setApplianceId(applianceId);
        this.pulseEnergyMeter.setApplianceId(applianceId);
    }

    protected void setPulsePowerMeter(PulsePowerMeter pulsePowerMeter) {
        this.pulsePowerMeter = pulsePowerMeter;
    }

    protected void setPulseEnergyMeter(PulseEnergyMeter pulseEnergyMeter) {
        this.pulseEnergyMeter = pulseEnergyMeter;
    }

    @Override
    public int getAveragePower() {
        return pulsePowerMeter.getAveragePower();
    }

    @Override
    public int getMinPower() {
        return pulsePowerMeter.getMinPower();
    }

    @Override
    public int getMaxPower() {
        return pulsePowerMeter.getMaxPower();
    }

    @Override
    public float getEnergy() {
        return this.pulseEnergyMeter.getEnergy();
    }

    @Override
    public void startEnergyMeter() {
        this.pulseEnergyMeter.startEnergyCounter();
    }

    @Override
    public void stopEnergyMeter() {
        this.pulseEnergyMeter.stopEnergyCounter();
    }

    @Override
    public void resetEnergyMeter() {
        this.pulseEnergyMeter.resetEnergyCounter();
    }

    @Override
    public void addPowerUpdateListener(PowerUpdateListener listener) {
        this.powerMeterListeners.add(listener);
    }

    @Override
    public void init() {
        pulsePowerMeter.setImpulsesPerKwh(impulsesPerKwh);
        pulseEnergyMeter.setImpulsesPerKwh(impulsesPerKwh);
        logger.debug("{}: configured: GPIO={} impulsesPerKwh={} minPulseDuration={} pinPullResistance={}",
                getApplianceId(), getPin() != null ? getPin() : null, getImpulsesPerKwh(), getMinPulseDuration(),
                getPinPullResistance());
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting {}", getApplianceId(), getClass().getSimpleName());
        Context gpioContext = getGpioContext();
        if(gpioContext != null) {
            try {
                DigitalInputConfig config = DigitalInput.newConfigBuilder(getGpioContext())
                        .address(getPin())
                        .pull(getPinPullResistance())
                        .build();
                input = gpioContext.digitalInput().create(config);
                input.addListener(this);
            }
            catch(Exception e) {
                logger.error("{}: Error start metering using {}", getApplianceId(), getPin(), e);
                if(this.notificationHandler != null) {
                    this.notificationHandler.sendNotification(NotificationType.COMMUNICATION_ERROR);
                }
            }
        }
        else {
            logGpioAccessDisabled(logger);
        }
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping {}", getApplianceId(), getClass().getSimpleName());
        Context gpioContext = getGpioContext();
        if(gpioContext != null && input != null) {
            input.removeListener(this);
        }
        else {
            logGpioAccessDisabled(logger);
        }
    }

    @Override
    public void startAveragingInterval(LocalDateTime now, Timer timer, int nextPollCompletedSecondsFromNow) {
    }

    @Override
    public void onDigitalStateChange(DigitalStateChangeEvent event) {
        long timestamp = System.currentTimeMillis();
        if((getPinPullResistance() == PullResistance.PULL_DOWN && event.state() == DigitalState.HIGH)
                || (getPinPullResistance() == PullResistance.PULL_UP && event.state() == DigitalState.LOW)) {
            pulseTimestamp = timestamp;
        }
        else if (pulseTimestamp != null && (timestamp - pulseTimestamp) > getMinPulseDuration()) {
            logger.debug("{}: S0 impulse detected on GPIO {}", getApplianceId(), getPin());
            pulsePowerMeter.addTimestamp(pulseTimestamp);
            pulseEnergyMeter.increasePulseCounter();
            int averagePower = getAveragePower();
            logger.debug("{}: power: {}W", getApplianceId(), averagePower);
            powerMeterListeners.forEach(listener -> listener.onPowerUpdate(averagePower));
            pulseTimestamp = null;
        }
    }
}
