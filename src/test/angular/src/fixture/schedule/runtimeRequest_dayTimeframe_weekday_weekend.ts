import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {RuntimeRequest} from '../../../../../main/angular/src/app/schedule/request/runtime/runtime-request';
import {DayTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/day/day-timeframe';
import {TimeOfDay} from '../../../../../main/angular/src/app/schedule/time-of-day';

export const runtimeRequest_dayTimeframe_weekday_weekend: Schedule[] = [
  new Schedule({
    enabled: true,
    timeframe: new DayTimeframe({
      start: new TimeOfDay(11, 0, 0),
      end: new TimeOfDay(17, 0, 0),
      daysOfWeekValues: [1, 2, 3, 4, 5]
    }),
    request: new RuntimeRequest({max: 9000}),
  }),
  new Schedule({
    enabled: true,
    timeframe: new DayTimeframe({
      start: new TimeOfDay(9, 0, 0),
      end: new TimeOfDay(13, 0, 0),
      daysOfWeekValues: [6, 7, 8]
    }),
    request: new RuntimeRequest({max: 9000}),
  })
];
