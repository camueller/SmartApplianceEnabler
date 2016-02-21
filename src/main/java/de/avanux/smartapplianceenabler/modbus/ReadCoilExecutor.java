/*
 * Copyright (C) 2016 Axel Müller <axel.mueller@avanux.de>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

/**
 * A <tt>ReadCoilRequest</tt> reads a bit.
 * The implementation directly correlates with the class 1 function <i>read coils (FC 1)</i>. 
 */
public class ReadCoilExecutor implements ModbusTransactionExecutor {
    
    private Logger logger = LoggerFactory.getLogger(ReadCoilExecutor.class);
    private String registerAddress;
    private boolean coil;
    
    /**
     * @param registerAddress
     */
    public ReadCoilExecutor(String registerAddress) {
        this.registerAddress = registerAddress;
    }
    
    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        ReadCoilsRequest req = new ReadCoilsRequest(Integer.parseInt(registerAddress, 16), 1);
        req.setUnitID(slaveAddress);
        
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();
        
        ReadCoilsResponse res = (ReadCoilsResponse) trans.getResponse();
        if(res != null) {
            coil = res.getCoils().getBit(0);
            logger.debug("Read coil register " + registerAddress + ": coil=" + coil);
        }
        else {
            logger.error("No response received.");
        }
    }
    
    public boolean getCoil() {
        return coil;
    }
}
