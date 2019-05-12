import {Component, Input, OnInit} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {EvModbusWriteRegisterName} from '../control-evcharger/ev-modbus-write-register-name';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {StartingCurrentSwitch} from '../control-startingcurrent/starting-current-switch';
import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';

@Component({
  selector: 'app-modbus-register',
  templateUrl: './modbus-register.component.html',
  styles: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ModbusRegisterComponent implements OnInit {

  @Input()
  register: ModbusRegisterConfguration;
  @Input()
  valueNames: string[];
  @Input()
  readRegisterTypes: string[];
  @Input()
  writeRegisterTypes: string[];
  @Input()
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  // @Input()
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  @Input()
  formHandler: FormHandler;
  modbusConfiguration: FormGroup;

  constructor(private logger: Logger,
              private parent: FormGroupDirective
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.modbusConfiguration = this.parent.form;
    this.expandParentForm(this.register);
  }

  expandParentForm(register: ModbusRegisterConfguration) {
    this.modbusConfiguration.addControl('name',
      new FormControl(register ? register.name : undefined));
    this.modbusConfiguration.addControl('registerAddress',
      new FormControl(register ? register.address : undefined));
    this.modbusConfiguration.addControl('write',
      new FormControl(register ? register.write : undefined));
    this.modbusConfiguration.addControl('registerType',
      new FormControl(register ? register.type : undefined));
    this.modbusConfiguration.addControl('bytes',
      new FormControl(register ? register.bytes : undefined));
    this.modbusConfiguration.addControl('byteOrder',
      new FormControl(register ? register.byteOrder : undefined));
    this.modbusConfiguration.addControl('extractionRegex',
      new FormControl(register ? register.extractionRegex : undefined));
    this.modbusConfiguration.addControl('value',
      new FormControl(register ? register.value : undefined));
    this.modbusConfiguration.addControl('factorToValue',
      new FormControl(register ? register.factorToValue : undefined));
  }

  // FIXME valueNames direkt verwenden
  getModbusRegisterNames(): string[] {
    return this.valueNames;
  }

  getTranslatedModbusRegisterName(name: string) {
    return 'name'; // this.translatedStrings[this.toTextKeyModbusRegisterName(name)];
  }

  getIndexedErrorMessage(key: string, index: number): string {
    const indexedKey = key + '.' + index.toString();
    return this.errors[indexedKey];
  }

  getModbusRegisterTypes(write: boolean): string[] {
    if (write) {
      return this.writeRegisterTypes;
    }
    return this.readRegisterTypes;
  }

  getRegisterType(modbusConfiguration: FormGroup): string {
    const typeControl = modbusConfiguration.controls['registerType'];
    return (typeControl ? typeControl.value : '');
  }

  isModbusWriteRegister(modbusConfiguration: FormGroup): boolean {
    const writeControl = modbusConfiguration.controls['write'];
    return (writeControl ? writeControl.value : false);
  }

  isChargingCurrentRegister(modbusConfiguration: FormGroup): boolean {
    const control = modbusConfiguration.controls['name'];
    return control.value === EvModbusWriteRegisterName.ChargingCurrent;
  }

  getByteOrders(): string[] {
    return ['BigEndian', 'LittleEndian'];
  }

  toTextKeyModbusRegisterName(name: string) {
    return 'ControlEvchargerComponent.' + name;
  }
}
