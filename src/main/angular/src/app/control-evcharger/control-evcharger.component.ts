import {
  AfterViewChecked,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {EvCharger} from './ev-charger';
import {EvChargerTemplates} from './ev-charger-templates';
import {Settings} from '../settings/settings';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
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
import {ControlEvchargerHttpComponent} from '../control-evcharger-http/control-evcharger-http.component';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidInt} from '../shared/form-util';
import {ElectricVehicleComponent} from '../electric-vehicle/electric-vehicle.component';
import {ControlEvchargerModbusComponent} from '../control-evcharger-modbus/control-evcharger-modbus.component';
import {HttpRead} from '../http-read/http-read';

declare const $: any;

@Component({
  selector: 'app-control-evcharger',
  templateUrl: './control-evcharger.component.html',
  styleUrls: ['./control-evcharger.component.css', '../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerComponent implements OnChanges, OnInit, AfterViewChecked {
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
  @ViewChild(ControlEvchargerModbusComponent, {static: false})
  evChargerModbusComp: ControlEvchargerModbusComponent;
  @ViewChild(ControlEvchargerHttpComponent, {static: false})
  evChargerHttpComp: ControlEvchargerHttpComponent;
  @ViewChildren('electricVehicles')
  electricVehicleComps: QueryList<ElectricVehicleComponent>;
  form: FormGroup;
  formHandler: FormHandler;
  templates: { [name: string]: EvCharger };
  templateNames: string[];
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  PROTOCOL_MODBUS = EvChargerProtocol.MODBUS;
  PROTOCOL_HTTP = EvChargerProtocol.HTTP;
  protocols: { id: string, name: string }[] = [
    {id: EvChargerProtocol.MODBUS, name: undefined},
    {id: EvChargerProtocol.HTTP, name: undefined}
  ];

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.evCharger) {
      if (changes.evCharger.currentValue) {
        this.evCharger = changes.evCharger.currentValue;
      } else {
        this.evCharger = new EvCharger();
        this.evCharger.vehicles = [];
      }
      this.updateForm(this.form, this.evCharger, this.formHandler);
    }
  }

  ngOnInit() {
    this.form = this.parent.form;
    this.errorMessages = new ErrorMessages('ControlEvchargerComponent.error.', [
      new ErrorMessage('voltage', ValidatorType.pattern),
      new ErrorMessage('phases', ValidatorType.pattern),
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('startChargingStateDetectionDelay', ValidatorType.pattern),
    ], this.translate);
    this.translate.get([
      'ControlEvchargerComponent.protocol.HTTP',
      'ControlEvchargerComponent.protocol.MODBUS',
    ]).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
      this.protocols.forEach(protocol =>
        protocol.name = this.translatedStrings[`ControlEvchargerComponent.protocol.${protocol.id}`]);
    });
    this.templates = EvChargerTemplates.getTemplates();
    this.templateNames = Object.keys(this.templates);
    this.expandParentForm(this.form, this.evCharger);
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  expandParentForm(form: FormGroup, evCharger: EvCharger) {
    this.formHandler.addFormControl(form, 'template', undefined);
    this.formHandler.addFormControl(form, 'protocol', this.evChargerProtocol);
    this.formHandler.addFormControl(form, 'voltage', undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'phases', undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'pollInterval', undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'startChargingStateDetectionDelay',
      undefined, [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(form, 'forceInitialCharging', evCharger.forceInitialCharging);
  }

  updateForm(form: FormGroup, evCharger: EvCharger, formHandler: FormHandler) {
    if (!(evCharger.modbusControl && evCharger.httpControl)) {
      formHandler.setFormControlValue(form, 'template', undefined);
      formHandler.setFormControlValue(form, 'protocol', this.evChargerProtocol);
    }
    formHandler.setFormControlValue(form, 'voltage', evCharger.voltage);
    formHandler.setFormControlValue(form, 'phases', evCharger.phases);
    formHandler.setFormControlValue(form, 'pollInterval', evCharger.pollInterval);
    formHandler.setFormControlValue(form, 'startChargingStateDetectionDelay', evCharger.startChargingStateDetectionDelay);
    formHandler.setFormControlValue(form, 'forceInitialCharging', evCharger.forceInitialCharging);
  }

  public updateModelFromForm(): EvCharger | undefined {
    const voltage = getValidInt(this.form.controls.voltage.value);
    const phases = getValidInt(this.form.controls.phases.value);
    const pollInterval = getValidInt(this.form.controls.pollInterval.value);
    const startChargingStateDetectionDelay = getValidInt(this.form.controls.startChargingStateDetectionDelay.value);
    const forceInitialCharging = this.form.controls.forceInitialCharging.value;
    let modbusControl: EvModbusControl;
    if (this.evChargerModbusComp) {
      modbusControl = this.evChargerModbusComp.updateModelFromForm();
      this.evCharger.httpControl = undefined;
    }
    let httpControl: EvHttpControl;
    if (this.evChargerHttpComp) {
      httpControl = this.evChargerHttpComp.updateModelFromForm();
      this.evCharger.modbusControl = undefined;
    }
    const electricVehicles = [];
    this.electricVehicleComps.forEach(electricVehicleComp => {
      const electricVehicle = electricVehicleComp.updateModelFromForm();
      if (electricVehicles) {
        electricVehicles.push(electricVehicle);
      }
    });

    if (!(voltage || phases || pollInterval || startChargingStateDetectionDelay
      || modbusControl || httpControl || electricVehicles.length > 0)) {
      return undefined;
    }

    this.evCharger.voltage = voltage;
    this.evCharger.phases = phases;
    this.evCharger.pollInterval = pollInterval;
    this.evCharger.startChargingStateDetectionDelay = startChargingStateDetectionDelay;
    this.evCharger.forceInitialCharging = forceInitialCharging;
    this.evCharger.modbusControl = modbusControl;
    this.evCharger.httpControl = httpControl;
    return this.evCharger;
  }

  getEvFormControlPrefix(index: number) {
    return `ev${index}.`;
  }

  getTemplateNameSelected(): string {
    return this.form.controls.template.value;
  }

  useTemplate() {
    const templateName = this.getTemplateNameSelected();
    this.evCharger = this.templates[templateName];
    this.setProtocol(this.evChargerProtocol);
    this.updateForm(this.form, this.evCharger, this.formHandler);
    this.form.markAsDirty();
  }

  isConfigured(): boolean {
    return this.evCharger && this.protocol;
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

  setProtocol(protocol: EvChargerProtocol) {
    this.form.controls.protocol.setValue(protocol);
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

  addElectricVehicle() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
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
