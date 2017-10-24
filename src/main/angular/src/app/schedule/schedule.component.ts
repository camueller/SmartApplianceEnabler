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

import {AfterViewInit, Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {Schedule} from '../shared/schedule';
import {DayTimeframe} from '../shared/day-timeframe';
import {ConsecutiveDaysTimeframe} from '../shared/consecutive-days-timeframe';
import {ScheduleFactory} from '../shared/schedule-factory';

declare const $: any;

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styles: []
})
export class ScheduleComponent implements OnInit, AfterViewInit, OnChanges {

  @Input() schedule: Schedule = ScheduleFactory.createEmptySchedule();
  @Input() index: number;
  @Output() removeScheduleEvent = new EventEmitter<number>();
  DAY_TIMEFRAME = DayTimeframe.TYPE;
  CONSECUTIVE_DAYS_TIMEFRAME = ConsecutiveDaysTimeframe.TYPE;


  ngOnInit() {
    this.timeframeTypeChanged(this.schedule.timeframeType);
    this.initializeDropdown();
  }

  ngAfterViewInit() {
    console.log('ngAfterViewInit timeframeType=' + this.schedule.timeframeType);
    this.initializeDropdown();
  }

  ngOnChanges() {
  }

  remove() {
    this.removeScheduleEvent.emit(this.index);
  }

  initializeDropdown() {
    $('.ui.dropdown').dropdown();
  }

  timeframeTypeChanged(newType: string) {
    if (newType === this.DAY_TIMEFRAME && this.schedule.dayTimeframe == null) {
      this.schedule.dayTimeframe = new DayTimeframe();
    } else if (newType === this.CONSECUTIVE_DAYS_TIMEFRAME && this.schedule.consecutiveDaysTimeframe == null) {
      this.schedule.consecutiveDaysTimeframe = new ConsecutiveDaysTimeframe();
    }
  }
}
