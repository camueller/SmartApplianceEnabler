import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControlName, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
import {DayTimeframe} from '../schedule-timeframe-day/day-timeframe';
import {TimeUtil} from '../shared/time-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Schedule} from '../schedule/schedule';

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
  selector: 'app-schedule-timeframe-consecutivedays',
  templateUrl: './schedule-timeframe-consecutivedays.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleTimeframeConsecutivedaysComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  consecutiveDaysTimeframe: ConsecutiveDaysTimeframe;
  @Input()
  formControlNamePrefix = '';
  form: FormGroup;
  formHandler: FormHandler;
  daysOfWeek: DayOfWeek[];
  initializeOnceAfterViewChecked = false;
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
      this.updateForm(this.parent.form, this.consecutiveDaysTimeframe, this.formHandler);
    }
  }

  ngOnInit() {
    DaysOfWeek.getDows(this.translate).subscribe(daysOfWeek => this.daysOfWeek = daysOfWeek);
    this.errorMessages = new ErrorMessages('ScheduleTimeframeDayComponent.error.', [
      new ErrorMessage(this.getFormControlName('startTime'), ValidatorType.required, 'startTime'),
      new ErrorMessage(this.getFormControlName('startTime'), ValidatorType.pattern, 'startTime'),
      new ErrorMessage(this.getFormControlName('endTime'), ValidatorType.required, 'endTime'),
      new ErrorMessage(this.getFormControlName('endTime'), ValidatorType.pattern, 'endTime'),
    ], this.translate);
    this.expandParentForm(this.form, this.consecutiveDaysTimeframe, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.initializeOnceAfterViewChecked = true;
  }

  ngAfterViewChecked() {
    if (this.initializeOnceAfterViewChecked) {
      this.formHandler.markLabelsRequired();
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
    }
  }

  initializeClockPicker() {
    $('.clockpicker').clockpicker({ autoclose: true });
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  expandParentForm(form: FormGroup, consecutiveDaysTimeframe: ConsecutiveDaysTimeframe, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('startDayOfWeek'),
      consecutiveDaysTimeframe && consecutiveDaysTimeframe.start.dayOfWeek);
    formHandler.addFormControl(form, this.getFormControlName('startTime'),
      consecutiveDaysTimeframe && TimeUtil.timestringFromTimeOfDay(consecutiveDaysTimeframe.start),
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    formHandler.addFormControl(form, this.getFormControlName('endDayOfWeek'),
      consecutiveDaysTimeframe && consecutiveDaysTimeframe.end.dayOfWeek);
    formHandler.addFormControl(form, this.getFormControlName('endTime'),
      consecutiveDaysTimeframe && TimeUtil.timestringFromTimeOfDay(consecutiveDaysTimeframe.end),
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
  }

  updateForm(form: FormGroup, consecutiveDaysTimeframe: ConsecutiveDaysTimeframe, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('startDayOfWeek'),
      consecutiveDaysTimeframe.startDayOfWeek);
    formHandler.setFormControlValue(form, this.getFormControlName('startTime'),
      consecutiveDaysTimeframe.startTime);
    formHandler.setFormControlValue(form, this.getFormControlName('endDayOfWeek'),
      consecutiveDaysTimeframe.endDayOfWeek);
    formHandler.setFormControlValue(form, this.getFormControlName('endTime'),
      consecutiveDaysTimeframe.endTime);
  }

  updateModelFromForm(): ConsecutiveDaysTimeframe | undefined {
    const startDayOfWeek = this.form.controls[this.getFormControlName('startDayOfWeek')].value;
    const startTime = this.form.controls[this.getFormControlName('startTime')].value;
    const endDayOfWeek = this.form.controls[this.getFormControlName('endDayOfWeek')].value;
    const endTime = this.form.controls[this.getFormControlName('endTime')].value;

    if (!(startDayOfWeek || startTime || endDayOfWeek || endTime)) {
      return undefined;
    }

    this.consecutiveDaysTimeframe.startDayOfWeek = startDayOfWeek;
    this.consecutiveDaysTimeframe.startTime = startTime;
    this.consecutiveDaysTimeframe.endDayOfWeek = endDayOfWeek;
    this.consecutiveDaysTimeframe.endTime = endTime;
    return this.consecutiveDaysTimeframe;
  }
}
