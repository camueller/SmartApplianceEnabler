export class ModbusWriteValue {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusWriteValue';
  }
  '@class' = ModbusWriteValue.TYPE;
  name: string;
  value: string;

  public constructor(init?: Partial<ModbusWriteValue>) {
    Object.assign(this, init);
  }
}
