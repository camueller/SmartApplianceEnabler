import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EvCharger} from './ev-charger';
import {EvChargerTemplates} from './ev-charger-templates';
import {Settings} from '../settings/settings';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {FormArray, FormControl, FormGroup, Validators} from '@angular/forms';
import {ControlEvchargerErrorMessages} from './control-evcharger-error-messages';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';
import {ControlService} from '../control/control-service';
import {Control} from '../control/control';
import {ElectricVehicle} from './electric-vehicle';
import {FormHandler} from '../shared/form-handler';
import {SocScript} from './soc-script';
import {ControlDefaults} from '../control/control-defaults';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {EvModbusWriteRegisterName} from './ev-modbus-write-register-name';

declare const $: any;

@Component({
  selector: 'app-control-evcharger',
  templateUrl: './control-evcharger.component.html',
  styleUrls: ['./control-evcharger.component.css', '../global.css']
})
export class ControlEvchargerComponent implements OnInit, AfterViewChecked {
  @Input()
  control: Control;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  applianceId: string;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  form: FormGroup;
  modbusConfigurations: FormArray;
  electricVehicles: FormArray;
  formHandler: FormHandler;
  templates: { [name: string]: EvCharger };
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages =  new ControlEvchargerErrorMessages(this.translate);
    this.translate.get([
      'ControlEvchargerComponent.VehicleNotConnected',
      'ControlEvchargerComponent.VehicleConnected',
      'ControlEvchargerComponent.Charging',
      'ControlEvchargerComponent.ChargingCompleted',
      'ControlEvchargerComponent.Error',
      'ControlEvchargerComponent.StartCharging',
      'ControlEvchargerComponent.StopCharging',
      'ControlEvchargerComponent.ChargingCurrent'
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
    this.templates = EvChargerTemplates.getTemplates();
    if (this.isConfigured()) {
      this.initForm(this.control.evCharger);
    } else {
      this.form = this.buildEmptyEvChargerFormGroup();
    }
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  initForm(evCharger: EvCharger) {
    this.form = this.buildEvChargerFormGroup(evCharger);
    this.form.markAsPristine();
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.pristine);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  buildEmptyEvChargerFormGroup(): FormGroup {
    return new FormGroup({
      template: new FormControl()
    });
  }

  buildEvChargerFormGroup(evCharger: EvCharger): FormGroup {
    this.modbusConfigurations = new FormArray(
      this.control.evCharger.control.configuration.map(
        configuration => this.buildModbusConfigurationFormGroup(configuration))
    );
    this.electricVehicles = new FormArray(this.control.evCharger.vehicles ?
      this.control.evCharger.vehicles.map(ev => this.buildElectricVehicleFormGroup(ev)) : []
    );
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'template', undefined);
    this.formHandler.addFormControl(fg, 'voltage', evCharger.voltage,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'phases', evCharger.phases,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'pollInterval', evCharger.pollInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'startChargingStateDetectionDelay',
      evCharger.startChargingStateDetectionDelay, [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'forceInitialCharging', evCharger.forceInitialCharging);
    this.formHandler.addFormControl(fg, 'modbusIdref', evCharger.control.idref, [Validators.required]);
    this.formHandler.addFormControl(fg, 'slaveAddress', evCharger.control.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    fg.addControl('modbusConfigurations', this.modbusConfigurations);
    fg.addControl('electricVehicles', this.electricVehicles);
    return fg;
  }

  buildElectricVehicleFormGroup(ev: ElectricVehicle, newId?: number): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'id', ev && ev.id || newId);
    this.formHandler.addFormControl(fg, 'name', ev && ev.name, [Validators.required]);
    this.formHandler.addFormControl(fg, 'batteryCapacity', ev && ev.batteryCapacity,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'phases', ev && ev.phases,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'maxChargePower', ev && ev.maxChargePower,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'chargeLoss', ev && ev.chargeLoss,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'defaultSocManual', ev && ev.defaultSocManual,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'defaultSocOptionalEnergy',
      ev && ev.defaultSocOptionalEnergy, [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    const scriptEnabled: boolean = ev && ev.socScript && (ev.socScript.script !== undefined);
    this.formHandler.addFormControl(fg, 'scriptEnabled', scriptEnabled);
    this.formHandler.addFormControl(fg, 'scriptFilename', ev && ev.socScript && ev.socScript.script,
      [Validators.required]);
    this.formHandler.addFormControl(fg, 'scriptExtractionRegex',
      ev && ev.socScript && ev.socScript.extractionRegex);
    this.setScriptEnabled(fg, scriptEnabled);
    return fg;
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
    this.updateModbusConfigurationValueValidator(fg, configuration.write);
    fg.get('write').valueChanges.forEach(write => this.updateModbusConfigurationValueValidator(fg, write));
    return fg;
  }

  private updateModbusConfigurationValueValidator(fg: FormGroup, writeEnabled: boolean) {
    // FIXME FormHandler cannot handle Validators on FormArrays within form
    // if (writeEnabled) {
    //   this.formHandler.setValidators(fg, 'value', [Validators.required]);
    // } else {
    //   this.formHandler.clearValidators(fg, 'value');
    // }
    // this.formHandler.markLabelsRequired();
  }

  public updateEvCharger(form: FormGroup, evCharger: EvCharger) {
    evCharger.voltage = form.controls.voltage.value;
    evCharger.phases = form.controls.phases.value;
    evCharger.pollInterval = form.controls.pollInterval.value;
    evCharger.startChargingStateDetectionDelay = form.controls.startChargingStateDetectionDelay.value;
    evCharger.forceInitialCharging = form.controls.forceInitialCharging.value;
    evCharger.control.idref = form.controls.modbusIdref.value;
    evCharger.control.slaveAddress = form.controls.slaveAddress.value;

    const configurations: Array<ModbusRegisterConfguration> = [];
    const configurationsFormArray = (form.controls.modbusConfigurations as FormArray);
    for (let i = 0; i < configurationsFormArray.getRawValue().length; i++) {
      const modbusConfigurationFormControl = configurationsFormArray.at(i) as FormGroup;
      configurations.push(this.buildModbusRegisterConfguration(modbusConfigurationFormControl));
    }
    evCharger.control.configuration = configurations;

    const evs: Array<ElectricVehicle> = [];
    for (let i = 0; i < this.electricVehicles.length; i++) {
      const evControl = this.electricVehicles.at(i) as FormGroup;
      evs.push(this.buildElectricVehicle(evControl));
    }
    evCharger.vehicles = evs;
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

 buildElectricVehicle(evFormControl: FormGroup): ElectricVehicle {
    let newSocScript: SocScript;
    if (evFormControl.controls.scriptEnabled.value) {
      newSocScript = new SocScript({
        script: evFormControl.controls.scriptFilename.value,
        extractionRegex: evFormControl.controls.scriptExtractionRegex.value
      });
    }
    return new ElectricVehicle({
      id: evFormControl.controls.id.value,
      name: evFormControl.controls.name.value,
      batteryCapacity: evFormControl.controls.batteryCapacity.value,
      phases: evFormControl.controls.phases.value,
      maxChargePower: evFormControl.controls.maxChargePower.value,
      chargeLoss: evFormControl.controls.chargeLoss.value,
      defaultSocManual: evFormControl.controls.defaultSocManual.value,
      defaultSocOptionalEnergy: evFormControl.controls.defaultSocOptionalEnergy.value,
      socScript: newSocScript
    });
  }

  findNextEvId(evs: FormArray): number {
    const ids: number[] = [];
    for (let i = 0; i < evs.length; i++) {
      const ev = evs.at(i) as FormGroup;
      ids.push(ev.controls.id.value);
    }
    for (let i = 1; i < 100; i++) {
      if (ids.indexOf(i) < 0) {
        return i;
      }
    }
    return 0;
  }

  getTemplateNames(): string[] {
    return Object.keys(this.templates);
  }

  getTemplateNameSelected(): string {
    return this.form.controls.template.value;
  }

  useTemplate() {
    const templateName = this.getTemplateNameSelected();
    this.control.evCharger = this.templates[templateName];
    this.initForm(this.control.evCharger);
  }

  isConfigured(): boolean {
    return this.control.evCharger.control !== undefined;
  }

  getTranslatedModbusRegisterName(name: string) {
    return this.translatedStrings[this.toTextKeyModbusRegisterName(name)];
  }

  toTextKeyModbusRegisterName(name: string) {
    return 'ControlEvchargerComponent.' + name;
  }

  isModbusWriteRegister(modbusConfiguration: FormGroup): boolean {
    const writeControl = modbusConfiguration.controls['write'];
    return (writeControl ? writeControl.value : false);
  }

  isChargingCurrentRegister(modbusConfiguration: FormGroup): boolean {
    const control = modbusConfiguration.controls['name'];
    return control.value === EvModbusWriteRegisterName.ChargingCurrent;
  }

  getRegisterType(modbusConfiguration: FormGroup): string {
    const typeControl = modbusConfiguration.controls['registerType'];
    return (typeControl ? typeControl.value : '');
  }

  getModbusRegisterTypes(write: boolean): string[] {
    if (write) {
      return this.settingsDefaults.modbusWriteRegisterTypes;
    }
    return this.settingsDefaults.modbusReadRegisterTypes;
  }

  getModbusRegisterNames(): string[] {
    return this.control.evCharger.control.configuration
      .map(configuration => configuration.name)
      .filter((v, i, a) => a.indexOf(v) === i);
  }

  addModbusConfiguration() {
    this.modbusConfigurations.push(this.buildModbusConfigurationFormGroup({} as ModbusRegisterConfguration));
    this.form.markAsDirty();
  }

  removeModbusConfiguration(index: number) {
    this.modbusConfigurations.removeAt(index);
    this.form.markAsDirty();
  }

  getByteOrders(): string[] {
    return ['BigEndian', 'LittleEndian'];
  }

  getIndexedErrorMessage(key: string, index: number): string {
    const indexedKey = key + '.' + index.toString();
    return this.errors[indexedKey];
  }

  addElectricVehicle() {
    const newEvId = this.findNextEvId(this.electricVehicles);
    const newEv = this.buildElectricVehicleFormGroup(undefined, newEvId);
    this.electricVehicles.push(newEv);
    this.form.markAsDirty();
  }

  removeElectricVehicle(index: number) {
    this.electricVehicles.removeAt(index);
    this.form.markAsDirty();
  }

  onScriptEnabledToggle(index: number, enabled: boolean) {
    const ev = this.electricVehicles.at(index) as FormGroup;
    this.setScriptEnabled(ev, enabled);
  }

  setScriptEnabled(ev: FormGroup, enabled: boolean) {
    if (enabled) {
      ev.controls.scriptFilename.enable();
      ev.controls.scriptExtractionRegex.enable();
    } else {
      ev.controls.scriptFilename.disable();
      ev.controls.scriptExtractionRegex.disable();
    }
  }

  isScriptEnabled(index: number) {
    const ev = this.electricVehicles.at(index) as FormGroup;
    return ev.enabled;
  }

  submitForm() {
    this.updateEvCharger(this.form, this.control.evCharger);
    this.controlService.updateControl(this.control, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
    this.childFormChanged.emit(this.form.pristine);
  }
}
