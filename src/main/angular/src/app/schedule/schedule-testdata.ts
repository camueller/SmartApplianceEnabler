import {Schedule} from './schedule';
import {DayTimeframe} from './day-timeframe';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';

export class ScheduleTestdata {

  public static daytimeframe12345_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      'enabled': true,
      'minRunningTime': 3600,
      'maxRunningTime': null,
      'timeframe': {
        '@class': 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
        'start': {
          '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDay',
          'hour': 6,
          'minute': 0,
          'second': 0
        },
        'end': {
          '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDay',
          'hour': 7,
          'minute': 30,
          'second': 0
        },
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
        ]
      }
    };
  }

  public static daytimeframe12345_json_put(): any {
    const json = this.daytimeframe12345_json();
    json['minRunningTimeHHMM'] = '01:00';
    json['timeframeType'] = 'de.avanux.smartapplianceenabler.schedule.DayTimeframe';
    const timeframe = json['timeframe'];
    timeframe['daysOfWeekValues'] = [1, 2, 3, 4, 5];
    timeframe['startTime'] = '06:00';
    timeframe['endTime'] = '07:30';
    return json;
  }

  public static daytimeframe12345_type(): Schedule {
    return new Schedule({
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      enabled: true,
      minRunningTime: 3600,
      minRunningTimeHHMM: '01:00',
      maxRunningTime: null,
      timeframeType: 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
      dayTimeframe: new DayTimeframe({
        '@class': 'de.avanux.smartapplianceenabler.schedule.DayTimeframe',
        daysOfWeekValues: [1, 2, 3, 4, 5],
        startTime: '06:00',
        endTime: '07:30'
      })
    });
  }

  public static consecutiveDaysTimeframe567_json(): any {
    return {
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      'enabled': true,
      'id': null,
      'minRunningTime': 3600,
      'maxRunningTime': null,
      'timeframe': {
        '@class': 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe',
        'end': {
          '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek',
          'dayOfWeek': 7,
          'hour': 20,
          'minute': 0,
          'second': 0
        },
        'start': {
          '@class': 'de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek',
          'dayOfWeek': 5,
          'hour': 18,
          'minute': 0,
          'second': 0
        }
      }
    };
  }

  public static consecutiveDaysTimeframe567_type(): Schedule {
    return new Schedule({
      '@class': 'de.avanux.smartapplianceenabler.schedule.Schedule',
      enabled: true,
      minRunningTime: 3600,
      minRunningTimeHHMM: '01:00',
      maxRunningTime: null,
      timeframeType: 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe',
      consecutiveDaysTimeframe: new ConsecutiveDaysTimeframe({
        '@class': 'de.avanux.smartapplianceenabler.schedule.ConsecutiveDaysTimeframe',
        startDayOfWeek: 5,
        startTime: '18:00',
        endDayOfWeek: 7,
        endTime: '20:00'
      })
    });
  }
}
