export class ModbusRegisterConfguration {
  enabled: boolean;
  name: string;
  value: any;
  address: string;
  bytes: number;
  byteOrder: string;
  type: string;
  write: boolean;
  extractionRegex: string;
  factorToValue: number;

  public constructor(init?: Partial<ModbusRegisterConfguration>) {
    Object.assign(this, init);
  }
}
