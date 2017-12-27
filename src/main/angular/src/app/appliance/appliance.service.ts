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
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import {ApplianceFactory} from './appliance-factory';
import {ApplianceHeader} from './appliance-header';
import {SaeService} from '../shared/sae-service';
import {ApplianceStatus} from './appliance-status';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class ApplianceService extends SaeService {

  constructor(protected http: HttpClient) {
    super(http);
  }

  getApplianceHeaders(): Observable<Array<ApplianceHeader>> {
    return this.http.get(`${SaeService.api}/appliances`)
      .map((applianceHeaders: Array<ApplianceHeader>) => {
        return applianceHeaders.map(
          applianceHeader => ApplianceFactory.toApplianceHeaderFromJSON(applianceHeader));
      });
  }

  getApplianceStatus(): Observable<Array<ApplianceStatus>> {
    return this.http.get(`${SaeService.api}/status`)
      .map((applianceStatuses: Array<any>) => {
        return applianceStatuses.map(
          applianceStatus => ApplianceFactory.toApplianceStatusFromJSON(applianceStatus));
      });
  }

  getAppliance(id: string): Observable<Appliance> {
    return this.http.get(`${SaeService.api}/appliance?id=${id}`)
      .map(applianceInfo => ApplianceFactory.toApplianceFromJSON(applianceInfo));
  }

  updateAppliance(appliance: Appliance, create: boolean): Observable<any> {
    const url = `${SaeService.api}/appliance?id=${appliance.id}&create=${create}`;
    const content = ApplianceFactory.toJSONfromApplianceInfo(appliance);
    console.log('Updating appliance using ' + url);
    return this.http.put(url, content,
      {headers: this.headersContentTypeJson, responseType: 'text'});
  }

  deleteAppliance(id: string): Observable<any> {
    const url = `${SaeService.api}/appliance?id=${id}`;
    console.log('Delete appliance using ' + url);
    return this.http.delete(url, {responseType: 'text'});
  }

  suggestRuntime(id: string): Observable<string> {
    const url = `${SaeService.api}/runtime?id=${id}`;
    console.log('Get suggested runtime using ' + url);
    return this.http.get(url, {responseType: 'text'});
  }

  setRuntime(id: string, runtime: number): Observable<any> {
    const url = `${SaeService.api}/runtime?id=${id}&runtime=${runtime}`;
    console.log('Set runtime using ' + url);
    return this.http.put(url, '', {responseType: 'text'});
  }

  toggleAppliance(id: string, turnOn: boolean): Observable<any> {
    const url = `${this.sempApi}`;
    const content = '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl>' +
      '<DeviceId>' + id + '</DeviceId><On>' + turnOn + '</On></DeviceControl></EM2Device>';
    console.log('Toggle appliance using ' + url);
    console.log('Content: ' + content);
    return this.http.post(url, content,
      {headers: this.headersContentTypeXml, responseType: 'text'});
  }
}
