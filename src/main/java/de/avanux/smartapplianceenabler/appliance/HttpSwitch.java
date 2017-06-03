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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Changes the on/off state of an appliance by sending an HTTP request.
 * Limitation: Currently the appliance state is not requested by from the appliance but
 * maintained internally.
 *
 * IMPORTANT: The URLs in Appliance.xml have to be escaped (e.g. use "&amp;" instead of "&")
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpSwitch extends HttpTransactionExecutor implements Control, ApplianceIdConsumer {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(HttpSwitch.class));
    @XmlAttribute
    private String onUrl;
    @XmlAttribute
    private String offUrl;
    @XmlAttribute
    private String onData;
    @XmlAttribute
    private String offData;
    @XmlTransient
    private boolean on;
    @XmlTransient
    List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();

    public void setOnUrl(String onUrl) {
        this.onUrl = onUrl;
    }

    public void setOffUrl(String offUrl) {
        this.offUrl = offUrl;
    }

    public void setOnData(String onData) {
        this.onData = onData;
    }

    public void setOffData(String offData) {
        this.offData = offData;
    }

    @Override
    public boolean on(boolean switchOn) {
        String url;
        String data;
        if(switchOn) {
            url = onUrl;
            data = onData;
        }
        else {
            url = offUrl;
            data = offData;
        }
        HttpResponse response = sendHttpRequest(url, data, getContentType(), getUsername(), getPassword());
        if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            on = switchOn;
            for(ControlStateChangedListener listener : controlStateChangedListeners) {
                listener.controlStateChanged(switchOn);
            }
            return true;
        }
        return false;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
        this.logger.setApplianceId(applianceId);
    }

    @Override
    public boolean isOn() {
        return on;
    }
}
