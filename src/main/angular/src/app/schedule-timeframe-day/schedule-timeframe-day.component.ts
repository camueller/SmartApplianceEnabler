import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {TranslateService} from '@ngx-translate/core';
import {Logger} from '../log/logger';
import {ControlContainer, FormControlName, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {DayTimeframe} from './day-timeframe';
import {TimeOfDay} from '../schedule/time-of-day';
import {TimeUtil} from '../shared/time-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';

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
  selector: 'app-schedule-timeframe-day',
  templateUrl: './schedule-timeframe-day.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleTimeframeDayComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  dayTimeFrame: DayTimeframe;
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
    if (changes.dayTimeFrame) {
      if (changes.dayTimeFrame.currentValue) {
        this.dayTimeFrame = changes.dayTimeFrame.currentValue;
      } else {
        this.dayTimeFrame = new DayTimeframe();
      }
      this.updateForm(this.parent.form, this.dayTimeFrame, this.formHandler);
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
    this.expandParentForm(this.form, this.dayTimeFrame, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.initializeOnceAfterViewChecked = true;
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
    }
  }

  initializeClockPicker() {
    console.log('initializeClockPicker');
    $('.clockpicker').clockpicker({ autoclose: true });
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  expandParentForm(form: FormGroup, dayTimeFrame: DayTimeframe, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('daysOfWeekValues'),
      dayTimeFrame && TimeUtil.toDayOfWeekValues(dayTimeFrame.daysOfWeek));
    formHandler.addFormControl(form, this.getFormControlName('startTime'),
      dayTimeFrame && TimeUtil.timestringFromTimeOfDay(dayTimeFrame.start),
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    formHandler.addFormControl(form, this.getFormControlName('endTime'),
      dayTimeFrame && TimeUtil.timestringFromTimeOfDay(dayTimeFrame.end),
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
  }

  updateForm(form: FormGroup, dayTimeFrame: DayTimeframe, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('daysOfWeekValues'),
      dayTimeFrame.daysOfWeekValues);
    formHandler.setFormControlValue(form, this.getFormControlName('startTime'), dayTimeFrame.startTime);
    formHandler.setFormControlValue(form, this.getFormControlName('endTime'), dayTimeFrame.endTime);
  }

  updateModelFromForm(): DayTimeframe | undefined {
    const daysOfWeekValues = this.form.controls[this.getFormControlName('daysOfWeekValues')].value;
    const startTime = this.form.controls[this.getFormControlName('startTime')].value;
    const endTime = this.form.controls[this.getFormControlName('endTime')].value;

    if (!(daysOfWeekValues || startTime || endTime)) {
      return undefined;
    }

    this.dayTimeFrame.daysOfWeek = TimeUtil.toDaysOfWeek(daysOfWeekValues);
    this.dayTimeFrame.start = TimeOfDay.fromString(startTime);
    this.dayTimeFrame.end = TimeOfDay.fromString(endTime);
    return this.dayTimeFrame;
  }
}
