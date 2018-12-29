import {Status} from './status';
import {Logger} from '../log/logger';
import {EvStatus} from './ev-status';
import {noUndefined} from '@angular/compiler/src/util';

export class StatusFactory {

  constructor(private logger: Logger) {
  }

  toStatusFromJSON(rawApplianceHeader: any): Status {
    this.logger.debug('Status (JSON)' + JSON.stringify(rawApplianceHeader));
    const applianceStatus = new Status();
    applianceStatus.id = rawApplianceHeader.id;
    applianceStatus.name = rawApplianceHeader.name;
    applianceStatus.vendor = rawApplianceHeader.vendor;
    applianceStatus.type = rawApplianceHeader.type;
    applianceStatus.runningTime = rawApplianceHeader.runningTime;
    applianceStatus.remainingMinRunningTime = rawApplianceHeader.remainingMinRunningTime;
    applianceStatus.remainingMaxRunningTime = rawApplianceHeader.remainingMaxRunningTime;
    applianceStatus.earliestStart = rawApplianceHeader.earliestStart;
    applianceStatus.latestStart = rawApplianceHeader.latestStart;
    applianceStatus.planningRequested = rawApplianceHeader.planningRequested;
    applianceStatus.on = rawApplianceHeader.on;
    applianceStatus.controllable = rawApplianceHeader.controllable;
    applianceStatus.interruptedSince = rawApplianceHeader.interruptedSince;
    applianceStatus.optionalEnergy = rawApplianceHeader.optionalEnergy;
    applianceStatus.evStatuses = this.toEvStatuses(rawApplianceHeader.evStatus)
    this.logger.debug('Status (TYPE)' + JSON.stringify(applianceStatus));
    return applianceStatus;
  }

  toEvStatuses(rawEVStatuses: any[]): EvStatus[] {
    if (rawEVStatuses) {
      return rawEVStatuses.map((rawEVStatus: any) => this.toEvStatusFromJSON(rawEVStatus));
    }
    return undefined;
  }

  toEvStatusFromJSON(rawEVStatus: any): EvStatus {
    const evStatus = new EvStatus(... rawEVStatus);
    return evStatus;
  }

}
