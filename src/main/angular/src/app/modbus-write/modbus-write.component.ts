import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
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
  @Input()
  valueNames: string[];
  @Input()
  writeRegisterTypes: string[];
  @Input()
  formControlNamePrefix = '';
  @Input()
  singleValue = false;
  form: FormGroup;
  formHandler: FormHandler;
  @Input()
  translationPrefix: string;
  @Input()
  translationKeys: string[];
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
    this.errorMessages = new ErrorMessages('ModbusReadComponent.error.', [
      new ErrorMessage(this.getFormControlName('address'), ValidatorType.required, 'address'),
      new ErrorMessage(this.getFormControlName('address'), ValidatorType.pattern, 'address'),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.modbusWrite, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    // this.translate.get(this.translationKeys).subscribe(translatedStrings => {
    //   this.translatedStrings = translatedStrings;
    // });
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
    if (this.modbusWrite.registerWriteValues && this.modbusWrite.registerWriteValues.length === 1) {
      const modbusReadValue = this.modbusWrite.registerWriteValues[0];
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

  addValue() {
    const newWriteValue = new ModbusWriteValue();
    if (!this.modbusWrite.registerWriteValues) {
      this.modbusWrite.registerWriteValues = [];
    }
    this.modbusWrite.registerWriteValues.push(newWriteValue);
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.modbusWrite.registerWriteValues.splice(index, 1);
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
}
