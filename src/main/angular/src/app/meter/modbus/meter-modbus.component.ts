import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ModbusRead} from '../../modbus/read/modbus-read';
import {ModbusReadComponent} from '../../modbus/read/modbus-read.component';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {getValidInt, getValidString} from '../../shared/form-util';
import {MeterDefaults} from '../meter-defaults';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ModbusElectricityMeter} from './modbus-electricity-meter';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {MeterValueName} from '../meter-value-name';
import {FormHandler} from '../../shared/form-handler';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {Logger} from '../../log/logger';
import {ModbusSetting} from '../../settings/modbus/modbus-setting';
import {MessageBoxLevel} from 'src/app/material/messagebox/messagebox.component';
import {ValueNameChangedEvent} from '../value-name-changed-event';
import {ReadValueMapKey} from '../../shared/read-value-map-key';

@Component({
  selector: 'app-meter-modbus',
  templateUrl: './meter-modbus.component.html',
  styleUrls: ['./meter-modbus.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeterModbusComponent implements OnChanges, OnInit {
  @Input()
  modbusElectricityMeter: ModbusElectricityMeter;
  @ViewChildren('modbusReadComponents')
  modbusReadComps: QueryList<ModbusReadComponent>;
  @Input()
  meterDefaults: MeterDefaults;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Input()
  modbusSettings: ModbusSetting[];
  @Input()
  isEvCharger: boolean;
  @Input()
  applianceId: string;
  form: FormGroup;
  formHandler: FormHandler;
  translatedStrings: { [key: string]: string } = {};
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  readValueNames = new Map<ReadValueMapKey, string>();
  MessageBoxLevel = MessageBoxLevel;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
              private changeDetectorRef: ChangeDetectorRef
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
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

  onNameChanged(index: number, event: ValueNameChangedEvent) {
    const key: ReadValueMapKey = {readIndex: index, readValueIndex: event.valueIndex};
    if (event.name) {
      this.readValueNames.set(key, event.name);
    } else {
      Array.from(this.readValueNames.keys()).forEach(aKey => {
        if (aKey.readIndex === key.readIndex && aKey.readValueIndex === key.readValueIndex) {
          this.readValueNames.delete(aKey);
        }
      });
    }
  }

  get displayPollInterval(): boolean {
    let display = false;
    this.readValueNames.forEach(value => {
      if (!display && value === MeterValueName.Power) {
        display = true;
      }
    });
    return display;
  }

  get isAddModbusReadPossible() {
    return this.modbusElectricityMeter.modbusReads.length === 0;
  }

  get maxValues() {
    return 1;
  }

  addModbusRead() {
    this.modbusElectricityMeter.modbusReads.push(ModbusRead.createWithSingleChild());
    this.modbusReadsFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  onModbusReadRemove(index: number) {
    this.modbusElectricityMeter.modbusReads.splice(index, 1);
    this.modbusReadsFormArray.removeAt(index);

    Array.from(this.readValueNames.keys()).forEach(aKey => {
      if (aKey.readIndex === index) {
        this.readValueNames.delete(aKey);
      }
    });

    this.form.markAsDirty();
  }

  get modbusReadsFormArray() {
    return this.form.controls.modbusReads as FormArray;
  }

  getModbusReadFormGroup(index: number) {
    return this.modbusReadsFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'idref', this.modbusElectricityMeter.idref,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'slaveAddress', this.modbusElectricityMeter.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(this.form, 'pollInterval', this.modbusElectricityMeter.pollInterval,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusReads',
      this.modbusElectricityMeter.modbusReads);
  }

  updateModelFromForm(): ModbusElectricityMeter | undefined {
    const idref = getValidString(this.form.controls.idref.value);
    const slaveAddress = getValidString(this.form.controls.slaveAddress.value);
    const pollInterval = getValidInt(this.form.controls.pollInterval.value);
    const modbusReads = [];
    this.modbusReadComps.forEach(modbusReadComponent => {
      const modbusRead = modbusReadComponent.updateModelFromForm();
      if (modbusRead) {
        modbusReads.push(modbusRead);
      }
    });

    if (!(idref || slaveAddress || pollInterval || modbusReads.length > 0)) {
      return undefined;
    }

    this.modbusElectricityMeter.idref = idref;
    this.modbusElectricityMeter.slaveAddress = slaveAddress;
    this.modbusElectricityMeter.pollInterval = pollInterval;
    this.modbusElectricityMeter.modbusReads = modbusReads;
    return this.modbusElectricityMeter;
  }
}
