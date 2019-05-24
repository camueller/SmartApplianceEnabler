import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';
import {ModbusRegisterRead} from '../shared/modbus-register-read';
import {ModbusRegisterWrite} from '../shared/modbus-register-write';

export class EvModbusControl {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.EVModbusControl';
  }

  '@class' = EvModbusControl.TYPE;
  idref: string;
  slaveAddress: string;
  configuration: ModbusRegisterConfguration[];
  registerReads: ModbusRegisterRead[];
  registerWrites: ModbusRegisterWrite[];

  public constructor(init?: Partial<EvModbusControl>) {
    Object.assign(this, init);
  }

}
