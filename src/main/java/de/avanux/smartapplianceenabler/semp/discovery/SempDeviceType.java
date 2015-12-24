/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.semp.discovery;

import java.util.regex.Matcher;

import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.InvalidValueException;

public class SempDeviceType extends DeviceType {
    
    public SempDeviceType(String type) {
        super(Semp.NAMESPACE, type, 1);
    }

    public SempDeviceType(String type, int version) {
        super(Semp.NAMESPACE, type, version);
    }

    public static SempDeviceType valueOf(String s) throws InvalidValueException {
        Matcher matcher = PATTERN.matcher(s);
        
        try {
            if (matcher.matches())
                return new SempDeviceType(matcher.group(1), Integer.valueOf(matcher.group(2)));
        } catch(RuntimeException e) {
            throw new InvalidValueException(String.format(
                "Can't parse device SEMP type string (namespace/type/version) '%s': %s", s, e.toString()
            ));
        }
        throw new InvalidValueException("Can't parse SEMP device type string (namespace/type/version): " + s);
    }
}
