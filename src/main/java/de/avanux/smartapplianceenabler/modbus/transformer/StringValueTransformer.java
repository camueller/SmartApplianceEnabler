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

public class StringValueTransformer extends ValueTransformerBase implements ValueTransformer<String> {
    private Logger logger = LoggerFactory.getLogger(StringValueTransformer.class);
    private String value = null;

    public void setByteValues(Integer[] byteValues) {
        StringBuilder stringValue = new StringBuilder();
        for(Integer byteValue : byteValues) {
            if(byteValue > 255) {
                // 16-bit Modbus register contains two 8-bit ASCII chars
                var hex = Integer.toHexString(byteValue);
                var hex1 = hex.substring(0, 2);
                var hex2 = hex.substring(2);
                stringValue.append(Character.valueOf((char) Integer.parseInt(hex1, 16)));
                stringValue.append(Character.valueOf((char) Integer.parseInt(hex2, 16)));
            }
            else {
                stringValue.append(Character.valueOf((char) byteValue.intValue()));
            }
        }
        value = stringValue.toString();
        logger.debug("{}: transformed value={}", applianceId, value);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean valueMatches(String regex) {
        String value = getValue();
        return value != null && regex != null && value.matches(regex);
    }
}
