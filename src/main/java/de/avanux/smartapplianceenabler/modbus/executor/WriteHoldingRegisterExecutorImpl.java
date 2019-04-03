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
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteHoldingRegisterExecutorImpl extends BaseTransactionExecutor
        implements ModbusWriteTransactionExecutor<Integer>, WriteHoldingRegisterExecutor {

    private Logger logger = LoggerFactory.getLogger(ReadInputRegisterExecutor.class);
    private Integer value;
    private Integer result;
    private Double factorToValue;

    public WriteHoldingRegisterExecutorImpl(String address, Double factorToValue) {
        super(address, 1);
        this.factorToValue = factorToValue;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getResult() {
        return result;
    }

    @Override
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        Integer factoredValue = Double.valueOf(value * getInitializedFactorToValue()).intValue();
        SimpleRegister register = new SimpleRegister(factoredValue);

        WriteSingleRegisterRequest req = new WriteSingleRegisterRequest(getAddress(), register);
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        WriteSingleRegisterResponse res = (WriteSingleRegisterResponse) trans.getResponse();
        if(res != null) {
            this.result = res.getRegisterValue();
            logger.debug("{}: Write holding register={} value={} confirmedValue={}", getApplianceId(), getAddress(),
                    factoredValue, this.result);
        }
        else {
            logger.error("{}: No response received: register={} value={} ", getApplianceId(), getAddress(),
                    factoredValue);
        }
    }

    private Double getInitializedFactorToValue() {
        if(this.factorToValue == null) {
            return 1.0;
        }
        return this.factorToValue;
    }
}
