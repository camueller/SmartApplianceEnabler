export class SettingsDefaults {
  holidaysUrl: string;
  modbusTcpHost: string;
  modbusTcpPort: number;
  pulseReceiverPort: number;

  public constructor(init?: Partial<SettingsDefaults>) {
    Object.assign(this, init);
  }
}
