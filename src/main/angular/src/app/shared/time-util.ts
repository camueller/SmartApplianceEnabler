import * as moment from 'moment';
import {Moment} from 'moment';

export class TimeUtil {

  static toHourMinute(seconds: number): string {
    return new Date(seconds * 1000).toISOString().substring(11, 16);
  }

  static toHourMinuteWithUnits(seconds: number): string {
    const date = new Date(seconds * 1000).toISOString();
    let daysExtracted = Number.parseInt(date.substring(8, 10)) - 1;
    let hoursExtracted = Number.parseInt(date.substring(11, 13));
    let minutesExtracted = Number.parseInt(date.substring(14, 16));
    const secondsExtracted = Number.parseInt(date.substring(17, 19));
    if (secondsExtracted > 0) {
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
    const timeOfDaySplit = timeOfDay.split(':');
    m.hour(Number.parseInt(timeOfDaySplit[0]));
    m.minute(Number.parseInt(timeOfDaySplit[1]));
    m.second(0);
    m.millisecond(0);
    return m.toISOString();
  }

  static toWeekdayFromDelta(seconds: number): number {
    return TimeUtil.toWeekdayFromDelta_(moment(), seconds);
  }

  static toWeekdayFromDelta_(m: Moment, seconds: number): number {
    m.add(seconds, 'second');
    return m.weekday();
  }

  static padLeadingZero(value: number, size: number): string {
    let s = String(value);
    while (s.length < (size || 2)) { s = '0' + s; }
    return s;
  }

}
