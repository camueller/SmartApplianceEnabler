import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {HttpRead} from './http-read';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpReadErrorMessages} from './http-read-error-messages';
import {HttpReadValue} from '../http-read-value/http-read-value';
import {getValidString} from '../shared/form-util';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-http-read',
  templateUrl: './http-read.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpReadComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input()
  httpRead: HttpRead;
  @Input()
  valueNames: string[];
  @Input()
  singleValue = false;
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
  nestedFormServiceSubscription: Subscription;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new HttpReadErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpRead, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
    this.nestedFormServiceSubscription = this.nestedFormService.submitted.subscribe(
      () => this.updateHttpRead(this.httpRead, this.form));
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    this.nestedFormServiceSubscription.unsubscribe();
  }

  getReadValueFormControlPrefix(index: number) {
    return `${this.formControlNamePrefix}readValue${index}.`;
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName.toLowerCase()}`;
    return this.translatedStrings[textKey];
  }

  get valueName() {
    if (this.httpRead.readValues && this.httpRead.readValues.length === 1) {
      const httpReadValue = this.httpRead.readValues[0];
      return this.getTranslatedValueName(httpReadValue.name);
    }
    return undefined;
  }

  get disabled() {
    return ! this.form.controls[this.getFormControlName('enabled')].value;
  }

  addValue() {
    const newReadValue = new HttpReadValue();
    this.httpRead.readValues.push(newReadValue);
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.httpRead.readValues.splice(index, 1);
    this.form.markAsDirty();
  }

  expandParentForm(form: FormGroup, httpRead: HttpRead, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('url'),
      httpRead ? httpRead.url : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
  }

  updateHttpRead(httpRead: HttpRead, form: FormGroup) {
    httpRead.url = getValidString(this.form.controls[this.getFormControlName('url')].value);
    this.nestedFormService.complete();
  }
}
