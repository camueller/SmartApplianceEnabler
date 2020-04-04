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
import {TranslateService} from '@ngx-translate/core';
import {ModbusWrite} from './modbus-write';
import {ModbusWriteValueComponent} from '../write-value/modbus-write-value.component';
import {ErrorMessages} from '../../shared/error-messages';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidInt, getValidString} from '../../shared/form-util';
import {ModbusWriteValue} from '../write-value/modbus-write-value';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';

@Component({
  selector: 'app-modbus-write',
  templateUrl: './modbus-write.component.html',
  styleUrls: [],
})
export class ModbusWriteComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  modbusWrite: ModbusWrite;
  @ViewChildren('modbusWriteValues')
  modbusWriteValueComps: QueryList<ModbusWriteValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  writeRegisterTypes: string[];
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
    if (changes.modbusWrite) {
      if (changes.modbusWrite.currentValue) {
        this.modbusWrite = changes.modbusWrite.currentValue;
      } else {
        this.modbusWrite = new ModbusWrite();
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

  removeModbusWrite() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.modbusWrite.writeValues || !this.maxValues || this.modbusWrite.writeValues.length < this.maxValues;
  }

  addValue() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    const newWriteValue = new ModbusWriteValue();
    if (!this.modbusWrite.writeValues) {
      this.modbusWrite.writeValues = [];
    }
    this.modbusWrite.writeValues.push(newWriteValue);
    this.modbusWriteValuesFormArray.push(this.createModbusWriteValueFormGroup());
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.modbusWrite.writeValues.splice(index, 1);
    this.modbusWriteValuesFormArray.removeAt(index);
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

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'address',
      this.modbusWrite && this.modbusWrite.address,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(this.form, 'type',
      this.modbusWrite && this.modbusWrite.type,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'factorToValue',
      this.modbusWrite && this.modbusWrite.factorToValue,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusWriteValues',
      this.modbusWrite.writeValues);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'address', this.modbusWrite.address);
    this.formHandler.setFormControlValue(this.form, 'type', this.modbusWrite.type);
    this.formHandler.setFormControlValue(this.form, 'factorToValue', this.modbusWrite.factorToValue);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'modbusWriteValues',
      this.modbusWrite.writeValues);
  }

  updateModelFromForm(): ModbusWrite | undefined {
    const address = this.form.controls.address.value;
    const type = this.form.controls.type.value;
    const factorToValue = this.form.controls.factorToValue.value;
    const modbusWriteValues = [];
    this.modbusWriteValueComps.forEach(modbusWriteValueComp => {
      const modbusWriteValue = modbusWriteValueComp.updateModelFromForm();
      if (modbusWriteValue) {
        modbusWriteValues.push(modbusWriteValue);
      }
    });

    if (!(address || type || factorToValue || modbusWriteValues.length > 0)) {
      return undefined;
    }

    this.modbusWrite.address = getValidString(address);
    this.modbusWrite.type = getValidString(type);
    this.modbusWrite.factorToValue = getValidInt(factorToValue);
    return this.modbusWrite;
  }
}
