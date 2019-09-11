/**
 * @Deprecated
 */
export class ModbusRegisterWriteValue {

  static get TYPE(): string {
    return 'de.avanux.smartapplianceenabler.modbus.ModbusRegisterWriteValue';
  }

  '@class' = ModbusRegisterWriteValue.TYPE;
  name: string;
  value: string;

  public constructor(init?: Partial<ModbusRegisterWriteValue>) {
    Object.assign(this, init);
  }
}
