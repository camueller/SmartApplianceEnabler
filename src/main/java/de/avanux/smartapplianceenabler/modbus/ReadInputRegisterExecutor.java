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
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a <tt>ReadInputRegistersRequest</tt>.
 * The implementation directly correlates with the class 0 function <i>read multiple registers (FC 4)</i>
 */
public class ReadInputRegisterExecutor implements ModbusTransactionExecutor, ApplianceIdConsumer {
    private Logger logger = LoggerFactory.getLogger(ReadInputRegisterExecutor.class);
    private String applianceId;
    private String registerAddress;
    private Float registerValue;
    
    /**
     * @param registerAddress
     */
    public ReadInputRegisterExecutor(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        ReadInputRegistersRequest req = new ReadInputRegistersRequest(Integer.parseInt(registerAddress, 16), 2);
        req.setUnitID(slaveAddress);
        
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();
        
        ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();
        if(res != null) {
            registerValue = Float.intBitsToFloat(res.getRegisterValue(0) << 16 | res.getRegisterValue(1));
            logger.debug("{}: Input register={} value={}", applianceId, registerAddress, registerValue);
        }
        else {
            logger.error("{}: No response received.", applianceId);
        }
    }
    
    public Float getRegisterValue() {
        return this.registerValue;
    }
}
