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

import de.avanux.smartapplianceenabler.meter.Meter;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class StartingCurrentSwitchTest {

    private StartingCurrentSwitch startingCurrentSwitch;
    private StartingCurrentSwitchListener startingCurrentSwitchListener;
    private Meter meter;
    private Control control;

    public StartingCurrentSwitchTest() {
        startingCurrentSwitch = spy(new StartingCurrentSwitch());
        startingCurrentSwitch.setApplianceId(getClass().getSimpleName());
        meter = mock(Meter.class);
        control = mock(Control.class);
        startingCurrentSwitchListener = mock(StartingCurrentSwitchListener.class);
        startingCurrentSwitch.setControl(control);
        startingCurrentSwitch.addStartingCurrentSwitchListener(startingCurrentSwitchListener);
    }

    @Test
    public void test() throws Exception {
        LocalDateTime now = new LocalDateTime();
        startingCurrentSwitch.setMeter(meter);
        startingCurrentSwitch.start(now, null);

        // right after start
        // ... the appliance should be switched on
        when(control.isOn()).thenReturn(true);
        assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... but from the outside perspective the control is switched off
        assertFalse(startingCurrentSwitch.isOn());

        // averagePower=0 lastAveragePowerOfPowerOnDetection=null
        startingCurrentSwitch.detectStartingCurrent(now, meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);
        // averagePower=0 lastAveragePowerOfPowerOnDetection=0
        startingCurrentSwitch.detectStartingCurrent(now, meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);
        // averagePower=10 lastAveragePowerOfPowerOnDetection=0
        when(meter.getAveragePower()).thenReturn(10);
        startingCurrentSwitch.detectStartingCurrent(now, meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);
        // averagePower=10 lastAveragePowerOfPowerOnDetection=10
        startingCurrentSwitch.detectStartingCurrent(now, meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);
        // averagePower=30 lastAveragePowerOfPowerOnDetection=0
        when(meter.getAveragePower()).thenReturn(30);
        startingCurrentSwitch.detectStartingCurrent(now, meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);
        // averagePower=30 lastAveragePowerOfPowerOnDetection=30
        startingCurrentSwitch.detectStartingCurrent(now, meter);

        // power threshold exceeded for more than configured starting current detection duration
        // (2 consecutive calls to detectStartingCurrent())
        // ... causing appliance power off
        verify(control).on(now, false);
        when(control.isOn()).thenReturn(false);
        assertFalse(startingCurrentSwitch.isApplianceOn());
        // ... and also from the outside perspective the control is switched off
        assertFalse(startingCurrentSwitch.isOn());
        // ... listeners are notified of starting current detection
        verify(startingCurrentSwitchListener).startingCurrentDetected(now);

        // power on recommendation received by energy manager
        reset(control);
        startingCurrentSwitch.on(now, true);
        // ... causing appliance power on
        verify(control).on(now, true);
        when(control.isOn()).thenReturn(true);
        assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... and also from the outside perspective the control is switched on
        assertTrue(startingCurrentSwitch.isOn());

        // averagePower=30 lastAveragePower=30
        when(meter.getAveragePower()).thenReturn(30);
        // power threshold still exceeds power threshold
        startingCurrentSwitch.detectStartingCurrent(now, meter);

        // the minimum running time has been exceeded now
        when(startingCurrentSwitch.isMinRunningTimeExceeded()).thenReturn(true);

        // averagePower=10 lastAveragePower=30
        when(meter.getAveragePower()).thenReturn(10);
        startingCurrentSwitch.detectFinishedCurrent(now, meter);
        verify(startingCurrentSwitchListener, never()).finishedCurrentDetected();
        // averagePower=10 lastAveragePower=10
        startingCurrentSwitch.detectFinishedCurrent(now, meter);

        // power consumption fell below threshold for more than configured finished current detection duration
        // (2 consecutive calls to detectFinishedCurrent())
        // ... causing power off from the outside perspective
        assertFalse(startingCurrentSwitch.isOn());
        // ... but appliance remaining powered on
        verify(control).on(now, true);
        when(control.isOn()).thenReturn(true);
        assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... listeners are notified of finish current detection
        verify(startingCurrentSwitchListener).finishedCurrentDetected();
    }
}
