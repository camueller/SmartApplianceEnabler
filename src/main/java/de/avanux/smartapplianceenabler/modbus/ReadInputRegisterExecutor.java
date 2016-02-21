package de.avanux.smartapplianceenabler.modbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

/**
 * Implements a <tt>ReadInputRegistersRequest</tt>.
 * The implementation directly correlates with the class 0 function <i>read multiple registers (FC 4)</i>
 */
public class ReadInputRegisterExecutor implements ModbusTransactionExecutor {
    private Logger logger = LoggerFactory.getLogger(ReadInputRegisterExecutor.class);
    private String registerAddress;
    private Float registerValue;
    
    /**
     * @param registerAddress
     */
    public ReadInputRegisterExecutor(String registerAddress) {
        this.registerAddress = registerAddress;
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
            logger.debug("Input register " + registerAddress + ": value=" + registerValue);
        }
        else {
            logger.error("No response received.");
        }
    }
    
    public Float getRegisterValue() {
        return this.registerValue;
    }
}
