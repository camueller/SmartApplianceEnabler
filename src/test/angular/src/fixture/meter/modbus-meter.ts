import {MeterValueName} from '../../../../../main/angular/src/app/meter/meter-value-name';
import {ModbusElectricityMeter} from '../../../../../main/angular/src/app/meter/modbus/modbus-electricity-meter';
import {ModbusRead} from '../../../../../main/angular/src/app/modbus/read/modbus-read';
import {ModbusReadValue} from '../../../../../main/angular/src/app/modbus/read-value/modbus-read-value';

export const modbusMeter_1ModbusRead_complete = new ModbusElectricityMeter({
  slaveAddress: '100',
  modbusReads: [
    new ModbusRead({
      type: 'InputFloat',
      address: '0x0C',
      bytes: 2,
      factorToValue: 10,
      readValues: [
        new ModbusReadValue({
          name: MeterValueName.Power,
          extractionRegex: ',.Power.:(\\d+)',
        })
      ]
    }),
  ]
});

export const modbusMeter_2ModbusRead_complete = new ModbusElectricityMeter({
  slaveAddress: '100',
  modbusReads: [
    ...modbusMeter_1ModbusRead_complete.modbusReads,
    new ModbusRead({
      type: 'InputDecimal',
      address: '0x0A',
      bytes: 4,
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
