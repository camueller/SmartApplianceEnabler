import {Status} from './status';
import {Logger} from '../log/logger';

export class StatusFactory {

  constructor(private logger: Logger) {
  }

  toStatusFromJSON(rawApplianceStatus: any): Status {
    this.logger.debug('Status (JSON)' + JSON.stringify(rawApplianceStatus));
    const applianceStatus = new Status(... rawApplianceStatus);
    this.logger.debug('Status (TYPE)' + JSON.stringify(applianceStatus));
    return applianceStatus;
  }
}
