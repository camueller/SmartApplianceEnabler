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
    $('.clockpicker').clockpicker({autoclose: true});
  }

  get startTime() {
    return this.dayTimeFrame.start && TimeUtil.timestringFromTimeOfDay(this.dayTimeFrame.start);
  }

  get endTime() {
    return this.dayTimeFrame.end && TimeUtil.timestringFromTimeOfDay(this.dayTimeFrame.end);
  }

  get daysOfWeekValues() {
    return this.dayTimeFrame.daysOfWeek && TimeUtil.toDayOfWeekValues(this.dayTimeFrame.daysOfWeek);
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'daysOfWeekValues', this.daysOfWeekValues);
    this.formHandler.addFormControl(this.form, 'startTime', this.startTime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(this.form, 'endTime', this.endTime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'daysOfWeekValues', this.daysOfWeekValues);
    this.formHandler.setFormControlValue(this.form, 'startTime', this.startTime);
    this.formHandler.setFormControlValue(this.form, 'endTime', this.endTime);
  }

  updateModelFromForm(): DayTimeframe | undefined {
    const daysOfWeekValues = this.form.controls.daysOfWeekValues.value;
    const startTime = this.form.controls.startTime.value;
    const endTime = this.form.controls.endTime.value;

    if (!(daysOfWeekValues || startTime || endTime)) {
      return undefined;
    }

    this.dayTimeFrame.daysOfWeek = TimeUtil.toDaysOfWeek(daysOfWeekValues);
    this.dayTimeFrame.start = TimeOfDay.fromString(startTime);
    this.dayTimeFrame.end = TimeOfDay.fromString(endTime);
    return this.dayTimeFrame;
  }
}
