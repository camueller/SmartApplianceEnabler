export class EvStatus {
  id: string;
  name: string;
  batteryCapacity: number;
  defaultEnergyCharge: number;
  stateOfCharge: number;

  public constructor(init?: Partial<EvStatus>) {
    Object.assign(this, init);
  }
}
