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

public class ReadStringInputRegisterExecutorImpl extends ReadInputRegisterExecutor<String>
        implements ReadStringInputRegisterExecutor {

    private Logger logger = LoggerFactory.getLogger(ReadStringInputRegisterExecutorImpl.class);

    public ReadStringInputRegisterExecutorImpl(String address, int bytes) {
        super(address, bytes);
    }

    @Override
    public String getValue() {
        StringBuilder stringValue = new StringBuilder();
        for(Integer byteValue : getByteValues()) {
            stringValue.append(new Character((char) byteValue.intValue()));
        }
        return stringValue.toString();
    }
}
