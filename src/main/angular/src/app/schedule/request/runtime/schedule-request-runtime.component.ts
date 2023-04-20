import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {RuntimeRequest} from './runtime-request';
import {ErrorMessages} from '../../../shared/error-messages';
import {TimeUtil} from '../../../shared/time-util';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../../shared/error-message';
import {InputValidatorPatterns} from '../../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../../shared/error-message-handler';
import {Logger} from '../../../log/logger';
import {TimepickerComponent} from '../../../material/timepicker/timepicker.component';
import {ScheduleRequestRuntimeModel} from './schedule-request-runtime.model';

@Component({
  selector: 'app-schedule-request-runtime',
  templateUrl: './schedule-request-runtime.component.html',
  styleUrls: ['./schedule-request-runtime.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestRuntimeComponent implements OnChanges, OnInit {
  @Input()
  runtimeRequest: RuntimeRequest;
  @Input()
  enabled: boolean;
  @ViewChild('minRuntimeComponent', {static: true})
  minRuntimeComp: TimepickerComponent;
  @ViewChild('maxRuntimeComponent', {static: true})
  maxRuntimeComp: TimepickerComponent;
  form: FormGroup<ScheduleRequestRuntimeModel>;
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
    if (changes.runtimeRequest) {
      if (changes.runtimeRequest.currentValue) {
        this.runtimeRequest = changes.runtimeRequest.currentValue;
      } else {
        this.runtimeRequest = new RuntimeRequest();
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ScheduleRequestRuntimeComponent.error.', [
      new ErrorMessage('minRuntime', ValidatorType.pattern),
      new ErrorMessage('maxRuntime', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('maxRuntime', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get minRuntime() {
    return this.runtimeRequest.min || this.runtimeRequest.min === 0 ? TimeUtil.toHourMinute(this.runtimeRequest.min) : undefined;
  }

  get maxRuntime() {
    return this.runtimeRequest.max && TimeUtil.toHourMinute(this.runtimeRequest.max);
  }

  expandParentForm() {
    this.form.addControl('minRuntime', new FormControl(this.minRuntime, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)));
    this.form.addControl('maxRuntime', new FormControl(this.maxRuntime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]));
    this.form.addControl('enabledExternally', new FormControl(!this.runtimeRequest.enabled));
  }

  updateModelFromForm(): RuntimeRequest | undefined {
    const enabled = !this.form.controls.enabledExternally.value;
    const minRuntime = this.minRuntimeComp.updateModelFromForm();
    const maxRuntime = this.maxRuntimeComp.updateModelFromForm();


    if (!(minRuntime || maxRuntime)) {
      return undefined;
    }

    this.runtimeRequest.enabled = enabled;
    this.runtimeRequest.min = minRuntime && minRuntime.length > 0 ? TimeUtil.toSeconds(minRuntime) : undefined;
    this.runtimeRequest.max = TimeUtil.toSeconds(maxRuntime);
    return this.runtimeRequest;
  }
}
