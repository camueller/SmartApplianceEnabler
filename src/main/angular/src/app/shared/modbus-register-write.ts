import {ModbusRegisterWriteValue} from './modbus-register-write-value';

export class ModbusRegisterWrite {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusRegisterWrite';
  }

  '@class' = ModbusRegisterWrite.TYPE;
  address: string;
  type: string;
  registerWriteValues: ModbusRegisterWriteValue[];

  public constructor(init?: Partial<ModbusRegisterWrite>) {
    Object.assign(this, init);
  }
}
