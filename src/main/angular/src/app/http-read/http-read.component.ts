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
import {FormArray, FormGroup, Validators} from '@angular/forms';
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
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.httpRead) {
      if (changes.httpRead.currentValue) {
        this.httpRead = changes.httpRead.currentValue;
      } else {
        this.httpRead = HttpRead.createWithSingleChild();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('HttpReadComponent.error.', [
      new ErrorMessage('url', ValidatorType.required),
      new ErrorMessage('url', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
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
    this.httpReadValuesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  removeHttpReadValue(index: number) {
    this.httpRead.readValues.splice(index, 1);
    this.httpReadValuesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get httpReadValuesFormArray() {
    return this.form.controls.httpReadValues as FormArray;
  }

  getHttpReadValueFormGroup(index: number) {
    return this.httpReadValuesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'url', this.httpRead.url,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'httpReadValues',
      this.httpRead.readValues);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'url', this.httpRead.url);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'httpReadValues',
      this.httpRead.readValues);
  }

  updateModelFromForm(): HttpRead | undefined {
    const url = this.form.controls.url.value;
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
