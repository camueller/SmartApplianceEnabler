export class ControlDefaults {
  startingCurrentSwitchDefaults_powerThreshold: number;
  startingCurrentSwitchDefaults_startingCurrentDetectionDuration: number;
  startingCurrentSwitchDefaults_finishedCurrentDetectionDuration: number;
  startingCurrentSwitchDefaults_minRunningTime: number;
  electricVehicleChargerDefaults_voltage: number;
  electricVehicleChargerDefaults_phases: number;
  electricVehicleChargerDefaults_chargeLoss: number;
  electricVehicleChargerDefaults_pollInterval: number;
  electricVehicleChargerDefaults_startChargingStateDetectionDelay: number;
  electricVehicleChargerDefaults_forceInitialCharging: boolean;

  public constructor(init?: Partial<ControlDefaults>) {
    Object.assign(this, init);
  }
}
