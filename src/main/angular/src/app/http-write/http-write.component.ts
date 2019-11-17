import {
  AfterViewChecked,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
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
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidString} from '../shared/form-util';
import {HttpWriteValueComponent} from '../http-write-value/http-write-value.component';

@Component({
  selector: 'app-http-write',
  templateUrl: './http-write.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpWriteComponent implements OnChanges, OnInit, AfterViewChecked {
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

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.httpWrite) {
      if (changes.httpWrite.currentValue) {
        this.httpWrite = changes.httpWrite.currentValue;
      } else {
        this.httpWrite = HttpWrite.createWithSingleChild();
      }
      this.updateForm(this.form, this.httpWrite, this.formHandler);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('HttpWriteComponent.error.', [
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.required, 'url'),
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.pattern, 'url'),
    ], this.translate);
    this.expandParentForm(this.form, this.httpWrite, this.formHandler);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    if (this.translate) {
      this.translate.get(this.translationKeys).subscribe(translatedStrings => {
        this.translatedStrings = translatedStrings;
      });
    }
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

  removeHttpWrite() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.httpWrite.writeValues || !this.maxValues || this.httpWrite.writeValues.length < this.maxValues;
  }

  addValue() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
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

  getRemoveValueCssClass(index: number): string {
    return `removeValue${index}`;
  }

  expandParentForm(form: FormGroup, httpWrite: HttpWrite, formHandler: FormHandler) {
    formHandler.addFormControl(form, this.getFormControlName('url'),
      httpWrite ? httpWrite.url : undefined,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
  }

  updateForm(form: FormGroup, httpWrite: HttpWrite, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('url'), httpWrite.url);
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

    this.httpWrite.url = getValidString(url);
    this.httpWrite.writeValues = httpWriteValues;
    return this.httpWrite;
  }
}
