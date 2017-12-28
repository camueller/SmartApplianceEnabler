export class ControlDefaults {
  startingCurrentSwitchDefaults_powerThreshold: number;
  startingCurrentSwitchDefaults_startingCurrentDetectionDuration: number;
  startingCurrentSwitchDefaults_finishedCurrentDetectionDuration: number;
  startingCurrentSwitchDefaults_minRunningTime: number;

  public constructor(init?: Partial<ControlDefaults>) {
    Object.assign(this, init);
  }
}
