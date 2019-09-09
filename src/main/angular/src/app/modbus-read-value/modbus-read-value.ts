export class ModbusReadValue {
  name: string;
  extractionRegex: string;

  public constructor(init?: Partial<ModbusReadValue>) {
    Object.assign(this, init);
  }
}
