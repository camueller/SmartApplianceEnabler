import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {ModbusElectricityMeter} from '../../../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {ModbusRead} from '../../../../../main/angular/src/app/modbus/read/modbus-read';
import {ModbusReadValue} from '../../../../../main/angular/src/app/modbus/read-value/modbus-read-value';

export const modbusMeter_complete = new ModbusElectricityMeter({
  slaveAddress: '100',
  modbusReads: [
    new ModbusRead({
      address: '0x0156',
      type: 'Input',
      valueType: 'Float',
      words: 2,
      byteOrder: 'LittleEndian',
      factorToValue: 0.01,
      readValues: [
        new ModbusReadValue({
          name: MeterValueName.Energy,
          extractionRegex: ',.Energy.:(\\d+)',
        })
      ]
    }),
  ]
});

export const modbusMeter_pollInterval = new ModbusElectricityMeter({
  pollInterval: 30,
  slaveAddress: '100',
  modbusReads: [
    new ModbusRead({
      type: 'Input',
      valueType: 'Integer2Float',
      address: '0x0A',
      readValues: [
        new ModbusReadValue({
          name: MeterValueName.Power,
        })
      ]
    }),
  ]
});
