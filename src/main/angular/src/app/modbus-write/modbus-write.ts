import {ModbusWriteValue} from '../modbus-write-value/modbus-write-value';

export class ModbusWrite {
  address: string;
  type: string;
  factorToValue: number;
  writeValues: ModbusWriteValue[];

  public constructor(init?: Partial<ModbusWrite>) {
    Object.assign(this, init);
  }
}
