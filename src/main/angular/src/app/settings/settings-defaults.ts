export class SettingsDefaults {
  holidaysUrl: string;
  modbusTcpHost: string;
  modbusTcpPort: number;
  modbusReadRegisterTypes: string[];
  modbusWriteRegisterTypes: string[];
  byteOrders: string[];
  pulseReceiverPort: number;

  public constructor(init?: Partial<SettingsDefaults>) {
    Object.assign(this, init);
  }
}
