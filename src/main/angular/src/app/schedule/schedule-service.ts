import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ScheduleFactory} from './schedule-factory';
import {Schedule} from './schedule';
import {Observable} from 'rxjs/Observable';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class ScheduleService extends SaeService {

  constructor(protected http: HttpClient) {
    super(http);
  }

  getSchedules(id: string): Observable<Array<Schedule>> {
    return this.http.get(`${SaeService.API}/schedules?id=${id}`)
      .map((schedules: Array<Schedule>) => {
        if (schedules == null) {
          return new Array();
        }
        return schedules.map(schedule => ScheduleFactory.toSchedule(schedule));
      });
  }

  setSchedules(id: string, schedules: Schedule[]): Observable<any> {
    const url = `${SaeService.API}/schedules?id=${id}`;
    const content = ScheduleFactory.toJSON(schedules);
    console.log('Set schedule using ' + url);
    return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
  }
}
