import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, QueryList, SimpleChanges, ViewChildren} from '@angular/core';
import {ControlDefaults} from '../control-defaults';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ModbusSwitch} from './modbus-switch';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {FormHandler} from '../../shared/form-handler';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ControlValueName} from '../control-value-name';
import {getValidString} from '../../shared/form-util';
import {ModbusWriteComponent} from '../../modbus/write/modbus-write.component';
import {ModbusWrite} from '../../modbus/write/modbus-write';
import {ModbusSetting} from '../../settings/modbus/modbus-setting';
import { MessageBoxLevel } from 'src/app/material/messagebox/messagebox.component';

@Component({
  selector: 'app-control-modbus',
  templateUrl: './control-modbus.component.html',
  styleUrls: ['./control-modbus.component.scss'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlModbusComponent implements OnChanges, OnInit {
  @Input()
  modbusSwitch: ModbusSwitch;
  @ViewChildren('modbusWriteComponents')
  modbusWriteComps: QueryList<ModbusWriteComponent>;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  modbusSettings: ModbusSetting[];
  @Input()
  settingsDefaults: SettingsDefaults;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
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
    if (changes.modbusSwitch) {
      if (changes.modbusSwitch.currentValue) {
        this.modbusSwitch = changes.modbusSwitch.currentValue;
      } else {
        this.modbusSwitch = new ModbusSwitch();
        this.modbusSwitch.modbusWrites = [ModbusWrite.createWithSingleChild()];
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlModbusComponent.error.', [
      new ErrorMessage('idref', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('slaveAddress', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
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
    return [ControlValueName.On, ControlValueName.Off];
  }

  get valueNameTextKeys() {
    return ['ControlModbusComponent.On', 'ControlModbusComponent.Off'];
  }

  get isAddModbusWritePossible() {
    if (this.modbusSwitch.modbusWrites.length === 1) {
      return this.modbusSwitch.modbusWrites[0].writeValues.length < 2;
    }
    return this.modbusSwitch.modbusWrites.length < 2;
  }

  get maxValues() {
    return this.modbusSwitch.modbusWrites.length === 2 ? 1 : 2;
  }

  addModbusWrite() {
    this.modbusSwitch.modbusWrites.push(ModbusWrite.createWithSingleChild());
    this.modbusWritesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  onModbusWriteRemove(index: number) {
    this.modbusSwitch.modbusWrites.splice(index, 1);
    this.modbusWritesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get modbusWritesFormArray() {
    return this.form.controls.modbusWrites as FormArray;
  }

  getModbusWriteFormGroup(index: number) {
    return this.modbusWritesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'idref', this.modbusSwitch.idref,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'slaveAddress', this.modbusSwitch.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusWrites',
      this.modbusSwitch.modbusWrites);
  }

  updateModelFromForm(): ModbusSwitch | undefined {
    const idref = getValidString(this.form.controls.idref.value);
    const slaveAddress = getValidString(this.form.controls.slaveAddress.value);
    const modbusWrites = [];
    this.modbusWriteComps.forEach(modbusWriteComponent => {
      const modbusWrite = modbusWriteComponent.updateModelFromForm();
      if (modbusWrite) {
        modbusWrites.push(modbusWrite);
      }
    });

    if (!(idref || slaveAddress || modbusWrites.length > 0)) {
      return undefined;
    }

    this.modbusSwitch.idref = idref;
    this.modbusSwitch.slaveAddress = slaveAddress;
    this.modbusSwitch.modbusWrites = modbusWrites;
    return this.modbusSwitch;
  }
}
