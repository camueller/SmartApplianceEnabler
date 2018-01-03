import {Injectable} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ScheduleFactory} from './schedule-factory';
import {Schedule} from './schedule';
import {Observable} from 'rxjs/Observable';
import {HttpClient} from '@angular/common/http';
import {Logger} from '../log/logger';

@Injectable()
export class ScheduleService extends SaeService {

  scheduleFactory: ScheduleFactory;

  constructor(private logger: Logger,
              protected http: HttpClient) {
    super(http);
    this.scheduleFactory = new ScheduleFactory(logger);
  }

  getSchedules(id: string): Observable<Array<Schedule>> {
    return this.http.get(`${SaeService.API}/schedules?id=${id}`)
      .map((schedules: Array<Schedule>) => {
        if (schedules.toString() === '') {
          return new Array<Schedule>();
        }
        return schedules.map(schedule => this.scheduleFactory.toSchedule(schedule));
      });
  }

  setSchedules(id: string, schedules: Schedule[]): Observable<any> {
    const url = `${SaeService.API}/schedules?id=${id}`;
    const content = this.scheduleFactory.toJSON(schedules);
    this.logger.debug('Set schedule using ' + url);
    return this.http.put(url, content, {headers: this.headersContentTypeJson, responseType: 'text'});
  }
}
