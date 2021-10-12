import {
  ChangeDetectionStrategy,
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
import {ModbusWrite} from './modbus-write';
import {ModbusWriteValueComponent} from '../write-value/modbus-write-value.component';
import {ErrorMessages} from '../../shared/error-messages';
import {FormHandler} from '../../shared/form-handler';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {getValidFloat, getValidString} from '../../shared/form-util';
import {ModbusWriteValue} from '../write-value/modbus-write-value';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {WriteRegisterType} from './write-register-type';
import {ReadRegisterType} from '../read/read-register-type';
import {ValueType} from '../read/value-type';

@Component({
  selector: 'app-modbus-write',
  templateUrl: './modbus-write.component.html',
  styleUrls: ['./modbus-write.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModbusWriteComponent implements OnChanges, OnInit {
  @Input()
  modbusWrite: ModbusWrite;
  @ViewChildren('modbusWriteValues')
  modbusWriteValueComps: QueryList<ModbusWriteValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  maxValues: number;
  @Input()
  disableFactorToValue = false;
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
    if (changes.modbusWrite) {
      if (changes.modbusWrite.currentValue) {
        this.modbusWrite = changes.modbusWrite.currentValue;
      } else {
        this.modbusWrite = new ModbusWrite({valueType: 'Integer'});
      }
      this.expandParentForm();
    } else if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ModbusReadComponent.error.', [
      new ErrorMessage('address', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('address', ValidatorType.pattern),
      new ErrorMessage('type', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('valueType', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('factorToValue', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.translate.get([
      ...this.registerTypes.map(regType => this.toRegisterTypeKey(regType)),
      ...this.valueTypes.map(valueType => this.toValueTypeKey(valueType)),
    ]).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  onNameChanged(index: number, event: ValueNameChangedEvent) {
    event.valueIndex = index;
    this.nameChanged.emit(event);
  }

  get type(): string {
    const typeControl = this.form.controls.type;
    return (typeControl ? typeControl.value : '');
  }

  get registerTypes(): string[] {
    return Object.values(WriteRegisterType);
  }

  toRegisterTypeKey(registerType: string): string {
    return `ModbusWriteComponent.type.${registerType}`;
  }

  public getTranslatedRegisterType(registerType: string) {
    return this.translatedStrings[this.toRegisterTypeKey(registerType)];
  }

  get valueTypes(): string[] {
    return Object.values(ValueType);
  }

  public getTranslatedValueType(valueType: string) {
    return this.translatedStrings[this.toValueTypeKey(valueType)];
  }

  toValueTypeKey(valueType: string): string {
    return `ModbusReadComponent.valueType.${valueType}`;
  }

  get isValueTypeDisplayed() {
    return this.form.controls.type.value === ReadRegisterType.Holding;
  }

  get isFactorToValueDisplayed() {
    return this.form.controls.type.value === WriteRegisterType.Holding && !this.disableFactorToValue;
  }

  removeModbusWrite() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.modbusWrite.writeValues || !this.maxValues || this.modbusWrite.writeValues.length < this.maxValues;
  }

  addValue() {
    const newWriteValue = new ModbusWriteValue();
    if (!this.modbusWrite.writeValues) {
      this.modbusWrite.writeValues = [];
    }
    this.modbusWrite.writeValues.push(newWriteValue);
    this.modbusWriteValuesFormArray.push(this.createModbusWriteValueFormGroup());
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  removeValue(index: number) {
    this.modbusWrite.writeValues.splice(index, 1);
    this.modbusWriteValuesFormArray.removeAt(index);

    const event: ValueNameChangedEvent = {valueIndex: index};
    this.nameChanged.emit(event);

    this.form.markAsDirty();
  }

  get modbusWriteValuesFormArray() {
    return this.form.controls.modbusWriteValues as FormArray;
  }

  createModbusWriteValueFormGroup(): FormGroup {
    return new FormGroup({});
  }

  getModbusWriteValueFormGroup(index: number) {
    return this.modbusWriteValuesFormArray.controls[index];
  }

  updateValueTypeValidator(readRegisterType: string) {
    if (readRegisterType  === ReadRegisterType.Holding) {
      this.form.controls.valueType.setValidators(Validators.required);
    } else {
      this.form.controls.valueType.removeValidators(Validators.required);
      this.form.controls.valueType.setErrors(null);
    }
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'address',
      this.modbusWrite && this.modbusWrite.address,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(this.form, 'type',
      this.modbusWrite && this.modbusWrite.type,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'valueType',
      this.modbusWrite && this.modbusWrite.valueType);
    if (!this.disableFactorToValue) {
      this.formHandler.addFormControl(this.form, 'factorToValue',
        this.modbusWrite && this.modbusWrite.factorToValue,
        [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    }
    this.form.controls.type.valueChanges.subscribe(value => {
      this.updateValueTypeValidator(value);
    });
    if (this.modbusWrite) {
      this.updateValueTypeValidator(this.form.controls.type.value);
    }
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusWriteValues',
      this.modbusWrite.writeValues);
  }

  updateModelFromForm(): ModbusWrite | undefined {
    const address = getValidString(this.form.controls.address.value);
    const type = getValidString(this.form.controls.type.value);
    const valueType = getValidString(this.form.controls.valueType.value);
    const factorToValue = this.form.controls.factorToValue && getValidFloat(this.form.controls.factorToValue.value);
    const modbusWriteValues = [];
    this.modbusWriteValueComps.forEach(modbusWriteValueComp => {
      const modbusWriteValue = modbusWriteValueComp.updateModelFromForm();
      if (modbusWriteValue) {
        modbusWriteValues.push(modbusWriteValue);
      }
    });

    if (!(address || type || valueType || factorToValue || modbusWriteValues.length > 0)) {
      return undefined;
    }

    this.modbusWrite.address = address;
    this.modbusWrite.type = type;
    this.modbusWrite.valueType = valueType;
    this.modbusWrite.factorToValue = factorToValue;
    return this.modbusWrite;
  }
}
