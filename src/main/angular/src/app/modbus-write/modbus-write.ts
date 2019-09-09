import {ModbusWriteValue} from '../modbus-write-value/modbus-write-value';

export class ModbusWrite {
  address: string;
  type: string;
  factorToValue: number;
  // FIXME: rename in Java and Angular
  registerWriteValues: ModbusWriteValue[];

  public constructor(init?: Partial<ModbusWrite>) {
    Object.assign(this, init);
  }
}
