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
import {MeterReadNameChangedEvent} from '../../meter/meter-read-name-changed-event';
import {ValueType} from './value-type';
import {ReadRegisterType} from './read-register-type';

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
  @Output()
  nameChanged = new EventEmitter<any>();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  translatedStrings: { [key: string]: string } = {};

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
      new ErrorMessage('valueType', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('words', ValidatorType.pattern),
      new ErrorMessage('factorToValue', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.translate.get([
      ...this.registerTypes.map(regType => this.toRegisterTypeKey(regType)),
      ...this.valueTypes.map(valueType => this.toValueTypeKey(valueType)),
      ...this.byteOrders.map(byteOrder => this.toByteOrderKey(byteOrder))
    ]).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  onNameChanged(index: number, event: MeterReadNameChangedEvent) {
    event.readValueIndex = index;
    this.nameChanged.emit(event);
  }

  get registerTypes(): string[] {
    return Object.values(ReadRegisterType);
  }

  toRegisterTypeKey(registerType: string): string {
    return `ModbusReadComponent.type.${registerType}`;
  }

  public getTranslatedRegisterType(registerType: string) {
    return this.translatedStrings[this.toRegisterTypeKey(registerType)];
  }

  get valueTypes(): string[] {
    return Object.values(ValueType);
  }

  toValueTypeKey(valueType: string): string {
    return `ModbusReadComponent.valueType.${valueType}`;
  }

  public getTranslatedValueType(valueType: string) {
    return this.translatedStrings[this.toValueTypeKey(valueType)];
  }

  get isValueTypeDisplayed() {
    return this.form.controls.type.value === ReadRegisterType.Input || this.form.controls.type.value === ReadRegisterType.Holding;
  }

  get wordsPlaceholder() {
    const key = `(${this.form.controls.type.value},${this.form.controls.valueType.value ? this.form.controls.valueType.value : null})`;
    return this.meterDefaults.modbusReadDefaults.wordsForRegisterType[key];
  }

  get isWordsDisplayed() {
    return this.form.controls.type.value === ReadRegisterType.Input || this.form.controls.type.value === ReadRegisterType.Holding;
  }

  get byteOrders(): string[] {
    return this.meterDefaults.modbusReadDefaults.byteOrders;
  }

  public getTranslatedByteOrder(byteOrder: string) {
    return this.translatedStrings[this.toByteOrderKey(byteOrder)];
  }

  toByteOrderKey(valueType: string): string {
    return `ModbusReadComponent.byteOrder.${valueType}`;
  }

  get isByteOrderDisplayed() {
    return (this.form.controls.words.value > 1 || this.wordsPlaceholder > 1) && this.form.controls.valueType.value !== ValueType.String;
  }

  get isFactorToValueDisplayed() {
    return (this.form.controls.type.value === ReadRegisterType.Input || this.form.controls.type.value === ReadRegisterType.Holding)
      && this.form.controls.valueType.value !== ValueType.String;
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

    const event: MeterReadNameChangedEvent = {readValueIndex: index};
    this.nameChanged.emit(event);

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
    this.formHandler.addFormControl(this.form, 'valueType',
      this.modbusRead && this.modbusRead.valueType,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'words',
      this.modbusRead && this.modbusRead.words,
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
    const valueType = getValidString(this.form.controls.valueType.value);
    const words = getValidInt(this.form.controls.words.value);
    const byteOrder = this.isByteOrderDisplayed ? getValidString(this.form.controls.byteOrder.value) : undefined;
    const factorToValue = getValidFloat(this.form.controls.factorToValue.value);
    const modbusReadValues = [];
    this.modbusReadValueComps.forEach(modbusReadValueComp => {
      const modbusReadValue = modbusReadValueComp.updateModelFromForm();
      if (modbusReadValue) {
        modbusReadValues.push(modbusReadValue);
      }
    });

    if (!(address || type || valueType || words || byteOrder || factorToValue || modbusReadValues.length > 0)) {
      return undefined;
    }

    this.modbusRead.address = address;
    this.modbusRead.type = type;
    this.modbusRead.valueType = valueType;
    this.modbusRead.words = words;
    this.modbusRead.byteOrder = byteOrder;
    this.modbusRead.factorToValue = factorToValue;
    return this.modbusRead;
  }
}
