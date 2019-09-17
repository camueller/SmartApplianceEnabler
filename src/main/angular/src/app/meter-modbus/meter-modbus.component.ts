import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
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

@Component({
  selector: 'app-meter-modbus',
  templateUrl: './meter-modbus.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterModbusComponent implements OnInit, AfterViewChecked {
  @Input()
  modbusElectricityMeter: ModbusElectricityMeter;
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
  translationKeys: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
    this.translationKeys = [].concat(this.powerValueNameTextKeys, this.energyValueNameTextKeys);
  }

  ngOnInit() {
    this.modbusElectricityMeter = this.modbusElectricityMeter || new ModbusElectricityMeter();
    if (!this.modbusElectricityMeter.powerModbusRead) {
      this.modbusElectricityMeter.powerModbusRead = new ModbusRead();
    }
    if (!this.modbusElectricityMeter.powerModbusRead.readValues) {
      this.modbusElectricityMeter.powerModbusRead.readValues = [new ModbusReadValue()];
    }
    if (!this.modbusElectricityMeter.energyModbusRead) {
      this.modbusElectricityMeter.energyModbusRead = new ModbusRead();
    }
    if (!this.modbusElectricityMeter.energyModbusRead.readValues) {
      this.modbusElectricityMeter.energyModbusRead.readValues = [new ModbusReadValue()];
    }
    this.errorMessages = new ErrorMessages('MeterModbusComponent.error.', [
      new ErrorMessage('slaveAddress', ValidatorType.required),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('measurementInterval', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.modbusElectricityMeter, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }


  get powerValueNames() {
    return ['Power'];
  }

  get powerValueNameTextKeys() {
    return ['MeterModbusComponent.Power'];
  }

  get energyValueNames() {
    return ['Energy'];
  }

  get energyValueNameTextKeys() {
    return ['MeterModbusComponent.Energy'];
  }

  expandParentForm(form: FormGroup, modbusElectricityMeter: ModbusElectricityMeter, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'idref',
      modbusElectricityMeter ? modbusElectricityMeter.idref : undefined);
    formHandler.addFormControl(form, 'slaveAddress',
      modbusElectricityMeter ? modbusElectricityMeter.slaveAddress : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    formHandler.addFormControl(form, 'pollInterval',
      modbusElectricityMeter ? modbusElectricityMeter.pollInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    formHandler.addFormControl(form, 'measurementInterval',
      modbusElectricityMeter ? modbusElectricityMeter.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(modbusElectricityMeter: ModbusElectricityMeter, form: FormGroup) {
    // modbusElectricityMeter.idref = form.controls.idref.value;
    // modbusElectricityMeter.slaveAddress = form.controls.slaveAddress.value;
    // modbusElectricityMeter.pollInterval = form.controls.pollInterval.value;
    // modbusElectricityMeter.measurementInterval = form.controls.measurementInterval.value;
  }
}
