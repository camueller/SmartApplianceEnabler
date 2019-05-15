import {AfterViewChecked, Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {MeterDefaults} from '../meter/meter-defaults';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {MeterService} from '../meter/meter-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusElectricityMeter} from './modbus-electricity-meter';
import {ModbusSettings} from '../settings/modbus-settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {NestedFormService} from '../shared/nested-form-service';
import {MeterModbusErrorMessages} from './meter-modbus-error-messages';

@Component({
  selector: 'app-meter-modbus',
  templateUrl: './meter-modbus.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class MeterModbusComponent implements OnInit, AfterViewChecked, OnDestroy {
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
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  form: FormGroup;
  formHandler: FormHandler;
  translatedStrings: string[];
  translationKeys: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private meterService: MeterService,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private appliancesReloadService: AppliancesReloadService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
    this.translationKeys = [].concat(this.powerValueNameTextKeys, this.energyValueNameTextKeys);
  }

  ngOnInit() {
    this.errorMessages = new MeterModbusErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.modbusElectricityMeter);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    console.log('Meter-Modbus subsribe');
    this.nestedFormService.submitted.subscribe(() => this.updateModbusElectricityMeter());
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
     // FIXME: erzeugt Fehler bei Wechsel des Zählertypes
    // this.nestedFormService.submitted.unsubscribe();
  }

  get powerValueNames() {
    return ['Power'];
  }

  get powerValueNameTextKeys() {
    return ['MeterModbusComponent.power'];
  }

  get powerConfiguration() {
    return this.modbusElectricityMeter.powerConfiguration;
  }

  get energyValueNames() {
    return ['Energy'];
  }

  get energyValueNameTextKeys() {
    return ['MeterModbusComponent.energy'];
  }

  get energyConfiguration() {
    return this.modbusElectricityMeter.energyConfiguration;
  }

  get readRegisterTypes() {
    return this.settingsDefaults.modbusReadRegisterTypes;
  }

  expandParentForm(modbusElectricityMeter: ModbusElectricityMeter) {
    this.formHandler.addFormControl(this.form, 'idref',
      modbusElectricityMeter ? modbusElectricityMeter.idref : undefined);
    this.formHandler.addFormControl(this.form, 'slaveAddress',
      modbusElectricityMeter ? modbusElectricityMeter.slaveAddress : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(this.form, 'pollInterval',
      modbusElectricityMeter ? modbusElectricityMeter.pollInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'measurementInterval',
      modbusElectricityMeter ? modbusElectricityMeter.measurementInterval : undefined,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModbusElectricityMeter() {
    this.modbusElectricityMeter.idref = this.form.controls.idref.value;
    this.modbusElectricityMeter.slaveAddress = this.form.controls.slaveAddress.value;
    this.modbusElectricityMeter.pollInterval = this.form.controls.pollInterval.value;
    this.modbusElectricityMeter.measurementInterval = this.form.controls.measurementInterval.value;
    this.nestedFormService.complete();
    console.log('ModbusElectricityMeter=', this.modbusElectricityMeter);
  }
}
