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

import {AfterViewInit, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Logger} from '../log/logger';
import {Schedule} from './schedule';
import {FormGroup, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ElectricVehicle} from '../control/evcharger/electric-vehicle/electric-vehicle';
import {ScheduleTimeframeDayComponent} from './timeframe/day/schedule-timeframe-day.component';
import {RuntimeRequest} from './request/runtime/runtime-request';
import {EnergyRequest} from './request/energy/energy-request';
import {DayTimeframe} from './timeframe/day/day-timeframe';
import {ConsecutiveDaysTimeframe} from './timeframe/consecutivedays/consecutive-days-timeframe';
import {ScheduleRequestEnergyComponent} from './request/energy/schedule-request-energy.component';
import {ScheduleRequestRuntimeComponent} from './request/runtime/schedule-request-runtime.component';
import {ScheduleTimeframeConsecutivedaysComponent} from './timeframe/consecutivedays/schedule-timeframe-consecutivedays.component';
import {ScheduleRequestSocComponent} from './request/soc/schedule-request-soc.component';
import {SocRequest} from './request/soc/soc-request';
import {simpleRequestType, simpleTimeframeType} from '../shared/form-util';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.scss'],
})
export class ScheduleComponent implements OnChanges, AfterViewInit {
  @Input()
  schedule: Schedule;
  @Input()
  form: FormGroup;
  @Input()
  timeframeTypes: { key: string, value?: string }[];
  @Input()
  validRequestTypes: { key: string, value?: string }[];
  @Input()
  electricVehicles: ElectricVehicle[];
  @Output()
  remove = new EventEmitter<any>();
  @ViewChild(ScheduleTimeframeDayComponent)
  timeframeDayComp: ScheduleTimeframeDayComponent;
  @ViewChild(ScheduleTimeframeConsecutivedaysComponent)
  timeframeConsecutiveDaysComp: ScheduleTimeframeConsecutivedaysComponent;
  @ViewChild(ScheduleRequestRuntimeComponent)
  requestRuntimeComp: ScheduleRequestRuntimeComponent;
  @ViewChild(ScheduleRequestEnergyComponent)
  requestEnergyComp: ScheduleRequestEnergyComponent;
  @ViewChild(ScheduleRequestSocComponent)
  requestSocComp: ScheduleRequestSocComponent;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private changeDetectorRef: ChangeDetectorRef
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.schedule) {
      if (changes.schedule.currentValue) {
        this.schedule = changes.schedule.currentValue;
      } else {
        this.schedule = new Schedule();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngAfterViewInit() {
    this.setEnabled(this.schedule ? this.schedule.enabled : true);
  }

  isEnabled() {
    return this.form.controls.enabled.value;
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.timeframeType?.enable();
      this.form.controls.requestType?.enable();
    } else {
      this.form.controls.timeframeType?.disable();
      this.form.controls.requestType?.disable();
    }
  }

  isDayTimeframe() {
    return this.form.controls.timeframeType?.value === simpleTimeframeType(DayTimeframe.TYPE);
  }

  isConsecutiveDaysTimeframe() {
    return this.form.controls.timeframeType?.value === simpleTimeframeType(ConsecutiveDaysTimeframe.TYPE);
  }

  isRuntimeRequest() {
    return this.form.controls.requestType?.value === simpleRequestType(RuntimeRequest.TYPE);
  }

  isEnergyRequest() {
    return this.form.controls.requestType?.value === simpleRequestType(EnergyRequest.TYPE);
  }

  isSocRequest() {
    return this.form.controls.requestType?.value === simpleRequestType(SocRequest.TYPE);
  }

  removeSchedule() {
    this.remove.emit();
  }

  get timeframeType() {
    return this.schedule && simpleTimeframeType(this.schedule.timeframeType);
  }

  get requestType() {
    return this.schedule && simpleRequestType(this.schedule.requestType);
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'enabled', this.schedule.enabled);
    this.formHandler.addFormControl(this.form, 'timeframeType',
      this.timeframeType, [Validators.required]);
    this.formHandler.addFormControl(this.form, 'requestType',
      this.requestType, [Validators.required]);
    this.form.controls.enabled.valueChanges.subscribe(value => {
      this.setEnabled(value);
      this.form.markAsDirty();
    });
  }

  updateModelFromForm(): Schedule | undefined {
    const enabled = this.form.controls.enabled.value;

    let timeframe: DayTimeframe | ConsecutiveDaysTimeframe;
    if (this.isDayTimeframe()) {
      timeframe = this.timeframeDayComp.updateModelFromForm();
    }
    if (this.isConsecutiveDaysTimeframe()) {
      timeframe = this.timeframeConsecutiveDaysComp.updateModelFromForm();
    }

    let request: RuntimeRequest | EnergyRequest | SocRequest;
    if (this.isRuntimeRequest()) {
      request = this.requestRuntimeComp.updateModelFromForm();
    }
    if (this.isEnergyRequest()) {
      request = this.requestEnergyComp.updateModelFromForm();
    }
    if (this.isSocRequest()) {
      request = this.requestSocComp.updateModelFromForm();
    }

    if (!(enabled || timeframe || request)) {
      return undefined;
    }

    this.schedule.enabled = enabled;
    this.schedule.timeframe = timeframe;
    this.schedule.request = request;
    return this.schedule;
  }
}
