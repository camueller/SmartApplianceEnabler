import {
  ChangeDetectorRef,
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
import {TranslateService} from '@ngx-translate/core';
import {ModbusRead} from './modbus-read';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {getValidFloat, getValidInt, getValidString} from '../../shared/form-util';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ModbusReadValue} from '../read-value/modbus-read-value';
import {ModbusReadValueComponent} from '../read-value/modbus-read-value.component';
import {FormHandler} from '../../shared/form-handler';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {Logger} from '../../log/logger';
import {MeterDefaults} from '../../meter/meter-defaults';

@Component({
  selector: 'app-modbus-read',
  templateUrl: './modbus-read.component.html',
  styleUrls: ['./modbus-read.component.scss'],
})
export class ModbusReadComponent implements OnChanges, OnInit {
  @Input()
  modbusRead: ModbusRead;
  @ViewChildren('modbusReadValues')
  modbusReadValueComps: QueryList<ModbusReadValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  readRegisterTypes: string[];
  @Input()
  meterDefaults: MeterDefaults;
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
              private translate: TranslateService,
              private changeDetectorRef: ChangeDetectorRef
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
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
    if (changes.meterDefaults && changes.meterDefaults.currentValue) {
      this.meterDefaults = changes.meterDefaults.currentValue;
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ModbusReadComponent.error.', [
      new ErrorMessage('address', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('address', ValidatorType.pattern),
      new ErrorMessage('type', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('bytes', ValidatorType.pattern),
      new ErrorMessage('factorToValue', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get type(): string {
    const typeControl = this.form.controls.type;
    return (typeControl ? typeControl.value : '');
  }

  get bytesPlaceholder() {
    return this.meterDefaults.modbusReadDefaults.bytesForRegisterType[this.form.controls.type.value];
  }

  get isByteOrderDisplayed() {
    return this.form.controls.type.value === 'InputDecimal' && (this.form.controls.bytes.value > 1 || this.bytesPlaceholder > 1);
  }

  // TODO move to config
  get byteOrders(): string[] {
    return ['BigEndian', 'LittleEndian'];
  }

  get isRemoveModbusPossible() {
    return !this.maxValues || this.maxValues > 1;
  }

  removeModbusRead() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.modbusRead.readValues || !this.maxValues || this.modbusRead.readValues.length < this.maxValues;
  }

  addValue() {
    const newReadValue = new ModbusReadValue();
    if (!this.modbusRead.readValues) {
      this.modbusRead.readValues = [];
    }
    this.modbusRead.readValues.push(newReadValue);
    this.modbusReadValuesFormArray.push(this.createModbusReadValueFormGroup());
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  get isRemoveValuePossible() {
    return !this.maxValues || this.maxValues > 1;
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
      this.modbusRead && this.modbusRead.byteOrder || 'BigEndian');
    this.formHandler.addFormControl(this.form, 'factorToValue',
      this.modbusRead && this.modbusRead.factorToValue,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusReadValues',
      this.modbusRead.readValues);
  }

  updateModelFromForm(): ModbusRead | undefined {
    const address = getValidString(this.form.controls.address.value);
    const type = getValidString(this.form.controls.type.value);
    const bytes = getValidInt(this.form.controls.bytes.value);
    const byteOrder = this.isByteOrderDisplayed ? getValidString(this.form.controls.byteOrder.value) : undefined;;
    const factorToValue = getValidFloat(this.form.controls.factorToValue.value);
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

    this.modbusRead.address = address;
    this.modbusRead.type = type;
    this.modbusRead.bytes = bytes;
    this.modbusRead.byteOrder = byteOrder;
    this.modbusRead.factorToValue = factorToValue;
    return this.modbusRead;
  }
}
