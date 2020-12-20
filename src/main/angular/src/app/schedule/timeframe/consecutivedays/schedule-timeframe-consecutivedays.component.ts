import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
import {ErrorMessages} from '../../../shared/error-messages';
import {DayOfWeek, DaysOfWeek} from '../../../shared/days-of-week';
import {TimeUtil} from '../../../shared/time-util';
import {FormHandler} from '../../../shared/form-handler';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {TimepickerComponent} from '../../../material/timepicker/timepicker.component';

@Component({
  selector: 'app-schedule-timeframe-consecutivedays',
  templateUrl: './schedule-timeframe-consecutivedays.component.html',
  styleUrls: ['./schedule-timeframe-consecutivedays.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleTimeframeConsecutivedaysComponent implements OnChanges, OnInit {
  @Input()
  consecutiveDaysTimeframe: ConsecutiveDaysTimeframe;
  @Input()
  enabled: boolean;
  @ViewChild('startTimeComponent', {static: true})
  startTimeComp: TimepickerComponent;
  @ViewChild('endTimeComponent', {static: true})
  endTimeComp: TimepickerComponent;
  form: FormGroup;
  formHandler: FormHandler;
  daysOfWeek: DayOfWeek[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.consecutiveDaysTimeframe) {
      if (changes.consecutiveDaysTimeframe.currentValue) {
        this.consecutiveDaysTimeframe = changes.consecutiveDaysTimeframe.currentValue;
      } else {
        this.consecutiveDaysTimeframe = new ConsecutiveDaysTimeframe();
      }
      this.expandParentForm();
    }
    if (changes.enabled) {
      this.setEnabled(changes.enabled.currentValue);
    }
  }

  ngOnInit() {
    DaysOfWeek.getDows(this.translate).subscribe(daysOfWeek => this.daysOfWeek = daysOfWeek);
    this.errorMessages = new ErrorMessages('ScheduleTimeframeDayComponent.error.', [
      new ErrorMessage('startTime', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('startTime', ValidatorType.pattern),
      new ErrorMessage('endTime', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('endTime', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get startDayOfWeek() {
    return this.consecutiveDaysTimeframe.start?.dayOfWeek;
  }

  get startTime() {
    return this.consecutiveDaysTimeframe.start?.time && TimeUtil.timestringFromTimeOfDay(this.consecutiveDaysTimeframe.start.time);
  }

  get endDayOfWeek() {
    return this.consecutiveDaysTimeframe.end?.dayOfWeek;
  }

  get endTime() {
    return this.consecutiveDaysTimeframe.end?.time && TimeUtil.timestringFromTimeOfDay(this.consecutiveDaysTimeframe.end.time);
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.startDayOfWeek?.enable();
      this.form.controls.endDayOfWeek?.enable();
    } else {
      this.form.controls.startDayOfWeek?.disable();
      this.form.controls.endDayOfWeek?.disable();
    }
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'startDayOfWeek', this.startDayOfWeek,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'endDayOfWeek', this.endDayOfWeek,
      Validators.required);
  }

  updateModelFromForm(): ConsecutiveDaysTimeframe | undefined {
    const startDayOfWeek = this.form.controls.startDayOfWeek.value;
    const startTime = this.startTimeComp.updateModelFromForm();
    const endDayOfWeek = this.form.controls.endDayOfWeek.value;
    const endTime = this.endTimeComp.updateModelFromForm();

    if (!(startDayOfWeek || startTime || endDayOfWeek || endTime)) {
      return undefined;
    }

    this.consecutiveDaysTimeframe.start = TimeUtil.toTimeOfDayOfWeek(startDayOfWeek, startTime);
    this.consecutiveDaysTimeframe.end = TimeUtil.toTimeOfDayOfWeek(endDayOfWeek, endTime);
    return this.consecutiveDaysTimeframe;
  }
}
