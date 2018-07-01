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

package de.avanux.smartapplianceenabler.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadHoldingRegisterExecutor implements ModbusTransactionExecutor, ApplianceIdConsumer {
    private Logger logger = LoggerFactory.getLogger(ReadInputRegisterExecutor.class);
    private String applianceId;
    private String registerAddress;
    private Float registerValue;

    /**
     * @param registerAddress
     */
    public ReadHoldingRegisterExecutor(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(Integer.parseInt(registerAddress, 16), 1);
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse) trans.getResponse();
        if(res != null) {
            logger.debug("{}: Register 0 = {}", applianceId, res.getRegisterValue(0));
            //logger.debug("{}: Register 1 = {}", applianceId, res.getRegisterValue(1));
            //registerValue = Float.intBitsToFloat(res.getRegisterValue(0) << 16 | res.getRegisterValue(1));
            //logger.debug("{}: Input register={} value={}", applianceId, registerAddress, registerValue);
        }
        else {
            logger.error("{}: No response received.", applianceId);
        }
    }
}
