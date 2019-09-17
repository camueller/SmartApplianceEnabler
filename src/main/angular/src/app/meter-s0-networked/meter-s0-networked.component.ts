import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {MeterDefaults} from '../meter/meter-defaults';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {S0ElectricityMeter} from '../meter-s0/s0-electricity-meter';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

@Component({
  selector: 'app-meter-s0-networked',
  templateUrl: './meter-s0-networked.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterS0NetworkedComponent implements OnInit, AfterViewChecked {
  @Input()
  s0ElectricityMeterNetworked: S0ElectricityMeter;
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
      new ErrorMessage('impulsesPerKwh', ValidatorType.required),
      new ErrorMessage('impulsesPerKwh', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.s0ElectricityMeterNetworked, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm(form: FormGroup, s0ElectricityMeterNetworked: S0ElectricityMeter, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'impulsesPerKwh',
      s0ElectricityMeterNetworked ? s0ElectricityMeterNetworked.impulsesPerKwh : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'measurementInterval',
      s0ElectricityMeterNetworked ? s0ElectricityMeterNetworked.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateS0ElectricityMeter(s0ElectricityMeterNetworked: S0ElectricityMeter, form: FormGroup) {
    s0ElectricityMeterNetworked.impulsesPerKwh = form.controls.impulsesPerKwh.value;
    s0ElectricityMeterNetworked.measurementInterval = form.controls.measurementInterval.value;
  }
}
