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
package de.avanux.smartapplianceenabler.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a <tt>ReadInputRegistersRequest</tt>.
 * The implementation directly correlates with the class 0 function <i>read multiple registers (FC 4)</i>
 */
public class ReadInputRegisterExecutor extends BaseReadTransactionExecutor implements ModbusReadTransactionExecutor {
    private Logger logger = LoggerFactory.getLogger(ReadInputRegisterExecutor.class);
    private int bytes = 1;
    private Integer[] byteValues;
    
    public ReadInputRegisterExecutor(String address, int bytes) {
        super(address);
        this.bytes = bytes;
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        ReadInputRegistersRequest req = new ReadInputRegistersRequest(getAddress(), this.bytes);
        req.setUnitID(slaveAddress);
        
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();
        
        ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
        this.byteValues = null;
        if(res != null) {
            this.byteValues = new Integer[this.bytes];
            for(int i = 0; i<this.bytes; i++) {
                this.byteValues[i] = res.getRegisterValue(i);
            }
            logger.debug("{}: Input register={} value={}", getApplianceId(), getAddress(), this.byteValues);
        }
        else {
            logger.error("{}: No response received.", getApplianceId());
        }
    }
    
    public Integer[] getRegisterValues() {
        return this.byteValues;
    }

    public Float getRegisterValueFloat() {
        if(this.bytes == 2) {
            return Float.intBitsToFloat(this.byteValues[0] << 16 | this.byteValues[1]);
        }
        logger.error("{}: Float has to be composed of 2 bytes!", getApplianceId());
        return null;
    }

    public String getRegisterValueString() {
        StringBuilder stringValue = new StringBuilder();
        for(Integer byteValue : this.byteValues) {
            stringValue.append(new Character((char) byteValue.intValue()));
        }
        return stringValue.toString();
    }

}
