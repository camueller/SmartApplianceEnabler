export class ModbusRegisterConfguration {
  name: string;
  value: any;
  registerAddress: string;
  bytes: number;
  registerType: string;
  write: boolean;
  extractionRegex: string;

  public constructor(init?: Partial<ModbusRegisterConfguration>) {
    Object.assign(this, init);
  }
}
