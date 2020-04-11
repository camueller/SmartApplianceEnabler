import {ModbusSwitch} from '../../../../../main/angular/src/app/control-modbus/modbus-switch';
import {ModbusWrite} from '../../../../../main/angular/src/app/modbus-write/modbus-write';
import {ModbusWriteValue} from '../../../../../main/angular/src/app/modbus-write-value/modbus-write-value';
import {ControlValueName} from '../../../../../main/angular/src/app/control/control-value-name';

export const modbusSwitch_2modbusWrite_complete = new ModbusSwitch({
  slaveAddress: '100',
  modbusWrites: [
    new ModbusWrite({
      address: '0x0A',
      type: 'Holding',
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
      writeValues: [
        new ModbusWriteValue({
          name: ControlValueName.Off,
          value: '0',
        }),
      ]
    }),
  ],
});
