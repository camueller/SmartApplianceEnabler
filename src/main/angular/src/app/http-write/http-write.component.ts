import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpWrite} from './http-write';
import {HttpWriteValue} from '../http-write-value/http-write-value';
import {HttpWriteErrorMessages} from './http-write-error-messages';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-http-write',
  templateUrl: './http-write.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpWriteComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input()
  httpWrite: HttpWrite;
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
    this.errorMessages = new HttpWriteErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpWrite, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
    this.nestedFormServiceSubscription = this.nestedFormService.submitted.subscribe(
      () => this.updateHttpWrite(this.httpWrite, this.form));
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    this.nestedFormServiceSubscription.unsubscribe();
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
    formHandler.addFormControl(form, 'url',
      httpWrite ? httpWrite.url : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
  }

  updateHttpWrite(httpWrite: HttpWrite, form: FormGroup) {
    httpWrite.url = this.form.controls.url.value;
    this.nestedFormService.complete();
  }
}
