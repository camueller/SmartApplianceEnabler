import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {MeterDefaults} from '../meter/meter-defaults';
import {S0ElectricityMeter} from './s0-electricity-meter';
import {Logger} from '../log/logger';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {NestedFormService} from '../shared/nested-form-service';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

@Component({
  selector: 'app-meter-s0',
  templateUrl: './meter-s0.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterS0Component implements OnInit, AfterViewChecked {
  @Input()
  s0ElectricityMeter: S0ElectricityMeter;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  applianceId: string;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterS0Component.error.', [
      new ErrorMessage('gpio', ValidatorType.required),
      new ErrorMessage('gpio', ValidatorType.pattern),
      new ErrorMessage('impulsesPerKwh', ValidatorType.required),
      new ErrorMessage('impulsesPerKwh', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.s0ElectricityMeter, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.nestedFormService.submitted.subscribe(
      () => this.updateModelFromForm(this.s0ElectricityMeter, this.form));
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm(form: FormGroup, s0ElectricityMeter: S0ElectricityMeter, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'gpio',
      s0ElectricityMeter ? s0ElectricityMeter.gpio : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'pinPullResistance',
      s0ElectricityMeter ? s0ElectricityMeter.pinPullResistance : undefined);
    formHandler.addFormControl(form, 'impulsesPerKwh',
      s0ElectricityMeter ? s0ElectricityMeter.impulsesPerKwh : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'measurementInterval',
      s0ElectricityMeter ? s0ElectricityMeter.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(s0ElectricityMeter: S0ElectricityMeter, form: FormGroup) {
    s0ElectricityMeter.gpio = form.controls.gpio.value;
    s0ElectricityMeter.pinPullResistance = form.controls.pinPullResistance.value;
    s0ElectricityMeter.impulsesPerKwh = form.controls.impulsesPerKwh.value;
    s0ElectricityMeter.measurementInterval = form.controls.measurementInterval.value;
    this.nestedFormService.complete();
  }
}
