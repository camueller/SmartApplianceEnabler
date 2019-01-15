export class EvStatus {
  id: number;
  name: string;
  stateOfCharge: number;

  public constructor(init?: Partial<EvStatus>) {
    Object.assign(this, init);
  }
}
