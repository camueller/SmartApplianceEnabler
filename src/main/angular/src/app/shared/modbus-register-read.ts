import {ModbusRegisterReadValue} from './modbus-register-read-value';

export class ModbusRegisterRead {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusRegisterRead';
  }

  '@class' = ModbusRegisterRead.TYPE;
  address: string;
  type: string;
  bytes: number;
  byteOrder: string;
  factorToValue: number;
  pollInterval: number;
  registerReadValues: ModbusRegisterReadValue[];

  public constructor(init?: Partial<ModbusRegisterRead>) {
    Object.assign(this, init);
  }
}
