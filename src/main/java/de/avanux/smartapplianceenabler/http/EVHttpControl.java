/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.http;

import de.avanux.smartapplianceenabler.control.ev.EVControl;
import de.avanux.smartapplianceenabler.control.ev.EVReadValueName;
import de.avanux.smartapplianceenabler.control.ev.EVWriteValueName;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolType;
import de.avanux.smartapplianceenabler.protocol.JsonContentProtocolHandler;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import de.avanux.smartapplianceenabler.util.RequestCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.text.MessageFormat;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class EVHttpControl implements EVControl {

    private transient Logger logger = LoggerFactory.getLogger(EVHttpControl.class);
    @XmlAttribute
    private String contentProtocol;
    @XmlElement(name = "HttpRead")
    private List<HttpRead> httpReads;
    @XmlElement(name = "HttpWrite")
    private List<HttpWrite> httpWrites;
    private transient String applianceId;
    private transient ContentProtocolHandler contentProtocolHandler;
    private transient RequestCache<ParentWithChild<HttpRead, HttpReadValue>, String> requestCache;
    private transient Integer pollInterval; // seconds


    public EVHttpControl() {
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        if(this.httpReads != null) {
            for(HttpRead read: this.httpReads) {
                read.setApplianceId(applianceId);
            }
        }
        if(this.httpWrites != null) {
            for(HttpWrite write: this.httpWrites) {
                write.setApplianceId(applianceId);
            }
        }
    }

    public void setContentProtocol(ContentProtocolType contentProtocol) {
        this.contentProtocol = contentProtocol.name();
    }

    public ContentProtocolHandler getContentProtocolHandler() {
        if(this.contentProtocolHandler == null) {
            if(ContentProtocolType.json.name().equals(this.contentProtocol)) {
                this.contentProtocolHandler = new JsonContentProtocolHandler();
            }
        }
        return this.contentProtocolHandler;
    }

    public List<HttpRead> getHttpReads() {
        return httpReads;
    }

    public void setHttpReads(List<HttpRead> httpReads) {
        this.httpReads = httpReads;
    }

    public List<HttpWrite> getHttpWrites() {
        return httpWrites;
    }

    public void setHttpWrites(List<HttpWrite> httpWrites) {
        this.httpWrites = httpWrites;
    }

    @Override
    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }

    @Override
    public void init(boolean checkRegisterConfiguration) {
        int cacheMaxAgeSeconds = this.pollInterval - 1;
        this.requestCache = new RequestCache<ParentWithChild<HttpRead, HttpReadValue>, String>(applianceId, cacheMaxAgeSeconds);
    }

    @Override
    public boolean isVehicleNotConnected() {
        // FIXME rename to ValueName
        return readValue(EVReadValueName.VehicleNotConnected);
    }

    @Override
    public boolean isVehicleConnected() {
        return readValue(EVReadValueName.VehicleConnected);
    }

    @Override
    public boolean isCharging() {
        return readValue(EVReadValueName.Charging);
    }

    @Override
    public boolean isChargingCompleted() {
        return readValue(EVReadValueName.ChargingCompleted);
    }

    @Override
    public boolean isInErrorState() {
        return readValue(EVReadValueName.Error);
    }

    protected boolean readValue(EVReadValueName valueName) {
        ParentWithChild<HttpRead, HttpReadValue> read = getReadValue(valueName);
        if(read != null) {
            String response = this.requestCache.get(read);
            if(response == null) {
                response = read.parent().executeGet(read.parent().getUrl());
                this.requestCache.put(read, response);
            }
            else {
                logger.debug("{}: Cached response: {}", applianceId, response);
            }
            getContentProtocolHandler().parse(response);
            String value = getContentProtocolHandler().readValue(read.child().getPath());
            String regex = read.child().getExtractionRegex();
            boolean match = value.matches(regex);
            logger.debug("value={} regex={} match={}", value, regex, match);
            return match;
        }
        return false;
    }

    public ParentWithChild<HttpRead, HttpReadValue> getReadValue(EVReadValueName name) {
        if(this.httpReads != null) {
            for(HttpRead read : this.httpReads) {
                for(HttpReadValue readValue : read.getReadValues()) {
                    if(readValue.getName().equals(name.name())) {
                        return new ParentWithChild(read, readValue);
                    }
                }
            }
        }
        return null;
    }

    public ParentWithChild<HttpWrite, HttpWriteValue> getWriteValue(EVWriteValueName name) {
        if(this.httpWrites != null) {
            for(HttpWrite write : this.httpWrites) {
                for(HttpWriteValue writeValue : write.getWriteValues()) {
                    if(writeValue.getName().equals(name.name())) {
                        return new ParentWithChild(write, writeValue);
                    }
                }
            }
        }
        return null;
    }

    protected void writeValue(ParentWithChild<HttpWrite, HttpWriteValue> write, String url) {
        if(write.child().getMethod() == HttpMethod.GET) {
            String response = write.parent().executeGet(url);
        }
    }

    @Override
    public void setChargeCurrent(int current) {
        ParentWithChild<HttpWrite, HttpWriteValue> write = getWriteValue(EVWriteValueName.ChargingCurrent);
        String urlWithPlaceholder = buildUrl(write);
        String url = MessageFormat.format(urlWithPlaceholder, current);
        writeValue(write, url);
    }

    @Override
    public void startCharging() {
        ParentWithChild<HttpWrite, HttpWriteValue> write = getWriteValue(EVWriteValueName.StartCharging);
        String url = buildUrl(write);
        writeValue(write, url);
    }

    @Override
    public void stopCharging() {
        ParentWithChild<HttpWrite, HttpWriteValue> write = getWriteValue(EVWriteValueName.StopCharging);
        String url = buildUrl(write);
        writeValue(write, url);
    }

    protected String buildUrl(ParentWithChild<HttpWrite, HttpWriteValue> write) {
        StringBuilder builder = new StringBuilder(write.parent().getUrl());
        if(write.child().getMethod() == HttpMethod.GET) {
            builder.append(write.child().getValue());
        }
        return builder.toString();
    }
}
