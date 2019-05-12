import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Meter} from '../meter/meter';
import {MeterDefaults} from '../meter/meter-defaults';
import {FormGroup, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {MeterService} from '../meter/meter-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {TranslateService} from '@ngx-translate/core';
import {MeterS0ErrorMessages} from '../meter-s0/meter-s0-error-messages';
import {S0ElectricityMeter} from '../meter/s0-electricity-meter';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';

@Component({
  selector: 'app-meter-s0-networked',
  templateUrl: './meter-s0-networked.component.html',
  styles: []
})
export class MeterS0NetworkedComponent implements OnInit, AfterViewChecked {
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
    this.errorMessages =  new MeterS0ErrorMessages(this.translate);
    this.form = this.buildS0ElectricityMeterNetworkedFormGroup(this.meter.s0ElectricityMeterNetworked);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  buildS0ElectricityMeterNetworkedFormGroup(s0ElectricityMeterNetworked: S0ElectricityMeter): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'impulsesPerKwh',
      s0ElectricityMeterNetworked ? s0ElectricityMeterNetworked.impulsesPerKwh : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'measurementInterval',
      s0ElectricityMeterNetworked ? s0ElectricityMeterNetworked.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    return fg;
  }

  updateS0ElectricityMeter(form: FormGroup, s0ElectricityMeterNetworked: S0ElectricityMeter) {
    s0ElectricityMeterNetworked.impulsesPerKwh = form.controls.impulsesPerKwh.value;
    s0ElectricityMeterNetworked.measurementInterval = form.controls.measurementInterval.value;
  }

  submitForm() {
    this.updateS0ElectricityMeter(this.form, this.meter.s0ElectricityMeterNetworked);
    this.meterService.updateMeter(this.meter, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
    this.childFormChanged.emit(this.form.valid);
  }
}
