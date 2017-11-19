package de.avanux.smartapplianceenabler.appliance;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

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
        startingCurrentSwitch.start(meter, null);

        // right after start
        // ... the appliance should be switched on
        when(control.isOn()).thenReturn(true);
        Assert.assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... but from the outside perspective the control is switched off
        Assert.assertFalse(startingCurrentSwitch.isOn());

        // averagePower=0 lastAveragePowerOfPowerOnDetection=null
        startingCurrentSwitch.detectStartingCurrent(meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected();
        // averagePower=0 lastAveragePowerOfPowerOnDetection=0
        startingCurrentSwitch.detectStartingCurrent(meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected();
        // averagePower=10 lastAveragePowerOfPowerOnDetection=0
        when(meter.getAveragePower()).thenReturn(10);
        startingCurrentSwitch.detectStartingCurrent(meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected();
        // averagePower=10 lastAveragePowerOfPowerOnDetection=10
        startingCurrentSwitch.detectStartingCurrent(meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected();
        // averagePower=30 lastAveragePowerOfPowerOnDetection=0
        when(meter.getAveragePower()).thenReturn(30);
        startingCurrentSwitch.detectStartingCurrent(meter);
        verify(startingCurrentSwitchListener, never()).startingCurrentDetected();
        // averagePower=30 lastAveragePowerOfPowerOnDetection=30
        startingCurrentSwitch.detectStartingCurrent(meter);

        // power threshold exceeded for more than configured starting current detection duration
        // (2 consecutive calls to detectStartingCurrent())
        // ... causing appliance power off
        verify(control).on(false);
        when(control.isOn()).thenReturn(false);
        Assert.assertFalse(startingCurrentSwitch.isApplianceOn());
        // ... and also from the outside perspective the control is switched off
        Assert.assertFalse(startingCurrentSwitch.isOn());
        // ... listeners are notified of starting current detection
        verify(startingCurrentSwitchListener).startingCurrentDetected();

        // power on recommendation received by energy manager
        reset(control);
        startingCurrentSwitch.on(true);
        // ... causing appliance power on
        verify(control).on(true);
        when(control.isOn()).thenReturn(true);
        Assert.assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... and also from the outside perspective the control is switched on
        Assert.assertTrue(startingCurrentSwitch.isOn());

        // averagePower=30 lastAveragePower=30
        when(meter.getAveragePower()).thenReturn(30);
        // power threshold still exceeds power threshold
        startingCurrentSwitch.detectStartingCurrent(meter);

        // the minimum running time has been exceeded now
        when(startingCurrentSwitch.isMinRunningTimeExceeded()).thenReturn(true);

        // averagePower=10 lastAveragePower=30
        when(meter.getAveragePower()).thenReturn(10);
        startingCurrentSwitch.detectFinishedCurrent(meter);
        verify(startingCurrentSwitchListener, never()).finishedCurrentDetected();
        // averagePower=10 lastAveragePower=10
        startingCurrentSwitch.detectFinishedCurrent(meter);

        // power consumption fell below threshold for more than configured finished current detection duration
        // (2 consecutive calls to detectFinishedCurrent())
        // ... causing power off from the outside perspective
        Assert.assertFalse(startingCurrentSwitch.isOn());
        // ... but appliance remaining powered on
        verify(control).on(true);
        when(control.isOn()).thenReturn(true);
        Assert.assertTrue(startingCurrentSwitch.isApplianceOn());
        // ... listeners are notified of finish current detection
        verify(startingCurrentSwitchListener).finishedCurrentDetected();
    }
}
