package de.avanux.smartapplianceenabler.appliance;

import org.slf4j.Logger;
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
public class WriteCoilExecutor implements ModbusTransactionExecutor {
    
    private Logger logger = LoggerFactory.getLogger(WriteCoilExecutor.class);
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
    public void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException {
        WriteCoilRequest req = new WriteCoilRequest(Integer.parseInt(registerAddress, 16), coil);
        req.setUnitID(slaveAddress);
        
        ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
        trans.setRequest(req);
        trans.execute();
        
        WriteCoilResponse res = (WriteCoilResponse) trans.getResponse();
        result = res.getCoil();
        logger.debug("Write coil register " + registerAddress + ": coil=" + coil + " result=" + result);
    }
    
    /**
     * Returns the result of the request.
     * @return true if the coil is set, false if unset.
     */
    public boolean getResult() {
        return result;
    }
}
