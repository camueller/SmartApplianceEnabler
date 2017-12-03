/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This switch only maintains its state and listeners.
 * It does not really switch anything.
 */
public class MockSwitch implements Control, ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(MockSwitch.class);
    private transient String applianceId;
    private transient boolean on;
    transient List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();

    @Override
    public boolean on(boolean switchOn) {
        logger.info("{}: Switching {}", applianceId, (switchOn ? "on" : "off"));
        on = switchOn;
        for(ControlStateChangedListener listener : controlStateChangedListeners) {
            listener.controlStateChanged(switchOn);
        }
        return true;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    @Override
    public boolean isOn() {
        return on;
    }
}
