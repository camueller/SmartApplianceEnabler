import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {EvCharger} from './ev-charger';
import {EvChargerTemplates} from './ev-charger-templates';
import {Settings} from '../settings/settings';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ControlService} from '../control/control-service';
import {ElectricVehicle} from './electric-vehicle';
import {FormHandler} from '../shared/form-handler';
import {ControlDefaults} from '../control/control-defaults';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {EvChargerProtocol} from './ev-charger-protocol';
import {EvModbusControl} from '../control-evcharger-modbus/ev-modbus-control';
import {EvHttpControl} from '../control-evcharger-http/ev-http-control';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

declare const $: any;

@Component({
  selector: 'app-control-evcharger',
  templateUrl: './control-evcharger.component.html',
  styleUrls: ['./control-evcharger.component.css', '../global.css']
})
export class ControlEvchargerComponent implements OnInit, AfterViewChecked {
  @Input()
  evCharger: EvCharger;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  applianceId: string;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  form: FormGroup;
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
    this.expandParentForm(this.form, this.evCharger);
    this.errorMessages = new ErrorMessages('ControlEvchargerComponent.error.', [
      new ErrorMessage('voltage', ValidatorType.pattern),
      new ErrorMessage('phases', ValidatorType.pattern),
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('startChargingStateDetectionDelay', ValidatorType.pattern),
    ], this.translate);
    this.translate.get([
      'ControlEvchargerComponent.protocol.HTTP',
      'ControlEvchargerComponent.protocol.MODBUS',
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
    this.templates = EvChargerTemplates.getTemplates();
    // if (this.isConfigured()) {
    //   this.initForm(this.control.evCharger);
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
  }


  public updateModelFromForm(form: FormGroup, evCharger: EvCharger) {
    evCharger.voltage = form.controls.voltage.value ? form.controls.voltage.value : undefined;
    evCharger.phases = form.controls.phases.value ? form.controls.phases.value : undefined;
    evCharger.pollInterval = form.controls.pollInterval.value ? form.controls.pollInterval.value : undefined;
    evCharger.startChargingStateDetectionDelay = form.controls.startChargingStateDetectionDelay.value
      ? form.controls.startChargingStateDetectionDelay.value : undefined;
    evCharger.forceInitialCharging = form.controls.forceInitialCharging.value;

    // const evs: Array<ElectricVehicle> = [];
    // for (let i = 0; i < this.electricVehicles.length; i++) {
    //   const evControl = this.electricVehicles.at(i) as FormGroup;
    //   evs.push(this.buildElectricVehicle(evControl));
    // }
    // evCharger.vehicles = evs;
  }

  getEvFormControlPrefix(index: number) {
    return `ev${index}.`;
  }

  getTemplateNames(): string[] {
    return Object.keys(this.templates);
  }

  getTemplateNameSelected(): string {
    return this.form.controls.template.value;
  }

  useTemplate() {
    const templateName = this.getTemplateNameSelected();
    this.evCharger = this.templates[templateName];
    this.initForm(this.evCharger);
  }

  isConfigured(): boolean {
    return this.evCharger.modbusControl !== undefined
      || this.evCharger.httpControl !== undefined;
  }

  get evChargerProtocol() {
    if (this.evCharger.modbusControl && this.evCharger.modbusControl['@class'] === EvModbusControl.TYPE) {
      return EvChargerProtocol.MODBUS;
    } else if (this.evCharger.httpControl && this.evCharger.httpControl['@class'] === EvHttpControl.TYPE) {
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
    const newEvId = this.findNextEvId(this.evCharger.vehicles);
    const newEv = new ElectricVehicle({id: newEvId});
    if (!this.evCharger.vehicles) {
      this.evCharger.vehicles = [];
    }
    this.evCharger.vehicles.push(newEv);
    this.form.markAsDirty();
  }

  onElectricVehicleRemove(index: number) {
    this.evCharger.vehicles.splice(index, 1);
  }

  findNextEvId(evs: ElectricVehicle[]): number {
    const ids: number[] = evs.map(ev => ev.id);
    for (let i = 1; i < 100; i++) {
      if (ids.indexOf(i) < 0) {
        return i;
      }
    }
    return 0;
  }
}
