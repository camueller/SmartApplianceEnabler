import {Schedule} from './schedule';
import {DayTimeframe} from './day-timeframe';

export class ScheduleTestdata {

  public static daytimeframe12345_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      'enabled': true,
      'id': null,
      'minRunningTime': 3600,
      'maxRunningTime': null,
      'timeframe': {
        '@class': 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
        'daysOfWeek': [
          {
            '@class': 'de.avanux.smartapplianceenabler.schedule.DayOfWeek',
            'value': 1
          },
          {
            '@class': 'de.avanux.smartapplianceenabler.schedule.DayOfWeek',
            'value': 2
          },
          {
            '@class': 'de.avanux.smartapplianceenabler.schedule.DayOfWeek',
            'value': 3
          },
          {
            '@class': 'de.avanux.smartapplianceenabler.schedule.DayOfWeek',
            'value': 4
          },
          {
            '@class': 'de.avanux.smartapplianceenabler.schedule.DayOfWeek',
            'value': 5
          }
        ],
        'end': {
          '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDay',
          'hour': 7,
          'minute': 30,
          'second': 0
        },
        'start': {
          '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDay',
          'hour': 6,
          'minute': 0,
          'second': 0
        }
      }
    };
  }

  public static daytimeframe12345__type(): Schedule {
    return new Schedule({
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      'enabled': true,
      'minRunningTime': 3600,
      'minRunningTimeHHMM': '01:00',
      'maxRunningTime': null,
      'timeframeType': 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
      'dayTimeframe': new DayTimeframe({
        '@class': 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
        'daysOfWeekValues': [1, 2, 3, 4, 5],
        'startTime': '06:00',
        'endTime': '07:30'
      })
    });
  }
}
