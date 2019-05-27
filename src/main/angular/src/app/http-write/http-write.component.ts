import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpWrite} from './http-write';
import {HttpWriteValue} from '../http-write-value/http-write-value';
import {HttpWriteErrorMessages} from './http-write-error-messages';

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
    console.log('singleValue=', this.singleValue);
    this.errorMessages = new HttpWriteErrorMessages(this.translate);
    this.form = this.parent.form;
    this.expandParentForm(this.form, this.httpWrite, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
    this.nestedFormService.submitted.subscribe(
      () => this.updateHttpWrite(this.httpWrite, this.form));
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    // this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    // FIXME: erzeugt Fehler bei Wechsel des Zählertypes
    // this.nestedFormService.submitted.unsubscribe();
  }

  getWriteValueFormControlPrefix(index: number) {
    return `${this.formControlNamePrefix}writeValue${index}.`;
  }

  getFormControlName(formControlName: string): string {
    return `${this.formControlNamePrefix}${formControlName.charAt(0).toUpperCase()}${formControlName.slice(1)}`;
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName.toLowerCase()}`;
    return this.translatedStrings[textKey];
  }

  get valueName() {
    if (this.httpWrite.writeValues && this.httpWrite.writeValues.length === 1) {
      const httpWriteValue = this.httpWrite.writeValues[0];
      return this.getTranslatedValueName(httpWriteValue.name);
    }
    return undefined;
  }

  get disabled() {
    return ! this.form.controls[this.getFormControlName('enabled')].value;
  }

  addValue() {
    // const newEv = this.buildElectricVehicleFormGroup(undefined, newEvId);
    // this.electricVehicles.push(newEv);
    const newWriteValue = new HttpWriteValue();
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

  updateHttpWrite(httpWrite: HttpWrite, form: FormGroup) {
    httpWrite.url = this.form.controls[this.getFormControlName('url')].value;
    this.nestedFormService.complete();
  }
}
