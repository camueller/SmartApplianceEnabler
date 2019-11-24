import {Schedule} from './schedule';
import {DayTimeframe} from '../schedule-timeframe-day/day-timeframe';
import {ConsecutiveDaysTimeframe} from '../schedule-timeframe-consecutivedays/consecutive-days-timeframe';
import {RuntimeRequest} from '../schedule-request-runtime/runtime-request';

export class ScheduleTestdata {

  public static daytimeframe12345_json(pure: boolean): any {
    const schedule = {};
    schedule['@class'] = 'de.avanux.smartapplianceenabler.schedule.Schedule';
    schedule['enabled'] = true;
    if (!pure) {
      schedule['requestType'] = 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest';
    }
    schedule['request'] = {
      '@class': 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest',
      'min': 3600,
      'minHHMM': '01:00'
    };

    const timeframe = {};
    timeframe['@class'] = 'de.avanux.smartapplianceenabler.schedule.DayTimeframe';
    if (!pure) {
      timeframe['daysOfWeekValues'] = [1, 2, 3, 4, 5];
      timeframe['startTime'] = '06:00';
      timeframe['endTime'] = '07:30';
    }
    timeframe['start'] = {
      '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDay',
      'hour': 6,
      'minute': 0,
      'second': 0
    };
    timeframe['end'] = {
      '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDay',
      'hour': 7,
      'minute': 30,
      'second': 0
    };
    timeframe['daysOfWeek'] = [
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
    ];
    if (!pure) {
      schedule['timeframeType'] = 'de.avanux.smartapplianceenabler.schedule.DayTimeframe';
    }
    schedule['timeframe'] = timeframe;
    return schedule;
  }

  public static daytimeframe12345_type(): Schedule {
    return new Schedule({
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      // enabled: true,
      // requestType: 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest',
      // runtimeRequest: new RuntimeRequest({
      //   '@class': 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest',
      //   min: 3600,
      //   max: undefined,
      // }),
      // timeframeType: 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
      // dayTimeframe: new DayTimeframe({
      //   '@class': 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
      //   daysOfWeekValues: [1, 2, 3, 4, 5],
      //   startTime: '06:00',
      //   endTime: '07:30'
      // })
    });
  }

  public static consecutiveDaysTimeframe567_json(pure: boolean): any {
    const schedule = {};
    schedule['@class'] = 'de.avanux.smartapplianceenabler.schedule.Schedule';
    schedule['enabled'] = true;
    if (!pure) {
      schedule['requestType'] = 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest';
    }
    schedule['request'] = {
      '@class': 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest',
      'min': 3600,
      'minHHMM': '01:00'
    };

    const timeframe = {};
    timeframe['@class'] = 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe';
    if (!pure) {
      timeframe['startTime'] = '06:00';
      timeframe['endTime'] = '07:30';
    }
    timeframe['start'] = {
      '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek',
      'dayOfWeek': 5,
      'hour': 18,
      'minute': 0,
      'second': 0
    };
    timeframe['end'] = {
      '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek',
      'dayOfWeek': 7,
      'hour': 20,
      'minute': 0,
      'second': 0
    };
    if (!pure) {
      schedule['timeframeType'] = 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe';
    }
    schedule['timeframe'] = timeframe;
    return schedule;
  }

  public static consecutiveDaysTimeframe567_type(): Schedule {
    return new Schedule({
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      // enabled: true,
      // requestType: 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest',
      // runtimeRequest: new RuntimeRequest({
      //   '@class': 'de.avanux.smartapplianceenabler.schedule.RuntimeRequest',
      //   min: 3600,
      //   max: undefined
      // }),
      // timeframeType: 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe',
      // timeframe: new ConsecutiveDaysTimeframe({
      //   '@class': 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe',
      //   startDayOfWeek: 5,
      //   startTime: '18:00',
      //   endDayOfWeek: 7,
      //   endTime: '20:00'
      // })
    });
  }
}
