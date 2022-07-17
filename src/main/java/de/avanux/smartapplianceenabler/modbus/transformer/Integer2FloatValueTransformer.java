/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.modbus.transformer;

import de.avanux.smartapplianceenabler.modbus.ByteOrder;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Integer2FloatValueTransformer extends ValueTransformerBase implements ValueTransformer<Double> {
    private Logger logger = LoggerFactory.getLogger(Integer2FloatValueTransformer.class);
    private Double value = null;
    private Double factorToValue = 1.0;
    private ByteOrder byteOrder = ByteOrder.BigEndian;

    public Integer2FloatValueTransformer(ByteOrder byteOrder, Double factorToValue) {
        if(byteOrder != null) {
            this.byteOrder = byteOrder;
        }
        if(factorToValue != null) {
            this.factorToValue = factorToValue;
        }
    }

    public void setByteValues(Integer[] byteValues) {
        // FIXME implment using ByteBuffer: https://schneide.blog/2014/08/26/bit-fiddling-is-possible-in-java/
        if(byteValues != null) {
            Integer[] bytesValuesInOrder = getByteValuesInOrder(byteValues);
            if(byteValues.length == 2) {
                value = (float) (bytesValuesInOrder[0] << 16 | bytesValuesInOrder[1]) * factorToValue;
                logger.debug("{}: transformed value={}", applianceId, value);
            }
            else {
                logger.error("{}: Cannot handle response composed of {} bytes", applianceId, byteValues.length);
            }
        }
    }

    private Integer[] getByteValuesInOrder(Integer[] byteValues) {
        return byteOrder == ByteOrder.LittleEndian ?
                (byteValues.length == 2 ? new Integer[]{byteValues[1], byteValues[0]} : byteValues)
                : byteValues;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        logger.error("{}: {}.setValue() has not yet been implemented.", applianceId, getClass().getSimpleName());
    }

    @Override
    public boolean valueMatches(String regex) {
        Double value = getValue();
        return value != null && regex != null && value.toString().matches(regex);
    }}
