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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.appliance.ApplianceLifeCycle;
import de.avanux.smartapplianceenabler.http.*;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import de.avanux.smartapplianceenabler.protocol.JsonContentProtocolHandler;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import de.avanux.smartapplianceenabler.util.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Electricity meter reading current power and energy from the response of a HTTP request.
 * <p>
 * IMPORTANT: URLs have to be escaped (e.g. use "&amp;" instead of "&")
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpElectricityMeter implements Meter, ApplianceLifeCycle, Validateable, PollPowerExecutor, PollEnergyExecutor,
        ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(HttpElectricityMeter.class);
    @XmlAttribute
    private Integer measurementInterval; // seconds
    @XmlAttribute
    private Integer pollInterval; // seconds
    @XmlAttribute
    private String contentProtocol;
    @XmlElement(name = "HttpConfiguration")
    private HttpConfiguration httpConfiguration;
    @XmlElement(name = "HttpRead")
    private List<HttpRead> httpReads;
    private transient String applianceId;
    private transient HttpTransactionExecutor httpTransactionExecutor = new HttpTransactionExecutor();
    private transient PollPowerMeter pollPowerMeter = new PollPowerMeter();
    private transient PollEnergyMeter pollEnergyMeter = new PollEnergyMeter();
    private transient HttpHandler httpHandler = new HttpHandler();
    private transient ContentProtocolHandler contentContentProtocolHandler;


    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        this.pollPowerMeter.setApplianceId(applianceId);
        this.pollEnergyMeter.setApplianceId(applianceId);
        this.httpHandler.setApplianceId(applianceId);
        this.httpTransactionExecutor.setApplianceId(applianceId);
    }

    public void setHttpTransactionExecutor(HttpTransactionExecutor httpTransactionExecutor) {
        this.httpTransactionExecutor = httpTransactionExecutor;
    }

    public void setHttpConfiguration(HttpConfiguration httpConfiguration) {
        this.httpConfiguration = httpConfiguration;
    }

    public void setHttpReads(List<HttpRead> httpReads) {
        this.httpReads = httpReads;
    }

    public void setContentProtocol(ContentProtocolType contentProtocolType) {
        this.contentProtocol = contentProtocolType != null ? contentProtocolType.name() : null;
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval != null ? measurementInterval : HttpElectricityMeterDefaults.getMeasurementInterval();
    }

    public void setMeasurementInterval(Integer measurementInterval) {
        this.measurementInterval = measurementInterval;
    }

    public Integer getPollInterval() {
        return pollInterval != null ? pollInterval : HttpElectricityMeterDefaults.getPollInterval();
    }

    protected PollPowerMeter getPollPowerMeter() {
        return pollPowerMeter;
    }

    protected PollEnergyMeter getPollEnergyMeter() {
        return pollEnergyMeter;
    }

    @Override
    public void validate() {
        logger.debug("{}: Validating configuration", applianceId);
        logger.debug("{}: configured: poll interval={}s / measurement interval={}s",
                applianceId, getPollInterval(), getMeasurementInterval());
        HttpValidator validator = new HttpValidator(applianceId);

        // Meter should meter either Power or Energy or both
        boolean powerValid = validator.validateReads(
                Collections.singletonList(MeterValueName.Power.name()), this.httpReads, false);
        boolean energyValid = validator.validateReads(
                Collections.singletonList(MeterValueName.Energy.name()), this.httpReads, false);
        if(! (powerValid || energyValid)) {
            logger.error("{}: Missing configuration for either {} or {}",
                    applianceId, MeterValueName.Power.name(), MeterValueName.Energy.name());

            logger.error("{}: Terminating because of incorrect configuration", applianceId);
            System.exit(-1);
        }
    }

    @Override
    public int getAveragePower() {
        int power = pollPowerMeter.getAveragePower();
        logger.debug("{}: average power = {}W", applianceId, power);
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollPowerMeter.getMinPower();
        logger.debug("{}: min power = {}W", applianceId, power);
        return power;
    }

    @Override
    public int getMaxPower() {
        int power = pollPowerMeter.getMaxPower();
        logger.debug("{}: max power = {}W", applianceId, power);
        return power;
    }

    @Override
    public float getEnergy() {
        return this.pollEnergyMeter.getEnergy();
    }

    @Override
    public void startEnergyMeter() {
        logger.debug("{}: Start energy meter ...", applianceId);
        Float energy = this.pollEnergyMeter.startEnergyCounter();
        logger.debug("{}: Current energy meter value: {} kWh", applianceId, energy);
    }

    @Override
    public void stopEnergyMeter() {
        logger.debug("{}: Stop energy meter ...", applianceId);
        Float energy = this.pollEnergyMeter.stopEnergyCounter();
        logger.debug("{}: Current energy meter value: {} kWh", applianceId, energy);
    }

    @Override
    public void resetEnergyMeter() {
        logger.debug("{}: Reset energy meter ...", applianceId);
        this.pollEnergyMeter.reset();
        // TODO rename method to reset since we are resetting not just energy but also power
        this.pollPowerMeter.reset();
    }

    @Override
    public boolean isOn() {
        return getAveragePower() > 0;
    }

    @Override
    public void init() {
        this.pollEnergyMeter.setPollEnergyExecutor(this);
        if(this.httpConfiguration != null) {
            this.httpTransactionExecutor.setConfiguration(this.httpConfiguration);
        }
        this.httpHandler.setHttpTransactionExecutor(httpTransactionExecutor);
    }

    @Override
    public void start(LocalDateTime now, Timer timer) {
        logger.debug("{}: Starting ...", applianceId);
        pollEnergyMeter.start(timer, getPollInterval(), getMeasurementInterval(), this);
        pollPowerMeter.start(timer, getPollInterval(), getMeasurementInterval(), this);
    }

    @Override
    public void stop(LocalDateTime now) {
        logger.debug("{}: Stopping ...", applianceId);
        pollEnergyMeter.cancelTimer();
        pollPowerMeter.cancelTimer();
    }

    private boolean isCalculatePowerFromEnergy() {
        return HttpRead.getFirstHttpRead(MeterValueName.Power.name(), this.httpReads) == null;
    }

    /**
     * Calculates power values from energy differences and time differences.
     * @param timestampWithEnergyValue a collection of timestamp with energy value
     * @return the power values in W
     */
    protected Vector<Float> calculatePower(TreeMap<LocalDateTime, Float> timestampWithEnergyValue) {
        Vector<Float> powerValues = new Vector<>();
        LocalDateTime previousTimestamp = null;
        Float previousEnergy = null;
        for(LocalDateTime timestamp: timestampWithEnergyValue.keySet()) {
            Float energy = timestampWithEnergyValue.get(timestamp);
            if(previousTimestamp != null && previousEnergy != null) {
                long diffTime = Duration.between(previousTimestamp, timestamp).toSeconds();
                Float diffEnergy = energy - previousEnergy;
                // diffEnergy kWh * 1000W/kW * 3600s/1h / diffTime ms
                float power = diffEnergy * 1000.0f * 3600.0f / diffTime;
                powerValues.add(power > 0 ? power : 0.0f);
            }
            previousTimestamp = timestamp;
            previousEnergy = energy;
        }
        return powerValues;
    }

    @Override
    public void addPowerUpdateListener(PowerUpdateListener listener) {
        this.pollPowerMeter.addPowerUpateListener(listener);
    }

    @Override
    public Float pollPower() {
        ParentWithChild<HttpRead, HttpReadValue> powerRead = HttpRead.getFirstHttpRead(MeterValueName.Power.name(), this.httpReads);
        if(powerRead != null) {
            return getValue(powerRead);
        }
        Vector<Float> powerValues = calculatePower(this.pollEnergyMeter.getValuesInMeasurementInterval());
        if(powerValues.size() > 0) {
            Float power = powerValues.lastElement();
            logger.debug("{}: Calculated power from energy: {}W", applianceId, power);
            return power;
        }
        return null;
    }

    @Override
    public Float pollEnergy(LocalDateTime now) {
        return pollEnergy();
    }

    protected float pollEnergy() {
        ParentWithChild<HttpRead, HttpReadValue> energyRead = HttpRead.getFirstHttpRead(MeterValueName.Energy.name(), this.httpReads);
        return getValue(energyRead);
    }

    private float getValue(ParentWithChild<HttpRead, HttpReadValue> read) {
        return this.httpHandler.getFloatValue(read, getContentContentProtocolHandler());
    }

    public ContentProtocolHandler getContentContentProtocolHandler() {
        if(this.contentContentProtocolHandler == null) {
            if(ContentProtocolType.JSON.name().equals(this.contentProtocol)) {
                this.contentContentProtocolHandler = new JsonContentProtocolHandler();
            }
        }
        return this.contentContentProtocolHandler;
    }
}
