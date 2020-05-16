import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {DayTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/day/day-timeframe';
import {TimeOfDay} from '../../../../../main/angular/src/app/schedule/time-of-day';
import {EnergyRequest} from '../../../../../main/angular/src/app/schedule/request/energy/energy-request';

export const energyRequest_dayTimeframe_nighthly: Schedule[] = [
  new Schedule({
    enabled: true,
    timeframe: new DayTimeframe({
      start: new TimeOfDay(0, 30, 0),
      end: new TimeOfDay(6, 0, 0),
      daysOfWeekValues: [1, 2, 3, 4, 5]
    }),
    request: new EnergyRequest({max: 20000}),
  }),
];
