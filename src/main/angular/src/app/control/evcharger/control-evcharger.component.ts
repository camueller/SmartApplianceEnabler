import {Component, Input, OnChanges, OnInit, QueryList, SimpleChanges, ViewChild, ViewChildren} from '@angular/core';
import {EvCharger} from './ev-charger';
import {EvChargerTemplates} from './ev-charger-templates';
import {Settings} from '../../settings/settings';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {ControlService} from '../control-service';
import {ElectricVehicle} from './electric-vehicle';
import {FormHandler} from '../../shared/form-handler';
import {ControlDefaults} from '../control-defaults';
import {AppliancesReloadService} from '../../appliance/appliances-reload-service';
import {EvChargerProtocol} from './ev-charger-protocol';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidInt} from '../../shared/form-util';
import {ElectricVehicleComponent} from '../electric-vehicle/electric-vehicle.component';
import {EvModbusControl} from '../evcharger-modbus/ev-modbus-control';
import {ControlEvchargerModbusComponent} from '../evcharger-modbus/control-evcharger-modbus.component';
import {EvHttpControl} from '../evcharger-http/ev-http-control';
import {ControlEvchargerHttpComponent} from '../evcharger-http/control-evcharger-http.component';

@Component({
  selector: 'app-control-evcharger',
  templateUrl: './control-evcharger.component.html',
  styleUrls: ['./control-evcharger.component.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerComponent implements OnChanges, OnInit {
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
  protocols: ListItem[] = [];

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
      this.updateForm();
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
    const protocolKeys = Object.keys(EvChargerProtocol).map(key => `ControlEvchargerComponent.protocol.${EvChargerProtocol[key]}`);
    this.translate.get(protocolKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.protocols.push({value: key.split('.')[2], viewValue: translatedStrings[key]});
      });
    });
    this.templates = EvChargerTemplates.getTemplates();
    this.templateNames = Object.keys(this.templates);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  getTemplateNameSelected(): string {
    return this.form.controls.template.value;
  }

  useTemplate() {
    const templateName = this.getTemplateNameSelected();
    this.evCharger = this.templates[templateName];
    this.setProtocol(this.evChargerProtocol);
    this.updateForm();
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

  setProtocol(protocol: EvChargerProtocol) {
    this.form.controls.protocol.setValue(protocol);
  }

  get protocol() {
    return this.form.controls.protocol.value;
  }

  get isProtocolModbus() {
    return this.protocol === EvChargerProtocol.MODBUS;
  }

  get isProtocolHttp() {
    return this.protocol === EvChargerProtocol.HTTP;
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

  findNextEvId(evs: ElectricVehicle[]): number {
    const ids: number[] = evs.map(ev => ev.id);
    for (let i = 1; i < 100; i++) {
      if (ids.indexOf(i) < 0) {
        return i;
      }
    }
    return 0;
  }

  addElectricVehicle() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    const newEvId = this.findNextEvId(this.evCharger.vehicles);
    const newEv = new ElectricVehicle({id: newEvId});
    if (!this.evCharger.vehicles) {
      this.evCharger.vehicles = [];
    }
    this.evCharger.vehicles.push(newEv);
    this.electricVehiclesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  onElectricVehicleRemove(index: number) {
    this.evCharger.vehicles.splice(index, 1);
    this.electricVehiclesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get electricVehiclesFormArray() {
    return this.form.controls.electricVehicles as FormArray;
  }

  getElectricVehicleFormGroup(index: number) {
    return this.electricVehiclesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'template', undefined);
    this.formHandler.addFormControl(this.form, 'protocol', this.evChargerProtocol);
    this.formHandler.addFormControl(this.form, 'voltage', this.evCharger.voltage,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'phases', this.evCharger.phases,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'pollInterval', this.evCharger.pollInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'startChargingStateDetectionDelay',
      this.evCharger.startChargingStateDetectionDelay, [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'forceInitialCharging',
      this.evCharger.forceInitialCharging);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'electricVehicles',
      this.evCharger.vehicles);
  }

  updateForm() {
    if (!(this.evCharger.modbusControl && this.evCharger.httpControl)) {
      this.formHandler.setFormControlValue(this.form, 'template', undefined);
      this.formHandler.setFormControlValue(this.form, 'protocol', this.evChargerProtocol);
    }
    this.formHandler.setFormControlValue(this.form, 'voltage', this.evCharger.voltage);
    this.formHandler.setFormControlValue(this.form, 'phases', this.evCharger.phases);
    this.formHandler.setFormControlValue(this.form, 'pollInterval', this.evCharger.pollInterval);
    this.formHandler.setFormControlValue(this.form, 'startChargingStateDetectionDelay',
      this.evCharger.startChargingStateDetectionDelay);
    this.formHandler.setFormControlValue(this.form, 'forceInitialCharging',
      this.evCharger.forceInitialCharging);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'electricVehicles',
      this.evCharger.vehicles);
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
}
