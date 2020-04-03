import {
  AfterViewChecked,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {FormArray, FormGroup, Validators} from '@angular/forms';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusRead} from './modbus-read';
import {ModbusReadValue} from '../modbus-read-value/modbus-read-value';
import {
  fixExpressionChangedAfterItHasBeenCheckedError,
  getValidFloat,
  getValidInt,
  getValidString
} from '../shared/form-util';
import {ModbusReadValueComponent} from '../modbus-read-value/modbus-read-value.component';

@Component({
  selector: 'app-modbus-read',
  templateUrl: './modbus-read.component.html',
  styleUrls: ['../global.css'],
})
export class ModbusReadComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  modbusRead: ModbusRead;
  @ViewChildren('modbusReadValues')
  modbusReadValueComps: QueryList<ModbusReadValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  readRegisterTypes: string[];
  @Input()
  maxValues: number;
  @Input()
  form: FormGroup;
  formHandler: FormHandler;
  @Input()
  translationPrefix: string;
  @Input()
  translationKeys: string[];
  @Output()
  remove = new EventEmitter<any>();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.modbusRead) {
      if (changes.modbusRead.currentValue) {
        this.modbusRead = changes.modbusRead.currentValue;
      } else {
        this.modbusRead = new ModbusRead();
      }
      this.updateForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ModbusReadComponent.error.', [
      new ErrorMessage('address', ValidatorType.required),
      new ErrorMessage('address', ValidatorType.pattern),
      new ErrorMessage('bytes', ValidatorType.pattern),
      new ErrorMessage('factorToValue', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get type(): string {
    const typeControl = this.form.controls.type;
    return (typeControl ? typeControl.value : '');
  }

  // TODO move to config
  get byteOrders(): string[] {
    return ['BigEndian', 'LittleEndian'];
  }

  removeModbusRead() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.modbusRead.readValues || !this.maxValues || this.modbusRead.readValues.length < this.maxValues;
  }

  addValue() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    const newReadValue = new ModbusReadValue();
    if (!this.modbusRead.readValues) {
      this.modbusRead.readValues = [];
    }
    this.modbusRead.readValues.push(newReadValue);
    this.modbusReadValuesFormArray.push(this.createModbusReadValueFormGroup());
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.modbusRead.readValues.splice(index, 1);
    this.modbusReadValuesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get modbusReadValuesFormArray() {
    return this.form.controls.modbusReadValues as FormArray;
  }

  createModbusReadValueFormGroup(): FormGroup {
    return new FormGroup({});
  }

  getModbusReadValueFormGroup(index: number) {
    return this.modbusReadValuesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'address',
      this.modbusRead && this.modbusRead.address,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(this.form, 'type',
      this.modbusRead && this.modbusRead.type,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'bytes',
      this.modbusRead && this.modbusRead.bytes,
      [Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(this.form, 'byteOrder',
      this.modbusRead && this.modbusRead.byteOrder);
    this.formHandler.addFormControl(this.form, 'factorToValue',
      this.modbusRead && this.modbusRead.factorToValue,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusReadValues',
      this.modbusRead.readValues);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'address', this.modbusRead.address);
    this.formHandler.setFormControlValue(this.form, 'type', this.modbusRead.type);
    this.formHandler.setFormControlValue(this.form, 'bytes', this.modbusRead.bytes);
    this.formHandler.setFormControlValue(this.form, 'byteOrder', this.modbusRead.byteOrder);
    this.formHandler.setFormControlValue(this.form, 'factorToValue', this.modbusRead.factorToValue);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'modbusReadValues',
      this.modbusRead.readValues);
  }

  updateModelFromForm(): ModbusRead | undefined {
    const address = this.form.controls.address.value;
    const type = this.form.controls.type.value;
    const bytes = this.form.controls.bytes.value;
    const byteOrder = this.form.controls.byteOrder.value;
    const factorToValue = this.form.controls.factorToValue.value;
    const modbusReadValues = [];
    this.modbusReadValueComps.forEach(modbusReadValueComp => {
      const modbusReadValue = modbusReadValueComp.updateModelFromForm();
      if (modbusReadValue) {
        modbusReadValues.push(modbusReadValue);
      }
    });

    if (!(address || type || bytes || byteOrder || factorToValue || modbusReadValues.length > 0)) {
      return undefined;
    }

    this.modbusRead.address = getValidString(address);
    this.modbusRead.type = getValidString(type);
    this.modbusRead.bytes = getValidInt(bytes);
    this.modbusRead.byteOrder = getValidString(byteOrder);
    this.modbusRead.factorToValue = getValidFloat(factorToValue);
    return this.modbusRead;
  }
}
