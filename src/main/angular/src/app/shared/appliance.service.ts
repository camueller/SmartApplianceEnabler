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
import {Schedule} from './schedule';
import {Settings} from './settings';
import {Headers, Http} from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {ScheduleFactory} from './schedule-factory';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import {ApplianceFactory} from './appliance-factory';
import {Control} from './control';
import {ControlFactory} from './control-factory';
import {Meter} from './meter';
import {MeterFactory} from './meter-factory';
import {SettingsFactory} from './settings-factory';
import {AppliancesReloadService} from './appliances-reload-service';
import {observable} from 'rxjs/symbol/observable';
import {Subject} from 'rxjs/Subject';

@Injectable()
export class ApplianceService {
  private api = 'http://localhost:8080/sae';
  private headers: Headers = new Headers();

  constructor(private http: Http) {
    this.headers.append('Content-Type', 'application/json');
  }

  getAppliances(): Observable<Array<Appliance>> {
    return this.http.get(`${this.api}/appliances`)
      .map(response => response.json())
      .map(applianceInfos => applianceInfos.map(applianceInfo => ApplianceFactory.fromApplianceInfo(applianceInfo)))
      .catch(this.errorHandler);
  }

  getAppliance(id: string): Observable<Appliance> {
    return this.http.get(`${this.api}/appliance?id=${id}`)
      .map(response => response.json())
      .map(applianceInfo => ApplianceFactory.fromApplianceInfo(applianceInfo))
      .catch(this.errorHandler);
  }

  updateAppliance(appliance: Appliance, create: boolean): Observable<any> {
    const url = `${this.api}/appliance?id=${appliance.id}&create=${create}`;
    const content = ApplianceFactory.toJSON(appliance);
    console.log('Updating appliance using ' + url);
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
    console.log('Delete appliance using ' + url);
    const observer = new Subject();
    this.http.delete(url, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => {
        console.log(res);
        observer.next();
      });
    return observer;
  }

  getControl(id: string): Observable<Control> {
    return this.http.get(`${this.api}/control?id=${id}`)
      .map(response => {
        if (response['_body'].length > 0) {
          return ControlFactory.fromJSON(response.json());
        }
        return ControlFactory.createEmptyControl();
      })
      .catch(this.errorHandler);
  }

  updateControl(control: Control, id: string) {
    const url = `${this.api}/control?id=${id}`;
    const content = ControlFactory.toJSON(control);
    console.log('Update control using ' + url);
    console.log('Content: ' + content);
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => console.log(res));
  }

  getMeter(id: string): Observable<Meter> {
    return this.http.get(`${this.api}/meter?id=${id}`)
      .map(response => {
        if (response['_body'].length > 0) {
          return MeterFactory.fromJSON(response.json());
        }
        return MeterFactory.createEmptyMeter();
      })
      .catch(this.errorHandler);
  }

  updateMeter(meter: Meter, id: string) {
    const url = `${this.api}/meter?id=${id}`;
    const content = MeterFactory.toJSON(meter);
    console.log('Update meter using ' + url);
    console.log('Content: ' + content);
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => console.log(res));
  }

  getSchedules(id: string): Observable<Array<Schedule>> {
    return this.http.get(`${this.api}/schedules?id=${id}`)
      .map(response => response.json())
      .map(rawSchedules => rawSchedules.map(rawSchedule => ScheduleFactory.fromObject(rawSchedule)))
      .catch(this.errorHandler);
  }

  setSchedules(id: string, schedules: Schedule[]) { // : Observable<any> {
    const url = `${this.api}/schedules?id=${id}`;
    const content = ScheduleFactory.toJSON(schedules);
    console.log('Set schedules using ' + url);
    console.log('Content: ' + content);
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => console.log(res));
  }

  getSettings(): Observable<Settings> {
    return this.http.get(`${this.api}/settings`)
      .map(response => response.json())
      .map(settings => SettingsFactory.fromJSON(settings))
      .catch(this.errorHandler);
  }

  updateSettings(settings: Settings) {
    const url = `${this.api}/settings`;
    const content = SettingsFactory.toJSON(settings);
    console.log('Update settings using ' + url);
    console.log('Content: ' + content);
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => console.log(res));
  }

  private errorHandler(error: Error | any): Observable<any> {
    console.error(error);
    return Observable.throw(error);
  }
}
