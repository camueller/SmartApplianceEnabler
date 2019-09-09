export class ModbusWriteValue {
  name: string;
  value: string;

  public constructor(init?: Partial<ModbusWriteValue>) {
    Object.assign(this, init);
  }
}
