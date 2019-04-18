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
import de.avanux.smartapplianceenabler.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class EVHttpControl implements EVControl {

    private transient Logger logger = LoggerFactory.getLogger(EVHttpControl.class);
    private Protocol protocol;
    @XmlElement(name = "HttpRead")
    private List<HttpRead> reads;
    @XmlElement(name = "HttpWrite")
    private List<HttpWrite> writes;

    private class HttpWriteWithValue {
        public HttpWrite write;
        public HttpWriteValue writeValue;

        public HttpWriteWithValue(HttpWrite write, HttpWriteValue writeValue) {
            this.write = write;
            this.writeValue = writeValue;
        }
    }

    public EVHttpControl(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setReads(List<HttpRead> reads) {
        this.reads = reads;
    }

    public void setWrites(List<HttpWrite> writes) {
        this.writes = writes;
    }

    public void parse(String response) {
        this.protocol.parse(response);
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
        HttpReadValue readValue = getReadValue(valueName);
        if(readValue != null) {
            String value = this.protocol.readValue(readValue.getPath());
            boolean match = value.matches(readValue.getExtractionRegex());
            logger.debug("value={} match={}", value, match);
            return match;
        }
        return false;
    }

    public HttpReadValue getReadValue(EVModbusReadRegisterName name) {
        if(this.reads != null) {
            for(HttpRead read : this.reads) {
                for(HttpReadValue readValue : read.getReadValues()) {
                    if(readValue.getName().equals(name.name())) {
                        return readValue;
                    }
                }
            }
        }
        return null;
    }

    public String httpMethod;
    public String url;
    protected void writeValue(EVModbusWriteRegisterName valueName) {

    }

    public HttpWriteWithValue getWriteValue(String name) {
        if(this.writes != null) {
            for(HttpWrite write : this.writes) {
                for(HttpWriteValue writeValue : write.getWriteValues()) {
                    if(writeValue.getName().equals(name)) {
                        return new HttpWriteWithValue(write, writeValue);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void setChargeCurrent(int current) {

    }

    @Override
    public void startCharging() {

    }

    @Override
    public void stopCharging() {

    }

    @Override
    public void setApplianceId(String applianceId) {

    }
}
