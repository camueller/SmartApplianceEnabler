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

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import de.avanux.smartapplianceenabler.modbus.transformer.ValueTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a <tt>ReadInputRegistersRequest</tt>.
 * The implementation directly correlates with the class 0 function <i>read multiple registers (FC 4)</i>
 */
public class ReadInputRegisterExecutor extends BaseTransactionExecutor implements ModbusReadTransactionExecutor {
    private Logger logger = LoggerFactory.getLogger(ReadInputRegisterExecutor.class);

    public ReadInputRegisterExecutor(String address, int requestWords, ValueTransformer transformer) {
        super(address, requestWords, transformer);
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        logger.trace("{}: Reading input register={} requestWords={}", getApplianceId(), getAddress(), getRequestWords());
        ReadInputRegistersRequest req = new ReadInputRegistersRequest(getAddress(), getRequestWords());
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
        if (res != null) {
            Integer[] byteValues = new Integer[res.getWordCount()];
            for (int i = 0; i < res.getWordCount(); i++) {
                byteValues[i] = res.getRegisterValue(i);
            }
            logger.debug("{}: Input register={} value={}", getApplianceId(), getAddress(), byteValues);
            getValueTransformer().setByteValues(byteValues);
        } else {
            logger.error("{}: No response received.", getApplianceId());
        }
    }
}
