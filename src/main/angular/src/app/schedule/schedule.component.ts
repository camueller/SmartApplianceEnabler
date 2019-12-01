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
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {Logger} from '../log/logger';
import {Schedule} from './schedule';
import {FormGroup, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {DayTimeframe} from '../schedule-timeframe-day/day-timeframe';
import {RuntimeRequest} from '../schedule-request-runtime/runtime-request';
import {SocRequest} from '../schedule-request-soc/soc-request';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {EnergyRequest} from '../schedule-request-energy/energy-request';
import {ConsecutiveDaysTimeframe} from '../schedule-timeframe-consecutivedays/consecutive-days-timeframe';
import {ScheduleTimeframeDayComponent} from '../schedule-timeframe-day/schedule-timeframe-day.component';
// tslint:disable-next-line:max-line-length
import {ScheduleTimeframeConsecutivedaysComponent} from '../schedule-timeframe-consecutivedays/schedule-timeframe-consecutivedays.component';
import {ScheduleRequestRuntimeComponent} from '../schedule-request-runtime/schedule-request-runtime.component';
import {ScheduleRequestEnergyComponent} from '../schedule-request-energy/schedule-request-energy.component';
import {ScheduleRequestSocComponent} from '../schedule-request-soc/schedule-request-soc.component';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.css', '../global.css'],
})
export class ScheduleComponent implements OnChanges, AfterViewChecked {
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
  @ViewChild(ScheduleTimeframeDayComponent, {static: false})
  timeframeDayComp: ScheduleTimeframeDayComponent;
  @ViewChild(ScheduleTimeframeConsecutivedaysComponent, {static: false})
  timeframeConsecutiveDaysComp: ScheduleTimeframeConsecutivedaysComponent;
  @ViewChild(ScheduleRequestRuntimeComponent, {static: false})
  requestRuntimeComp: ScheduleRequestRuntimeComponent;
  @ViewChild(ScheduleRequestEnergyComponent, {static: false})
  requestEnergyComp: ScheduleRequestEnergyComponent;
  @ViewChild(ScheduleRequestSocComponent, {static: false})
  requestSocComp: ScheduleRequestSocComponent;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
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
      this.updateForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  isEnabled() {
    return this.form.controls.enabled.value;
  }

  isDayTimeframe() {
    return this.form.controls.timeframeType.value === DayTimeframe.TYPE;
  }

  isConsecutiveDaysTimeframe() {
    return this.form.controls.timeframeType.value === ConsecutiveDaysTimeframe.TYPE;
  }

  isRuntimeRequest() {
    return this.form.controls.requestType.value === RuntimeRequest.TYPE;
  }

  isEnergyRequest() {
    return this.form.controls.requestType.value === EnergyRequest.TYPE;
  }

  isSocRequest() {
    return this.form.controls.requestType.value === SocRequest.TYPE;
  }

  removeSchedule() {
    this.remove.emit();
  }

  get timeframeType() {
    if (this.schedule && this.schedule.timeframe) {
      if (this.schedule.timeframe['@class'] === DayTimeframe.TYPE) {
        return DayTimeframe.TYPE;
      } else if (this.schedule.timeframe['@class'] === ConsecutiveDaysTimeframe.TYPE) {
        return ConsecutiveDaysTimeframe.TYPE;
      }
    }
    return undefined;
  }

  get requestType() {
    if (this.schedule && this.schedule.request) {
      if (this.schedule.request['@class'] === RuntimeRequest.TYPE) {
        return RuntimeRequest.TYPE;
      } else if (this.schedule.request['@class'] === EnergyRequest.TYPE) {
        return EnergyRequest.TYPE;
      } else if (this.schedule.request['@class'] === SocRequest.TYPE) {
        return SocRequest.TYPE;
      }
    }
    return undefined;
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'enabled', this.schedule.enabled);
    this.formHandler.addFormControl(this.form, 'timeframeType',
      this.timeframeType, [Validators.required]);
    this.formHandler.addFormControl(this.form, 'requestType',
      this.requestType, [Validators.required]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'enabled', this.schedule.enabled);
    this.formHandler.setFormControlValue(this.form, 'timeframeType', this.timeframeType);
    this.formHandler.setFormControlValue(this.form, 'requestType', this.requestType);
  }

  updateModelFromForm(): Schedule | undefined {
    const enabled = this.form.controls.enabled.value;
    const timeframeType = this.form.controls.timeframeType.value;
    const requestType = this.form.controls.requestType.value;

    let timeframe: DayTimeframe | ConsecutiveDaysTimeframe;
    if (timeframeType === DayTimeframe.TYPE) {
      timeframe = this.timeframeDayComp.updateModelFromForm();
    }
    if (timeframeType === ConsecutiveDaysTimeframe.TYPE) {
      timeframe = this.timeframeConsecutiveDaysComp.updateModelFromForm();
    }

    let request: RuntimeRequest | EnergyRequest | SocRequest;
    if (requestType === RuntimeRequest.TYPE) {
      request = this.requestRuntimeComp.updateModelFromForm();
    }
    if (requestType === EnergyRequest.TYPE) {
      request = this.requestEnergyComp.updateModelFromForm();
    }
    if (requestType === SocRequest.TYPE) {
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
