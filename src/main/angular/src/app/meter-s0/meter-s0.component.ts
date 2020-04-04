import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
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
import {getValidInt} from '../shared/form-util';
import {PinPullResistance} from './PinPullResistance';

@Component({
  selector: 'app-meter-s0',
  templateUrl: './meter-s0.component.html',
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterS0Component implements OnChanges, OnInit, AfterViewChecked {
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
  pinPullResistanceTypes: ListItem[] = [];


  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.s0ElectricityMeter) {
      if (changes.s0ElectricityMeter.currentValue) {
        this.s0ElectricityMeter = changes.s0ElectricityMeter.currentValue;
      } else {
        this.s0ElectricityMeter = this.s0ElectricityMeter || new S0ElectricityMeter();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterS0Component.error.', [
      new ErrorMessage('gpio', ValidatorType.required),
      new ErrorMessage('gpio', ValidatorType.pattern),
      new ErrorMessage('impulsesPerKwh', ValidatorType.required),
      new ErrorMessage('impulsesPerKwh', ValidatorType.pattern),
      new ErrorMessage('measurementInterval', ValidatorType.pattern),
    ], this.translate);
    const pinPullResistanceTypeKeys = Object.keys(PinPullResistance)
      .map(key => `MeterS0Component.pinPullResistance.${PinPullResistance[key]}`);
    this.translate.get(pinPullResistanceTypeKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.pinPullResistanceTypes.push({value: key.split('.')[2], viewValue: translatedStrings[key]});
      });
      console.log('TYPES=', this.pinPullResistanceTypes);
    });
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'gpio',
      this.s0ElectricityMeter && this.s0ElectricityMeter.gpio,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'pinPullResistance',
      this.s0ElectricityMeter && this.s0ElectricityMeter.pinPullResistance);
    this.formHandler.addFormControl(this.form, 'impulsesPerKwh',
      this.s0ElectricityMeter && this.s0ElectricityMeter.impulsesPerKwh,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'measurementInterval',
      this.s0ElectricityMeter && this.s0ElectricityMeter.measurementInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'gpio', this.s0ElectricityMeter.gpio);
    this.formHandler.setFormControlValue(this.form, 'pinPullResistance', this.s0ElectricityMeter.pinPullResistance);
    this.formHandler.setFormControlValue(this.form, 'impulsesPerKwh', this.s0ElectricityMeter.impulsesPerKwh);
    this.formHandler.setFormControlValue(this.form, 'measurementInterval', this.s0ElectricityMeter.measurementInterval);
  }

  updateModelFromForm(): S0ElectricityMeter | undefined {
    const gpio = getValidInt(this.form.controls.gpio.value);
    const pinPullResistance = this.form.controls.pinPullResistance.value;
    const impulsesPerKwh = getValidInt(this.form.controls.impulsesPerKwh.value);
    const measurementInterval = getValidInt(this.form.controls.measurementInterval.value);

    if (!(gpio || pinPullResistance || impulsesPerKwh || measurementInterval)) {
      return undefined;
    }

    this.s0ElectricityMeter.gpio = gpio;
    this.s0ElectricityMeter.pinPullResistance = pinPullResistance;
    this.s0ElectricityMeter.impulsesPerKwh = impulsesPerKwh;
    this.s0ElectricityMeter.measurementInterval = measurementInterval;
    return this.s0ElectricityMeter;
  }
}
