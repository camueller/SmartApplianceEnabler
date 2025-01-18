import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
import {ErrorMessages} from '../../../shared/error-messages';
import {DayOfWeek, DaysOfWeek} from '../../../shared/days-of-week';
import {TimeUtil} from '../../../shared/time-util';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {TimepickerComponent} from '../../../material/timepicker/timepicker.component';
import {getValidInt, isRequired} from '../../../shared/form-util';
import {ScheduleTimeframeConsecutivedaysModel} from './schedule-timeframe-consecutivedays.model';

@Component({
    selector: 'app-schedule-timeframe-consecutivedays',
    templateUrl: './schedule-timeframe-consecutivedays.component.html',
    styleUrls: ['./schedule-timeframe-consecutivedays.component.scss'],
    viewProviders: [
        { provide: ControlContainer, useExisting: FormGroupDirective }
    ],
    standalone: false
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
  form: FormGroup<ScheduleTimeframeConsecutivedaysModel>;
  daysOfWeek: DayOfWeek[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('startDayOfWeek', new FormControl(this.startDayOfWeek, Validators.required));
    this.form.addControl('endDayOfWeek', new FormControl(this.endDayOfWeek, Validators.required));
  }

  updateModelFromForm(): ConsecutiveDaysTimeframe | undefined {
    const startDayOfWeek = getValidInt(this.form.controls.startDayOfWeek.value);
    const startTime = this.startTimeComp.updateModelFromForm();
    const endDayOfWeek = getValidInt(this.form.controls.endDayOfWeek.value);
    const endTime = this.endTimeComp.updateModelFromForm();

    if (!(startDayOfWeek || startTime || endDayOfWeek || endTime)) {
      return undefined;
    }

    this.consecutiveDaysTimeframe.start = TimeUtil.toTimeOfDayOfWeek(startDayOfWeek, startTime);
    this.consecutiveDaysTimeframe.end = TimeUtil.toTimeOfDayOfWeek(endDayOfWeek, endTime);
    return this.consecutiveDaysTimeframe;
  }
}
