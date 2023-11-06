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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.pigpioj.PigpioInterface;
import uk.pigpioj.PigpioJ;

public class GpioAccessProvider {
    private static Logger logger = LoggerFactory.getLogger(GpioAccessProvider.class);

    private static PigpioInterface pigpioInterfaceInstance = null;

    public static PigpioInterface getPigpioInterface() {
        if(System.getProperty("os.arch").equals("arm") || System.getProperty("os.arch").equals("aarch64")) {
            try {
                if(pigpioInterfaceInstance == null) {
                    pigpioInterfaceInstance = PigpioJ.autoDetectedImplementation();
                }
                return pigpioInterfaceInstance;
            }
            catch(Error e) {
                logger.error("Error creating PigpioInterface.", e);
            }
        }
        else {
            logger.warn("GPIO access disabled - not running on Raspberry Pi.");
        }
        return null;
    }

}
