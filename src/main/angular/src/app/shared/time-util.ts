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

  static padLeadingZero(value: number, size: number): string {
    let s = String(value);
    while (s.length < (size || 2)) { s = '0' + s; }
    return s;
  }
}
