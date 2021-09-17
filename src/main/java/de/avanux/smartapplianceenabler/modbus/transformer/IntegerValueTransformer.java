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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerValueTransformer extends ValueTransformerBase implements ValueTransformer<Integer> {
    private Logger logger = LoggerFactory.getLogger(FloatValueTransformer.class);
    private Integer value = null;

    public void setByteValues(Integer[] byteValues) {
        if(byteValues != null) {
            if (byteValues.length == 1) {
                value = byteValues[0];
            }
            else if (byteValues.length == 2) {
                value = byteValues[0] << 16 | byteValues[1];
                logger.debug("{}: transformed value={}", applianceId, value);
            }
            else {
                logger.error("{}: Cannot handle response composed of {} bytes", applianceId, byteValues.length);
            }
        }
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public boolean valueMatches(String regex) {
        Integer value = getValue();
        return value != null && regex != null && value.toString().matches(regex);
    }

}
