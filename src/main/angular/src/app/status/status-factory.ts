import {Status} from './status';
import {Logger} from '../log/logger';
import {EvStatus} from './ev-status';
import {noUndefined} from '@angular/compiler/src/util';

export class StatusFactory {

  constructor(private logger: Logger) {
  }

  toStatusFromJSON(rawApplianceStatus: any): Status {
    this.logger.debug('Status (JSON)' + JSON.stringify(rawApplianceStatus));
    const applianceStatus = new Status(... rawApplianceStatus);
    applianceStatus.evStatuses = this.toEvStatuses(rawApplianceStatus.evStatus);
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
    return new EvStatus(... rawEVStatus);
  }

}
