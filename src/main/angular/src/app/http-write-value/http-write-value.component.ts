import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpWriteValue} from './http-write-value';
import {HttpWriteValueErrorMessages} from './http-write-value-error-messages';
import {EvChargerProtocol} from '../control-evcharger/ev-charger-protocol';
import {HttpMethod} from '../shared/http-method';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-http-write-value',
  templateUrl: './http-write-value.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpWriteValueComponent implements OnInit, AfterViewChecked, OnDestroy {
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
  nestedFormServiceSubscription: Subscription;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    // console.log('formControlNamePrefix=', this.formControlNamePrefix);
    // console.log('httpReadValue=', this.httpReadValue);
    // console.log('valueNames=', this.valueNames);
    // console.log('translationKeys=', this.translationKeys);
    this.errorMessages = new HttpWriteValueErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpWriteValue, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
      // console.log('translatedStrings=', this.translatedStrings);
    });
    this.nestedFormServiceSubscription = this.nestedFormService.submitted.subscribe(
      () => this.updateHttpWriteValue(this.httpWriteValue, this.form));
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    this.nestedFormServiceSubscription.unsubscribe();
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName}`;
    return this.translatedStrings[textKey];
  }

  get readValueName() {
    return this.valueNames.length === 1 ? this.valueNames[0] : this.httpWriteValue.name;
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
      httpWriteValue ? httpWriteValue.name : undefined);
    formHandler.addFormControl(form, this.getFormControlName('value'),
      httpWriteValue ? httpWriteValue.value : undefined);
    if (!this.disableFactorToValue) {
      formHandler.addFormControl(form, this.getFormControlName('factorToValue'),
        httpWriteValue ? httpWriteValue.factorToValue : undefined,
        [Validators.pattern(InputValidatorPatterns.FLOAT)]);
    }
    formHandler.addFormControl(form, this.getFormControlName('method'),
      httpWriteValue ? httpWriteValue.method : undefined);
    console.log('form=', this.form);
  }

  updateHttpWriteValue(httpWriteValue: HttpWriteValue, form: FormGroup) {
    httpWriteValue.name = this.form.controls[this.getFormControlName('name')].value;
    httpWriteValue.value = this.form.controls[this.getFormControlName('value')].value;
    httpWriteValue.factorToValue = this.form.controls[this.getFormControlName('factorToValue')].value;
    httpWriteValue.method = this.form.controls[this.getFormControlName('method')].value;
    this.nestedFormService.complete();
  }
}
