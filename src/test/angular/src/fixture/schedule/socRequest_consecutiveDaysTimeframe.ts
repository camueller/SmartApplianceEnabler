import {Schedule} from '../../../../../main/angular/src/app/schedule/schedule';
import {ConsecutiveDaysTimeframe} from '../../../../../main/angular/src/app/schedule/timeframe/consecutivedays/consecutive-days-timeframe';
import {TimeOfDayOfWeek} from '../../../../../main/angular/src/app/schedule/time-of-day-of-week';
import {SocRequest} from '../../../../../main/angular/src/app/schedule/request/soc/soc-request';

export const socRequest_consecutiveDaysTimeframe: Schedule[] = [
  new Schedule({
    enabled: true,
    timeframe: new ConsecutiveDaysTimeframe({
      start: new TimeOfDayOfWeek(5, 16, 0, 0),
      end: new TimeOfDayOfWeek(7, 18, 0, 0),
    }),
    request: new SocRequest({evId: 1, soc: 90}),
  }),
];
