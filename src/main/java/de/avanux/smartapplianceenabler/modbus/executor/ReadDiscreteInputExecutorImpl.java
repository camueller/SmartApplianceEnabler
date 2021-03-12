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
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import de.avanux.smartapplianceenabler.modbus.transformer.ValueTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadDiscreteInputExecutorImpl extends BaseTransactionExecutor
        implements ModbusReadTransactionExecutor, ReadDiscreteInputExecutor {

    private Logger logger = LoggerFactory.getLogger(ReadCoilExecutorImpl.class);
    private boolean discrete;

    public ReadDiscreteInputExecutorImpl(String registerAddress) {
        super(registerAddress, 1, null);
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        logger.trace("{}: Reading discrete input register={}", getApplianceId(), getAddress());
        ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(getAddress(), 1);
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) trans.getResponse();
        if(res != null) {
            discrete = res.getDiscreteStatus(0);
            logger.debug("{}: Discrete input register={} value={}", getApplianceId(), getAddress(), discrete);
        }
        else {
            logger.error("{}: No response received.", getApplianceId());
        }
    }

    @Override
    public Boolean getValue() {
        return discrete;
    }
}
