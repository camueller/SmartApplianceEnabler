import {ModbusReadValue} from '../modbus-read-value/modbus-read-value';

export class ModbusRead {
  address: string;
  type: string;
  bytes: number;
  byteOrder: string;
  factorToValue: number;
  readValues: ModbusReadValue[];

  public constructor(init?: Partial<ModbusRead>) {
    Object.assign(this, init);
  }
}
