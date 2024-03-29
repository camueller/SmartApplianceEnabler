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

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;

import static org.mockito.Mockito.*;

public class S0ElectricityMeterTest {
    private S0ElectricityMeter meter;
    private int gpioNumber = 3;
    private PulsePowerMeter pulsePowerMeter = mock(PulsePowerMeter.class);
    private PulseEnergyMeter pulseEnergyMeter = mock(PulseEnergyMeter.class);
    private LocalDateTime now = LocalDateTime.now();


//    public S0ElectricityMeterTest() {
//        this.meter = new S0ElectricityMeter();
//        this.meter.setApplianceId(getClass().getSimpleName());
//        this.meter.setPulsePowerMeter(pulsePowerMeter);
//        this.meter.setPulseEnergyMeter(pulseEnergyMeter);
//        when(gpioPin.getPin()).thenReturn(pin);
//        when(pin.getAddress()).thenReturn(gpioNumber);
//        when(pulsePowerMeter.getAveragePower()).thenReturn(33);
//    }
//
//    @Test
//    public void handleEvent_PullDown() {
//        this.meter.handleEvent(now, gpioPin, PinState.HIGH, PinPullResistance.PULL_DOWN);
//        this.meter.handleEvent(now.plusNanos(90 * 1000000), gpioPin, PinState.LOW, PinPullResistance.PULL_DOWN);
//        verify(pulsePowerMeter).addTimestamp(now);
//        verify(pulseEnergyMeter).increasePulseCounter();
//    }
//
//    @Test
//    public void handleEvent_PullUp() {
//        this.meter.handleEvent(now, gpioPin, PinState.LOW, PinPullResistance.PULL_UP);
//        this.meter.handleEvent(now.plusNanos(90 * 1000000), gpioPin, PinState.HIGH, PinPullResistance.PULL_UP);
//        verify(pulsePowerMeter).addTimestamp(now);
//        verify(pulseEnergyMeter).increasePulseCounter();
//    }
//
//    @Test
//    public void handleEvent_PullDown_ImpulseDurationTooShort() {
//        this.meter.handleEvent(now, gpioPin, PinState.HIGH, PinPullResistance.PULL_DOWN);
//        this.meter.handleEvent(now.plusNanos(10 * 1000000), gpioPin, PinState.LOW, PinPullResistance.PULL_DOWN);
//        verify(pulsePowerMeter, never()).addTimestamp(now);
//        verify(pulseEnergyMeter, never()).increasePulseCounter();
//    }
//
//    @Test
//    public void handleEvent_PullUp_ImpulseDurationTooShort() {
//        this.meter.handleEvent(now, gpioPin, PinState.LOW, PinPullResistance.PULL_UP);
//        this.meter.handleEvent(now.plusNanos(10 * 1000000), gpioPin, PinState.HIGH, PinPullResistance.PULL_UP);
//        verify(pulsePowerMeter, never()).addTimestamp(now);
//        verify(pulseEnergyMeter, never()).increasePulseCounter();
//    }
}
