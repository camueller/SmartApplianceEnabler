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
  @Input()
  formControlNamePrefix = '';
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
      this.updateForm(this.parent.form, this.runtimeRequest, this.formHandler);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestRuntimeComponent.error.', [
      new ErrorMessage(this.getFormControlName('minRuntime'), ValidatorType.required, 'minRuntime'),
      new ErrorMessage(this.getFormControlName('minRuntime'), ValidatorType.pattern, 'minRuntime'),
      new ErrorMessage(this.getFormControlName('maxRuntime'), ValidatorType.pattern, 'maxRuntime'),
    ], this.translate);
    this.expandParentForm(this.form, this.runtimeRequest, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  expandParentForm(form: FormGroup, runtimeRequest: RuntimeRequest, formHandler: FormHandler) {
    console.log('expand=', runtimeRequest);
    formHandler.addFormControl(form, this.getFormControlName('minRuntime'),
      runtimeRequest && TimeUtil.toHourMinute(runtimeRequest.min),
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    formHandler.addFormControl(form, this.getFormControlName('maxRuntime'),
      runtimeRequest && TimeUtil.toHourMinute(runtimeRequest.max),
      [Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
  }

  updateForm(form: FormGroup, runtimeRequest: RuntimeRequest, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('minRuntime'), runtimeRequest.min);
    formHandler.setFormControlValue(form, this.getFormControlName('maxRuntime'), runtimeRequest.max);
  }

  updateModelFromForm(): RuntimeRequest | undefined {
    const minRuntime = this.form.controls[this.getFormControlName('minRuntime')].value;
    const maxRuntime = this.form.controls[this.getFormControlName('maxRuntime')].value;

    if (!(minRuntime || maxRuntime)) {
      return undefined;
    }

    this.runtimeRequest.min = TimeUtil.toSeconds(minRuntime);
    this.runtimeRequest.max = TimeUtil.toSeconds(maxRuntime);
    return this.runtimeRequest;
  }
}
