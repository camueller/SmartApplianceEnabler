package de.avanux.smartapplianceenabler.appliance;

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
        coil = res.getCoils().getBit(0);
        logger.debug("Read coil register " + registerAddress + ": coil=" + coil);
    }
    
    public boolean getCoil() {
        return coil;
    }
}
