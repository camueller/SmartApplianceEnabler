import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EvCharger} from '../control/ev-charger';
import {EvChargerTemplates} from '../control/ev-charger-templates';
import {Settings} from '../settings/settings';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {ControlEvchargerErrorMessages} from './control-evcharger-error-messages';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';
import {ControlService} from '../control/control-service';
import {Control} from '../control/control';
import {EvModbusControl} from '../control/ev-modbus-control';

@Component({
  selector: 'app-control-evcharger',
  templateUrl: './control-evcharger.component.html',
  styles: []
})
export class ControlEvchargerComponent implements OnInit {
  @Input()
  control: Control;
  @Input()
  applianceId: string;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  evChargerForm: FormGroup;
  modbusConfigurations: FormArray;
  templates: { [name: string]: EvCharger };
  translatedStrings: string[];
  discardChangesMessage: string;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private formbuilder: FormBuilder,
              private controlService: ControlService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    console.log('control=', this.control);
    this.errorMessages =  new ControlEvchargerErrorMessages(this.translate);
    this.translate.get([
      'ControlComponent.evcharger_VehicleNotConnected',
      'ControlComponent.evcharger_VehicleConnected',
      'ControlComponent.evcharger_Charging',
      'ControlComponent.evcharger_ChargingCompleted',
      'ControlComponent.evcharger_Error',
      'ControlComponent.evcharger_StartCharging',
      'ControlComponent.evcharger_StopCharging',
      'ControlComponent.evcharger_ChargingCurrent'
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.templates = EvChargerTemplates.getTemplates();
    if (this.control.evCharger) {
      // this.evChargerForm = this.buildEvChargerFormGroup(this.control.evCharger);
      // this.evChargerForm.markAsPristine();
      // this.evChargerForm.statusChanges.subscribe(() => {
      //   this.childFormChanged.emit(this.evChargerForm.valid);
      //   this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.evChargerForm, this.errorMessages);
      // });
      this.initForm(this.control.evCharger);
    } else {
      this.evChargerForm = new FormGroup({template: new FormControl()});
    }
  }

  initForm(evCharger: EvCharger) {
    this.evChargerForm = this.buildEvChargerFormGroup(evCharger);
    this.evChargerForm.markAsPristine();
    this.evChargerForm.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.evChargerForm.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.evChargerForm, this.errorMessages);
    });
  }

  buildEvChargerFormGroup(evCharger: EvCharger): FormGroup {
    this.modbusConfigurations = this.formbuilder.array(
      this.control.evCharger.control.configuration.map(
        configuration => this.buildModbusConfigurationFormGroup(configuration))
    );
    return new FormGroup({
      template: new FormControl(),
      voltage: new FormControl(evCharger.voltage),
      phases: new FormControl(evCharger.phases),
      pollInterval: new FormControl(evCharger.pollInterval),
      startChargingStateDetectionDelay: new FormControl(evCharger.startChargingStateDetectionDelay),
      forceInitialCharging: new FormControl(evCharger.forceInitialCharging),
      modbusIdref: new FormControl(evCharger.control.idref),
      modbusSlaveAddress: new FormControl(evCharger.control.slaveAddress,
        [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]),
      modbusConfigurations: this.modbusConfigurations
    });
  }

  public buildEvCharger(form: FormGroup, evCharger: EvCharger): EvCharger {
    evCharger.voltage = form.controls.voltage.value;
    evCharger.phases = form.controls.phases.value;
    evCharger.pollInterval = form.controls.pollInterval.value;
    evCharger.startChargingStateDetectionDelay = form.controls.startChargingStateDetectionDelay.value;
    evCharger.forceInitialCharging = form.controls.forceInitialCharging.value;
    evCharger.control.idref = form.controls.modbusIdref.value;
    evCharger.control.slaveAddress = form.controls.modbusSlaveAddress.value;
    const configurations: Array<ModbusRegisterConfguration> = [];

    const configurationsFormArray = (form.controls.modbusConfigurations as FormArray);
    for (let i = 0; i < configurationsFormArray.getRawValue().length ; i++) {
      const modbusConfigurationFormControl = configurationsFormArray.at(i) as FormGroup;
      configurations.push(this.buildModbusRegisterConfguration(modbusConfigurationFormControl));
    }
    evCharger.control.configuration = configurations;
    return evCharger;
  }

  buildModbusConfigurationFormGroup(configuration: ModbusRegisterConfguration): FormGroup {
    const modbusConfiguration = new FormGroup({
      name: new FormControl(configuration.name, [Validators.required]),
      registerAddress: new FormControl(configuration.address,
        [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]),
      write: new FormControl(configuration.write),
      registerType: new FormControl(configuration.type, [Validators.required]),
      bytes: new FormControl(configuration.bytes),
      byteOrder: new FormControl(configuration.byteOrder),
      extractionRegex: new FormControl(configuration.extractionRegex),
      value: new FormControl(configuration.value),
    });
    return modbusConfiguration;
  }

  buildModbusRegisterConfguration(modbusConfigurationFormControl: FormGroup): ModbusRegisterConfguration {
    return {
      name: modbusConfigurationFormControl.controls.name.value,
      address: modbusConfigurationFormControl.controls.registerAddress.value,
      write: modbusConfigurationFormControl.controls.write.value,
      type: modbusConfigurationFormControl.controls.registerType.value,
      bytes: modbusConfigurationFormControl.controls.bytes.value,
      byteOrder: modbusConfigurationFormControl.controls.byteOrder.value,
      extractionRegex: modbusConfigurationFormControl.controls.extractionRegex.value,
      factorToValue: undefined,
      value: modbusConfigurationFormControl.controls.value.value,
    };
  }

  getTemplateNames(): string[] {
    return Object.keys(this.templates);
  }

  getTemplateNameSelected(): string {
    return this.evChargerForm.controls.template.value;
  }

  useTemplate() {
    this.control.type = EvCharger.TYPE;
    const templateName = this.getTemplateNameSelected();
    this.control.evCharger = this.templates[templateName];
    this.initForm(this.control.evCharger);
  }

  isConfigured(): boolean {
    return this.control.evCharger !== undefined;
  }

  getTranslatedModbusRegisterName(name: string) {
    return this.translatedStrings[this.toTextKeyModbusRegisterName(name)];
  }

  toTextKeyModbusRegisterName(name: string) {
    return 'ControlComponent.evcharger_' + name;
  }

  isModbusWriteRegister(modbusConfiguration: FormGroup): boolean {
    const writeControl = modbusConfiguration.controls['write'];
    return (writeControl ? writeControl.value : false);
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
    this.evChargerForm.markAsDirty();
  }

  removeModbusConfiguration(index: number) {
    this.modbusConfigurations.removeAt(index);
    this.evChargerForm.markAsDirty();
  }

  getByteOrders(): string[] {
    return ['BigEndian', 'LittleEndian'];
  }

  getIndexedErrorMessage(key: string, index: number): string {
    const indexedKey = key + '.' + index.toString();
    return this.errors[indexedKey];
  }

  submitForm() {
    this.control.evCharger = this.buildEvCharger(this.evChargerForm, this.control.evCharger);
    this.controlService.updateControl(this.control, this.applianceId).subscribe();
    this.evChargerForm.markAsPristine();
  }
}
