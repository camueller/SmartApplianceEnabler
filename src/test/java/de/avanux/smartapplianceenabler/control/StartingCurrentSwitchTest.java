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
import java.time.LocalDateTime;
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
    public void detectStartingCurrent() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        startingCurrentSwitch.setMeter(meter);
        startingCurrentSwitch.start(now, null);

        // test fixture
        // ... the appliance should be switched on
        when(control.isOn()).thenReturn(true);
        assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... but from the outside perspective the control is switched off
        assertFalse(startingCurrentSwitch.isOn());
        int startingCurrentDetectionDuration = startingCurrentSwitch.getStartingCurrentDetectionDuration();

        startingCurrentSwitch.addPowerUpdate(now, 0, startingCurrentDetectionDuration);
        startingCurrentSwitch.detectStartingCurrent(now);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);

        now = now.plusSeconds(10);
        startingCurrentSwitch.addPowerUpdate(now, 10, startingCurrentDetectionDuration);
        startingCurrentSwitch.detectStartingCurrent(now);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);

        now = now.plusSeconds(30);
        startingCurrentSwitch.addPowerUpdate(now, 20, startingCurrentDetectionDuration);
        startingCurrentSwitch.detectStartingCurrent(now);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);

        now = now.plusSeconds(31);
        startingCurrentSwitch.addPowerUpdate(now, 20, startingCurrentDetectionDuration);
        startingCurrentSwitch.detectStartingCurrent(now);

        // power threshold exceeded for more than configured starting current detection duration
        // ... causing appliance power off
        verify(control).on(now, false);
        when(control.isOn()).thenReturn(false);
        assertFalse(startingCurrentSwitch.isApplianceOn());
        // ... and also from the outside perspective the control is switched off
        assertFalse(startingCurrentSwitch.isOn());
        // ... listeners are notified of starting current detection
        verify(startingCurrentSwitchListener).startingCurrentDetected(now);
    }

    @Test
    public void detectFinishedCurrent() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        startingCurrentSwitch.setMeter(meter);
        startingCurrentSwitch.start(now, null);
        startingCurrentSwitch.on(now, true);

        // test fixture
        // ... the appliance should be switched on
        when(control.isOn()).thenReturn(true);
        assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... from the outside perspective the control is switched on
        assertTrue(startingCurrentSwitch.isOn());
        int finishedCurrentDetectionDuration = startingCurrentSwitch.getFinishedCurrentDetectionDuration();

        now = now.plusSeconds(10);
        startingCurrentSwitch.addPowerUpdate(now, 50, finishedCurrentDetectionDuration);
        startingCurrentSwitch.detectFinishedCurrent(now);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);

        now = now.plusSeconds(30);
        startingCurrentSwitch.addPowerUpdate(now, 10, finishedCurrentDetectionDuration);
        startingCurrentSwitch.detectFinishedCurrent(now);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);

        now = now.plusSeconds(331);
        startingCurrentSwitch.addPowerUpdate(now, 10, finishedCurrentDetectionDuration);
        startingCurrentSwitch.detectFinishedCurrent(now);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected(now);

        // with minRunningTime reached ...
        when(startingCurrentSwitch.isMinRunningTimeExceeded(now)).thenReturn(true);

        startingCurrentSwitch.addPowerUpdate(now, 10, finishedCurrentDetectionDuration);
        startingCurrentSwitch.detectFinishedCurrent(now);

        // ... causing power off from the outside perspective
        assertFalse(startingCurrentSwitch.isOn());
        // ... but appliance remaining powered on
        assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... listeners are notified of finish current detection
        verify(startingCurrentSwitchListener).finishedCurrentDetected();
    }

}
