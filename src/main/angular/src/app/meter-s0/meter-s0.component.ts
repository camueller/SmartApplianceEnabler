import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Meter} from '../meter/meter';
import {MeterDefaults} from '../meter/meter-defaults';
import {S0ElectricityMeter} from './s0-electricity-meter';
import {Logger} from '../log/logger';
import {MeterService} from '../meter/meter-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {MeterS0NetworkedErrorMessages} from '../meter-s0-networked/meter-s0-networked-error-messages';

@Component({
  selector: 'app-meter-s0',
  templateUrl: './meter-s0.component.html',
  styles: []
})
export class MeterS0Component implements OnInit, AfterViewChecked {
  @Input()
  meter: Meter;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  applianceId: string;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private meterService: MeterService,
              private formMarkerService: FormMarkerService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages =  new MeterS0NetworkedErrorMessages(this.translate);
    this.form = this.buildS0ElectricityMeterFormGroup(this.meter.s0ElectricityMeter);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  buildS0ElectricityMeterFormGroup(s0ElectricityMeter: S0ElectricityMeter): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'gpio',
      s0ElectricityMeter ? s0ElectricityMeter.gpio : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'pinPullResistance',
      s0ElectricityMeter ? s0ElectricityMeter.pinPullResistance : undefined);
    this.formHandler.addFormControl(fg, 'impulsesPerKwh',
      s0ElectricityMeter ? s0ElectricityMeter.impulsesPerKwh : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'powerOnAlways',
      s0ElectricityMeter && s0ElectricityMeter.powerOnAlways );
    this.formHandler.addFormControl(fg, 'measurementInterval',
      s0ElectricityMeter ? s0ElectricityMeter.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    return fg;
  }

  updateS0ElectricityMeter(form: FormGroup, s0ElectricityMeter: S0ElectricityMeter) {
    s0ElectricityMeter.gpio = form.controls.gpio.value;
    s0ElectricityMeter.pinPullResistance = form.controls.pinPullResistance.value;
    s0ElectricityMeter.impulsesPerKwh = form.controls.impulsesPerKwh.value;
    s0ElectricityMeter.powerOnAlways = form.controls.powerOnAlways.value;
    s0ElectricityMeter.measurementInterval = form.controls.measurementInterval.value;
  }

  submitForm() {
    this.updateS0ElectricityMeter(this.form, this.meter.s0ElectricityMeter);
    this.meterService.updateMeter(this.meter, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
    this.childFormChanged.emit(this.form.valid);
  }
}
