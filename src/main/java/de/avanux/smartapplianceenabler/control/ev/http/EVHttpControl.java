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

package de.avanux.smartapplianceenabler.control.ev.http;

import de.avanux.smartapplianceenabler.control.ev.EVControl;
import de.avanux.smartapplianceenabler.control.ev.EVModbusReadRegisterName;
import de.avanux.smartapplianceenabler.control.ev.EVModbusWriteRegisterName;
import de.avanux.smartapplianceenabler.protocol.JsonProtocol;
import de.avanux.smartapplianceenabler.protocol.Protocol;
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
    private String contentType;
    @XmlElement(name = "HttpRead")
    private List<HttpRead> reads;
    @XmlElement(name = "HttpWrite")
    private List<HttpWrite> writes;
    private transient Protocol protocol;

    private class HttpReadWithValue {
        public HttpRead read;
        public HttpReadValue readValue;

        public HttpReadWithValue(HttpRead read, HttpReadValue readValue) {
            this.read = read;
            this.readValue = readValue;
        }
    }

    private class HttpWriteWithValue {
        public HttpWrite write;
        public HttpWriteValue writeValue;

        public HttpWriteWithValue(HttpWrite write, HttpWriteValue writeValue) {
            this.write = write;
            this.writeValue = writeValue;
        }
    }

    public EVHttpControl() {
    }

    public EVHttpControl(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        if(this.protocol == null) {
            if(HttpContentType.json.name().equals(this.contentType)) {
                this.protocol = new JsonProtocol();
            }
        }
        return this.protocol;
    }

    public List<HttpRead> getReads() {
        return reads;
    }

    public void setReads(List<HttpRead> reads) {
        this.reads = reads;
    }

    public List<HttpWrite> getWrites() {
        return writes;
    }

    public void setWrites(List<HttpWrite> writes) {
        this.writes = writes;
    }

    @Override
    public void setPollInterval(Integer pollInterval) {
    }

    @Override
    public void init(boolean checkRegisterConfiguration) {

    }

    @Override
    public boolean isVehicleNotConnected() {
        // FIXME rename to ValueName
        return readValue(EVModbusReadRegisterName.VehicleNotConnected);
    }

    @Override
    public boolean isVehicleConnected() {
        return readValue(EVModbusReadRegisterName.VehicleConnected);
    }

    @Override
    public boolean isCharging() {
        return readValue(EVModbusReadRegisterName.Charging);
    }

    @Override
    public boolean isChargingCompleted() {
        return readValue(EVModbusReadRegisterName.ChargingCompleted);
    }

    @Override
    public boolean isInErrorState() {
        return readValue(EVModbusReadRegisterName.Error);
    }

    protected boolean readValue(EVModbusReadRegisterName valueName) {
        HttpReadWithValue readWithValue = getReadValue(valueName);
        if(readWithValue != null) {
            String response = readWithValue.read.executeGet(readWithValue.read.getUrl());
            getProtocol().parse(response);
            String value = getProtocol().readValue(readWithValue.readValue.getPath());
            boolean match = value.matches(readWithValue.readValue.getExtractionRegex());
            logger.debug("value={} match={}", value, match);
            return match;
        }
        return false;
    }

    public HttpReadWithValue getReadValue(EVModbusReadRegisterName name) {
        if(this.reads != null) {
            for(HttpRead read : this.reads) {
                for(HttpReadValue readValue : read.getReadValues()) {
                    if(readValue.getName().equals(name.name())) {
                        return new HttpReadWithValue(read, readValue);
                    }
                }
            }
        }
        return null;
    }

    public HttpWriteWithValue getWriteValue(EVModbusWriteRegisterName name) {
        if(this.writes != null) {
            for(HttpWrite write : this.writes) {
                for(HttpWriteValue writeValue : write.getWriteValues()) {
                    if(writeValue.getName().equals(name.name())) {
                        return new HttpWriteWithValue(write, writeValue);
                    }
                }
            }
        }
        return null;
    }

    protected void writeValue(HttpWriteWithValue writeWithValueValue, String url) {
        if(writeWithValueValue.writeValue.getMethod() == HttpMethod.GET) {
            String response = writeWithValueValue.write.executeGet(url);
        }
    }

    @Override
    public void setChargeCurrent(int current) {
        HttpWriteWithValue writeWithValueValue = getWriteValue(EVModbusWriteRegisterName.ChargingCurrent);
        String urlWithPlaceholder = buildUrl(writeWithValueValue);
        String url = MessageFormat.format(urlWithPlaceholder, current);
        writeValue(writeWithValueValue, url);
    }

    @Override
    public void startCharging() {
        HttpWriteWithValue writeWithValueValue = getWriteValue(EVModbusWriteRegisterName.StartCharging);
        String url = buildUrl(writeWithValueValue);
        writeValue(writeWithValueValue, url);
    }

    @Override
    public void stopCharging() {
        HttpWriteWithValue writeWithValueValue = getWriteValue(EVModbusWriteRegisterName.StopCharging);
        String url = buildUrl(writeWithValueValue);
        writeValue(writeWithValueValue, url);
    }

    @Override
    public void setApplianceId(String applianceId) {
        if(this.reads != null) {
            for(HttpRead read: this.reads) {
                read.setApplianceId(applianceId);
            }
        }
        if(this.writes != null) {
            for(HttpWrite write: this.writes) {
                write.setApplianceId(applianceId);
            }
        }
    }

    protected String buildUrl(HttpWriteWithValue writeWithValueValue) {
        StringBuilder builder = new StringBuilder(writeWithValueValue.write.getUrl());
        HttpWriteValue writeValue = writeWithValueValue.writeValue;
        if(writeValue.getType().equals(HttpWriteValueType.QueryParameter)) {
            builder.append(writeValue.getValue());
        }
        return builder.toString();
    }
}
