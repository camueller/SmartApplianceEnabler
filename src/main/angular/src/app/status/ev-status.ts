export class EvStatus {
  id: number;
  name: string;
  defaultEnergyCharge: number;
  stateOfCharge: number;

  public constructor(init?: Partial<EvStatus>) {
    Object.assign(this, init);
  }
}
