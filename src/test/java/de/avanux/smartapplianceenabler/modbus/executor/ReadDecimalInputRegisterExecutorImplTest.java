/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.modbus.executor;

import de.avanux.smartapplianceenabler.modbus.ByteOrder;
import org.junit.Assert;
import org.junit.Test;

public class ReadDecimalInputRegisterExecutorImplTest {

    private ReadDecimalInputRegisterExecutorImpl executor;

    public ReadDecimalInputRegisterExecutorImplTest() {
    }

    @Test
    public void getValueLittleEndian() {
        this.executor = new ReadDecimalInputRegisterExecutorImpl("182", 2, ByteOrder.LittleEndian, 0.01);
        this.executor.setByteValues(new Integer[]{18254, 1});
        Assert.assertEquals(837.90, this.executor.getValue(), 0.0001f);
    }
}
