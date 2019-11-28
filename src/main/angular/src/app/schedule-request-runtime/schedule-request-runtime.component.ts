import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Logger} from '../log/logger';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {RuntimeRequest} from './runtime-request';
import {ErrorMessages} from '../shared/error-messages';
import {DayTimeframe} from '../schedule-timeframe-day/day-timeframe';
import {TimeUtil} from '../shared/time-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Schedule} from '../schedule/schedule';

@Component({
  selector: 'app-schedule-request-runtime',
  templateUrl: './schedule-request-runtime.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestRuntimeComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  runtimeRequest: RuntimeRequest;
  form: FormGroup;
  formHandler: FormHandler;
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
    if (changes.runtimeRequest) {
      if (changes.runtimeRequest.currentValue) {
        this.runtimeRequest = changes.runtimeRequest.currentValue;
      } else {
        this.runtimeRequest = new RuntimeRequest();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestRuntimeComponent.error.', [
      new ErrorMessage('minRuntime', ValidatorType.required),
      new ErrorMessage('minRuntime', ValidatorType.pattern),
      new ErrorMessage('maxRuntime', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get minRuntime() {
    return this.runtimeRequest.min && TimeUtil.toHourMinute(this.runtimeRequest.min);
  }

  get maxRuntime() {
    return this.runtimeRequest.max && TimeUtil.toHourMinute(this.runtimeRequest.max);
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'minRuntime', this.minRuntime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(this.form, 'maxRuntime', this.maxRuntime,
      [Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'minRuntime', this.minRuntime);
    this.formHandler.setFormControlValue(this.form, 'maxRuntime', this.maxRuntime);
  }

  updateModelFromForm(): RuntimeRequest | undefined {
    const minRuntime = this.form.controls.minRuntime.value;
    const maxRuntime = this.form.controls.maxRuntime.value;

    if (!(minRuntime || maxRuntime)) {
      return undefined;
    }

    this.runtimeRequest.min = TimeUtil.toSeconds(minRuntime);
    this.runtimeRequest.max = TimeUtil.toSeconds(maxRuntime);
    return this.runtimeRequest;
  }
}
