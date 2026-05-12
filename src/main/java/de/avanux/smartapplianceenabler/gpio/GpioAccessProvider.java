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
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalInputProvider;
import com.pi4j.plugin.gpiod.provider.gpio.digital.GpioDDigitalOutputProvider;
import com.pi4j.plugin.linuxfs.provider.pwm.LinuxFsPwmProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GpioAccessProvider {
    private static Logger logger = LoggerFactory.getLogger(GpioAccessProvider.class);
    private static final String PWM_SYS_FS_PATH = "/sys/class/pwm/";

    private static Context pi4jContext = null;

    public static Context getPi4jContext() {
        if(System.getProperty("os.arch").equals("arm") || System.getProperty("os.arch").equals("aarch64")) {
            try {
                if(pi4jContext == null) {
                    var builder = Pi4J.newContextBuilder()
                            .add(GpioDDigitalInputProvider.newInstance())
                            .add(GpioDDigitalOutputProvider.newInstance());
                    int pwmChip = detectPwmChip();
                    if(pwmChip >= 0) {
                        logger.info("PWM chip detected: pwmchip{}", pwmChip);
                        builder.add(new LinuxFsPwmProviderImpl(PWM_SYS_FS_PATH, pwmChip));
                    } else {
                        logger.warn("No PWM chip found at {} - PWM not available.", PWM_SYS_FS_PATH);
                    }
                    pi4jContext = builder.build();
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

    private static int detectPwmChip() {
        Path pwmPath = Path.of(PWM_SYS_FS_PATH);
        if(!Files.exists(pwmPath)) return -1;
        try (var chips = Files.list(pwmPath)) {
            return chips
                    .filter(p -> p.getFileName().toString().startsWith("pwmchip"))
                    .mapToInt(p -> Integer.parseInt(p.getFileName().toString().substring("pwmchip".length())))
                    .min()
                    .orElse(-1);
        } catch(IOException e) {
            logger.warn("Error scanning {}", PWM_SYS_FS_PATH, e);
            return -1;
        }
    }
}
