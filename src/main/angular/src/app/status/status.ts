import {EvStatus} from './ev-status';

export class Status {
  id: string;
  name: string;
  type: string;
  vendor: string;
  runningTime: number;
  remainingMinRunningTime: number;
  remainingMaxRunningTime: number;
  plannedEnergyAmount: number;
  remainingMinEnergyAmount: number;
  remainingMaxEnergyAmount: number;
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
  evStatuses: EvStatus[];

  public constructor(init?: Partial<Status>) {
    Object.assign(this, init);
  }
}
