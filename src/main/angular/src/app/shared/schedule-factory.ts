/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import {Schedule} from './schedule';
import {DayTimeframe} from './day-timeframe';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
import {TimeOfDayOfWeek} from './time-of-day-of-week';
import {DayOfWeek} from './day-of-week';
import {TimeOfDay} from './time-of-day';

export class ScheduleFactory {

  static createEmptySchedule(): Schedule {
    const schedule: Schedule = new Schedule();
    schedule.timeframeType = DayTimeframe.TYPE;
    schedule.dayTimeframe = new DayTimeframe();
    schedule.consecutiveDaysTimeframe = new ConsecutiveDaysTimeframe();
    return schedule;
  }

  static toJSON(schedules: Schedule[]): String {
    for (let i = 0; i < schedules.length; i++) {
      if (schedules[i].dayTimeframe != null) {
        const start = schedules[i].dayTimeframe.startTime;
        const end = schedules[i].dayTimeframe.endTime;
        schedules[i].dayTimeframe.start = new TimeOfDay(
          parseInt(start.substr(0, 2), 10),
          parseInt(start.substr(3, 2), 10),
          parseInt(start.substr(6, 2), 10));
        schedules[i].dayTimeframe.end = new TimeOfDay(
          parseInt(end.substr(0, 2), 10),
          parseInt(end.substr(3, 2), 10),
          parseInt(end.substr(6, 2), 10));

        schedules[i].dayTimeframe.daysOfWeek = new Array();
        const dowValues = schedules[i].dayTimeframe.daysOfWeekValues;
        for (let j = 0; j < dowValues.length; j++) {
          schedules[i].dayTimeframe.daysOfWeek.push(new DayOfWeek(dowValues[j]));
        }
      } else if (schedules[i].consecutiveDaysTimeframe != null) {
        const start = schedules[i].consecutiveDaysTimeframe.startTime;
        const end = schedules[i].consecutiveDaysTimeframe.endTime;
        schedules[i].consecutiveDaysTimeframe['start'] = new TimeOfDayOfWeek(
          schedules[i].consecutiveDaysTimeframe.startDayOfWeek,
          parseInt(start.substr(0, 2), 10),
          parseInt(start.substr(3, 2), 10),
          parseInt(start.substr(6, 2), 10));
        schedules[i].consecutiveDaysTimeframe['end'] = new TimeOfDayOfWeek(
          schedules[i].consecutiveDaysTimeframe.endDayOfWeek,
          parseInt(end.substr(0, 2), 10),
          parseInt(end.substr(3, 2), 10),
          parseInt(end.substr(6, 2), 10));
      }
    }

    let content = JSON.stringify(schedules);
    content = content.replace(/dayTimeframe/g, 'timeframe');
    content = content.replace(/consecutiveDaysTimeframe/g, 'timeframe');
    return content;
  }

  static fromObject(rawSchedule: any): Schedule {
    console.log('rawSchedule ' + JSON.stringify(rawSchedule));
    const schedule = new Schedule();
    schedule.minRunningTime = rawSchedule.minRunningTime;
    schedule.maxRunningTime = rawSchedule.maxRunningTime;
    if (rawSchedule.timeframe['@class'] === DayTimeframe.TYPE) {
      schedule.timeframeType = DayTimeframe.TYPE;
      schedule.dayTimeframe = ScheduleFactory.createDayTimeframe(rawSchedule.timeframe);
    } else if (rawSchedule.timeframe['@class'] === ConsecutiveDaysTimeframe.TYPE) {
      schedule.timeframeType = ConsecutiveDaysTimeframe.TYPE;
      schedule.consecutiveDaysTimeframe = ScheduleFactory.createConsecutiveDaysTimeframe(rawSchedule.timeframe);
    }
    console.log('schedule ' + JSON.stringify(schedule));
    return schedule;
  }

  static createDayTimeframe(rawTimeframe: any): DayTimeframe {
    console.log('createDayTimeframe from ' + JSON.stringify(rawTimeframe));
    const dayTimeframe = new DayTimeframe();
    dayTimeframe.daysOfWeekValues = new Array();
    for (let i = 0; i < rawTimeframe.daysOfWeek.length; i++) {
      dayTimeframe.daysOfWeekValues.push(rawTimeframe.daysOfWeek[i].value);
    }
    dayTimeframe.startTime = this.timestring(rawTimeframe.start.hour, rawTimeframe.start.minute, rawTimeframe.start.second);
    dayTimeframe.endTime = this.timestring(rawTimeframe.end.hour, rawTimeframe.end.minute, rawTimeframe.end.second);
    return dayTimeframe;
  }

  static createConsecutiveDaysTimeframe(rawTimeframe: any): ConsecutiveDaysTimeframe {
    console.log('createConsecutiveDaysTimeframe from ' + JSON.stringify(rawTimeframe));
    const consecutiveDaysTimeframe = new ConsecutiveDaysTimeframe();
    consecutiveDaysTimeframe.startDayOfWeek = rawTimeframe.start.dayOfWeek;
    consecutiveDaysTimeframe.startTime = this.timestring(rawTimeframe.start.hour, rawTimeframe.start.minute,
      rawTimeframe.start.second);
    consecutiveDaysTimeframe.endDayOfWeek = rawTimeframe.end.dayOfWeek;
    consecutiveDaysTimeframe.endTime = this.timestring(rawTimeframe.end.hour, rawTimeframe.end.minute,
      rawTimeframe.end.second);
    return consecutiveDaysTimeframe;
  }

  static timestring(hour: number, minute: number, second: number): string {
    return this.pad(hour, 2) + ':' + this.pad(minute, 2) + ':' + this.pad(second, 2);
  }

  static pad(value: number, size: number): string {
    let s = String(value);
    while (s.length < (size || 2)) { s = '0' + s; }
    return s;
  }
}
