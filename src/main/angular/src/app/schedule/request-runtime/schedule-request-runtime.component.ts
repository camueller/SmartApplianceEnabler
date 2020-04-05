import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControlName, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {RuntimeRequest} from './runtime-request';
import {ErrorMessages} from '../../shared/error-messages';
import {TimeUtil} from '../../shared/time-util';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';

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
  selector: 'app-schedule-request-runtime',
  templateUrl: './schedule-request-runtime.component.html',
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ScheduleRequestRuntimeComponent implements OnChanges, OnInit {
  @Input()
  runtimeRequest: RuntimeRequest;
  form: FormGroup;
  formHandler: FormHandler;
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
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.initializeOnceAfterViewChecked = true;
  }

  ngAfterViewChecked() {
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
    }
  }

  initializeClockPicker() {
    $('.clockpicker').clockpicker({autoclose: true});
  }

  get minRuntime() {
    return this.runtimeRequest.min && TimeUtil.toHourMinute(this.runtimeRequest.min);
  }

  get maxRuntime() {
    return this.runtimeRequest.max && TimeUtil.toHourMinute(this.runtimeRequest.max);
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'minRuntime', this.minRuntime,
      [Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
    this.formHandler.addFormControl(this.form, 'maxRuntime', this.maxRuntime,
      [Validators.required, Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]);
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
