import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {SaeService} from '../shared/sae-service';
import {ScheduleFactory} from './schedule-factory';
import {Schedule} from './schedule';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class ScheduleService extends SaeService {

  constructor(protected http: Http) {
    super(http);
  }

  getSchedules(id: string): Observable<Array<Schedule>> {
    return this.http.get(`${this.api}/schedules?id=${id}`)
      .map(response => response.json())
      .map(rawSchedules => rawSchedules.map(rawSchedule => ScheduleFactory.toSchedule(rawSchedule)))
      .catch(this.errorHandler);
  }

  setSchedules(id: string, schedules: Schedule[]) { // : Observable<any> {
    const url = `${this.api}/schedules?id=${id}`;
    const content = ScheduleFactory.toJSON(schedules);
    console.log('Set schedule using ' + url);
    console.log('Content: ' + content);
    this.http.put(url, content, {headers: this.headers})
      .catch(this.errorHandler)
      .subscribe(res => console.log(res));
  }
}
