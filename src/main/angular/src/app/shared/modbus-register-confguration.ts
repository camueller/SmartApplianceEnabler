export class ModbusRegisterConfguration {
  name: string;
  value: any;
  address: string;
  bytes: number;
  type: string;
  write: boolean;
  extractionRegex: string;

  public constructor(init?: Partial<ModbusRegisterConfguration>) {
    Object.assign(this, init);
  }
}
