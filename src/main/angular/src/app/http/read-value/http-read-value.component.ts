import {Component, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Logger} from '../../log/logger';
import {UntypedFormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {HttpReadValue} from './http-read-value';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {getValidFloat, getValidString} from '../../shared/form-util';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import { EventEmitter } from '@angular/core';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {HttpMethod} from '../http-method';

@Component({
  selector: 'app-http-read-value',
  templateUrl: './http-read-value.component.html',
  styleUrls: ['./http-read-value.component.scss'],
})
export class HttpReadValueComponent implements OnChanges, OnInit {
  @Input()
  httpReadValue: HttpReadValue;
  @Input()
  valueNames: string[];
  @Input()
  contentProtocol: string;
  @Input()
  disableFactorToValue = false;
  @Input()
  form: UntypedFormGroup;
  formHandler: FormHandler;
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
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.httpReadValue) {
      if (changes.httpReadValue.currentValue) {
        this.httpReadValue = changes.httpReadValue.currentValue;
      } else {
        this.httpReadValue = new HttpReadValue();
      }
      this.updateForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    if (this.form && !this.form.controls.name.value && this.valueNames.length === 1) {
      this.formHandler.setFormControlValue(this.form, 'name', this.valueNames[0]);
    }
    this.errorMessages = new ErrorMessages('HttpReadValueComponent.error.', [
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

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'name',
      this.httpReadValue && this.httpReadValue.name,
      [Validators.required]);
    if (this.httpReadValue) {
      this.onNameChanged(this.httpReadValue.name);
    }
    this.formHandler.addFormControl(this.form, 'method',
      this.httpReadValue && this.httpReadValue.method || HttpMethod.GET);
    this.formHandler.addFormControl(this.form, 'data',
      this.httpReadValue && this.httpReadValue.data);
    this.formHandler.addFormControl(this.form, 'path',
      this.httpReadValue && this.httpReadValue.path);
    this.formHandler.addFormControl(this.form, 'extractionRegex',
      this.httpReadValue && this.httpReadValue.extractionRegex);
    if (!this.disableFactorToValue) {
      this.formHandler.addFormControl(this.form, 'factorToValue',
        this.httpReadValue && this.httpReadValue.factorToValue,
        [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    }
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'name', this.httpReadValue.name);
    this.formHandler.setFormControlValue(this.form, 'method', this.httpReadValue.method);
    this.formHandler.setFormControlValue(this.form, 'data', this.httpReadValue.data);
    this.formHandler.setFormControlValue(this.form, 'path', this.httpReadValue.path);
    this.formHandler.setFormControlValue(this.form, 'extractionRegex', this.httpReadValue.extractionRegex);
    if (!this.disableFactorToValue) {
      this.formHandler.setFormControlValue(this.form, 'factorToValue', this.httpReadValue.factorToValue);
    }
  }

  updateModelFromForm(): HttpReadValue | undefined {
    const name = getValidString(this.form.controls.name.value);
    const method = getValidString(this.form.controls.method.value);
    const data = getValidString(this.form.controls.data.value);
    const path = getValidString(this.form.controls.path.value);
    const extractionRegex = getValidString(this.form.controls.extractionRegex.value);
    let factorToValue;
    if (!this.disableFactorToValue) {
      factorToValue = getValidFloat(this.form.controls.factorToValue.value);
    }

    if (!(name || method || data || path || extractionRegex || factorToValue)) {
      return undefined;
    }

    this.httpReadValue.name = name;
    this.httpReadValue.method = method;
    this.httpReadValue.data = data;
    this.httpReadValue.path = path;
    this.httpReadValue.extractionRegex = extractionRegex;
    this.httpReadValue.factorToValue = factorToValue;
    return this.httpReadValue;
  }
}
