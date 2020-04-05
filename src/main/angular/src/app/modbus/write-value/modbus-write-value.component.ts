import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ModbusWriteValue} from './modbus-write-value';
import {ErrorMessages} from '../../shared/error-messages';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {getValidString} from '../../shared/form-util';
import {Logger} from '../../log/logger';

@Component({
  selector: 'app-modbus-write-value',
  templateUrl: './modbus-write-value.component.html',
  styleUrls: [],
})
export class ModbusWriteValueComponent implements OnChanges, OnInit {
  @Input()
  modbusWriteValue: ModbusWriteValue;
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
    if (changes.modbusWriteValue) {
      if (changes.modbusWriteValue.currentValue) {
        this.modbusWriteValue = changes.modbusWriteValue.currentValue;
      } else {
        this.modbusWriteValue = new ModbusWriteValue();
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
      this.modbusWriteValue && this.modbusWriteValue.name,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'value',
      this.modbusWriteValue && this.modbusWriteValue.value,
      [Validators.required]);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'name', this.modbusWriteValue.name);
    this.formHandler.setFormControlValue(this.form, 'value', this.modbusWriteValue.value);
  }

  updateModelFromForm(): ModbusWriteValue | undefined {
    const name = this.form.controls.name.value;
    const value = this.form.controls.value.value;

    if (!(name || value)) {
      return undefined;
    }

    this.modbusWriteValue.name = getValidString(name);
    this.modbusWriteValue.value = getValidString(value);
    return this.modbusWriteValue;
  }
}
