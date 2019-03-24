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
import {RuntimeRequest} from './runtime-request';
import {EnergyRequest} from './energy-request';
import {SocRequest} from './soc-request';

export class Schedule {
  '@class' = 'de.avanux.smartapplianceenabler.schedule.Schedule';
  enabled: boolean;
  requestType: string;
  runtimeRequest: RuntimeRequest;
  energyRequest: EnergyRequest;
  socRequest: SocRequest;
  timeframeType: string;
  dayTimeframe: DayTimeframe;
  consecutiveDaysTimeframe: ConsecutiveDaysTimeframe;

  public constructor(init?: Partial<Schedule>) {
    Object.assign(this, init);
  }
}
