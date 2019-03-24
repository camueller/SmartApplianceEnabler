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
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteCoilResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <tt>WriteCoilRequest</tt> writes a bit.
 * The implementation directly correlates with the class 0 function <i>write coil (FC 5)</i>.
 */
public class WriteCoilExecutorImpl extends BaseTransactionExecutor
        implements ModbusWriteTransactionExecutor<Boolean>, WriteCoilExecutor {
    
    private Logger logger = LoggerFactory.getLogger(WriteCoilExecutorImpl.class);
    private boolean value;
    private Boolean result;
    
    public WriteCoilExecutorImpl(String address) {
        super(address, 1);
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getResult() {
        return result;
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        WriteCoilRequest req = new WriteCoilRequest(getAddress(), value);
        req.setUnitID(slaveAddress);
        
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();
        
        WriteCoilResponse res = (WriteCoilResponse) trans.getResponse();
        if(res != null) {
            result = res.getCoil();
            logger.debug("{}: Write coil register={} coil={} confirmedValue={}", getApplianceId(), getAddress(), value, result);
        }
        else {
            logger.error("{}: No response received.", getApplianceId());
        }
    }
}
