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
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import de.avanux.smartapplianceenabler.modbus.RegisterValueType;
import de.avanux.smartapplianceenabler.modbus.transformer.FloatValueTransformer;
import de.avanux.smartapplianceenabler.modbus.transformer.IntegerValueTransformer;
import de.avanux.smartapplianceenabler.modbus.transformer.ValueTransformer;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WriteHoldingRegisterExecutorImpl extends BaseTransactionExecutor
        implements ModbusWriteTransactionExecutor<Integer>, WriteHoldingRegisterExecutor {

    private Logger logger = LoggerFactory.getLogger(WriteHoldingRegisterExecutorImpl.class);
    private Integer value;
    private RegisterValueType registerValueType;
    private Integer result;
    private Double factorToValue;

    public WriteHoldingRegisterExecutorImpl(String address, RegisterValueType registerValueType, Double factorToValue, ValueTransformer<?> transformer) {
        super(address, 1, transformer);
        this.registerValueType = registerValueType;
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
        Integer factoredValue = factorToValue != null ? Double.valueOf(this.value * this.factorToValue).intValue() : this.value;
        logger.debug("{}: Write holding register={} value={} factoredValue={}", getApplianceId(), getAddress(), this.value, factoredValue);
        if(registerValueType == RegisterValueType.Integer32 || registerValueType == RegisterValueType.Float || registerValueType == RegisterValueType.Float64) {
            writeMultipleRegisters(con, slaveAddress, factoredValue);
        } else {
            writeSingleRegister(con, slaveAddress, factoredValue);
        }
    }

    public void writeSingleRegister(TCPMasterConnection con, int slaveAddress, Integer factoredValue) throws ModbusException {
        SimpleRegister register = new SimpleRegister(factoredValue);

        WriteSingleRegisterRequest req = new WriteSingleRegisterRequest(getAddress(), register);
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        WriteSingleRegisterResponse res = (WriteSingleRegisterResponse) trans.getResponse();
        if(res != null) {
            this.result = res.getRegisterValue();
            logger.debug("{}: Write holding register={} confirmedValue={}", getApplianceId(), getAddress(), this.result);
        }
        else {
            logger.error("{}: No response received: register={} value={} ", getApplianceId(), getAddress(),
                    factoredValue);
        }
    }

    public void writeMultipleRegisters(TCPMasterConnection con, int slaveAddress, Integer factoredValue) throws ModbusException {
        var valueTransformer = getValueTransformer();
        if(valueTransformer instanceof IntegerValueTransformer) {
            valueTransformer.setValue(factoredValue);
        } else if(valueTransformer instanceof FloatValueTransformer) {
            valueTransformer.setValue(factoredValue.doubleValue());
        } else {
            throw new NotImplementedException(valueTransformer.getClass().getSimpleName() + " not supported");
        }

        List<Register> registers = new ArrayList<>();
        var bytesValues = valueTransformer.getByteValues();
        for(int i=0; i<bytesValues.length; i+=2) {
            registers.add(new SimpleRegister(bytesValues[i] << 8 | bytesValues[i + 1]));
        }

        StringBuilder sb = new StringBuilder();
        registers.forEach(register -> sb.append(register.toString()).append(" "));
        logger.debug("{}: Write holding register={} words={}", getApplianceId(), getAddress(), sb);

        WriteMultipleRegistersRequest req = new WriteMultipleRegistersRequest(getAddress(), registers.toArray(Register[]::new));
        req.setUnitID(slaveAddress);

        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();

        WriteMultipleRegistersResponse res = (WriteMultipleRegistersResponse) trans.getResponse();
        if(res != null) {
            logger.debug("{}: Write holding register={} confirmedValue={}", getApplianceId(), getAddress(), res.getHexMessage());
        }
        else {
            logger.error("{}: No response received: register={} value={} ", getApplianceId(), getAddress(),
                    factoredValue);
        }

    }
}
