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

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteCoilResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

/**
 * A <tt>WriteCoilRequest</tt> writes a bit.
 * The implementation directly correlates with the class 0 function <i>write coil (FC 5)</i>.
 */
public class WriteCoilExecutor implements ModbusTransactionExecutor, ApplianceIdConsumer {
    
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(WriteCoilExecutor.class));
    private String registerAddress;
    private boolean coil;
    private boolean result;
    
    /**
     * @param registerAddress
     * @param coil true if the coil should be set of false if it should be unset.
     */
    public WriteCoilExecutor(String registerAddress, boolean coil) {
        this.registerAddress = registerAddress;
        this.coil = coil;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.logger.setApplianceId(applianceId);
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        WriteCoilRequest req = new WriteCoilRequest(Integer.parseInt(registerAddress, 16), coil);
        req.setUnitID(slaveAddress);
        
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();
        
        WriteCoilResponse res = (WriteCoilResponse) trans.getResponse();
        if(res != null) {
            result = res.getCoil();
            logger.debug("Write coil register " + registerAddress + ": coil=" + coil + " result=" + result);
        }
        else {
            logger.error("No response received.");
        }
    }
    
    /**
     * Returns the result of the request.
     * @return true if the coil is set, false if unset.
     */
    public boolean getResult() {
        return result;
    }
}
