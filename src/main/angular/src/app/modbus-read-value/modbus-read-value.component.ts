import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Logger} from '../log/logger';
import {FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ModbusReadValue} from './modbus-read-value';
import {getValidString} from '../shared/form-util';

@Component({
  selector: 'app-modbus-read-value',
  templateUrl: './modbus-read-value.component.html',
  styleUrls: ['../global.css'],
})
export class ModbusReadValueComponent implements OnChanges, OnInit, AfterViewChecked {
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
      this.updateForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ModbusReadValueComponent.error.', [
      new ErrorMessage('factorToValue', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
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

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'name', this.modbusReadValue.name);
    this.formHandler.setFormControlValue(this.form, 'extractionRegex', this.modbusReadValue.extractionRegex);
  }

  updateModelFromForm(): ModbusReadValue | undefined {
    const name = this.form.controls.name.value;
    const extractionRegex = this.form.controls.extractionRegex.value;

    if (!(name || extractionRegex)) {
      return undefined;
    }

    this.modbusReadValue.name = getValidString(name);
    this.modbusReadValue.extractionRegex = getValidString(extractionRegex);
    return this.modbusReadValue;
  }
}
