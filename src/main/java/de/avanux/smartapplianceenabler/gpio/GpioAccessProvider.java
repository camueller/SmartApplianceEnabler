/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.gpio;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpioAccessProvider {
    private static Logger logger = LoggerFactory.getLogger(GpioAccessProvider.class);

    private static Context pi4jContext = null;

    public static Context getPi4jContext() {
        if(System.getProperty("os.arch").equals("arm") || System.getProperty("os.arch").equals("aarch64")) {
            try {
                if(pi4jContext == null) {
                    pi4jContext = Pi4J.newAutoContext();
                }
                return pi4jContext;
            }
            catch(Exception e) {
                logger.error("Error creating Pi4J context.", e);
            }
        }
        else {
            logger.warn("GPIO access disabled - not running on Raspberry Pi.");
        }
        return null;
    }

}
