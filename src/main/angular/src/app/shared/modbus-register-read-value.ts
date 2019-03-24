export class ModbusRegisterReadValue {
  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusRegisterReadValue';
  }

  '@class' = ModbusRegisterReadValue.TYPE;
  name: string;
  extractionRegex: string;

  public constructor(init?: Partial<ModbusRegisterReadValue>) {
    Object.assign(this, init);
  }

}
