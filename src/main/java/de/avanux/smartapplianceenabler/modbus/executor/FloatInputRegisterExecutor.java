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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloatInputRegisterExecutor extends InputRegisterExecutor<Float> {
    private Logger logger = LoggerFactory.getLogger(FloatInputRegisterExecutor.class);

    public FloatInputRegisterExecutor(String address, int bytes) {
        super(address, bytes);
    }

    @Override
    public Float getValue() {
        if(getBytes() == 2) {
            Integer[] byteValues = getByteValues();
            return Float.intBitsToFloat(byteValues[0] << 16 | byteValues[1]);
        }
        logger.error("{}: Float has to be composed of 2 bytes!", getApplianceId());
        return null;
    }
}
