import {AfterViewChecked, Component, Input, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpWriteValue} from './http-write-value';
import {HttpMethod} from '../shared/http-method';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {getValidFloat, getValidString} from '../shared/form-util';

@Component({
  selector: 'app-http-write-value',
  templateUrl: './http-write-value.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpWriteValueComponent implements OnInit, AfterViewChecked {
  @Input()
  httpWriteValue: HttpWriteValue;
  @Input()
  valueNames: string[];
  @Input()
  disableFactorToValue = false;
  @Input()
  formControlNamePrefix = '';
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
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.httpWriteValue = this.httpWriteValue || new HttpWriteValue();
    this.errorMessages = new ErrorMessages('HttpWriteValueComponent.error.', [
      new ErrorMessage(this.getFormControlName('factorToValue'), ValidatorType.pattern, 'factorToValue'),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpWriteValue, this.formHandler);
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

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
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

  expandParentForm(form: FormGroup, httpWriteValue: HttpWriteValue, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('name'),
      httpWriteValue ? httpWriteValue.name : undefined, [Validators.required]);
    formHandler.addFormControl(form, this.getFormControlName('value'),
      httpWriteValue ? httpWriteValue.value : undefined);
    if (!this.disableFactorToValue) {
      formHandler.addFormControl(form, this.getFormControlName('factorToValue'),
        httpWriteValue ? httpWriteValue.factorToValue : undefined,
        [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    }
    formHandler.addFormControl(form, this.getFormControlName('method'),
      httpWriteValue ? httpWriteValue.method : undefined);
  }

  updateModelFromForm(): HttpWriteValue | undefined {
    const name = getValidString(this.form.controls[this.getFormControlName('name')].value);
    const value = getValidString(this.form.controls[this.getFormControlName('value')].value);
    const factorToValue = getValidFloat(this.form.controls[this.getFormControlName('factorToValue')].value);
    const method = getValidString(this.form.controls[this.getFormControlName('method')].value);

    if (!(name || value || factorToValue || method)) {
      return undefined;
    }

    this.httpWriteValue.name = name;
    this.httpWriteValue.value = value;
    this.httpWriteValue.factorToValue = factorToValue;
    this.httpWriteValue.method = method;
    return this.httpWriteValue;
  }
}
