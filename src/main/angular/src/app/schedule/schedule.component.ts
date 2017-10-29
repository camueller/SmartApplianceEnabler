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

import {
  AfterViewChecked,
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
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
export class ScheduleComponent implements OnInit, AfterViewInit, AfterViewChecked, OnChanges {
  @ViewChild('dayTimeframeStartTime') dayTimeframeStartTimeElement: ElementRef;
  @ViewChild('dayTimeframeEndTime') dayTimeframeEndTimeElement: ElementRef;
  @ViewChild('consecutiveDaysTimeframeStartTime') consecutiveDaysTimeframeStartTimeElement: ElementRef;
  @ViewChild('consecutiveDaysTimeframeEndTime') consecutiveDaysTimeframeEndTimeElement: ElementRef;
  @Input() schedule: Schedule = ScheduleFactory.createEmptySchedule();
  @Input() index: number;
  @Output() removeScheduleEvent = new EventEmitter<number>();
  DAY_TIMEFRAME = DayTimeframe.TYPE;
  CONSECUTIVE_DAYS_TIMEFRAME = ConsecutiveDaysTimeframe.TYPE;
  initializeOnceAfterViewChecked = false;

  ngOnInit() {
    this.timeframeTypeChanged(this.schedule.timeframeType);
  }

  ngAfterViewInit() {
    console.log('ngAfterViewInit timeframeType=' + this.schedule.timeframeType);
  }

  ngOnChanges() {
  }

  ngAfterViewChecked() {
    console.log('ngAfterViewChecked timeframeType=' + this.schedule.timeframeType
      + ' initializeOnceAfterViewChecked=' + this.initializeOnceAfterViewChecked);
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
      this.initializeDropdown();
    }
  }

  remove() {
    this.removeScheduleEvent.emit(this.index);
  }

  initializeDropdown() {
    $('.ui.dropdown').dropdown();
  }

  initializeClockPicker() {
    if (this.schedule.timeframeType === this.DAY_TIMEFRAME) {
      if (this.dayTimeframeStartTimeElement != null) {
        $(this.dayTimeframeStartTimeElement.nativeElement).on('change', (event) => {
          this.schedule.dayTimeframe.startTime = event.target.value;
        });
      }
      if (this.dayTimeframeEndTimeElement) {
        $(this.dayTimeframeEndTimeElement.nativeElement).on('change', (event) => {
          this.schedule.dayTimeframe.endTime = event.target.value;
        });
      }
    } else if (this.schedule.timeframeType === this.CONSECUTIVE_DAYS_TIMEFRAME) {
      if (this.consecutiveDaysTimeframeStartTimeElement) {
        $(this.consecutiveDaysTimeframeStartTimeElement.nativeElement).on('change', (event) => {
          this.schedule.consecutiveDaysTimeframe.startTime = event.target.value;
        });
      }
      if (this.consecutiveDaysTimeframeEndTimeElement) {
        $(this.consecutiveDaysTimeframeEndTimeElement.nativeElement).on('change', (event) => {
          this.schedule.consecutiveDaysTimeframe.endTime = event.target.value;
        });
      }
    }
    $('.clockpicker').clockpicker({ autoclose: true });
  }


  timeframeTypeChanged(newType: string) {
    console.log('timeframeTypeChanged: ' + this.schedule.timeframeType);
    if (newType === this.DAY_TIMEFRAME && this.schedule.dayTimeframe == null) {
      this.schedule.dayTimeframe = new DayTimeframe();
    } else if (newType === this.CONSECUTIVE_DAYS_TIMEFRAME && this.schedule.consecutiveDaysTimeframe == null) {
      this.schedule.consecutiveDaysTimeframe = new ConsecutiveDaysTimeframe();
    }
    this.initializeOnceAfterViewChecked = true;
  }
}
