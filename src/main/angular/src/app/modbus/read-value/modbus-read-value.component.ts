import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ModbusReadValue} from './modbus-read-value';
import {ErrorMessages} from '../../shared/error-messages';
import {FormHandler} from '../../shared/form-handler';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {getValidString} from '../../shared/form-util';
import {Logger} from '../../log/logger';

@Component({
  selector: 'app-modbus-read-value',
  templateUrl: './modbus-read-value.component.html',
  styleUrls: ['./modbus-read-value.component.scss'],
})
export class ModbusReadValueComponent implements OnChanges, OnInit {
  @Input()
  modbusReadValue: ModbusReadValue;
  @Input()
  valueNames: string[];
  @Input()
  form: FormGroup;
  formHandler: FormHandler;
  @Input()
  translationPrefix = '';
  @Input()
  translationKeys: string[];
  translatedStrings: string[];
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
    if (changes.modbusReadValue) {
      if (changes.modbusReadValue.currentValue) {
        this.modbusReadValue = changes.modbusReadValue.currentValue;
      } else {
        this.modbusReadValue = new ModbusReadValue();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    if (this.form && !this.form.controls.name.value && this.valueNames.length === 1) {
      this.formHandler.setFormControlValue(this.form, 'name', this.valueNames[0]);
    }
    this.errorMessages = new ErrorMessages('ModbusReadValueComponent.error.', [
      new ErrorMessage('name', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('factorToValue', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName}`;
    return this.translatedStrings[textKey];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'name',
      this.modbusReadValue && this.modbusReadValue.name,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'extractionRegex',
      this.modbusReadValue && this.modbusReadValue.extractionRegex);
  }

  updateModelFromForm(): ModbusReadValue | undefined {
    const name = getValidString(this.form.controls.name.value);
    const extractionRegex = getValidString(this.form.controls.extractionRegex.value);

    if (!(name || extractionRegex)) {
      return undefined;
    }

    this.modbusReadValue.name = name;
    this.modbusReadValue.extractionRegex = extractionRegex;
    return this.modbusReadValue;
  }
}
