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
import de.avanux.smartapplianceenabler.control.ev.http.HttpMethod;
import de.avanux.smartapplianceenabler.http.HttpTransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Electricity meter reading power consumption from the response of a HTTP request.
 * <p>
 * IMPORTANT: The URLs in Appliance.xml have to be escaped (e.g. use "&amp;" instead of "&")
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpElectricityMeter extends HttpTransactionExecutor implements Meter, PollPowerExecutor, ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(HttpElectricityMeter.class);
    @XmlAttribute
    private String url;
    @XmlAttribute
    private Float factorToWatt;
    @XmlAttribute
    private Integer measurementInterval; // seconds
    @XmlAttribute
    private Integer pollInterval; // seconds
    @XmlAttribute
    private String data;
    @XmlAttribute
    private String powerValueExtractionRegex;
    private transient Pattern powerValueExtractionPattern;
    private transient PollPowerMeter pollPowerMeter = new PollPowerMeter();

    public void setUrl(String url) {
        this.url = url;
    }

    public Float getFactorToWatt() {
        return factorToWatt != null ? factorToWatt : HttpElectricityMeterDefaults.getFactorToWatt();
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval != null ? measurementInterval : HttpElectricityMeterDefaults.getMeasurementInterval();
    }

    public Integer getPollInterval() {
        return pollInterval != null ? pollInterval : HttpElectricityMeterDefaults.getPollInterval();
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setPowerValueExtractionRegex(String powerValueExtractionRegex) {
        this.powerValueExtractionRegex = powerValueExtractionRegex;
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.pollPowerMeter.setApplianceId(applianceId);
    }

    @Override
    public int getAveragePower() {
        int power = pollPowerMeter.getAveragePower();
        logger.debug("{}: average power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollPowerMeter.getMinPower();
        logger.debug("{}: min power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public int getMaxPower() {
        int power = pollPowerMeter.getMaxPower();
        logger.debug("{}: max power = {}W", getApplianceId(), power);
        return power;
    }

    @Override
    public float getEnergy() {
        return 0f;
    }

    @Override
    public void startEnergyMeter() {
        // TODO implement
    }

    @Override
    public void stopEnergyMeter() {
        // TODO implement
    }

    @Override
    public void resetEnergyMeter() {
        // TODO implement
    }

    @Override
    public boolean isOn() {
        return getPower() > 0;
    }

    @Override
    public void start(Timer timer) {
        logger.debug("{}: Starting ...", getApplianceId());
        pollPowerMeter.start(timer, getPollInterval(), getMeasurementInterval(), this);
    }

    @Override
    public void stop() {
        logger.debug("{}: Stopping ...", getApplianceId());
        pollPowerMeter.cancelTimer();
    }

    @Override
    public float getPower() {
        HttpMethod httpMethod = data != null ? HttpMethod.POST : HttpMethod.GET;
        String response = execute(httpMethod, url, data);
        if(response != null) {
            logger.debug("{}: Power value extraction regex: {}", getApplianceId(), powerValueExtractionRegex);
            String valueString = extractPowerValueFromResponse(response, powerValueExtractionRegex);
            logger.debug("{}: Power value extracted from HTTP response: {} factorToWatt={}",
                    getApplianceId(), valueString, getFactorToWatt());
            return Float.parseFloat(valueString.replace(',', '.')) * getFactorToWatt();
        }
        return 0;
    }

    /**
     * Extract the power value from the the response using a regular expression.
     * The regular expression has contain a capture group containing the power value.
     * @param response the HTTP response containing a power value
     * @param regex the regular expression to be used to extract the power value
     * @return the power value extracted or the full response if the regular expression is null or could not be matched
     */
    protected String extractPowerValueFromResponse(String response, String regex)  {
        if(regex == null) {
            return response;
        }
        if( this.powerValueExtractionPattern == null) {
            this.powerValueExtractionPattern = Pattern.compile(regex, Pattern.DOTALL);
        }
        Matcher regexMatcher = this.powerValueExtractionPattern.matcher(response);
        if (regexMatcher.find()) {
            return regexMatcher.group(1);
        }
        return response;
    }
}
