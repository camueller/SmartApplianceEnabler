import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {MeterDefaults} from '../meter/meter-defaults';
import {S0ElectricityMeter} from './s0-electricity-meter';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {getValidInt, getValidString} from '../shared/form-util';

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
      new ErrorMessage('measurementInterval', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.s0ElectricityMeter, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
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

  updateModelFromForm(): S0ElectricityMeter | undefined {
    const gpio = getValidInt(this.form.controls['gpio'].value);
    const pinPullResistance = this.form.controls['pinPullResistance'].value;
    const impulsesPerKwh = getValidInt(this.form.controls['impulsesPerKwh'].value);
    const measurementInterval = getValidInt(this.form.controls['measurementInterval'].value);

    if (!(gpio || pinPullResistance || impulsesPerKwh || measurementInterval)) {
      return undefined;
    }

    this.s0ElectricityMeter.gpio = gpio;
    this.s0ElectricityMeter.pinPullResistance = pinPullResistance;
    this.s0ElectricityMeter.impulsesPerKwh = impulsesPerKwh;
    this.s0ElectricityMeter.measurementInterval = measurementInterval;
  }
}
