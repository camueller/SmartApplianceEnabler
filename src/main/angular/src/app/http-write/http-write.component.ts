import {AfterViewChecked, Component, Input, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpWrite} from './http-write';
import {HttpWriteValue} from '../http-write-value/http-write-value';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {getValidString} from '../shared/form-util';
import {HttpWriteValueComponent} from '../http-write-value/http-write-value.component';

@Component({
  selector: 'app-http-write',
  templateUrl: './http-write.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpWriteComponent implements OnInit, AfterViewChecked {
  @Input()
  httpWrite: HttpWrite;
  @ViewChildren('httpWriteValues')
  httpWriteValueComps: QueryList<HttpWriteValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  maxValues: number;
  // FIXME: brauchen wir das noch?
  @Input()
  disableFactorToValue = false;
  @Input()
  formControlNamePrefix = '';
  form: FormGroup;
  formHandler: FormHandler;
  @Input()
  translationPrefix: string;
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
    this.httpWrite = this.httpWrite || new HttpWrite();
    this.errorMessages = new ErrorMessages('HttpWriteComponent.error.', [
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.required, 'url'),
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.pattern, 'url'),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpWrite, this.formHandler);
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

  getWriteValueFormControlPrefix(index: number) {
    return `${this.formControlNamePrefix}writeValue${index}.`;
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName.toLowerCase()}`;
    return this.translatedStrings[textKey];
  }

  get valueName() {
    if (this.httpWrite.writeValues && this.httpWrite.writeValues.length === 1) {
      const httpWriteValue = this.httpWrite.writeValues[0];
      if (httpWriteValue.name) {
        return this.getTranslatedValueName(httpWriteValue.name);
      }
    }
    return undefined;
  }

  get isAddValuePossible() {
    return !this.maxValues || this.httpWrite.writeValues.length < this.maxValues;
  }

  addValue() {
    const newWriteValue = new HttpWriteValue();
    if (!this.httpWrite.writeValues) {
      this.httpWrite.writeValues = [];
    }
    this.httpWrite.writeValues.push(newWriteValue);
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.httpWrite.writeValues.splice(index, 1);
    this.form.markAsDirty();
  }

  expandParentForm(form: FormGroup, httpWrite: HttpWrite, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('url'),
      httpWrite ? httpWrite.url : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
  }

  updateModelFromForm(): HttpWrite | undefined {
    const url = this.form.controls[this.getFormControlName('url')].value;
    const httpWriteValues = [];
    this.httpWriteValueComps.forEach(httpWriteValueComp => {
      const httpWriteValue = httpWriteValueComp.updateModelFromForm();
      if (httpWriteValue) {
        httpWriteValues.push(httpWriteValue);
      }
    });

    if (!(url || httpWriteValues.length > 0)) {
      return undefined;
    }

    this.httpWrite.url = getValidString(url.value);
    return this.httpWrite;
  }
}
