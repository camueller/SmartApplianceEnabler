import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {Schedule} from './schedule';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Logger} from '../log/logger';
import {map} from 'rxjs/operators';

@Injectable()
export class ScheduleService extends SaeService {

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
  }

  getSchedules(id: string): Observable<Array<Schedule>> {
    return this.http.get(`${SaeService.API}/schedules?id=${id}`)
      .pipe(map((schedules: Array<Schedule>) => {
        if (!schedules) {
          return new Array<Schedule>();
        }
        return schedules.map(schedule => (schedule as Schedule));
      }));
  }

  setSchedules(id: string, schedules: Schedule[]): Observable<any> {
    const url = `${SaeService.API}/schedules?id=${id}`;
    this.logger.debug('Set schedule using ' + url);
    return this.http.put(url, schedules, {headers: this.headersContentTypeJson, responseType: 'text'});
  }
}
