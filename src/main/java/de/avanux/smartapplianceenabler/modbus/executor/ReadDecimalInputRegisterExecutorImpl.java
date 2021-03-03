/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.modbus.executor;

import de.avanux.smartapplianceenabler.modbus.ByteOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadDecimalInputRegisterExecutorImpl extends ReadInputRegisterExecutor<Double>
        implements ReadDecimalInputRegisterExecutor {

    private Logger logger = LoggerFactory.getLogger(ReadDecimalInputRegisterExecutorImpl.class);
    private ByteOrder byteOrder;
    private Double factorToValue;

    public ReadDecimalInputRegisterExecutorImpl(String address, int requestWords, ByteOrder byteOrder, Double factorToValue) {
        super(address, requestWords);
        this.byteOrder = byteOrder;
        this.factorToValue = factorToValue;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Double getValue() {
        Integer[] byteValues = getByteValues();
        if(byteValues != null) {
            if (this.byteOrder == ByteOrder.LittleEndian) {
                if (byteValues.length == 2) {
                    return Float.valueOf(byteValues[1] << 16 | byteValues[0]).doubleValue() * getInitializedFactorToValue();
                }
            }
            else {
                if (byteValues.length == 2) {
                    return Float.valueOf(byteValues[0] << 16 | byteValues[1]).doubleValue() * getInitializedFactorToValue();
                }
            }
            logger.error("{}: Cannot handle response composed of {} bytes", getApplianceId(), byteValues.length);
        }
        return null;
    }

    private Double getInitializedFactorToValue() {
        if(this.factorToValue == null) {
            return 1.0;
        }
        return this.factorToValue;
    }
}
