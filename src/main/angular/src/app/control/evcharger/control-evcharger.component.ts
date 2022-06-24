import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, QueryList, SimpleChanges, ViewChild, ViewChildren} from '@angular/core';
import {EvCharger} from './ev-charger';
import {Settings} from '../../settings/settings';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {ControlService} from '../control-service';
import {ElectricVehicle} from './electric-vehicle/electric-vehicle';
import {FormHandler} from '../../shared/form-handler';
import {ControlDefaults} from '../control-defaults';
import {AppliancesReloadService} from '../../appliance/appliances-reload-service';
import {EvChargerProtocol} from './ev-charger-protocol';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {getValidFloat, getValidInt} from '../../shared/form-util';
import {ElectricVehicleComponent} from './electric-vehicle/electric-vehicle.component';
import {EvModbusControl} from './modbus/ev-modbus-control';
import {ControlEvchargerModbusComponent} from './modbus/control-evcharger-modbus.component';
import {EvHttpControl} from './http/ev-http-control';
import {ControlEvchargerHttpComponent} from './http/control-evcharger-http.component';
import {ListItem} from '../../shared/list-item';
import {MeterDefaults} from '../../meter/meter-defaults';
import {EvReadValueName} from './ev-read-value-name';
import {EvWriteValueName} from './ev-write-value-name';
import {EvChargerTemplate} from './ev-charger-template';

@Component({
  selector: 'app-control-evcharger',
  templateUrl: './control-evcharger.component.html',
  styleUrls: ['./control-evcharger.component.scss'],
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
  meterDefaults: MeterDefaults;
  @Input()
  applianceId: string;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Input()
  templates: EvChargerTemplate[];
  @ViewChild(ControlEvchargerModbusComponent)
  evChargerModbusComp: ControlEvchargerModbusComponent;
  @ViewChild(ControlEvchargerHttpComponent)
  evChargerHttpComp: ControlEvchargerHttpComponent;
  @ViewChildren('electricVehicles')
  electricVehicleComps: QueryList<ElectricVehicleComponent>;
  form: FormGroup;
  formHandler: FormHandler;
  translatedStrings: { [key: string]: string } = {};
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  protocols: ListItem[] = [];

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService,
              private changeDetectorRef: ChangeDetectorRef) {
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
        this.evCharger.vehicles = [this.createElectricVehicle()];
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
      new ErrorMessage('latitude', ValidatorType.pattern),
      new ErrorMessage('longitude', ValidatorType.pattern),
    ], this.translate);
    const protocolKeys = Object.keys(EvChargerProtocol).map(key => `ControlEvchargerComponent.protocol.${EvChargerProtocol[key]}`);
    this.translate.get(protocolKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.protocols.push({value: key.split('.')[2], viewValue: translatedStrings[key]});
      });
    });
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get templateNames(): string[] {
    return this.templates.map(template => template.name);
  }

  getTemplateNameSelected(): string {
    return this.form.controls.template.value;
  }

  useTemplate() {
    const templateName = this.getTemplateNameSelected();
    const evChargerFromTemplate = this.templates.find(template => template.name === templateName)?.template;
    this.updateModelFromForm(); // preserve configured but unsaved vehicles
    this.evCharger = new EvCharger({...evChargerFromTemplate, vehicles: this.evCharger.vehicles});
    this.setProtocol(this.evCharger.protocol);
    this.updateForm();
    this.changeDetectorRef.detectChanges();
    this.form.markAsDirty();
  }

  get isConfigured(): boolean {
    return this.evCharger && this.protocol;
  }

  setProtocol(protocol: string) {
    this.form.controls.protocol.setValue(protocol);
  }

  get protocol() {
    return this.form.controls.protocol?.value;
  }

  get isProtocolModbus() {
    return this.protocol === EvChargerProtocol.MODBUS;
  }

  get isProtocolHttp() {
    return this.protocol === EvChargerProtocol.HTTP;
  }

  get valueNames() {
    return [
      ...Object.keys(EvReadValueName),
      ...Object.keys(EvWriteValueName),
    ].map(key => `ControlEvchargerComponent.${key}`);
  }

  findNextEvId(evs: ElectricVehicle[]): number {
    if (evs) {
      const ids: number[] = evs.map(ev => ev.id);
      for (let i = 1; i < 100; i++) {
        if (ids.indexOf(i) < 0) {
          return i;
        }
      }
    }
    return 1;
  }

  addElectricVehicle() {
    if (!this.evCharger.vehicles) {
      this.evCharger.vehicles = [];
    }
    this.evCharger.vehicles.push(this.createElectricVehicle());
    this.electricVehiclesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  createElectricVehicle() {
    const newEvId = this.findNextEvId(this.evCharger.vehicles);
    return new ElectricVehicle({id: newEvId});
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
    this.formHandler.addFormControl(this.form, 'protocol', this.evCharger.protocol);
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
    this.formHandler.addFormControl(this.form, 'latitude', this.evCharger.latitude,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    this.formHandler.addFormControl(this.form, 'longitude', this.evCharger.longitude,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'electricVehicles',
      this.evCharger.vehicles);
  }

  updateForm() {
    if (!this.evCharger.modbusControl && !this.evCharger.httpControl) {
      this.formHandler.setFormControlValue(this.form, 'template', undefined);
      this.formHandler.setFormControlValue(this.form, 'protocol', this.evCharger.protocol);
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
    const latitude = getValidFloat(this.form.controls.latitude.value);
    const longitude = getValidFloat(this.form.controls.longitude.value);
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

    if (!(voltage || phases || pollInterval || startChargingStateDetectionDelay || longitude || latitude
      || modbusControl || httpControl || electricVehicles.length > 0)) {
      return undefined;
    }

    this.evCharger.voltage = voltage;
    this.evCharger.phases = phases;
    this.evCharger.pollInterval = pollInterval;
    this.evCharger.startChargingStateDetectionDelay = startChargingStateDetectionDelay;
    this.evCharger.forceInitialCharging = forceInitialCharging;
    this.evCharger.latitude = latitude;
    this.evCharger.longitude = longitude;
    this.evCharger.modbusControl = modbusControl;
    this.evCharger.httpControl = httpControl;
    return this.evCharger;
  }
}
