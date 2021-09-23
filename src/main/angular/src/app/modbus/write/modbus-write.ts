import {ModbusWriteValue} from '../write-value/modbus-write-value';

export class ModbusWrite {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusWrite';
  }
  '@class' = ModbusWrite.TYPE;
  address: string;
  type: string;
  valueType: string;
  factorToValue: number;
  writeValues: ModbusWriteValue[];

  public constructor(init?: Partial<ModbusWrite>) {
    Object.assign(this, init);
  }

  public static createWithSingleChild() {
    return new ModbusWrite({valueType: 'Integer', writeValues: [new ModbusWriteValue()]});
  }
}
