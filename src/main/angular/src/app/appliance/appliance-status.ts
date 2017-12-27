export class ApplianceStatus {
  id: string;
  name: string;
  type: string;
  vendor: string;
  runningTime: number;
  remainingMinRunningTime: number;
  remainingMaxRunningTime: number;
  planningRequested: boolean;
  earliestStart: number;
  latestStart: number;
  on: boolean;
  controllable: boolean;
  interruptedSince: number;

  public constructor(init?: Partial<ApplianceStatus>) {
    Object.assign(this, init);
  }
}
