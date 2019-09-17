import {AfterViewChecked, Component, Input, OnInit, QueryList, ViewChildren} from '@angular/core';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {Settings} from '../settings/settings';
import {EvModbusControl} from './ev-modbus-control';
import {SettingsDefaults} from '../settings/settings-defaults';
import {EvModbusWriteRegisterName} from './ev-modbus-write-register-name';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusRead} from '../modbus-read/modbus-read';
import {EvModbusReadRegisterName} from './ev-modbus-read-register-name';
import {ModbusWrite} from '../modbus-write/modbus-write';
import {ModbusReadComponent} from '../modbus-read/modbus-read.component';
import {ModbusWriteComponent} from '../modbus-write/modbus-write.component';
import {getValidString} from '../shared/form-util';

@Component({
  selector: 'app-control-evcharger-modbus',
  templateUrl: './control-evcharger-modbus.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlEvchargerModbusComponent implements OnInit, AfterViewChecked {
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

  ngOnInit() {
    this.evModbusControl = this.evModbusControl || new EvModbusControl();
    // this.errorMessages = new ErrorMessages('ControlEvchargerModbusComponent.error.', [
    //   new ErrorMessage('voltage', ValidatorType.pattern),
    // ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.evModbusControl, this.formHandler);
    // this.form.statusChanges.subscribe(() => {
    //   this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    // });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  getReadFormControlPrefix(index: number) {
    return `read${index}.`;
  }

  getWriteFormControlPrefix(index: number) {
    return `write${index}.`;
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
    const modbusRead = new ModbusRead();
    if (!this.evModbusControl.modbusReads) {
      this.evModbusControl.modbusReads = [];
    }
    this.evModbusControl.modbusReads.push(modbusRead);
    this.form.markAsDirty();
  }

  addModbusWrite() {
    const modbusWrite = new ModbusWrite();
    if (!this.evModbusControl.modbusWrites) {
      this.evModbusControl.modbusWrites = [];
    }
    this.evModbusControl.modbusWrites.push(modbusWrite);
    this.form.markAsDirty();
  }

  expandParentForm(form: FormGroup, evModbusControl: EvModbusControl, formHandler: FormHandler) {
    this.formHandler.addFormControl(form, 'idref', evModbusControl ? evModbusControl.idref : undefined,
      [Validators.required]);
    this.formHandler.addFormControl(form, 'slaveAddress', evModbusControl ? evModbusControl.slaveAddress : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
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
