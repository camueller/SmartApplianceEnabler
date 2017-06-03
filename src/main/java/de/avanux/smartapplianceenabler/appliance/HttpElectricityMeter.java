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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.util.Timer;

/**
 * Electricity meter reading power consumption from the response of a HTTP request.
 * <p>
 * IMPORTANT: The URLs in Appliance.xml have to be escaped (e.g. use "&amp;" instead of "&")
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpElectricityMeter extends HttpTransactionExecutor implements Meter, PollPowerExecutor, ApplianceIdConsumer {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(HttpElectricityMeter.class));
    @XmlAttribute
    private String url;
    @XmlAttribute
    private Float factorToWatt = 1.0f;
    @XmlAttribute
    private Integer measurementInterval = 60; // seconds
    @XmlAttribute
    private Integer pollInterval = 10; // seconds
    @XmlAttribute
    private String data;
    @XmlAttribute
    private String powerValueExtractionRegex = "";
    @XmlTransient
    private PollElectricityMeter pollElectricityMeter = new PollElectricityMeter();

    public void setUrl(String url) {
        this.url = url;
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
        this.pollElectricityMeter.setApplianceId(applianceId);
        this.logger.setApplianceId(applianceId);
    }

    @Override
    public int getAveragePower() {
        int power = pollElectricityMeter.getAveragePower();
        logger.debug("average power = " + power + "W");
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollElectricityMeter.getMinPower();
        logger.debug("min power = " + power + "W");
        return power;
    }

    @Override
    public int getMaxPower() {
        int power = pollElectricityMeter.getMaxPower();
        logger.debug("max power = " + power + "W");
        return power;
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    @Override
    public boolean isOn() {
        return getPower() > 0;
    }

    public void start(Timer timer) {
        pollElectricityMeter.start(timer, pollInterval, measurementInterval, this);
    }

    @Override
    public float getPower() {
        CloseableHttpResponse response = null;
        try {
            response = sendHttpRequest(url, data, getContentType(), getUsername(), getPassword());
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseString = EntityUtils.toString(response.getEntity());
                String valueString = extractPowerValueFromResponse(responseString, powerValueExtractionRegex);
                logger.debug("Power value extracted from HTTP response: " + valueString);
                return Float.parseFloat(valueString) * factorToWatt;
            }
        } catch (Exception e) {
            logger.error("Error reading HTTP response", e);
        } finally {
            try {
                if(response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("Error closing HTTP response", e);
            }
        }
        return 0;
    }

    protected String extractPowerValueFromResponse(String response, String regex)  {
        if(powerValueExtractionRegex == null) {
            return response;
        }
        return response.replaceAll(regex, "$1");
    }
}
