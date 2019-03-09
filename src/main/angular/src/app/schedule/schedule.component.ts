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

import {AfterViewChecked, AfterViewInit, Component, OnInit} from '@angular/core';
import {ActivatedRoute, CanDeactivate} from '@angular/router';
import {Schedule} from './schedule';
import {FormArray, FormBuilder, FormControlName, FormGroup, Validators} from '@angular/forms';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
import {DayTimeframe} from './day-timeframe';
import {ErrorMessages} from '../shared/error-messages';
import {TranslateService} from '@ngx-translate/core';
import {ScheduleErrorMessages} from './schedule-error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ScheduleFactory} from './schedule-factory';
import {ScheduleService} from './schedule-service';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {DialogService} from '../shared/dialog.service';
import {Observable} from 'rxjs';
import {Logger} from '../log/logger';
import {RuntimeRequest} from './runtime-request';
import {EnergyRequest} from './energy-request';
import {SocRequest} from './soc-request';
import {ElectricVehicle} from '../control-evcharger/electric-vehicle';
import {FormHandler} from '../shared/form-handler';

declare const $: any;

/**
 * The time set by clock picker is displayed in input field but not set in the form model.
 * Since there is no direct access to the native element from the form control we have to add a hook to
 * propagate time changes on the native element to the form control.
 * Inspired by https://stackoverflow.com/questions/39642547/is-it-possible-to-get-native-element-for-formcontrol
 */
const originFormControlNameNgOnChanges = FormControlName.prototype.ngOnChanges;
FormControlName.prototype.ngOnChanges = function () {
  const result = originFormControlNameNgOnChanges.apply(this, arguments);
  this.control.nativeElement = this.valueAccessor._elementRef;

  const elementRef = this.valueAccessor._elementRef;
  if (elementRef) {
    const classAttribute: string = elementRef.nativeElement.attributes.getNamedItem('class');
    if (classAttribute != null) {
      const classAttributeValues = classAttribute['nodeValue'];
      if (classAttributeValues.indexOf('clockpicker') > -1) {
        $(this.valueAccessor._elementRef.nativeElement).on('change', (event) => {
          this.control.setValue(event.target.value);
          this.control.markAsDirty();
        });
      }
    }
  }
  return result;
};

@Component({
  selector: 'app-schedules',
  templateUrl: './schedule.component.html',
  styleUrls: ['../global.css']
})
export class SchedulesComponent implements OnInit, AfterViewInit, AfterViewChecked, CanDeactivate<SchedulesComponent> {
  form: FormGroup;
  formHandler: FormHandler;
  schedules: FormArray;
  applianceId: string;
  electricVehicles: ElectricVehicle[];
  timeframeTypes: {key: string, value?: string}[] = [
    { key: DayTimeframe.TYPE },
    { key: ConsecutiveDaysTimeframe.TYPE },
  ];
  requestTypes: {key: string, value?: string}[] = [
    { key: RuntimeRequest.TYPE },
    { key: EnergyRequest.TYPE },
    { key: SocRequest.TYPE },
  ];
  initializeOnceAfterViewChecked = false;
  RUNTIME_REQUEST = RuntimeRequest.TYPE;
  ENERGY_REQUEST = EnergyRequest.TYPE;
  SOC_REQUEST = SocRequest.TYPE;
  DAY_TIMEFRAME = DayTimeframe.TYPE;
  CONSECUTIVE_DAYS_TIMEFRAME = ConsecutiveDaysTimeframe.TYPE;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  discardChangesMessage: string;

  constructor(private logger: Logger,
              private fb: FormBuilder,
              private scheduleService: ScheduleService,
              private route: ActivatedRoute,
              private dialogService: DialogService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages =  new ScheduleErrorMessages(this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    const timeframeTypeKeys = this.timeframeTypes.map(timeframeType => timeframeType.key);
    this.translate.get(timeframeTypeKeys).subscribe(
      translatedKeys => {
        this.timeframeTypes.forEach(timeframeType => timeframeType.value = translatedKeys[timeframeType.key]);
      });
    const requestTypeKeys = this.requestTypes.map(requestType => requestType.key);
    this.translate.get(requestTypeKeys).subscribe(
      translatedKeys => {
        this.requestTypes.forEach(requestType => requestType.value = translatedKeys[requestType.key]);
      });
    this.initForm();
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {schedules: Schedule[], electricVehicles: ElectricVehicle[]}) => {
      this.initForm();
      this.electricVehicles = data.electricVehicles;
      data.schedules.forEach(schedule => {
        const scheduleFormGroup = this.buildScheduleFormGroup(schedule);
        this.schedules.push(scheduleFormGroup);
      });
      this.initializeOnceAfterViewChecked = true;
    });
  }

  ngAfterViewInit() {
  }

  ngAfterViewChecked() {
    this.logger.debug('ngAfterViewChecked initializeOnceAfterViewChecked=' + this.initializeOnceAfterViewChecked);
    this.formHandler.markLabelsRequired();
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
      this.initializeDropdown();
    }
  }

  initializeDropdown() {
    $('.ui.dropdown').dropdown();
  }

  initializeClockPicker() {
    $('.clockpicker').clockpicker({ autoclose: true });
  }

  initForm() {
    this.schedules = new FormArray([]);
    this.form = this.fb.group({
      schedules: this.schedules
    });
    this.form.statusChanges.subscribe(() =>
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages));
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  buildScheduleFormGroup(schedule: Schedule): FormGroup {
    const fg = new FormGroup({});
    this.formHandler.addFormControl(fg, 'enabled', schedule != null ? schedule.enabled : true);

    const requestType = schedule != null ? schedule.requestType : this.RUNTIME_REQUEST;
    if (requestType === this.RUNTIME_REQUEST) {
      fg.addControl('runtimeRequest', this.buildRuntimeRequest(schedule));
    }
    if (requestType === this.ENERGY_REQUEST) {
      fg.addControl('energyRequest', this.buildEnergyRequest(schedule));
    }
    if (requestType === this.SOC_REQUEST) {
      fg.addControl('socRequest', this.buildSocRequest(schedule));
    }
    this.formHandler.addFormControl(fg, 'requestType',  requestType, Validators.required);

    const timeframeType = schedule != null ? schedule.timeframeType : this.DAY_TIMEFRAME;
    if (timeframeType === this.DAY_TIMEFRAME) {
      fg.addControl('dayTimeframe', this.buildDayTimeframe(schedule));
    }
    if (timeframeType === this.CONSECUTIVE_DAYS_TIMEFRAME) {
      fg.addControl('consecutiveDaysTimeframe',  this.buildConsecutiveDaysTimeframe(schedule));
    }
    this.formHandler.addFormControl(fg, 'timeframeType', timeframeType, Validators.required);

    fg.get('requestType').valueChanges.forEach(
      (newRequestType) => {
        this.logger.debug('requestType changed to ' + newRequestType);
        if (newRequestType === this.RUNTIME_REQUEST) {
          fg.removeControl('energyRequest');
          fg.removeControl('socRequest');
          fg.setControl('runtimeRequest', this.buildRuntimeRequest(schedule));
        } else if (newRequestType === this.ENERGY_REQUEST) {
          fg.removeControl('runtimeRequest');
          fg.removeControl('socRequest');
          fg.setControl('energyRequest', this.buildEnergyRequest(schedule));
        } else if (newRequestType === this.SOC_REQUEST) {
          fg.removeControl('runtimeRequest');
          fg.removeControl('energyRequest');
          fg.setControl('socRequest', this.buildSocRequest(schedule));
        }
        this.initializeOnceAfterViewChecked = true;
      }
    );
    fg.get('timeframeType').valueChanges.forEach(
      (newTimeframeType) => {
        this.logger.debug('timeframeType changed to ' + newTimeframeType);
        if (newTimeframeType === this.DAY_TIMEFRAME) {
          fg.removeControl('consecutiveDaysTimeframe');
          fg.setControl(
            'dayTimeframe', this.buildDayTimeframe(schedule)
          );
        } else if (newTimeframeType === this.CONSECUTIVE_DAYS_TIMEFRAME) {
          fg.removeControl('dayTimeframe');
          fg.setControl(
            'consecutiveDaysTimeframe', this.buildConsecutiveDaysTimeframe(schedule)
          );
        }
        this.initializeOnceAfterViewChecked = true;
      }
    );
    return fg;
  }

  buildRuntimeRequest(schedule: Schedule): FormGroup {
    const fg = new FormGroup({});
    this.formHandler.addFormControl(fg, 'min',
      this.hasRuntimeRequest(schedule) ? schedule.runtimeRequest.minHHMM : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(fg, 'max',
      this.hasRuntimeRequest(schedule) ? schedule.runtimeRequest.maxHHMM : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    return fg;
  }

  hasRuntimeRequest(schedule: Schedule): boolean {
    return schedule != null && schedule.runtimeRequest != null;
  }

  buildEnergyRequest(schedule: Schedule): FormGroup {
    const fg = new FormGroup({});
    this.formHandler.addFormControl(fg, 'min',
      this.hasRuntimeRequest(schedule) ? schedule.energyRequest.min : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'max',
      this.hasRuntimeRequest(schedule) ? schedule.energyRequest.max : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    return fg;
  }

  hasEnergyRequest(schedule: Schedule): boolean {
    return schedule != null && schedule.energyRequest != null;
  }

  buildSocRequest(schedule: Schedule): FormGroup {
    const fg = new FormGroup({});
    this.formHandler.addFormControl(fg, 'evId',
      this.hasSocRequest(schedule) ? schedule.socRequest.evId : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'soc',
      this.hasSocRequest(schedule) ? schedule.socRequest.soc : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    return fg;
  }

  hasSocRequest(schedule: Schedule): boolean {
    return schedule != null && schedule.socRequest != null;
  }

  get hasElectricVehicles(): boolean {
    return this.electricVehicles.length > 0;
  }

  buildDayTimeframe(schedule: Schedule): FormGroup {
    const fg = new FormGroup({});
    this.formHandler.addFormControl(fg, 'daysOfWeekValues',
      this.hasDayTimeframe(schedule) ? schedule.dayTimeframe.daysOfWeekValues : []);
    this.formHandler.addFormControl(fg, 'startTime',
      this.hasDayTimeframe(schedule) ? schedule.dayTimeframe.startTime : null,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(fg, 'endTime',
      this.hasDayTimeframe(schedule) ? schedule.dayTimeframe.endTime : null,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    return fg;
  }

  hasDayTimeframe(schedule: Schedule): boolean {
    return schedule != null && schedule.dayTimeframe != null;
  }

  buildConsecutiveDaysTimeframe(schedule: Schedule): FormGroup {
    const fg = new FormGroup({});
    this.formHandler.addFormControl(fg, 'startDayOfWeek',
      this.hasConsecutiveDaysTimeframe(schedule) ? schedule.consecutiveDaysTimeframe.startDayOfWeek : null,
      Validators.required);
    this.formHandler.addFormControl(fg, 'startTime',
      this.hasConsecutiveDaysTimeframe(schedule) ? schedule.consecutiveDaysTimeframe.startTime : null,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(fg, 'endDayOfWeek',
      this.hasConsecutiveDaysTimeframe(schedule) ? schedule.consecutiveDaysTimeframe.endDayOfWeek : null,
      Validators.required);
    this.formHandler.addFormControl(fg, 'endTime',
      this.hasConsecutiveDaysTimeframe(schedule) ? schedule.consecutiveDaysTimeframe.endTime : null,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    return fg;
  }

  hasConsecutiveDaysTimeframe(schedule: Schedule): boolean {
    return schedule != null && schedule.consecutiveDaysTimeframe != null;
  }

  getIndexedErrorMessage(key: string, index: number): string {
    const indexedKey = key + '.' + index.toString();
    return this.errors[indexedKey];
  }

  addSchedule() {
    const schedulesControl = <FormArray>this.form.controls['schedules'];
    schedulesControl.push(this.buildScheduleFormGroup(null));
    this.initializeOnceAfterViewChecked = true;
    this.form.markAsDirty();
  }

  removeSchedule(index: number) {
    this.logger.debug('Remove ' + index);
    const schedulesControl = <FormArray>this.form.controls['schedules'];
    schedulesControl.removeAt(index);
    this.form.markAsDirty();
  }

  submitForm() {
    const scheduleFactory = new ScheduleFactory(this.logger);
    const schedules = scheduleFactory.fromForm(this.form.value);
    this.scheduleService.setSchedules(this.applianceId, schedules).subscribe();
    this.form.markAsPristine();
  }
}
