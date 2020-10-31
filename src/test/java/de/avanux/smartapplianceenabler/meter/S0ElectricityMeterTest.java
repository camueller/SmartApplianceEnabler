/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.meter;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class S0ElectricityMeterTest {
    private S0ElectricityMeter meter;
    private GpioPin gpioPin = mock(GpioPin.class);
    private Pin pin = mock(Pin.class);
    private int gpioNumber = 3;
    private PulsePowerMeter pulsePowerMeter = mock(PulsePowerMeter.class);
    private PulseEnergyMeter pulseEnergyMeter = mock(PulseEnergyMeter.class);
    private long timestamp = 0l;


    public S0ElectricityMeterTest() {
        this.meter = new S0ElectricityMeter();
        this.meter.setApplianceId(getClass().getSimpleName());
        this.meter.setPulsePowerMeter(pulsePowerMeter);
        this.meter.setPulseEnergyMeter(pulseEnergyMeter);
        when(gpioPin.getPin()).thenReturn(pin);
        when(pin.getAddress()).thenReturn(gpioNumber);
        when(pulsePowerMeter.getAveragePower()).thenReturn(33);
    }

    @Test
    public void handleEvent_PullDown() {
        this.meter.handleEvent(gpioPin, PinState.HIGH, PinPullResistance.PULL_DOWN, timestamp);
        this.meter.handleEvent(gpioPin, PinState.LOW, PinPullResistance.PULL_DOWN, 90l);
        verify(pulsePowerMeter).addTimestamp(timestamp);
        verify(pulseEnergyMeter).increasePulseCounter();
    }

    @Test
    public void handleEvent_PullUp() {
        this.meter.handleEvent(gpioPin, PinState.LOW, PinPullResistance.PULL_UP, timestamp);
        this.meter.handleEvent(gpioPin, PinState.HIGH, PinPullResistance.PULL_UP, 90l);
        verify(pulsePowerMeter).addTimestamp(timestamp);
        verify(pulseEnergyMeter).increasePulseCounter();
    }

    @Test
    public void handleEvent_PullDown_ImpulseDurationTooShort() {
        this.meter.handleEvent(gpioPin, PinState.HIGH, PinPullResistance.PULL_DOWN, timestamp);
        this.meter.handleEvent(gpioPin, PinState.LOW, PinPullResistance.PULL_DOWN, 10l);
        verify(pulsePowerMeter, never()).addTimestamp(timestamp);
        verify(pulseEnergyMeter, never()).increasePulseCounter();
    }

    @Test
    public void handleEvent_PullUp_ImpulseDurationTooShort() {
        this.meter.handleEvent(gpioPin, PinState.LOW, PinPullResistance.PULL_UP, timestamp);
        this.meter.handleEvent(gpioPin, PinState.HIGH, PinPullResistance.PULL_UP, 10l);
        verify(pulsePowerMeter, never()).addTimestamp(timestamp);
        verify(pulseEnergyMeter, never()).increasePulseCounter();
    }
}
