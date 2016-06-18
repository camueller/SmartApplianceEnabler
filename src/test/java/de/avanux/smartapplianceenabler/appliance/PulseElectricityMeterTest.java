package de.avanux.smartapplianceenabler.appliance;

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
public class PulseElectricityMeterTest {



    private PulseElectricityMeter pulseElectricityMeter;
    private long currentTimeMillis = 0; // System.currentTimeMillis();


    public PulseElectricityMeterTest() {
        pulseElectricityMeter = new PulseElectricityMeter();
        pulseElectricityMeter.setApplianceId(getClass().getSimpleName());
        pulseElectricityMeter.setImpulsesPerKwh(1000);
        pulseElectricityMeter.setMeasurementInterval(60);
    }

    @Test
    public void isOn_0ts() {
        Assert.assertFalse(pulseElectricityMeter.isOn(currentTimeMillis, true));
    }

    @Test
    public void isOn_0ts_powerOnAlways() {
        pulseElectricityMeter.setPowerOnAlways(true);
        Assert.assertTrue(pulseElectricityMeter.isOn(currentTimeMillis, true));
    }

    /**
     * O----------O
     */
    @Test
    public void isOn_rightAfterPulse() {
        addTimestamps(600, 0);
        Assert.assertTrue(pulseElectricityMeter.isOn(currentTimeMillis, true));
    }

    /**
     * O----------O--
     */
    @Test
    public void isOn_noIntervalIncreaseAboveFactor() {
        addTimestamps(700, 100);
        Assert.assertTrue(pulseElectricityMeter.isOn(currentTimeMillis, true));
    }

    /**
     * O----------O--------------------
     */
    @Test
    public void isOn_intervalIncreaseAboveFactor() {
        addTimestamps(1900, 1300);
        Assert.assertFalse(pulseElectricityMeter.isOn(currentTimeMillis, true));
    }

    /**
     * O----------O--------------------
     */
    @Test
    public void isOn_powerOnAlways() {
        pulseElectricityMeter.setPowerOnAlways(true);
        addTimestamps(1900, 1300);
        Assert.assertTrue(pulseElectricityMeter.isOn(currentTimeMillis, true));
    }

    /**
     * (O----------O
     */
    @Test
    public void getImpulsesInMeasurementInterval_2ts_2tsi() {
        pulseElectricityMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 0);
        Assert.assertEquals(1, pulseElectricityMeter.getImpulsesInMeasurementInterval(currentTimeMillis).size());
    }

    /**
     * O----------O-------(-O--O
     */
    @Test
    public void getImpulsesInMeasurementInterval_4ts_2tsi() {
        addTimestamps(3600, 2000, 50, 0);
        Assert.assertEquals(2, pulseElectricityMeter.getImpulsesInMeasurementInterval(currentTimeMillis).size());
    }

    /**
     * O--------(--O
     */
    @Test
    public void getAveragePower_2ts_1tsi() {
        addTimestamps(600, 0);
        Assert.assertEquals(6, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O----------O
     */
    @Test
    public void getAveragePower_2ts_2tsi_3600mi() {
        pulseElectricityMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 0);
        Assert.assertEquals(1, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (OO----------O
     */
    @Test
    public void getAveragePower_3ts_3tsi_3600mi() {
        pulseElectricityMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 3599, 0);
        Assert.assertEquals(2, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * OO----------O----------(O-O-O-O-O-O-O
     */
    @Test
    public void getAveragePower_9ts_6tsi() {
        addTimestamps(3600, 3599, 60, 50, 40, 30, 20, 10, 0);
        Assert.assertEquals(360, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * OO----------O----------O--(O--O-O-O-O
     */
    @Test
    public void getAveragePower_9ts_5tsi() {
        addTimestamps(3600, 3599, 80, 60, 30, 20, 10, 0);
        Assert.assertEquals(240, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----O-----O-----O
     */
    @Test
    public void getAveragePower_5ts_5tsi_3600mi() {
        pulseElectricityMeter.setMeasurementInterval(3600);
        addTimestamps(3600, 2700, 1800, 900, 0);
        Assert.assertEquals(4, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----------(-
     */
    @Test
    public void getAveragePower_2ts_0tsi() {
        addTimestamps(911, 612);
        Assert.assertEquals(0, pulseElectricityMeter.getAveragePower(currentTimeMillis));
        // again with control added
        Control control = mock(Control.class);
        when(control.isOn()).thenReturn(true);
        pulseElectricityMeter.setControl(control);
        Assert.assertEquals(0, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (O-----O-----------(-
     */
    @Test
    public void getAveragePower_2ts_0tsi_alwaysOn() {
        pulseElectricityMeter.setPowerOnAlways(true);
        addTimestamps(911, 612);
        Assert.assertEquals(12, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that after a period of high power normal power is reported even before the first impulse
     * of the low power period is received. Otherwise high power would be reported for a long time even though
     * low power was consumed.
     * O-----O----(-O-O-O-O---
     */
    @Test
    public void getAveragePower_NH() {
        pulseElectricityMeter.setPowerOnAlways(true);
        addTimestamps(1200, 600, 36, 30, 24, 18);
        Assert.assertEquals(6, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that after a period of high power normal power is reported even before the first impulse
     * of the low power period is received. After that impulse power should be calculated as usual.
     * O----------------------------------------O--O--O--O--O--O--O---------------O-
     */
    @Test
    public void getAveragePower_NHN_8ts() {
        pulseElectricityMeter.setPowerOnAlways(true);
        // 1889s / 16:28:46 = 189s
        addTimestamps(1889, 189);
        Assert.assertEquals(2, pulseElectricityMeter.getAveragePower(currentTimeMillis - 180000));
        // 16:28:52 = 183s
        addTimestamps(183);
        // 16:28:57 = 178s
        Assert.assertEquals(120, pulseElectricityMeter.getAveragePower(currentTimeMillis - 178000));
        // 16:28:59 = 176s / 16:29:04 = 171s / 16:29:11 = 164s / 16:29:17 = 158s
        addTimestamps(176,171,164,158);
        // 16:29:55 = 120s
        Assert.assertEquals(2, pulseElectricityMeter.getAveragePower(currentTimeMillis - 120000));
        // 16:30:55 = 60s
        Assert.assertEquals(2, pulseElectricityMeter.getAveragePower(currentTimeMillis - 60000));
        // 16:30:15 = 20s
        addTimestamps(20);
        // 16:31:55 = 0s
        Assert.assertEquals(26, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that after a period of high power normal power is reported after the first impulse
     * of the low power period is received.
     * O-----O-----O-O-O-(O---O
     */
    @Test
    public void getAveragePower_NHN() {
        addTimestamps(1800, 1200, 618, 612, 606, 600, 0);
        Assert.assertEquals(6, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * Make sure that some power value during ramp up is not reported as "before increase" value
     * after switch off.
     * O-----O-----O---OOOO--------------------------------------------------------(---
     */
    @Test
    public void getAveragePower_NHZ() {
        //            02:01:24, 02:01:36, 02:01:47, 02:01:53, 02:01:54, 02:01:55, 02:01:56
        addTimestamps(    1832,     1820,     1809,     1803,     1802,     1801,     1800);
        Assert.assertEquals(0, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (OO----------O
     */
    @Test
    public void getMinPower_3ts_3tsi_3600mi() {
        pulseElectricityMeter.setMeasurementInterval(3600);
        addTimestamps(3599, 3598, 0);
        Assert.assertEquals(1, pulseElectricityMeter.getMinPower(currentTimeMillis));
    }

    /**
     * Make sure that min power is not greater than average power
     * O-O-O-------------(---
     */
    @Test
    public void getMinPower_3ts_3tsi() {
        addTimestamps(58, 50, 45);
        Assert.assertEquals(180, pulseElectricityMeter.getMinPower(currentTimeMillis));
        Assert.assertEquals(180, pulseElectricityMeter.getAveragePower(currentTimeMillis));
    }

    /**
     * (OO----------O
     */
    @Test
    public void getMaxPower_3ts_3tsi_3600mi() {
        pulseElectricityMeter.setMeasurementInterval(3600);
        addTimestamps(3599, 3598, 0);
        Assert.assertEquals(3600, pulseElectricityMeter.getMaxPower(currentTimeMillis));
    }

    private void addTimestamps(int... seconds) {
        for(int i=0;i<seconds.length;i++) {
            pulseElectricityMeter.addTimestampAndMaintain(currentTimeMillis - seconds[i] * 1000L);
        }
    }
}
