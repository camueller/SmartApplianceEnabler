import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {RuntimeRequest} from '../../../../../main/angular/src/app/schedule/request/runtime/runtime-request';
import {DayTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/day/day-timeframe';

export const runtimeRequest_dayTimeframe_weekday_weekend: Schedule[] = [
  new Schedule({
    enabled: true,
    timeframe: new DayTimeframe({
      startTime: '11:00',
      endTime: '17:00',
      daysOfWeekValues: [1, 2, 3, 4, 5]
    }),
    request: new RuntimeRequest({max: 9000}),
  }),
  new Schedule({
    enabled: true,
    timeframe: new DayTimeframe({
      startTime: '09:00',
      endTime: '13:00',
      daysOfWeekValues: [6, 7, 8]
    }),
    request: new RuntimeRequest({max: 9000}),
  })
];
