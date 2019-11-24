import {
  AfterViewChecked,
  ChangeDetectionStrategy,
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
import {ControlContainer, FormControl, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {HttpRead} from './http-read';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {HttpReadValue} from '../http-read-value/http-read-value';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidString} from '../shared/form-util';
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
export class HttpReadComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  httpRead: HttpRead;
  @ViewChildren('httpReadValues')
  httpReadValueComps: QueryList<HttpReadValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  minValues: number;
  @Input()
  maxValues: number;
  @Input()
  disableFactorToValue = false;
  @Input()
  disableRemove = false;
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
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    console.log('form=', this.form);
    if (changes.httpRead) {
      if (changes.httpRead.currentValue) {
        this.httpRead = changes.httpRead.currentValue;
      } else {
        this.httpRead = HttpRead.createWithSingleChild();
      }
      this.updateForm(this.form, this.httpRead, this.formHandler);
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('HttpReadComponent.error.', [
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.required, 'url'),
      new ErrorMessage(this.getFormControlName('url'), ValidatorType.pattern, 'url'),
    ], this.translate);
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
    return `${this.formControlNamePrefix}.readValue${index}.`;
  }

  get isRemoveValuePossible() {
    return !this.minValues || this.httpRead.readValues.length > this.minValues;
  }

  removeHttpRead() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.httpRead.readValues || !this.maxValues || this.httpRead.readValues.length < this.maxValues;
  }

  addValue() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
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
    formHandler.addFormControl(form, this.getFormControlName('url'), httpRead.url,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
  }

  updateForm(form: FormGroup, httpRead: HttpRead, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, this.getFormControlName('url'), httpRead.url);
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

    this.httpRead.url = getValidString(url);
    return this.httpRead;
  }
}
