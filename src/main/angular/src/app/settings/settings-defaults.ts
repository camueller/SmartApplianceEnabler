export class SettingsDefaults {
  mqttHost: string;
  mqttPort: number;
  mqttRootTopic: string;
  nodeRedDashboardUrl: string;
  holidaysUrl: string;
  modbusTcpHost: string;
  modbusTcpPort: number;

  public constructor(init?: Partial<SettingsDefaults>) {
    Object.assign(this, init);
  }
}
