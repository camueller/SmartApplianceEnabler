export class ApplianceStatus {
  id: string;
  name: string;
  type: string;
  vendor: string;
  statusChangedAt: string;
  remainingMinRunningTime: string;
  remainingMaxRunningTime: string;
  planningRequested: boolean;
  earliestStartPassed: boolean;
  on: boolean;
  controllable: boolean;
}
