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

import de.avanux.smartapplianceenabler.meter.PulseReceiver;
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
