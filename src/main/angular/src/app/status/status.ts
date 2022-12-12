export class Status {
  id: string;
  name: string;
  type: string;
  vendor: string;
  runningTime: number;
  remainingMinRunningTime: number;
  remainingMaxRunningTime: number;
  remainingMinEnergy: number;
  remainingMaxEnergy: number;
  plannedEnergyAmount: number;
  chargedEnergyAmount: number;
  currentChargePower: number;
  planningRequested: boolean;
  earliestStart: number;
  latestStart: number;
  latestEnd: number;
  on: boolean;
  controllable: boolean;
  interruptedSince: number;
  optionalEnergy: boolean;
  evIdCharging: number;
  state: string;
  stateLastChangedTimestamp: number;
  soc: number;
  socInitial: number;
  socTarget: number;
  socInitialTimestamp: number;


  public constructor(init?: Partial<Status>) {
    Object.assign(this, init);
  }
}
