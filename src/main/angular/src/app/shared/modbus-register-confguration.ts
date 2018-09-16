export class ModbusRegisterConfguration {
  name: string;
  value: any;
  address: string;
  bytes: number;
  byteOrder: string;
  type: string;
  write: boolean;
  extractionRegex: string;
  factorToValue: string;

  public constructor(init?: Partial<ModbusRegisterConfguration>) {
    Object.assign(this, init);
  }
}
