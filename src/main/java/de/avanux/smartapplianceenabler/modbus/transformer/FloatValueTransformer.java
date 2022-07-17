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

import de.avanux.smartapplianceenabler.modbus.RegisterValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class FloatValueTransformer extends ValueTransformerBase implements ValueTransformer<Double> {
    private Logger logger = LoggerFactory.getLogger(FloatValueTransformer.class);
    private Double value = null;
    private RegisterValueType registerValueType;
    private Double factorToValue = 1.0;

    public FloatValueTransformer(Double factorToValue, RegisterValueType valueType) {
        if(factorToValue != null) {
            this.factorToValue = factorToValue;
        }
        this.registerValueType = valueType;
    }

    public void setByteValues(Integer[] byteValues) {
        if(byteValues != null) {
            if(byteValues.length == 2) {
                value = Float.valueOf(Float.intBitsToFloat(byteValues[0] << 16 | byteValues[1])).doubleValue() * factorToValue;
                logger.debug("{}: transformed value={}", applianceId, value);
            }
            else if(byteValues.length == 4) {
                // source: https://community.openhab.org/t/modbus-configuration-for-wallbox-access/119383/20
                var hex_komplett = toHexStringLen4(byteValues[0]) + toHexStringLen4(byteValues[1])
                        + toHexStringLen4(byteValues[2]) + toHexStringLen4(byteValues[3]);
                var hex_vorzexp = hex_komplett.substring(0, 3);
                var hex_sig = hex_komplett.substring(3);
                var dec_vorzexp = Integer.parseInt(hex_vorzexp, 16);
                var dec_sig = Long.valueOf(hex_sig, 16);

                var bin_vorzexp = Integer.toBinaryString(dec_vorzexp);
                var vorz = 0;
                double dec_exp = 0;
                if (bin_vorzexp.charAt(0) == '1' && bin_vorzexp.length() == 12) {
                    vorz = -1;
                    dec_exp = (dec_vorzexp - Math.pow(2, bin_vorzexp.length() - 1));
                }
                else {
                    vorz = 1;
                    dec_exp = dec_vorzexp;
                }
                var sig = (1 + (dec_sig / Math.pow(2,52)));
                double exp = dec_exp - 1023;
                value = vorz * sig * Math.pow(2, exp) * factorToValue;
                logger.debug("{}: transformed value={}", applianceId, value);
            }
            else {
                logger.error("{}: Cannot handle response composed of {} bytes", applianceId, byteValues.length);
            }
        }
    }

    private String toHexStringLen4(Integer intValue) {
        var hex = Integer.toHexString(intValue);
        return hex.equals("0") ? "0000" : hex;
    }

    @Override
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
        if(this.registerValueType == RegisterValueType.Float) {
            ByteBuffer buff = ByteBuffer.allocate(4);
            buff.putFloat(value.floatValue());
            var byteArray = buff.array();
            byteValues = new Integer[byteArray.length];
            int i = 0;
            for (byte byteValue : byteArray) {
                byteValues[i++] = Byte.toUnsignedInt(byteValue);
            }
        } else {
            logger.error("{}: Cannot handle RegisterValueType: {}", applianceId, this.registerValueType);
        }
    }

    @Override
    public boolean valueMatches(String regex) {
        Double value = getValue();
        return value != null && regex != null && value.toString().matches(regex);
    }
}
