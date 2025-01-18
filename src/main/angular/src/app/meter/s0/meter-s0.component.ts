import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {S0ElectricityMeter} from './s0-electricity-meter';
import {TranslateService} from '@ngx-translate/core';
import {PinPullResistance} from './pin-pull-resistance';
import {ErrorMessages} from '../../shared/error-messages';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {MeterDefaults} from '../meter-defaults';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {ListItem} from '../../shared/list-item';
import {MeterS0Model} from './meter-s0.model';
import { isRequired } from 'src/app/shared/form-util';

@Component({
    selector: 'app-meter-s0',
    templateUrl: './meter-s0.component.html',
    styleUrls: ['./meter-s0.component.scss'],
    viewProviders: [
        { provide: ControlContainer, useExisting: FormGroupDirective }
    ],
    standalone: false
})
export class MeterS0Component implements OnChanges, OnInit {
  @Input()
  s0ElectricityMeter: S0ElectricityMeter;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  applianceId: string;
  form: FormGroup<MeterS0Model>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  pinPullResistanceTypes: ListItem[] = [];


  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.s0ElectricityMeter) {
      if (changes.s0ElectricityMeter.currentValue) {
        this.s0ElectricityMeter = changes.s0ElectricityMeter.currentValue;
      } else {
        this.s0ElectricityMeter = this.s0ElectricityMeter || new S0ElectricityMeter();
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterS0Component.error.', [
      new ErrorMessage('gpio', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('gpio', ValidatorType.pattern),
      new ErrorMessage('impulsesPerKwh', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('impulsesPerKwh', ValidatorType.pattern),
      new ErrorMessage('minPulseDuration', ValidatorType.pattern),
    ], this.translate);
    const pinPullResistanceTypeKeys = Object.keys(PinPullResistance)
      .map(key => `MeterS0Component.pinPullResistance.${PinPullResistance[key]}`);
    this.translate.get(pinPullResistanceTypeKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.pinPullResistanceTypes.push({value: key.split('.')[2], viewValue: translatedStrings[key]});
      });
    });
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('gpio', new FormControl(this.s0ElectricityMeter?.gpio,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]));
    this.form.addControl('pinPullResistance', new FormControl(this.s0ElectricityMeter?.pinPullResistance));
    this.form.addControl('impulsesPerKwh', new FormControl(this.s0ElectricityMeter?.impulsesPerKwh,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]));
    this.form.addControl('minPulseDuration', new FormControl(this.s0ElectricityMeter?.minPulseDuration,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
  }

  updateModelFromForm(): S0ElectricityMeter | undefined {
    const gpio = this.form.controls.gpio.value;
    const pinPullResistance = this.form.controls.pinPullResistance.value;
    const impulsesPerKwh = this.form.controls.impulsesPerKwh.value;
    const minPulseDuration = this.form.controls.minPulseDuration.value;

    if (!(gpio || pinPullResistance || impulsesPerKwh || minPulseDuration)) {
      return undefined;
    }

    this.s0ElectricityMeter.gpio = gpio;
    this.s0ElectricityMeter.pinPullResistance = pinPullResistance;
    this.s0ElectricityMeter.impulsesPerKwh = impulsesPerKwh;
    this.s0ElectricityMeter.minPulseDuration = minPulseDuration;
    return this.s0ElectricityMeter;
  }
}
