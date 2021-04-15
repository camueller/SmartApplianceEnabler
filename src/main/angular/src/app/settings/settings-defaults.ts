export class SettingsDefaults {
  holidaysUrl: string;
  modbusTcpHost: string;
  modbusTcpPort: number;

  public constructor(init?: Partial<SettingsDefaults>) {
    Object.assign(this, init);
  }
}
