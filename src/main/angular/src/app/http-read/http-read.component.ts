import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output, QueryList, ViewChildren} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {HttpRead} from './http-read';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpReadValue} from '../http-read-value/http-read-value';
import {getValidString} from '../shared/form-util';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {HttpReadValueComponent} from '../http-read-value/http-read-value.component';

@Component({
  selector: 'app-http-read',
  templateUrl: './http-read.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpReadComponent implements OnInit, AfterViewChecked {
  @Input()
  httpRead: HttpRead;
  @ViewChildren('httpReadValues')
  httpReadValueComps: QueryList<HttpReadValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  maxValues: number;
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
  @Output()
  remove = new EventEmitter<any>();
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
    this.httpRead = this.httpRead || new HttpRead();
    this.errorMessages = new ErrorMessages('HttpReadComponent.error.', [
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.required, 'url'),
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.pattern, 'url'),
    ], this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpRead, this.formHandler);
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

  getReadValueFormControlPrefix(index: number) {
    return `${this.formControlNamePrefix}readValue${index}.`;
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName.toLowerCase()}`;
    return this.translatedStrings[textKey];
  }

  get valueName() {
    // TODO ist das so notwendig?
    if (this.httpRead.readValues && this.httpRead.readValues.length === 1) {
      const httpReadValue = this.httpRead.readValues[0];
      if (httpReadValue.name) {
        return this.getTranslatedValueName(httpReadValue.name);
      }
    }
    return undefined;
  }

  removeHttpRead() {
    this.remove.emit();
  }

  get isAddHttpReadPossible() {
    return this.httpRead.readValues.length < this.maxValues;
  }

  addValue() {
    const newReadValue = new HttpReadValue();
    if (!this.httpRead.readValues) {
      this.httpRead.readValues = [];
    }
    this.httpRead.readValues.push(newReadValue);
    this.form.markAsDirty();
  }

  removeHttpReadValue(index: number) {
    this.httpRead.readValues.splice(index, 1);
    this.form.markAsDirty();
  }

  expandParentForm(form: FormGroup, httpRead: HttpRead, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('url'),
      httpRead ? httpRead.url : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
  }

  updateModelFromForm(): HttpRead | undefined {
    const url = this.form.controls[this.getFormControlName('url')].value;
    const httpReadValues = [];
    this.httpReadValueComps.forEach(httpReadValueComp => {
      const httpReadValue = httpReadValueComp.updateModelFromForm();
      if (httpReadValue) {
        httpReadValues.push(httpReadValue);
      }
    });

    if (!(url || httpReadValues.length > 0)) {
      return undefined;
    }

    this.httpRead.url = getValidString(url.value);
    return this.httpRead;
  }
}
