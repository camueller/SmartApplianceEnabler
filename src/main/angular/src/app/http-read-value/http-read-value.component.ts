import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Logger} from '../log/logger';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {HttpReadValue} from './http-read-value';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {getValidFloat, getValidString} from '../shared/form-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';

@Component({
  selector: 'app-http-read-value',
  templateUrl: './http-read-value.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpReadValueComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  httpReadValue: HttpReadValue;
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

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    console.log('form=', this.form);
    if (changes.httpReadValue) {
      if (changes.httpReadValue.currentValue) {
        this.httpReadValue = changes.httpReadValue.currentValue;
      } else {
        this.httpReadValue = new HttpReadValue();
      }
      this.updateForm(this.form, this.httpReadValue, this.formHandler);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('HttpReadValueComponent.error.', [
      new ErrorMessage(this.getFormControlName('factorToValue'), ValidatorType.pattern, 'factorToValue'),
    ], this.translate);
    this.expandParentForm(this.form, this.httpReadValue, this.formHandler);
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

  expandParentForm(form: FormGroup, httpReadValue: HttpReadValue, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('name'),
      httpReadValue ? httpReadValue.name : undefined,
      [Validators.required]);
    formHandler.addFormControl(form, this.getFormControlName('data'),
      httpReadValue ? httpReadValue.data : undefined);
    formHandler.addFormControl(form, this.getFormControlName('path'),
      httpReadValue ? httpReadValue.path : undefined);
    formHandler.addFormControl(form, this.getFormControlName('extractionRegex'),
      httpReadValue ? httpReadValue.extractionRegex : undefined);
    if (!this.disableFactorToValue) {
      formHandler.addFormControl(form, this.getFormControlName('factorToValue'),
        httpReadValue ? httpReadValue.factorToValue : undefined,
        [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    }
  }

  updateForm(form: FormGroup, httpReadValue: HttpReadValue, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('name'), httpReadValue.name);
    formHandler.setFormControlValue(form, this.getFormControlName('data'), httpReadValue.data);
    formHandler.setFormControlValue(form, this.getFormControlName('path'), httpReadValue.path);
    formHandler.setFormControlValue(form, this.getFormControlName('extractionRegex'),
      httpReadValue.extractionRegex);
    if (!this.disableFactorToValue) {
      formHandler.setFormControlValue(form, this.getFormControlName('factorToValue'),
        httpReadValue.factorToValue);
    }
  }

  updateModelFromForm(): HttpReadValue | undefined {
    const name = getValidString(this.form.controls[this.getFormControlName('name')].value);
    const data = getValidString(this.form.controls[this.getFormControlName('data')].value);
    const path = getValidString(this.form.controls[this.getFormControlName('path')].value);
    const extractionRegex = getValidString(this.form.controls[this.getFormControlName('extractionRegex')].value);
    let factorToValue;
    if (!this.disableFactorToValue) {
      factorToValue = getValidFloat(this.form.controls[this.getFormControlName('factorToValue')].value);
    }

    if (!(name || data || path || extractionRegex || factorToValue)) {
      return undefined;
    }

    this.httpReadValue.name = name;
    this.httpReadValue.data = data;
    this.httpReadValue.path = path;
    this.httpReadValue.extractionRegex = extractionRegex;
    this.httpReadValue.factorToValue = factorToValue;
    return this.httpReadValue;
  }
}
