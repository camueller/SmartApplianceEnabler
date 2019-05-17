import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Logger} from '../log/logger';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {FormHandler} from '../shared/form-handler';
import {MeterHttpErrorMessages} from '../meter-http/meter-http-error-messages';
import {ErrorMessages} from '../shared/error-messages';
import {HttpReadValue} from './http-read-value';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpReadValueErrorMessages} from './http-read-value-error-messages';

@Component({
  selector: 'app-http-read-value',
  templateUrl: './http-read-value.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpReadValueComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input()
  httpReadValue: HttpReadValue;
  @Input()
  valueNames: string[];
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
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new HttpReadValueErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpReadValue, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
    this.nestedFormService.submitted.subscribe(
      () => this.updateHttpReadValue(this.httpReadValue, this.form));
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    // FIXME: erzeugt Fehler bei Wechsel des Zählertypes
    // this.nestedFormService.submitted.unsubscribe();
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName.toLowerCase()}`;
    return this.translatedStrings[textKey];
  }

  get selectedValueName() {
    return this.valueNames.length === 1 && this.valueNames[0];
  }

  get disabled() {
    return ! this.form.controls[this.getFormControlName('enabled')].value;
  }

  expandParentForm(form: FormGroup, httpReadValue: HttpReadValue, formHandler: FormHandler) {
    form.addControl(this.getFormControlName('enabled'), new FormControl({}));
    formHandler.addFormControl(form, this.getFormControlName('name'),
      httpReadValue ? httpReadValue.name : undefined);
    formHandler.addFormControl(form, this.getFormControlName('data'),
      httpReadValue ? httpReadValue.data : undefined);
    formHandler.addFormControl(form, this.getFormControlName('path'),
      httpReadValue ? httpReadValue.path : undefined);
    formHandler.addFormControl(form, this.getFormControlName('extractionRegex'),
      httpReadValue ? httpReadValue.extractionRegex : undefined);
    formHandler.addFormControl(form, this.getFormControlName('factorToValue'),
      httpReadValue ? httpReadValue.factorToValue : undefined,
    [Validators.pattern(InputValidatorPatterns.FLOAT)]);
  }

  updateHttpReadValue(httpReadValue: HttpReadValue, form: FormGroup) {
    httpReadValue.name = this.form.controls[this.getFormControlName('name')].value;
    httpReadValue.data = this.form.controls[this.getFormControlName('data')].value;
    httpReadValue.path = this.form.controls[this.getFormControlName('path')].value;
    httpReadValue.extractionRegex = this.form.controls[this.getFormControlName('extractionRegex')].value;
    httpReadValue.factorToValue = this.form.controls[this.getFormControlName('factorToValue')].value;
    this.nestedFormService.complete();
  }
}
