import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output, QueryList, ViewChildren} from '@angular/core';
import {Logger} from '../log/logger';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ModbusWrite} from './modbus-write';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusWriteValue} from '../modbus-write-value/modbus-write-value';
import {getValidString} from '../shared/form-util';
import {ModbusWriteValueComponent} from '../modbus-write-value/modbus-write-value.component';

@Component({
  selector: 'app-modbus-write',
  templateUrl: './modbus-write.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ModbusWriteComponent implements OnInit, AfterViewChecked {
  @Input()
  modbusWrite: ModbusWrite;
  @ViewChildren('modbusWriteValues')
  modbusWriteValueComps: QueryList<ModbusWriteValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  writeRegisterTypes: string[];
  @Input()
  formControlNamePrefix = '';
  @Input()
  maxValues: number;
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
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.modbusWrite = this.modbusWrite || new ModbusWrite();
    this.errorMessages = new ErrorMessages('ModbusReadComponent.error.', [
      new ErrorMessage(this.getFormControlName('address'), ValidatorType.required, 'address'),
      new ErrorMessage(this.getFormControlName('address'), ValidatorType.pattern, 'address'),
      new ErrorMessage(this.getFormControlName('factorToValue'), ValidatorType.pattern, 'factorToValue'),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.modbusWrite, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  getWriteValueFormControlPrefix(index: number) {
    return `${this.formControlNamePrefix}writeValue${index}.`;
  }

  get valueName() {
    // TODO ist das so notwendig?
    if (this.modbusWrite.writeValues && this.modbusWrite.writeValues.length === 1) {
      const modbusReadValue = this.modbusWrite.writeValues[0];
      // if (modbusReadValue.name) {
      //   return this.getTranslatedValueName(modbusReadValue.name);
      // }
    }
    return undefined;
  }

  get type(): string {
    const typeControl = this.form.controls[this.getFormControlName('type')];
    return (typeControl ? typeControl.value : '');
  }

  removeModbusWrite() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return this.modbusWrite.writeValues.length < this.maxValues;
  }

  addValue() {
    const newWriteValue = new ModbusWriteValue();
    if (!this.modbusWrite.writeValues) {
      this.modbusWrite.writeValues = [];
    }
    this.modbusWrite.writeValues.push(newWriteValue);
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.modbusWrite.writeValues.splice(index, 1);
    this.form.markAsDirty();
  }

  expandParentForm(form: FormGroup, modbusWrite: ModbusWrite, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('address'),
      modbusWrite ? modbusWrite.address : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    formHandler.addFormControl(form, this.getFormControlName('type'),
      modbusWrite ? modbusWrite.type : undefined,
      [Validators.required]);
    formHandler.addFormControl(form, this.getFormControlName('factorToValue'),
      modbusWrite ? modbusWrite.factorToValue : undefined,
      [Validators.pattern(InputValidatorPatterns.FLOAT)]);
  }

  updateModelFromForm(): ModbusWrite | undefined {
    const address = this.form.controls[this.getFormControlName('address')].value;
    const type = this.form.controls[this.getFormControlName('type')].value;
    const factorToValue = this.form.controls[this.getFormControlName('factorToValue')].value;
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
    this.modbusWrite.address = getValidString(address);
    return this.modbusWrite;
  }
}
