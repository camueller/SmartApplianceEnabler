import {
  ChangeDetectorRef,
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
import {Settings} from '../../settings/settings';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {TranslateService} from '@ngx-translate/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {ControlService} from '../control-service';
import {ElectricVehicle} from './electric-vehicle/electric-vehicle';
import {ControlDefaults} from '../control-defaults';
import {AppliancesReloadService} from '../../appliance/appliances-reload-service';
import {EvChargerProtocol} from './ev-charger-protocol';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../../shared/form-util';
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
import {ControlEvchargerModel} from './control-evcharger.model';
import {ElectricVehicleModel} from './electric-vehicle/electric-vehicle.model';

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
  form: FormGroup<ControlEvchargerModel>;
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
      if(! changes.evCharger.isFirstChange()) {
        this.updateForm();
      }
    }
  }

  ngOnInit() {
    this.form = this.parent.form;
    this.errorMessages = new ErrorMessages('ControlEvchargerComponent.error.', [
      new ErrorMessage('voltage', ValidatorType.pattern),
      new ErrorMessage('phases', ValidatorType.pattern),
      new ErrorMessage('pollInterval', ValidatorType.pattern),
      new ErrorMessage('chargePowerRepetition', ValidatorType.pattern),
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
    return this.evCharger && !!this.protocol;
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
    this.electricVehiclesFormArray.push(new FormGroup({} as ElectricVehicleModel));
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
    return this.form.controls.electricVehicles;
  }

  getElectricVehicleFormGroup(index: number) {
    return this.electricVehiclesFormArray.controls[index];
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('template', new FormControl());
    this.form.addControl('protocol', new FormControl(this.evCharger.protocol));
    this.form.addControl('voltage', new FormControl(this.evCharger.voltage, Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('phases', new FormControl(this.evCharger.phases, Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('pollInterval', new FormControl(this.evCharger.pollInterval, Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('startChargingStateDetectionDelay', new FormControl(this.evCharger.startChargingStateDetectionDelay,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('chargePowerRepetition', new FormControl(this.evCharger.chargePowerRepetition,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('forceInitialCharging', new FormControl(this.evCharger.forceInitialCharging));
    this.form.addControl('latitude', new FormControl(this.evCharger.latitude, Validators.pattern(InputValidatorPatterns.FLOAT)));
    this.form.addControl('longitude', new FormControl(this.evCharger.longitude, Validators.pattern(InputValidatorPatterns.FLOAT)));
    this.form.addControl('electricVehicles', buildFormArrayWithEmptyFormGroups(this.evCharger.vehicles));
  }

  updateForm() {
    if (!this.evCharger.modbusControl && !this.evCharger.httpControl) {
      this.form.controls.template.setValue(undefined);
      this.form.controls.protocol.setValue(this.evCharger.protocol);
    }
    this.form.controls.voltage.setValue(this.evCharger.voltage);
    this.form.controls.phases.setValue(this.evCharger.phases);
    this.form.controls.pollInterval.setValue(this.evCharger.pollInterval);
    this.form.controls.startChargingStateDetectionDelay.setValue(this.evCharger.startChargingStateDetectionDelay);
    this.form.controls.chargePowerRepetition.setValue(this.evCharger.chargePowerRepetition);
    this.form.controls.forceInitialCharging.setValue(this.evCharger.forceInitialCharging);
    this.form.setControl('electricVehicles', buildFormArrayWithEmptyFormGroups(this.evCharger.vehicles));
  }

  public updateModelFromForm(): EvCharger | undefined {
    const voltage = this.form.controls.voltage.value;
    const phases = this.form.controls.phases.value;
    const pollInterval = this.form.controls.pollInterval.value;
    const startChargingStateDetectionDelay = this.form.controls.startChargingStateDetectionDelay.value;
    const chargePowerRepetition = this.form.controls.chargePowerRepetition.value;
    const forceInitialCharging = this.form.controls.forceInitialCharging.value;
    const latitude = this.form.controls.latitude.value;
    const longitude = this.form.controls.longitude.value;
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

    if (!(voltage || phases || pollInterval || startChargingStateDetectionDelay || chargePowerRepetition
      || forceInitialCharging || longitude || latitude || modbusControl || httpControl || electricVehicles.length > 0)) {
      return undefined;
    }

    this.evCharger.voltage = voltage;
    this.evCharger.phases = phases;
    this.evCharger.pollInterval = pollInterval;
    this.evCharger.startChargingStateDetectionDelay = startChargingStateDetectionDelay;
    this.evCharger.chargePowerRepetition = chargePowerRepetition;
    this.evCharger.forceInitialCharging = forceInitialCharging;
    this.evCharger.latitude = latitude;
    this.evCharger.longitude = longitude;
    this.evCharger.modbusControl = modbusControl;
    this.evCharger.httpControl = httpControl;
    return this.evCharger;
  }
}
