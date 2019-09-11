import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';
import {ModbusRegisterRead} from '../shared/modbus-register-read';
import {ModbusRegisterWrite} from '../shared/modbus-register-write';
import {ModbusRead} from '../modbus-read/modbus-read';
import {ModbusWrite} from '../modbus-write/modbus-write';

export class EvModbusControl {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.EVModbusControl';
  }

  '@class' = EvModbusControl.TYPE;
  idref: string;
  slaveAddress: string;
  modbusReads: ModbusRead[];
  modbusWrites: ModbusWrite[];

  public constructor(init?: Partial<EvModbusControl>) {
    Object.assign(this, init);
  }

}
