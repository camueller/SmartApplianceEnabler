import {
  AfterViewChecked,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Settings} from '../../settings/settings';
import {EvModbusControl} from './ev-modbus-control';
import {SettingsDefaults} from '../../settings/settings-defaults';
import {EvModbusWriteRegisterName} from './ev-modbus-write-register-name';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {EvModbusReadRegisterName} from './ev-modbus-read-register-name';
import {getValidString} from '../../shared/form-util';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ModbusWriteComponent} from '../../modbus/write/modbus-write.component';
import {ModbusWrite} from '../../modbus/write/modbus-write';
import {ModbusRead} from '../../modbus/read/modbus-read';
import {ModbusReadComponent} from '../../modbus/read/modbus-read.component';

@Component({
  selector: 'app-control-evcharger-modbus',
  templateUrl: './control-evcharger-modbus.component.html',
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerModbusComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  evModbusControl: EvModbusControl;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  @ViewChildren('modbusReadComponents')
  modbusReadComps: QueryList<ModbusReadComponent>;
  @ViewChildren('modbusWriteComponents')
  modbusWriteComps: QueryList<ModbusWriteComponent>;
  form: FormGroup;
  formHandler: FormHandler;
  @Input()
  translationKeys: string[];
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.evModbusControl) {
      if (changes.evModbusControl.currentValue) {
        this.evModbusControl = changes.evModbusControl.currentValue;
      } else {
        this.evModbusControl = new EvModbusControl();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlEvchargerModbusComponent.error.', [
      new ErrorMessage('slaveAddress', ValidatorType.required),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get readValueNames() {
    return Object.keys(EvModbusReadRegisterName);
  }

  get writeValueNames() {
    return Object.keys(EvModbusWriteRegisterName);
  }

  get readValueNameTextKeys() {
    return Object.keys(EvModbusReadRegisterName).map(key => `ControlEvchargerComponent.${key}`);
  }

  get writeValueNameTextKeys() {
    return Object.keys(EvModbusWriteRegisterName).map(key => `ControlEvchargerComponent.${key}`);
  }

  addModbusRead() {
    const modbusRead = ModbusRead.createWithSingleChild();
    if (!this.evModbusControl.modbusReads) {
      this.evModbusControl.modbusReads = [];
    }
    this.evModbusControl.modbusReads.push(modbusRead);
    this.modbusReadsFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  onModbusReadRemove(index: number) {
    this.evModbusControl.modbusReads.splice(index, 1);
    this.modbusReadsFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  addModbusWrite() {
    const modbusWrite = ModbusWrite.createWithSingleChild();
    if (!this.evModbusControl.modbusWrites) {
      this.evModbusControl.modbusWrites = [];
    }
    this.evModbusControl.modbusWrites.push(modbusWrite);
    this.modbusWritesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  onModbusWriteRemove(index: number) {
    this.evModbusControl.modbusWrites.splice(index, 1);
    this.modbusWritesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get modbusReadsFormArray() {
    return this.form.controls.modbusReads as FormArray;
  }

  getModbusReadFormGroup(index: number) {
    return this.modbusReadsFormArray.controls[index];
  }

  get modbusWritesFormArray() {
    return this.form.controls.modbusWrites as FormArray;
  }

  getModbusWriteFormGroup(index: number) {
    return this.modbusWritesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'idref',
      this.evModbusControl && this.evModbusControl.idref,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'slaveAddress',
      this.evModbusControl && this.evModbusControl.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusReads',
      this.evModbusControl.modbusReads);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusWrites',
      this.evModbusControl.modbusWrites);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'idref', this.evModbusControl.idref);
    this.formHandler.setFormControlValue(this.form, 'slaveAddress', this.evModbusControl.slaveAddress);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'modbusReads',
      this.evModbusControl.modbusReads);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'modbusWrites',
      this.evModbusControl.modbusWrites);
  }

  updateModelFromForm(): EvModbusControl | undefined {
    const idref = getValidString(this.form.controls.idref.value);
    const slaveAdress = getValidString(this.form.controls.slaveAddress.value);
    const modbusReads = [];
    this.modbusReadComps.forEach(modbusReadComponent => {
      const modbusRead = modbusReadComponent.updateModelFromForm();
      if (modbusRead) {
        modbusReads.push(modbusRead);
      }
    });
    const modbusWrites = [];
    this.modbusWriteComps.forEach(modbusWriteComponent => {
      const modbusWrite = modbusWriteComponent.updateModelFromForm();
      if (modbusWrite) {
        modbusWrites.push(modbusWrite);
      }
    });

    if (!(slaveAdress || modbusReads.length > 0 || modbusWrites.length > 0)) {
      return undefined;
    }

    this.evModbusControl.idref = idref;
    this.evModbusControl.slaveAddress = slaveAdress;
    this.evModbusControl.modbusReads = modbusReads;
    this.evModbusControl.modbusWrites = modbusWrites;
    return this.evModbusControl;
  }
}
