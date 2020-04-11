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

import {Injectable} from '@angular/core';
import {Appliance} from './appliance';
import {Observable} from 'rxjs';
import {ApplianceHeader} from './appliance-header';
import {SaeService} from '../shared/sae-service';
import {HttpClient} from '@angular/common/http';
import {Logger} from '../log/logger';
import {map} from 'rxjs/operators';

@Injectable()
export class ApplianceService extends SaeService {

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
  }

  getApplianceHeaders(): Observable<Array<ApplianceHeader>> {
    return this.http.get(`${SaeService.API}/appliances`)
      .pipe(map((applianceHeaders: Array<ApplianceHeader>) => applianceHeaders));
  }

  getAppliance(id: string): Observable<Appliance> {
    return this.http.get(`${SaeService.API}/appliance?id=${id}`)
      .pipe(map((appliance: Appliance) => appliance));
  }

  updateAppliance(appliance: Appliance, create: boolean): Observable<any> {
    const url = `${SaeService.API}/appliance?id=${appliance.id}&create=${create}`;
    this.logger.debug('Updating appliance using ' + url);
    return this.http.put(url, appliance,
      {headers: this.headersContentTypeJson, responseType: 'text'});
  }

  deleteAppliance(id: string): Observable<any> {
    const url = `${SaeService.API}/appliance?id=${id}`;
    this.logger.debug('Delete appliance using ' + url);
    return this.http.delete(url, {responseType: 'text'});
  }
}
