import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {NestedFormService} from '../shared/nested-form-service';
import {MeterHttpErrorMessages} from '../meter-http/meter-http-error-messages';
import {FormMarkerService} from '../shared/form-marker-service';
import {Settings} from '../settings/settings';
import {EvModbusControl} from '../control-evcharger/ev-modbus-control';
import {SettingsDefaults} from '../settings/settings-defaults';
import {EvModbusWriteRegisterName} from '../control-evcharger/ev-modbus-write-register-name';
import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';

@Component({
  selector: 'app-control-evcharger-modbus',
  templateUrl: './control-evcharger-modbus.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerModbusComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input()
  evModbusControl: EvModbusControl;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  modbusConfigurations: FormArray;
  form: FormGroup;
  formHandler: FormHandler;
  @Input()
  translationKeys: string[];
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new MeterHttpErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.evModbusControl, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
    this.nestedFormService.submitted.subscribe(
      () => this.updateFromForm(this.evModbusControl, this.form));
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    // FIXME: erzeugt Fehler bei Wechsel des Zählertypes
    // this.nestedFormService.submitted.unsubscribe();
  }

  getModbusRegisterNames(): string[] {
    return this.evModbusControl.configuration
      .map(configuration => configuration.name)
      .filter((v, i, a) => a.indexOf(v) === i);
  }

  getTranslatedModbusRegisterName(name: string) {
    return this.translatedStrings[this.toTextKeyModbusRegisterName(name)];
  }

  toTextKeyModbusRegisterName(name: string) {
    return 'ControlEvchargerComponent.' + name;
  }

  getModbusRegisterTypes(write: boolean): string[] {
    if (write) {
      return this.settingsDefaults.modbusWriteRegisterTypes;
    }
    return this.settingsDefaults.modbusReadRegisterTypes;
  }

  getRegisterType(modbusConfiguration: FormGroup): string {
    const typeControl = modbusConfiguration.controls['registerType'];
    return (typeControl ? typeControl.value : '');
  }

  isModbusWriteRegister(modbusConfiguration: FormGroup): boolean {
    const writeControl = modbusConfiguration.controls['write'];
    return (writeControl ? writeControl.value : false);
  }

  isChargingCurrentRegister(modbusConfiguration: FormGroup): boolean {
    const control = modbusConfiguration.controls['name'];
    return control.value === EvModbusWriteRegisterName.ChargingCurrent;
  }

  getByteOrders(): string[] {
    return ['BigEndian', 'LittleEndian'];
  }

  getIndexedErrorMessage(key: string, index: number): string {
    const indexedKey = key + '.' + index.toString();
    return this.errors[indexedKey];
  }

  expandParentForm(form: FormGroup, evModbusControl: EvModbusControl, formHandler: FormHandler) {
    // formHandler.addFormControl(form, 'pollInterval',
    //   httpElectricityMeter ? httpElectricityMeter.pollInterval : undefined,
    //   [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    // formHandler.addFormControl(form, 'measurementInterval',
    //   httpElectricityMeter ? httpElectricityMeter.measurementInterval : undefined,
    //   [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    // formHandler.addFormControl(form, 'contentProtocol',
    //   httpElectricityMeter ? httpElectricityMeter.contentProtocol : undefined);
    this.modbusConfigurations = new FormArray(
      this.evModbusControl.configuration.map(
        configuration => this.buildModbusConfigurationFormGroup(configuration))
    );
    this.form.addControl('modbusConfigurations', this.modbusConfigurations);
    this.formHandler.addFormControl(form, 'modbusIdref', evModbusControl.idref,
      [Validators.required]);
    this.formHandler.addFormControl(form, 'slaveAddress', evModbusControl.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateFromForm(evModbusControl: EvModbusControl, form: FormGroup) {
    const configurations: Array<ModbusRegisterConfguration> = [];
    const configurationsFormArray = (form.controls.modbusConfigurations as FormArray);
    for (let i = 0; i < configurationsFormArray.getRawValue().length; i++) {
      const modbusConfigurationFormControl = configurationsFormArray.at(i) as FormGroup;
      configurations.push(this.buildModbusRegisterConfguration(modbusConfigurationFormControl));
    }
    this.evModbusControl.configuration = configurations;
    this.nestedFormService.complete();
  }

  buildModbusConfigurationFormGroup(configuration: ModbusRegisterConfguration): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'name', configuration.name, [Validators.required]);
    this.formHandler.addFormControl(fg, 'registerAddress', configuration.address,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(fg, 'write', configuration.write);
    this.formHandler.addFormControl(fg, 'registerType', configuration.type, [Validators.required]);
    this.formHandler.addFormControl(fg, 'bytes', configuration.bytes,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'byteOrder', configuration.byteOrder);
    this.formHandler.addFormControl(fg, 'extractionRegex', configuration.extractionRegex);
    this.formHandler.addFormControl(fg, 'value', configuration.value);
    this.formHandler.addFormControl(fg, 'factorToValue', configuration.factorToValue);
    // this.updateModbusConfigurationValueValidator(fg, configuration.write);
    // fg.get('write').valueChanges.forEach(write => this.updateModbusConfigurationValueValidator(fg, write));
    return fg;
  }

  buildModbusRegisterConfguration(modbusConfigurationFormControl: FormGroup): ModbusRegisterConfguration {
    const name = modbusConfigurationFormControl.controls.name.value;

    let value = modbusConfigurationFormControl.controls.value.value;
    if (name === EvModbusWriteRegisterName.ChargingCurrent && ! value) {
      value = '0';
    }

    const factorToValueControlValue = modbusConfigurationFormControl.controls.factorToValue.value;
    let factorToValue;
    if (factorToValueControlValue !== null) {
      factorToValue = factorToValueControlValue.toString().length > 0 ? factorToValueControlValue : undefined;
    }

    return {
      enabled: true,
      name,
      address: modbusConfigurationFormControl.controls.registerAddress.value,
      write: modbusConfigurationFormControl.controls.write.value,
      type: modbusConfigurationFormControl.controls.registerType.value,
      bytes: modbusConfigurationFormControl.controls.bytes.value,
      byteOrder: modbusConfigurationFormControl.controls.byteOrder.value,
      extractionRegex: modbusConfigurationFormControl.controls.extractionRegex.value,
      factorToValue,
      value,
    };
  }

  addModbusConfiguration() {
    this.modbusConfigurations.push(this.buildModbusConfigurationFormGroup({} as ModbusRegisterConfguration));
    this.form.markAsDirty();
  }

  removeModbusConfiguration(index: number) {
    this.modbusConfigurations.removeAt(index);
    this.form.markAsDirty();
  }
}
