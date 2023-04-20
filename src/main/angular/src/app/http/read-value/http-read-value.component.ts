import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Logger} from '../../log/logger';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {HttpReadValue} from './http-read-value';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {getValidFloat, getValidString, isRequired} from '../../shared/form-util';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {HttpMethod} from '../http-method';
import {HttpReadValueModel} from './http-read-value.model';

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
  form: FormGroup<HttpReadValueModel>;
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
    if (changes.httpReadValue) {
      if (changes.httpReadValue.currentValue) {
        this.httpReadValue = changes.httpReadValue.currentValue;
      } else {
        this.httpReadValue = new HttpReadValue();
      }
      if(! changes.httpReadValue.isFirstChange()) {
        this.updateForm();
      }
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    if (this.form && !this.form.controls.name.value && this.valueNames.length === 1) {
      this.form.controls.name.setValue(this.valueNames[0]);
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('name', new FormControl(this.httpReadValue?.name, Validators.required));
    if (this.httpReadValue) {
      this.onNameChanged(this.httpReadValue.name);
    }
    this.form.addControl('method', new FormControl((this.httpReadValue?.method) ?? HttpMethod.GET));
    this.form.addControl('data', new FormControl(this.httpReadValue?.data));
    this.form.addControl('path', new FormControl(this.httpReadValue?.path));
    this.form.addControl('extractionRegex', new FormControl(this.httpReadValue?.extractionRegex));
    if (!this.disableFactorToValue) {
      this.form.addControl('factorToValue', new FormControl(this.httpReadValue?.factorToValue, Validators.pattern(InputValidatorPatterns.FLOAT)));
    }
  }

  updateForm() {
    console.log('updateForm');
    this.form.controls.name.setValue(this.httpReadValue.name);
    this.form.controls.method.setValue(this.httpReadValue.method);
    this.form.controls.data.setValue(this.httpReadValue.data);
    this.form.controls.path.setValue(this.httpReadValue.path);
    this.form.controls.extractionRegex.setValue(this.httpReadValue.extractionRegex);
    if (!this.disableFactorToValue) {
      this.form.controls.factorToValue.setValue(this.httpReadValue.factorToValue);
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
