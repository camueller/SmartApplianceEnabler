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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.http.*;
import de.avanux.smartapplianceenabler.meter.MeterValueName;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
    private transient Logger logger = LoggerFactory.getLogger(HttpSwitch.class);
    @XmlElement(name = "HttpWrite")
    private List<HttpWrite> httpWrites;
    private transient boolean on;
    transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();

    @Override
    public void setApplianceId(String applianceId) {
        super.setApplianceId(applianceId);
    }

    public void validate() {
        // FIXME implementieren
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public boolean on(LocalDateTime now, boolean switchOn) {
        ParentWithChild<HttpWrite, HttpWriteValue> write = HttpWrite.getFirstHttpWrite(getValueName(switchOn).name(), this.httpWrites);
        if(write != null) {
            HttpMethod httpMethod = write.child().getMethod();
            String data = httpMethod == HttpMethod.POST ? write.child().getValue() : null;
            CloseableHttpResponse response = executeLeaveOpen(write.child().getMethod(), write.parent().getUrl(), data);
            if(response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                closeResponse(response);
                if(statusCode == HttpStatus.SC_OK) {
                    on = switchOn;
                    for(ControlStateChangedListener listener : controlStateChangedListeners) {
                        listener.controlStateChanged(now, switchOn);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private ControlValueName getValueName(boolean switchOn) {
        return switchOn ? ControlValueName.On : ControlValueName.Off;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }
}
