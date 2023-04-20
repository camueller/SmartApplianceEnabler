import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {HttpWriteValue} from './http-write-value';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {getValidFloat, getValidString, isRequired} from '../../shared/form-util';
import {HttpMethod} from '../http-method';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {HttpWriteValueModel} from './http-write-value.model';

@Component({
  selector: 'app-http-write-value',
  templateUrl: './http-write-value.component.html',
  styleUrls: ['./http-write-value.component.scss'],
})
export class HttpWriteValueComponent implements OnChanges, OnInit {
  @Input()
  httpWriteValue: HttpWriteValue;
  @Input()
  valueNames: string[];
  @Input()
  disableFactorToValue = false;
  @Input()
  form: FormGroup<HttpWriteValueModel>;
  @Input()
  translationPrefix = '';
  @Input()
  translationKeys: string[];
  translatedStrings: { [key: string]: string } = {};
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  @Output()
  nameChanged = new EventEmitter<any>();

  constructor(private logger: Logger,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.httpWriteValue) {
      if (changes.httpWriteValue.currentValue) {
        this.httpWriteValue = changes.httpWriteValue.currentValue;
      } else {
        this.httpWriteValue = new HttpWriteValue();
      }
      if(! changes.httpWriteValue.isFirstChange()) {
        this.updateForm();
      }
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('HttpWriteValueComponent.error.', [
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

  get method() {
    return this.form.controls.method && this.form.controls.method.value;
  }

  get methods() {
    return Object.keys(HttpMethod);
  }

  getMethodTranslationKey(method: string) {
    return `HttpMethod.${method}`;
  }

  onNameChanged(newName?: string) {
    if (newName) {
      const event: ValueNameChangedEvent = {name: newName};
      this.nameChanged.emit(event);
    }
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('name', new FormControl(this.httpWriteValue?.name, Validators.required))
    if (this.httpWriteValue) {
      this.onNameChanged(this.httpWriteValue.name);
    }
    this.form.addControl('method', new FormControl(this.httpWriteValue?.method ?? HttpMethod.GET))
    this.form.addControl('value', new FormControl(this.httpWriteValue?.value));
    if (!this.disableFactorToValue) {
      this.form.addControl('factorToValue', new FormControl(this.httpWriteValue?.factorToValue,
        Validators.pattern(InputValidatorPatterns.FLOAT)));
    }
  }

  updateForm() {
    this.form.controls.name.setValue(this.httpWriteValue.name)
    this.form.controls.method.setValue(this.httpWriteValue.method);
    this.form.controls.value.setValue(this.httpWriteValue.value)
    if (!this.disableFactorToValue) {
      this.form.controls.factorToValue.setValue(this.httpWriteValue.factorToValue);
    }
  }

  updateModelFromForm(): HttpWriteValue | undefined {
    const name = getValidString(this.form.controls.name.value);
    const method = getValidString(this.form.controls.method.value);
    const value = getValidString(this.form.controls.value.value);
    const factorToValue = this.form.controls.factorToValue && getValidFloat(this.form.controls.factorToValue.value);

    if (!(name || method || value || factorToValue)) {
      return undefined;
    }

    this.httpWriteValue.name = name;
    this.httpWriteValue.method = method;
    this.httpWriteValue.value = value;
    this.httpWriteValue.factorToValue = factorToValue;
    return this.httpWriteValue;
  }
}
