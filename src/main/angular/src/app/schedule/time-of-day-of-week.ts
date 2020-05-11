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

import {TimeOfDay} from './time-of-day';

export class TimeOfDayOfWeek {
  '@class' = 'de.avanux.smartapplianceenabler.schedule.TimeOfDayOfWeek';
  dayOfWeek: number;
  hour: number;
  minute: number;
  second: number;

  constructor(dayOfWeek: number, hour: number, minute: number, second: number) {
    this.dayOfWeek = dayOfWeek;
    this.hour = hour;
    this.minute = minute;
    this.second = second;
  }

  public get time(): TimeOfDay {
    return new TimeOfDay(this.hour, this.minute, this.second);
  }

  public set time(timeOfDay: TimeOfDay) {
    this.hour = timeOfDay.hour;
    this.minute = timeOfDay.minute;
    this.second = timeOfDay.second;
  }
}
