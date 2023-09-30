import { ActivatedRouteSnapshot } from '@angular/router';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {Schedule} from './schedule';
import {ScheduleService} from './schedule-service';

@Injectable()
export class ScheduleResolver  {

  constructor(private scheduleService: ScheduleService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<Schedule[]> {
    return this.scheduleService.getSchedules(route.params['id']);
  }
}
