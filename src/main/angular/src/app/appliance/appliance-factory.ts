/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import {Appliance} from './appliance';
import {ApplianceHeader} from './appliance-header';
import {Logger} from '../log/logger';
import {stringyfyWithoutFalsyNumbers} from '../shared/json-util';

export class ApplianceFactory {

  constructor(private logger: Logger) {
  }

  createEmptyAppliance(): Appliance {
    return new Appliance();
  }

  toApplianceHeaderFromJSON(rawApplianceHeader: any): ApplianceHeader {
    this.logger.debug('ApplianceHeader (JSON)' + JSON.stringify(rawApplianceHeader));
    const applianceHeader = new ApplianceHeader(...rawApplianceHeader);
    this.logger.debug('ApplianceHeader (TYPE)' + JSON.stringify(applianceHeader));
    return applianceHeader;
  }

  toApplianceFromJSON(applianceInfo: any): Appliance {
    this.logger.debug('Appliance (JSON)' + JSON.stringify(applianceInfo));
    const appliance = new Appliance(...applianceInfo);
    this.logger.debug('Appliance (TYPE)' + JSON.stringify(appliance));
    return appliance;
  }

  toJSONfromApplianceInfo(appliance: Appliance): String {
    this.logger.debug('Appliance (TYPE)' + JSON.stringify(appliance));
    // const clone = JSON.parse(JSON.stringify(appliance));
    // Object.keys(clone).forEach((key) => {
    //   this.logger.debug(`key=${key} clone.key=${clone[key]}`);
    //   if(clone[key] === null || clone[key] === '') {
    //     delete clone[key];
    //   }
    // });
    // const json = JSON.stringify(clone);
    const json = stringyfyWithoutFalsyNumbers(appliance);
    this.logger.debug('Appliance (JSON)' + json);
    return json;
  }
}
