package de.avanux.smartapplianceenabler.appliance;

import org.junit.Assert;
import org.junit.Test;

public class PulseReceiverTest {

    private PulseReceiver pulseReceiver = new PulseReceiver();

    @Test
    public void isPacketValid() {
        Assert.assertTrue(pulseReceiver.isPacketValid("F-00000001-000000000001-00:00143"));
        Assert.assertFalse(pulseReceiver.isPacketValid("F-00000001-000000000001-00:143"));
        Assert.assertFalse(pulseReceiver.isPacketValid("F-00000001-000000000001-00143"));
        Assert.assertFalse(pulseReceiver.isPacketValid("abc"));
    }

    @Test
    public void parseApplianceId() {
        Assert.assertEquals("F-00000001-000000000001-00", pulseReceiver.parseApplianceId("F-00000001-000000000001-00:00143"));
    }

    @Test
    public void parseCounter() {
        Assert.assertEquals(143, pulseReceiver.parseCounter("F-00000001-000000000001-00:00143"));
    }
}
