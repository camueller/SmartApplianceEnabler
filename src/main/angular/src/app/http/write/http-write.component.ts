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
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {HttpWrite} from './http-write';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidString} from '../../shared/form-util';
import {HttpWriteValueComponent} from '../write-value/http-write-value.component';
import {HttpWriteValue} from '../write-value/http-write-value';

@Component({
  selector: 'app-http-write',
  templateUrl: './http-write.component.html',
  styleUrls: [],
})
export class HttpWriteComponent implements OnChanges, OnInit {
  @Input()
  httpWrite: HttpWrite;
  @ViewChildren('httpWriteValues')
  httpWriteValueComps: QueryList<HttpWriteValueComponent>;
  @Input()
  valueNames: string[];
  @Input()
  maxValues: number;
  @Input()
  disableFactorToValue = false;
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
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.form) {
      this.expandParentForm();
    }
    if (changes.httpWrite) {
      if (changes.httpWrite.currentValue) {
        this.httpWrite = changes.httpWrite.currentValue;
      } else {
        this.httpWrite = HttpWrite.createWithSingleChild();
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('HttpWriteComponent.error.', [
      new ErrorMessage('url', ValidatorType.required),
      new ErrorMessage('url', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    if (this.translate) {
      this.translate.get(this.translationKeys).subscribe(translatedStrings => {
        this.translatedStrings = translatedStrings;
      });
    }
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
    this.httpWrite.writeValues.push(newWriteValue);
    this.httpWriteValuesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  removeValue(index: number) {
    this.httpWrite.writeValues.splice(index, 1);
    this.httpWriteValuesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  getRemoveValueCssClass(index: number): string {
    return `removeValue${index}`;
  }

  get httpWriteValuesFormArray() {
    return this.form.controls.httpWriteValues as FormArray;
  }

  getHttpWriteValueFormGroup(index: number) {
    return this.httpWriteValuesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'url',
      this.httpWrite && this.httpWrite.url,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'httpWriteValues',
      this.httpWrite.writeValues);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'url', this.httpWrite.url);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'httpWriteValues',
      this.httpWrite.writeValues);
  }

  updateModelFromForm(): HttpWrite | undefined {
    const url = this.form.controls.url.value;
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
