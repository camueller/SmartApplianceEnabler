export class MeterDefaults {
  s0ElectricityMeter_measurementInterval: number;
  modbusElectricityMeter_pollInterval: number;
  modbusElectricityMeter_measurementInterval: number;
  httpElectricityMeter_factorToWatt: number;
  httpElectricityMeter_measurementInterval: number;
  httpElectricityMeter_pollInterval: number;

  public constructor(init?: Partial<MeterDefaults>) {
    Object.assign(this, init);
  }
}
