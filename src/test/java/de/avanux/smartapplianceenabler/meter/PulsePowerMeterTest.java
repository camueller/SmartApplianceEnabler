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

package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.control.AlwaysOnSwitch;
import de.avanux.smartapplianceenabler.control.Control;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 * Documentation of this class uses a specific notation to describe test cases:
 * Example:
 * O-----O-----O-O-O-(O---
 * ^ Impulses received
 *  ^^^^^ Time (number of hyphens corresponds to amount of time)
 *                   ^ measurement interval begin
 *  The most right character represents the present.
 *  Further left of a character represents further in the past.
 *
 *  ts ... timestamp
 *  tsi ... timestamps in interval
 *  mi ... measurement interval (default 60s)
 *  N/H/Z ... normal/high/zero power
 */
public class PulsePowerMeterTest {



    private PulsePowerMeter pulsePowerMeter;
    private long currentTimeMillis = 0; // System.currentTimeMillis();


    public PulsePowerMeterTest() {
        pulsePowerMeter = new PulsePowerMeter();
        pulsePowerMeter.setApplianceId(getClass().getSimpleName());
        pulsePowerMeter.setImpulsesPerKwh(1000);
        pulsePowerMeter.setMeasurementInterval(60);
    }

    @Test
    public void isOn_0ts() {
        Assert.assertFalse(pulsePowerMeter.isOn(currentTimeMillis));
    }

    @Test
    public void isOn_0ts_powerOnAlways() {
        pulsePowerMeter.setControl(new AlwaysOnSwitch());
        Assert.assertTrue(pulsePowerMeter.isOn(currentTimeMillis));
    }

    /**
     * O----------O
     */
    @Test
    public void isOn_rightAfterPulse() {
        addTimestamps(600, 0);
        Assert.assertTrue(pulsePowerMeter.isOn(currentTimeMillis));
    }

    /**
     * O----------O--
     */
    @Test
    public void isOn_noIntervalIncreaseAboveFactor() {
        addTimestamps(700, 100);
        Assert.assertTrue(pulsePowerMeter.isOn(currentTimeMillis));
    }

    /**
     * O----------O--------------------
     */
    @Test
    public void isOn_intervalIncreaseAboveFactor() {
        addTimestamps(1900, 1300);
        Assert.assertFalse(pulsePowerMeter.isOn(currentTimeMillis));
    }

    /**
     * O----------O--------------------
     */
    @Test
    public void isOn_powerOnAlways() {
        pulsePowerMeter.setControl(new AlwaysOnSwitch());
        addTimestamps(1900, 1300);
        Assert.assertTrue(pulsePowerMeter.isOn(currentTimeMillis));
    }

    /**
     * (O----------O
     */
    @Test
    public void getImpulsesInMeasurementInterval_2ts_2tsi() {
        pulsePowerMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 0);
        Assert.assertEquals(1, pulsePowerMeter.getImpulsesInMeasurementInterval(currentTimeMillis).size());
    }

    /**
     * O----------O-------(-O--O
     */
    @Test
    public void getImpulsesInMeasurementInterval_4ts_2tsi() {
        addTimestamps(3600, 2000, 50, 0);
        Assert.assertEquals(2, pulsePowerMeter.getImpulsesInMeasurementInterval(currentTimeMillis).size());
    }

    /**
     * O--------(--O
     */
    @Test
    public void getAveragePower_2ts_1tsi() {
        addTimestamps(600, 0);
        Assert.assertEquals(6, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O----------O
     */
    @Test
    public void getAveragePower_2ts_2tsi_3600mi() {
        pulsePowerMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 0);
        Assert.assertEquals(1, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (OO----------O
     */
    @Test
    public void getAveragePower_3ts_3tsi_3600mi() {
        pulsePowerMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 3599, 0);
        Assert.assertEquals(2, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * OO----------O----------(O-O-O-O-O-O-O
     */
    @Test
    public void getAveragePower_9ts_6tsi() {
        addTimestamps(3600, 3599, 60, 50, 40, 30, 20, 10, 0);
        Assert.assertEquals(360, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * OO----------O----------O--(O--O-O-O-O
     */
    @Test
    public void getAveragePower_9ts_5tsi() {
        addTimestamps(3600, 3599, 80, 60, 30, 20, 10, 0);
        Assert.assertEquals(240, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----O-----O-----O
     */
    @Test
    public void getAveragePower_5ts_5tsi_3600mi() {
        pulsePowerMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 2700, 1800, 900, 0);
        Assert.assertEquals(4, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----------(-
     */
    @Test
    public void getAveragePower_2ts_0tsi() {
        addTimestamps(911, 612);
        Assert.assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----------(-
     */
    @Test
    public void getAveragePower_2ts_0tsi_controlOn() {
        Control control = mock(Control.class);
        when(control.isOn()).thenReturn(true);
        pulsePowerMeter.setControl(control);
        addTimestamps(911, 612);
        Assert.assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----------(-
     */
    @Test
    public void getAveragePower_2ts_0tsi_alwaysOn() {
        pulsePowerMeter.setControl(new AlwaysOnSwitch());
        addTimestamps(911, 612);
        Assert.assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that after a period of high power normal power is reported even before the first impulse
     * of the low power period is received. Otherwise high power would be reported for a long time even though
     * low power was consumed.
     * O-----O----(-O-O-O-O---
     */
    @Test
    public void getAveragePower_NH() {
        pulsePowerMeter.setControl(new AlwaysOnSwitch());
        addTimestamps(1200, 600, 36, 30, 24, 18);
        Assert.assertEquals(6, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that after a period of high power normal power is reported even before the first impulse
     * of the low power period is received. After that impulse power should be calculated as usual.
     * O----------------------------------------O--O--O--O--O--O--O---------------O-
     */
    @Test
    public void getAveragePower_NHN_8ts() {
        pulsePowerMeter.setControl(new AlwaysOnSwitch());
        // 1889s / 16:28:46 = 189s
        addTimestamps(1889, 189);
        Assert.assertEquals(2, pulsePowerMeter.getAveragePower(currentTimeMillis - 180000));
        // 16:28:52 = 183s
        addTimestamps(183);
        // 16:28:57 = 178s
        Assert.assertEquals(120, pulsePowerMeter.getAveragePower(currentTimeMillis - 178000));
        // 16:28:59 = 176s / 16:29:04 = 171s / 16:29:11 = 164s / 16:29:17 = 158s
        addTimestamps(176,171,164,158);
        // 16:29:55 = 120s
        Assert.assertEquals(2, pulsePowerMeter.getAveragePower(currentTimeMillis - 120000));
        // 16:30:55 = 60s
        Assert.assertEquals(2, pulsePowerMeter.getAveragePower(currentTimeMillis - 60000));
        // 16:30:15 = 20s
        addTimestamps(20);
        // 16:31:55 = 0s
        Assert.assertEquals(26, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that after a period of high power normal power is reported after the first impulse
     * of the low power period is received.
     * O-----O-----O-O-O-(O---O
     */
    @Test
    public void getAveragePower_NHN() {
        addTimestamps(1800, 1200, 618, 612, 606, 600, 0);
        Assert.assertEquals(6, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that some power value during ramp up is not reported as "before increase" value
     * after switch off.
     * O-----O-----O---OOOO-(---
     */
    @Test
    public void getAveragePower_NHZ() {
        addTimestamps(3000, 1800, 700, 100, 90, 80, 70);
        Control control = mock(Control.class);
        when(control.isOn()).thenReturn(true);
        pulsePowerMeter.setControl(control);
        Assert.assertEquals(6, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * The power before a power increase should not be reported anymore if there has been no
     * timestamp for the time calculated by multiplying the factor with the number of seconds after
     * which a timestamp was to be expected (based on power before increase).
     * O-----O-----O---OOOO------------------------(---
     */
    @Test
    public void getAveragePower_NHZ_NoIntervalIncreaseAboveFactor_TimestampMaxAgeExceeded() {
        addTimestamps(2085, 1535, 985, 685, 680, 675, 670);
        Control control = mock(Control.class);
        when(control.isOn()).thenReturn(true);
        pulsePowerMeter.setControl(control);
        Assert.assertEquals(0, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (OO----------O
     */
    @Test
    public void getMinPower_3ts_3tsi_3600mi() {
        pulsePowerMeter.setMeasurementInterval(3600);
        addTimestamps(3599, 3598, 0);
        Assert.assertEquals(1, pulsePowerMeter.getMinPower(currentTimeMillis));
    }

    /**
     * Make sure that min power is not greater than average power
     * O-O-O-------------(---
     */
    @Test
    public void getMinPower_3ts_3tsi() {
        addTimestamps(58, 50, 45);
        Assert.assertEquals(180, pulsePowerMeter.getMinPower(currentTimeMillis));
        Assert.assertEquals(180, pulsePowerMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (OO----------O
     */
    @Test
    public void getMaxPower_3ts_3tsi_3600mi() {
        pulsePowerMeter.setMeasurementInterval(3600);
        addTimestamps(3599, 3598, 0);
        Assert.assertEquals(3600, pulsePowerMeter.getMaxPower(currentTimeMillis));
    }

    private void addTimestamps(int... seconds) {
        for(int i=0;i<seconds.length;i++) {
            pulsePowerMeter.addTimestampAndMaintain(currentTimeMillis - seconds[i] * 1000L);
        }
    }
}
