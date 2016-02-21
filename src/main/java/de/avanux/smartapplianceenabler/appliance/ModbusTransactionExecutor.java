package de.avanux.smartapplianceenabler.appliance;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

/**
 * Executor of a ModBus transaction.
 *
 */
public interface ModbusTransactionExecutor {

    /**
     * Execute a ModBus transaction with a slave using a TCP connection.
     * @param con the TCP connection
     * @param slaveAddress the address of the slave
     * @throws ModbusException
     */
    void execute(TCPMasterConnection con, int slaveAddress) throws ModbusException;
    
}
