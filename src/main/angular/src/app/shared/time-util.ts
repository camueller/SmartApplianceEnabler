import * as moment from 'moment';
import {Moment} from 'moment';
import {TimeOfDay} from '../schedule/time-of-day';
import {DayOfWeek} from '../schedule/day-of-week';
import { TimeOfDayOfWeek } from '../schedule/time-of-day-of-week';

export class TimeUtil {

  static toHourMinute(seconds: number): string {
    return new Date(seconds * 1000).toISOString().substring(11, 16);
  }

  static toHourMinuteWithUnits(seconds: number): string {
    const date = new Date(seconds * 1000).toISOString();
    let daysExtracted = Number.parseInt(date.substring(8, 10), 10) - 1;
    let hoursExtracted = Number.parseInt(date.substring(11, 13), 10);
    let minutesExtracted = Number.parseInt(date.substring(14, 16), 10);
    const secondsExtracted = Number.parseInt(date.substring(17, 19), 10);
    if (secondsExtracted > 59) {
      minutesExtracted++;
      if (minutesExtracted > 59) {
        hoursExtracted++;
        minutesExtracted = 0;
        if (hoursExtracted > 23) {
          daysExtracted++;
          hoursExtracted = 0;
        }
      }
    }
    let daysPrefix = '';
    if (daysExtracted > 0) {
      daysPrefix = daysExtracted + 'd ';
    }
    return daysPrefix + hoursExtracted + 'h ' + minutesExtracted + 'min';
  }

  static toSeconds(hhmmString: string): number {
    if (hhmmString != null) {
      const hhmm = hhmmString.split(':');
      return (+hhmm[0]) * 3600 + (+hhmm[1]) * 60;
    }
    return 0;
  }

  static hours(hhmmString: string): number | undefined {
    return hhmmString && Number.parseInt(hhmmString.split(':')[0], 10);
  }

  static minutes(hhmmString: string): number | undefined {
    return hhmmString && Number.parseInt(hhmmString.split(':')[1], 10);
  }

  static timestringFromTimeOfDay(time: TimeOfDay): string {
    return this.timestring(time.hour, time.minute);
  }

  static timestring(hour: number, minute: number): string {
    return this.padLeadingZero(hour, 2) + ':' + this.padLeadingZero(minute, 2);
  }

  static timestringFromDelta(seconds: number): string {
    return TimeUtil.timestringFromDelta_(moment(), seconds);
  }

  static timestringFromDelta_(m: Moment, seconds: number): string {
    m.add(seconds, 'second');
    return m.format('H:mm');
  }

  static timestringOfNextMatchingDow(dow: number, timeOfDay: string): string {
    return TimeUtil.timestringOfNextMatchingDow_(moment(), dow, timeOfDay);
  }

  static timestringOfNextMatchingDow_(m: Moment, dow: number, timeOfDay: string): string {
    if (!dow || !timeOfDay) {
      return undefined;
    }
    while (m.isoWeekday() !== dow) {
      m.add(1, 'day');
    }
    m.hour(TimeUtil.hours(timeOfDay));
    m.minute(TimeUtil.minutes(timeOfDay));
    m.second(0);
    m.millisecond(0);
    return m.toISOString();
  }

  static timestringFromTimestamp(timestamp: number): string {
    const m = moment(timestamp);
    return m.format('H:mm');
  }

  static toWeekdayFromDelta(seconds: number): number {
    return TimeUtil.toWeekdayFromDelta_(moment(), seconds);
  }

  static toWeekdayFromDelta_(m: Moment, seconds: number): number {
    m.add(seconds, 'second');
    return m.isoWeekday();
  }

  static toWeekdayFromTimestamp(timestamp: number): number {
    const m = moment(timestamp);
    return m.isoWeekday();
  }

  static toDayOfWeekValue(rawDayOfWeek: { value: number }): number {
    return rawDayOfWeek.value;
  }

  static toDayOfWeekValues(rawDaysOfWeek: { value: number }[]): number[] {
    const daysOfWeek: number[] = [];
    if (rawDaysOfWeek) {
      for (let i = 0; i < rawDaysOfWeek.length; i++) {
        daysOfWeek.push(this.toDayOfWeekValue(rawDaysOfWeek[i]));
      }
    }
    return daysOfWeek;
  }

  static toDayOfWeek(dayOfWeekValue: number): DayOfWeek {
    return new DayOfWeek(dayOfWeekValue);
  }

  static toDaysOfWeek(dayOfWeekValues: number[] | undefined): DayOfWeek[] {
    const daysOfWeek: DayOfWeek[] = [];
    if (dayOfWeekValues) {
      dayOfWeekValues.forEach((dayOfWeekValue) => daysOfWeek.push(TimeUtil.toDayOfWeek(dayOfWeekValue)));
    }
    return daysOfWeek;
  }

  static toTimeOfDayOfWeek(dayOfWeekValue: number, hhmmString: string): TimeOfDayOfWeek {
    return new TimeOfDayOfWeek(dayOfWeekValue, TimeUtil.hours(hhmmString), TimeUtil.minutes(hhmmString), 0);
  }

  static padLeadingZero(value: number, size: number): string {
    let s = String(value);
    while (s.length < (size || 2)) {
      s = '0' + s;
    }
    return s;
  }

}
