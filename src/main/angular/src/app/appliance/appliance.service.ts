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
import {Http} from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import {ApplianceFactory} from './appliance-factory';
import {Subject} from 'rxjs/Subject';
import {ApplianceHeader} from './appliance-header';
import {SaeService} from '../shared/sae-service';
import {ApplianceStatus} from './appliance-status';

@Injectable()
export class ApplianceService extends SaeService {

  constructor(protected http: Http) {
    super(http);
  }

  getApplianceHeaders(): Observable<Array<ApplianceHeader>> {
    return this.http.get(`${this.api}/appliances`)
      .map(response => response.json())
      .map(applianceHeaders => applianceHeaders.map(applianceHeader => ApplianceFactory.toApplianceHeaderFromJSON(applianceHeader)))
      .catch(this.errorHandler);
  }

  getApplianceStatus(): Observable<Array<ApplianceStatus>> {
    return this.http.get(`${this.api}/status`)
      .map(response => response.json())
      .map(applianceStatuses => applianceStatuses.map(applianceStatus => ApplianceFactory.toApplianceStatusFromJSON(applianceStatus)))
      .catch(this.errorHandler);
  }

  getAppliance(id: string): Observable<Appliance> {
    return this.http.get(`${this.api}/appliance?id=${id}`)
      .map(response => response.json())
      .map(applianceInfo => ApplianceFactory.toApplianceFromJSON(applianceInfo))
      .catch(this.errorHandler);
  }

  updateAppliance(appliance: Appliance, create: boolean): Observable<any> {
    const url = `${this.api}/appliance?id=${appliance.id}&create=${create}`;
    const content = ApplianceFactory.toJSONfromApplianceInfo(appliance);
    console.log('Updating applianceHeader using ' + url);
    console.log('Content: ' + content);
    const observer = new Subject();
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => {
        console.log(res);
        observer.next();
      });
    return observer;
  }

  deleteAppliance(id: string): Observable<any> {
    const url = `${this.api}/appliance?id=${id}`;
    console.log('Delete applianceHeader using ' + url);
    const observer = new Subject();
    this.http.delete(url, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => {
        console.log(res);
        observer.next();
      });
    return observer;
  }

  suggestRuntime(id: string): Observable<number> {
    const url = `${this.api}/runtime?id=${id}`;
    console.log('Get suggested runtime using ' + url);
    return this.http.get(url, {headers: this.headers})
      .map(response => response.json())
      .catch(this.errorHandler);
  }

  setRuntime(id: string, runtime: number): Observable<any> {
    const url = `${this.api}/runtime?id=${id}&runtime=${runtime}`;
    console.log('Set runtime using ' + url);
    const observer = new Subject();
    this.http.put(url, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => {
        console.log(res);
        observer.next();
      });
    return observer;
  }

  toggleAppliance(id: string, turnOn: boolean): Observable<any> {
    const url = `${this.sempApi}`;
    const content = '<EM2Device xmlns="http://www.sma.de/communication/schema/SEMP/v1"><DeviceControl>' +
      '<DeviceId>' + id + '</DeviceId><On>' + turnOn + '</On></DeviceControl></EM2Device>';
    console.log('Toggle appliance using ' + url);
    console.log('Content: ' + content);
    const observer = new Subject();
    this.http.post(url, content, {headers: this.sempHeaders})
      .catch(this.errorHandler)
      .subscribe(res => {
        console.log(res);
        observer.next();
      });
    return observer;
  }
}
