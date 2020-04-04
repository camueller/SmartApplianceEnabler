import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControlName, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {DayOfWeek, DaysOfWeek} from '../shared/days-of-week';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ConsecutiveDaysTimeframe} from './consecutive-days-timeframe';
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
  selector: 'app-schedule-timeframe-consecutivedays',
  templateUrl: './schedule-timeframe-consecutivedays.component.html',
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleTimeframeConsecutivedaysComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  consecutiveDaysTimeframe: ConsecutiveDaysTimeframe;
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
      this.updateForm();
    }
  }

  ngOnInit() {
    DaysOfWeek.getDows(this.translate).subscribe(daysOfWeek => this.daysOfWeek = daysOfWeek);
    this.errorMessages = new ErrorMessages('ScheduleTimeframeDayComponent.error.', [
      new ErrorMessage('startTime', ValidatorType.required),
      new ErrorMessage('startTime', ValidatorType.pattern),
      new ErrorMessage('endTime', ValidatorType.required),
      new ErrorMessage('endTime', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
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

  get startDayOfWeek() {
    return this.consecutiveDaysTimeframe.start && this.consecutiveDaysTimeframe.start.dayOfWeek;
  }

  get startTime() {
    return this.consecutiveDaysTimeframe.start && TimeUtil.timestringFromTimeOfDay(this.consecutiveDaysTimeframe.start);
  }

  get endDayOfWeek() {
    return this.consecutiveDaysTimeframe.end && this.consecutiveDaysTimeframe.end.dayOfWeek;
  }

  get endTime() {
    return this.consecutiveDaysTimeframe.end && TimeUtil.timestringFromTimeOfDay(this.consecutiveDaysTimeframe.end);
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'startDayOfWeek', this.startDayOfWeek,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'startTime', this.startTime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(this.form, 'endDayOfWeek', this.endDayOfWeek,
      Validators.required);
    this.formHandler.addFormControl(this.form, 'endTime', this.endTime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'startDayOfWeek', this.startDayOfWeek);
    this.formHandler.setFormControlValue(this.form, 'startTime', this.startTime);
    this.formHandler.setFormControlValue(this.form, 'endDayOfWeek', this.endDayOfWeek);
    this.formHandler.setFormControlValue(this.form, 'endTime', this.endTime);
  }

  updateModelFromForm(): ConsecutiveDaysTimeframe | undefined {
    const startDayOfWeek = this.form.controls.startDayOfWeek.value;
    const startTime = this.form.controls.startTime.value;
    const endDayOfWeek = this.form.controls.endDayOfWeek.value;
    const endTime = this.form.controls.endTime.value;

    if (!(startDayOfWeek || startTime || endDayOfWeek || endTime)) {
      return undefined;
    }

    this.consecutiveDaysTimeframe.start = TimeUtil.toTimeOfDayOfWeek(startDayOfWeek, startTime);
    this.consecutiveDaysTimeframe.end = TimeUtil.toTimeOfDayOfWeek(endDayOfWeek, endTime);
    return this.consecutiveDaysTimeframe;
  }
}
