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
import {MeterS0NetworkedErrorMessages} from '../meter-s0-networked/meter-s0-networked-error-messages';
import {S0ElectricityMeter} from '../meter/s0-electricity-meter';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusElectricityMeter} from '../meter/modbus-electricity-meter';
import {ModbusSettings} from '../settings/modbus-settings';

@Component({
  selector: 'app-meter-modbus',
  templateUrl: './meter-modbus.component.html',
  styles: []
})
export class MeterModbusComponent implements OnInit, AfterViewChecked {
  @Input()
  meter: Meter;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  modbusSettings: ModbusSettings[];
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
    console.log('meter=', this.meter);
    console.log('modbusSettings=', this.modbusSettings);
    this.errorMessages =  new MeterS0NetworkedErrorMessages(this.translate);
    this.form = this.buildFormGroup(this.meter.modbusElectricityMeter);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get powerValueNames() {
    return ['Power'];
  }

  get readRegisterTypes() {
    return ['InputFloat'];
  }

  get writeRegisterTypes() {
    return ['Holding'];
  }

  get powerConfiguration() {
    return this.meter.modbusElectricityMeter.powerConfiguration;
  }

  buildFormGroup(modbusElectricityMeter: ModbusElectricityMeter): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'idref',
      modbusElectricityMeter ? modbusElectricityMeter.idref : undefined);
    this.formHandler.addFormControl(fg, 'slaveAddress',
      modbusElectricityMeter ? modbusElectricityMeter.slaveAddress : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(fg, 'pollInterval',
      modbusElectricityMeter ? modbusElectricityMeter.pollInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'measurementInterval',
      modbusElectricityMeter ? modbusElectricityMeter.measurementInterval : undefined,
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
