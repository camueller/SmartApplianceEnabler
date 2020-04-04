export class ModbusReadValue {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusReadValue';
  }
  '@class' = ModbusReadValue.TYPE;
  name: string;
  extractionRegex: string;

  public constructor(init?: Partial<ModbusReadValue>) {
    Object.assign(this, init);
  }
}
