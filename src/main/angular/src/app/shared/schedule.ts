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

import {DayTimeframe} from './day-timeframe';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';

export class Schedule {
  '@class' = 'de.avanux.smartapplianceenabler.appliance.Schedule';
  enabled = true;
  minRunningTime: string;
  maxRunningTime: string;
  timeframeType: string;
  dayTimeframe: DayTimeframe;
  consecutiveDaysTimeframe: ConsecutiveDaysTimeframe;

  /*
  timeframe: DayTimeframe | ConsecutiveDaysTimeframe;

  get timeframeType() {
    if (this.dayTimeframe != null) {
      return this.dayTimeframe['atclass'];
    } else if (this.consecutiveDaysTimeframe != null) {
      return this.consecutiveDaysTimeframe['atclass'];
    }
    return null;
  }
  set timeframeType(type: string) {
    if (this.timeframe != null) {
      this.timeframe['atclass'] = type;
    }
  }
  */
}
