import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {EvCharger} from './ev-charger';
import {EvChargerTemplates} from './ev-charger-templates';
import {Settings} from '../settings/settings';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {FormArray, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ControlEvchargerErrorMessages} from './control-evcharger-error-messages';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ControlService} from '../control/control-service';
import {Control} from '../control/control';
import {ElectricVehicle} from './electric-vehicle';
import {FormHandler} from '../shared/form-handler';
import {SocScript} from './soc-script';
import {ControlDefaults} from '../control/control-defaults';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {EvChargerProtocol} from './ev-charger-protocol';
import {EvModbusControl} from '../control-evcharger-modbus/ev-modbus-control';
import {EvHttpControl} from '../control-evcharger-http/ev-http-control';

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
  form: FormGroup;
  electricVehicles: FormArray;
  formHandler: FormHandler;
  templates: { [name: string]: EvCharger };
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  PROTOCOL_MODBUS = EvChargerProtocol.MODBUS;
  PROTOCOL_HTTP = EvChargerProtocol.HTTP;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.control.evCharger);
    this.errorMessages = new ControlEvchargerErrorMessages(this.translate);
    this.translate.get([
      'ControlEvchargerComponent.protocol.HTTP',
      'ControlEvchargerComponent.protocol.MODBUS',
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
    this.templates = EvChargerTemplates.getTemplates();
    // if (this.isConfigured()) {
      // this.initForm(this.control.evCharger);
    // } else {
    //   this.form = this.buildEmptyEvChargerFormGroup();
    // }
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  initForm(evCharger: EvCharger) {
    // this.form = this.buildEvChargerFormGroup(evCharger);
    this.form.markAsPristine();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  // FIXME: alle Enums indirect liefern
  get modbusTranslationKeys() {
    return [
      'ControlEvchargerComponent.VehicleNotConnected',
      'ControlEvchargerComponent.VehicleConnected',
      'ControlEvchargerComponent.Charging',
      'ControlEvchargerComponent.ChargingCompleted',
      'ControlEvchargerComponent.Error',
      'ControlEvchargerComponent.StartCharging',
      'ControlEvchargerComponent.StopCharging',
      'ControlEvchargerComponent.ChargingCurrent'
    ];
  }

  buildEmptyEvChargerFormGroup(): FormGroup {
    return new FormGroup({
      template: new FormControl()
    });
  }

  expandParentForm(form: FormGroup, evCharger: EvCharger) {
    this.electricVehicles = new FormArray(this.control.evCharger.vehicles ?
      this.control.evCharger.vehicles.map(ev => this.buildElectricVehicleFormGroup(ev)) : []
    );
    this.formHandler.addFormControl(form, 'template', undefined);
    this.formHandler.addFormControl(form, 'protocol', this.evChargerProtocol);
    this.formHandler.addFormControl(form, 'voltage', evCharger.voltage,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'phases', evCharger.phases,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'pollInterval', evCharger.pollInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'startChargingStateDetectionDelay',
      evCharger.startChargingStateDetectionDelay, [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'forceInitialCharging', evCharger.forceInitialCharging);
    form.addControl('electricVehicles', this.electricVehicles);
  }

  buildElectricVehicleFormGroup(ev: ElectricVehicle, newId?: number): FormGroup {
    const fg = new FormGroup({});
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

  public updateModelFromForm(form: FormGroup, evCharger: EvCharger) {
    evCharger.voltage = form.controls.voltage.value ? form.controls.voltage.value : undefined;
    evCharger.phases = form.controls.phases.value ? form.controls.phases.value : undefined;
    evCharger.pollInterval = form.controls.pollInterval.value ? form.controls.pollInterval.value : undefined;
    evCharger.startChargingStateDetectionDelay = form.controls.startChargingStateDetectionDelay.value
      ? form.controls.startChargingStateDetectionDelay.value : undefined;
    evCharger.forceInitialCharging = form.controls.forceInitialCharging.value;
    evCharger.modbusControl.idref = form.controls.modbusIdref.value;
    evCharger.modbusControl.slaveAddress = form.controls.slaveAddress.value;

    const evs: Array<ElectricVehicle> = [];
    for (let i = 0; i < this.electricVehicles.length; i++) {
      const evControl = this.electricVehicles.at(i) as FormGroup;
      evs.push(this.buildElectricVehicle(evControl));
    }
    evCharger.vehicles = evs;
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
    return this.control.evCharger.modbusControl !== undefined
      || this.control.evCharger.httpControl !== undefined;
  }

  get evChargerProtocol() {
    if (this.control.evCharger.modbusControl && this.control.evCharger.modbusControl['@class'] === EvModbusControl.TYPE) {
      return EvChargerProtocol.MODBUS;
    } else if (this.control.evCharger.httpControl && this.control.evCharger.httpControl['@class'] === EvHttpControl.TYPE) {
      return EvChargerProtocol.HTTP;
    }
    return undefined;
  }

  get protocol() {
    return this.form.controls.protocol.value;
  }

  get protocols() {
    return Object.keys(EvChargerProtocol);
  }

  getProtocolTranslationKey(protocol: string) {
    return `ControlEvchargerComponent.protocol.${protocol}`;
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
}
