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
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import org.slf4j.Logger;

abstract public class ReadHoldingRegisterExecutor<V> extends BaseTransactionExecutor implements ModbusReadTransactionExecutor<V> {
    public Integer[] byteValues;

    public ReadHoldingRegisterExecutor(String address, int requestWords) {
        super(address, requestWords);
    }

    abstract Logger getLogger();

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        getLogger().trace("{}: Reading holding register={} requestWords={}", getApplianceId(), getAddress(), getRequestWords());
        ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(getAddress(), getRequestWords());
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
        this.byteValues = null;
        if (res != null) {
            this.byteValues = new Integer[res.getWordCount()];
            for (int i = 0; i < res.getWordCount(); i++) {
                this.byteValues[i] = res.getRegisterValue(i);
            }
            getLogger().debug("{}: Input register={} value={}", getApplianceId(), getAddress(), this.byteValues);
        } else {
            getLogger().error("{}: No response received.", getApplianceId());
        }
    }

    protected Integer[] getByteValues() {
        return byteValues;
    }

    // Should only be used for testing.
    public void setByteValues(Integer[] byteValues) {
        this.byteValues = byteValues;
    }
}
