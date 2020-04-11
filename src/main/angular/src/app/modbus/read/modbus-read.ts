import {ModbusReadValue} from '../read-value/modbus-read-value';

export class ModbusRead {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusRead';
  }
  '@class' = ModbusRead.TYPE;
  address: string;
  type: string;
  bytes: number;
  byteOrder: string;
  factorToValue: number;
  readValues: ModbusReadValue[];

  public constructor(init?: Partial<ModbusRead>) {
    Object.assign(this, init);
  }

  public static createWithSingleChild() {
    return new ModbusRead({readValues: [new ModbusReadValue()]});
  }
}
