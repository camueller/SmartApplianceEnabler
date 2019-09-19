import {AfterViewChecked, Component, Input, OnInit, QueryList, ViewChild} from '@angular/core';
import {ControlDefaults} from '../control/control-defaults';
import {FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusSwitch} from './modbus-switch';
import {ModbusSettings} from '../settings/modbus-settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {FormHandler} from '../shared/form-handler';
import {StartingCurrentSwitch} from '../control-startingcurrent/starting-current-switch';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ModbusWriteComponent} from '../modbus-write/modbus-write.component';
import {ModbusWrite} from '../modbus-write/modbus-write';
import {ModbusWriteValue} from '../modbus-write-value/modbus-write-value';
import {ControlValueName} from '../control/control-value-name';

@Component({
  selector: 'app-control-modbus',
  templateUrl: './control-modbus.component.html',
  styleUrls: ['../global.css']
})
export class ControlModbusComponent implements OnInit, AfterViewChecked {
  @Input()
  modbusSwitch: ModbusSwitch;
  @ViewChild('modbusWrites')
  modbusWriteComps: QueryList<ModbusWriteComponent>;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  modbusSettings: ModbusSettings[];
  @Input()
  settingsDefaults: SettingsDefaults;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.modbusSwitch = this.modbusSwitch || new ModbusSwitch();
    if (!this.modbusSwitch.modbusWrites) {
      this.modbusSwitch.modbusWrites = [this.createModbusWrite()];
    }
    this.errorMessages = new ErrorMessages('ControlModbusComponent.error.', [
      new ErrorMessage('slaveAddress', ValidatorType.required),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.modbusSwitch, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  getWriteFormControlPrefix(index: number) {
    return `write${index}.`;
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
    this.modbusSwitch.modbusWrites.push(this.createModbusWrite());
    this.form.markAsDirty();
  }

  onModbusWriteRemove(index: number) {
    this.modbusSwitch.modbusWrites.splice(index, 1);
  }

  createModbusWrite() {
    const modbusWrite = new ModbusWrite();
    modbusWrite.writeValues = [new ModbusWriteValue()];
    return modbusWrite;
  }

  expandParentForm(form: FormGroup, modbusSwitch: ModbusSwitch, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'modbusTcpId', modbusSwitch && modbusSwitch.idref,
      [Validators.required]);
    formHandler.addFormControl(form, 'slaveAddress', modbusSwitch && modbusSwitch.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
  }

  updateModelFromForm(form: FormGroup, modbusSwitch: ModbusSwitch, startingCurrentSwitch: StartingCurrentSwitch) {
    // modbusSwitch.idref = form.controls.modbusTcpId.value;
    // modbusSwitch.slaveAddress = form.controls.slaveAddress.value;
    // modbusSwitch.registerAddress = form.controls.registerAddress.value;
    // modbusSwitch.registerType = form.controls.registerType.value;
    // modbusSwitch.onValue = form.controls.onValue.value;
    // modbusSwitch.offValue = form.controls.offValue.value;
    // if (this.control.startingCurrentDetection) {
    //   ControlStartingcurrentComponent.updateModelFromForm(form, startingCurrentSwitch);
    // }
  }
}
