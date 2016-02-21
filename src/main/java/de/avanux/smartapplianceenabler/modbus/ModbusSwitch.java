package de.avanux.smartapplianceenabler.modbus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.avanux.smartapplianceenabler.appliance.Control;
import de.avanux.smartapplianceenabler.appliance.RunningTimeController;

public class ModbusSwitch extends ModbusSlave implements Control {

    @XmlTransient
    private Logger logger = LoggerFactory.getLogger(ModbusElectricityMeter.class);
    @XmlAttribute
    private String registerAddress;
    @XmlTransient
    RunningTimeController runningTimeController;    

    public boolean on(boolean switchOn) {
        boolean result = false;
        try {
            logger.info("Switching " + (switchOn ? "on" : "off"));
            WriteCoilExecutor executor = new WriteCoilExecutor(registerAddress, switchOn);
            executeTransaction(executor, false);
            result = executor.getResult();
            
            if(runningTimeController != null) {
                runningTimeController.setRunning(switchOn);
            }
        }
        catch (Exception e) {
            logger.error("Error switching coil register " + registerAddress, e);
        }
        return ! (switchOn ^ result);
    }

    @Override
    public boolean isOn() {
        boolean coil = false;
        try {
            ReadCoilExecutor executor = new ReadCoilExecutor(registerAddress);
            executeTransaction(executor, false);
            coil = executor.getCoil();
        }
        catch (Exception e) {
            logger.error("Error switching coil register " + registerAddress, e);
        }
        return coil;
    }
    
    public void setRunningTimeController(RunningTimeController runningTimeController) {
        this.runningTimeController = runningTimeController;
    }
}
