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
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.plugin.ffm.providers.gpio.FFMDigitalInputProviderImpl;
import com.pi4j.plugin.ffm.providers.gpio.FFMDigitalOutputProviderImpl;
import com.pi4j.plugin.ffm.providers.pwm.FFMPwmProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GpioAccessProvider {
    private static Logger logger = LoggerFactory.getLogger(GpioAccessProvider.class);

    private static Context pi4jContext = null;
    /**
     * Caches shared DigitalInput instances keyed by GPIO pin number.
     * This prevents Pi4J's internal ComponentRegistry from accumulating
     * references to DigitalInput objects across repeated start/stop cycles.
     */
    private static final Map<Integer, SharedDigitalInput> digitalInputCache = new ConcurrentHashMap<>();

    public static Context getPi4jContext() {
        if(System.getProperty("os.arch").equals("arm") || System.getProperty("os.arch").equals("aarch64")) {
            try {
                if(pi4jContext == null) {
                    pi4jContext = Pi4J.newContextBuilder()
                            .add(new FFMDigitalInputProviderImpl())
                            .add(new FFMDigitalOutputProviderImpl())
                            .add(new FFMPwmProviderImpl())
                            .build();
                }
                return pi4jContext;
            }
            catch(Throwable e) {
                logger.error("Error creating Pi4J context.", e);
            }
        }
        else {
            logger.warn("GPIO access disabled - not running on Raspberry Pi.");
        }
        return null;
    }

    /**
     * Provides a shared DigitalInput for the given GPIO pin.
     * If no shared instance exists yet, creates one and caches it.
     * Multiple callers can share the same physical input via reference counting.
     */
    public static SharedDigitalInput provideSharedDigitalInput(int pin) {
        if(pi4jContext == null) {
            return null;
        }
        return digitalInputCache.compute(pin, (p, existing) -> {
            if(existing != null && !existing.isClosed()) {
                // Reuse existing shared instance - increment ref count
                existing.incrementRef();
                logger.debug("Reusing cached DigitalInput for GPIO pin {}", p);
                return existing;
            } else {
                // Create new instance
                DigitalInput input = pi4jContext.create(
                        DigitalInput.newConfigBuilder(pi4jContext)
                                .id("gpio-input-" + p)
                                .address(p)
                                .pull(com.pi4j.io.gpio.digital.PullResistance.OFF)
                                .provider("ffm-digital-input")
                                .build()
                );
                SharedDigitalInput shared = new SharedDigitalInput(pin, input);
                logger.debug("Created new cached DigitalInput for GPIO pin {}", p);
                return shared;
            }
        });
    }

    /**
     * Closes the cached DigitalInput for the given GPIO pin when all users have released it.
     */
    public static void closeAllInputs() {
        logger.debug("Closing all cached DigitalInputs ({})...", digitalInputCache.size());
        // Close a copy of values to avoid ConcurrentModificationException
        for (SharedDigitalInput shared : new java.util.ArrayList<>(digitalInputCache.values())) {
            shared.close(); // decrements ref, closes when ref reaches 0
        }
        digitalInputCache.clear();
        logger.debug("All cached DigitalInputs closed.");
    }

    /**
     * Reference-counted wrapper for a shared DigitalInput.
     */
    public static class SharedDigitalInput implements Closeable {
        private final int pin;
        private final DigitalInput delegate;
        private volatile boolean closed = false;
        private int refCount = 0;

        SharedDigitalInput(int pin, DigitalInput delegate) {
            this.pin = pin;
            this.delegate = delegate;
        }

        void incrementRef() {
            refCount++;
        }

        synchronized int decrementRef() {
            if (refCount > 0) {
                refCount--;
                if (refCount == 0) {
                    closed = true;
                    delegate.close();
                    logger.debug("Released last reference to DigitalInput on GPIO pin {}", pin);
                }
            }
            return refCount;
        }

        public int getPin() {
            return pin;
        }

        public DigitalInput getDelegate() {
            if (closed) {
                throw new IllegalStateException("DigitalInput on pin " + pin + " has been closed");
            }
            return delegate;
        }

        @Override
        public void close() {
            int remaining = decrementRef();
            if (remaining == 0) {
                digitalInputCache.remove(pin, this);
            }
        }

        synchronized boolean isClosed() {
            return closed;
        }
    }
}
