import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ModbusRead} from '../../modbus/read/modbus-read';
import {ModbusReadComponent} from '../../modbus/read/modbus-read.component';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../../shared/form-util';
import {MeterDefaults} from '../meter-defaults';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ModbusElectricityMeter} from './modbus-electricity-meter';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {MeterValueName} from '../meter-value-name';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {Logger} from '../../log/logger';
import {ModbusSetting} from '../../settings/modbus/modbus-setting';
import {MessageBoxLevel} from 'src/app/material/messagebox/messagebox.component';
import {ValueNameChangedEvent} from '../value-name-changed-event';
import {MeterModbusModel} from './meter-modbus.model';

@Component({
    selector: 'app-meter-modbus',
    templateUrl: './meter-modbus.component.html',
    styleUrls: ['./meter-modbus.component.scss'],
    viewProviders: [
        { provide: ControlContainer, useExisting: FormGroupDirective }
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class MeterModbusComponent implements OnChanges, OnInit {
  @Input()
  modbusElectricityMeter: ModbusElectricityMeter;
  @ViewChild(ModbusReadComponent, {static: true})
  modbusReadComp: ModbusReadComponent;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Input()
  modbusSettings: ModbusSetting[];
  @Input()
  applianceId: string;
  form: FormGroup<MeterModbusModel>;
  translatedStrings: { [key: string]: string } = {};
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  readValueName: MeterValueName;
  MessageBoxLevel = MessageBoxLevel;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.modbusElectricityMeter) {
      if (changes.modbusElectricityMeter.currentValue) {
        this.modbusElectricityMeter = changes.modbusElectricityMeter.currentValue;
      } else {
        this.modbusElectricityMeter = new ModbusElectricityMeter();
        this.modbusElectricityMeter.modbusReads = [ModbusRead.createWithSingleChild()];
      }
      this.expandParentForm();
    }
    if (changes.meterDefaults && changes.meterDefaults.currentValue) {
      this.meterDefaults = changes.meterDefaults.currentValue;
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterModbusComponent.error.', [
      new ErrorMessage('idref', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('slaveAddress', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
      new ErrorMessage('pollInterval', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get displayNoneStyle() {
    return this.modbusSettings.length === 0 ? {display: 'none'} : undefined;
  }

  get valueNames() {
    return [MeterValueName.Energy, MeterValueName.Power];
  }

  get valueNameTextKeys() {
    return ['MeterModbusComponent.Energy', 'MeterModbusComponent.Power'];
  }

  onNameChanged(event: ValueNameChangedEvent) {
    if (event.name === MeterValueName.Energy) {
      this.readValueName = MeterValueName.Energy;
    } else if (event.name === MeterValueName.Power) {
      this.readValueName = MeterValueName.Power;
    }
  }

  get displayPollInterval(): boolean {
    return this.readValueName === MeterValueName.Power;
  }

  get isAddModbusReadPossible() {
    return this.modbusElectricityMeter.modbusReads.length === 0;
  }

  get maxValues() {
    return 1;
  }

  get modbusReadsFormArray() {
    return this.form.controls.modbusReads;
  }

  getModbusReadFormGroup(index: number) {
    return this.modbusReadsFormArray.controls[index];
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('idref', new FormControl(this.modbusElectricityMeter.idref, Validators.required));
    this.form.addControl('slaveAddress', new FormControl(this.modbusElectricityMeter.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]));
    this.form.addControl('pollInterval', new FormControl(this.modbusElectricityMeter.pollInterval,
      Validators.pattern(InputValidatorPatterns.INTEGER)));
    this.form.addControl('modbusReads', buildFormArrayWithEmptyFormGroups(this.modbusElectricityMeter.modbusReads));
  }

  updateModelFromForm(): ModbusElectricityMeter | undefined {
    const idref = this.form.controls.idref.value;
    const slaveAddress = this.form.controls.slaveAddress.value;
    const pollInterval = this.form.controls.pollInterval.value;
    const modbusRead = this.modbusReadComp.updateModelFromForm();

    if (!(idref || slaveAddress || pollInterval || modbusRead)) {
      return undefined;
    }

    this.modbusElectricityMeter.idref = idref;
    this.modbusElectricityMeter.slaveAddress = slaveAddress;
    this.modbusElectricityMeter.pollInterval = pollInterval;
    this.modbusElectricityMeter.modbusReads = modbusRead ? [modbusRead] : undefined;
    return this.modbusElectricityMeter;
  }
}
