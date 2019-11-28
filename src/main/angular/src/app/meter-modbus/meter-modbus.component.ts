import {
  AfterViewChecked,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {MeterDefaults} from '../meter/meter-defaults';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusElectricityMeter} from './modbus-electricity-meter';
import {ModbusSettings} from '../settings/modbus-settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ModbusReadValue} from '../modbus-read-value/modbus-read-value';
import {ModbusRead} from '../modbus-read/modbus-read';
import {MeterValueName} from '../meter/meter-value-name';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidInt, getValidString} from '../shared/form-util';
import {ModbusReadComponent} from '../modbus-read/modbus-read.component';

@Component({
  selector: 'app-meter-modbus',
  templateUrl: './meter-modbus.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterModbusComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  modbusElectricityMeter: ModbusElectricityMeter;
  @ViewChildren('modbusReadComponents')
  modbusReadComps: QueryList<ModbusReadComponent>;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Input()
  modbusSettings: ModbusSettings[];
  @Input()
  applianceId: string;
  form: FormGroup;
  formHandler: FormHandler;
  translatedStrings: string[];
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
    if (changes.modbusElectricityMeter) {
      if (changes.modbusElectricityMeter.currentValue) {
        this.modbusElectricityMeter = changes.modbusElectricityMeter.currentValue;
      } else {
        this.modbusElectricityMeter = new ModbusElectricityMeter();
        this.modbusElectricityMeter.modbusReads = [this.createModbusRead()];
      }
    }
    this.form = this.parent.form;
    this.updateForm();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterModbusComponent.error.', [
      new ErrorMessage('slaveAddress', ValidatorType.required),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('measurementInterval', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get valueNames() {
    return [MeterValueName.Power, MeterValueName.Energy];
  }

  get valueNameTextKeys() {
    return ['MeterModbusComponent.Power', 'MeterModbusComponent.Energy'];
  }

  get isAddModbusReadPossible() {
    if (this.modbusElectricityMeter.modbusReads.length === 1) {
      return this.modbusElectricityMeter.modbusReads[0].readValues.length < 2;
    }
    return this.modbusElectricityMeter.modbusReads.length < 2;
  }

  get maxValues() {
    return this.modbusElectricityMeter.modbusReads.length === 2 ? 1 : 2;
  }

  addModbusRead() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    this.modbusElectricityMeter.modbusReads.push(this.createModbusRead());
    this.form.markAsDirty();
  }

  onModbusReadRemove(index: number) {
    this.modbusElectricityMeter.modbusReads.splice(index, 1);
  }

  createModbusRead() {
    const modbusRead = new ModbusRead();
    modbusRead.readValues = [new ModbusReadValue()];
    return modbusRead;
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'idref', this.modbusElectricityMeter.idref,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'slaveAddress', this.modbusElectricityMeter.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(this.form, 'pollInterval', this.modbusElectricityMeter.pollInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'measurementInterval', this.modbusElectricityMeter.measurementInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'idref', this.modbusElectricityMeter.idref);
    this.formHandler.setFormControlValue(this.form, 'slaveAddress', this.modbusElectricityMeter.slaveAddress);
    this.formHandler.setFormControlValue(this.form, 'pollInterval', this.modbusElectricityMeter.pollInterval);
    this.formHandler.setFormControlValue(this.form, 'measurementInterval', this.modbusElectricityMeter.measurementInterval);
  }

  updateModelFromForm(): ModbusElectricityMeter | undefined {
    const idref = getValidString(this.form.controls.idref.value);
    const slaveAddress = getValidString(this.form.controls.slaveAddress.value);
    const pollInterval = getValidInt(this.form.controls.pollInterval.value);
    const measurementInterval = getValidInt(this.form.controls.measurementInterval.value);
    const modbusReads = [];
    this.modbusReadComps.forEach(modbusReadComponent => {
      const modbusRead = modbusReadComponent.updateModelFromForm();
      if (modbusRead) {
        modbusReads.push(modbusRead);
      }
    });

    if (!(idref || slaveAddress || pollInterval || measurementInterval || modbusReads.length > 0)) {
      return undefined;
    }

    this.modbusElectricityMeter.idref = idref;
    this.modbusElectricityMeter.slaveAddress = slaveAddress;
    this.modbusElectricityMeter.pollInterval = pollInterval;
    this.modbusElectricityMeter.measurementInterval = measurementInterval;
    this.modbusElectricityMeter.modbusReads = modbusReads;
    return this.modbusElectricityMeter;
  }
}
