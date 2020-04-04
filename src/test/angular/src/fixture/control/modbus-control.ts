import {ModbusWrite} from '../../../../../main/angular/src/app/modbus-write/modbus-write';
import {ModbusWriteValue} from '../../../../../main/angular/src/app/modbus-write-value/modbus-write-value';
import {ControlValueName} from '../../../../../main/angular/src/app/control/control-value-name';
import {ModbusSwitch} from '../../../../../main/angular/src/app/control/modbus/modbus-switch';

export const modbusSwitch_2modbusWrite_complete = new ModbusSwitch({
  slaveAddress: '100',
  modbusWrites: [
    new ModbusWrite({
      address: '0x0A',
      type: 'Holding',
      factorToValue: 1000,
      writeValues: [
        new ModbusWriteValue({
          name: ControlValueName.On,
          value: '1',
        }),
      ]
    }),
    new ModbusWrite({
      address: '0x0E',
      type: 'Coil',
      factorToValue: 10,
      writeValues: [
        new ModbusWriteValue({
          name: ControlValueName.Off,
          value: '0',
        }),
      ]
    }),
  ],
});
