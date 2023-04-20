import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
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
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {HttpWrite} from './http-write';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../../shared/form-util';
import {HttpWriteValueComponent} from '../write-value/http-write-value.component';
import {HttpWriteValue} from '../write-value/http-write-value';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {HttpWriteModel} from './http-write.model';
import {HttpWriteValueModel} from '../write-value/http-write-value.model';

@Component({
  selector: 'app-http-write',
  templateUrl: './http-write.component.html',
  styleUrls: ['./http-write.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
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
  form: FormGroup<HttpWriteModel>;
  @Input()
  translationPrefix: string;
  @Input()
  translationKeys: string[];
  @Output()
  remove = new EventEmitter<any>();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  @Output()
  nameChanged = new EventEmitter<any>();

  constructor(private logger: Logger,
              private translate: TranslateService,
              private changeDetectorRef: ChangeDetectorRef
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
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
      new ErrorMessage('url', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('url', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  onNameChanged(index: number, event: ValueNameChangedEvent) {
    event.valueIndex = index;
    this.nameChanged.emit(event);
  }

  removeHttpWrite() {
    this.remove.emit();
  }

  get isAddValuePossible() {
    return !this.httpWrite.writeValues || !this.maxValues || this.httpWrite.writeValues.length < this.maxValues;
  }

  addValue() {
    const newWriteValue = new HttpWriteValue();
    this.httpWrite.writeValues.push(newWriteValue);
    this.httpWriteValuesFormArray.push(new FormGroup({} as HttpWriteValueModel));
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  removeValue(index: number) {
    this.httpWrite.writeValues.splice(index, 1);
    this.httpWriteValuesFormArray.removeAt(index);

    const event: ValueNameChangedEvent = {valueIndex: index};
    this.nameChanged.emit(event);

    this.form.markAsDirty();
  }

  getRemoveValueCssClass(index: number): string {
    return `removeValue${index}`;
  }

  get httpWriteValuesFormArray() {
    return this.form.controls.httpWriteValues;
  }

  getHttpWriteValueFormGroup(index: number) {
    return this.httpWriteValuesFormArray.controls[index];
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('url', new FormControl(this.httpWrite?.url,
      [Validators.required, Validators.pattern(InputValidatorPatterns.URL)]))
    this.form.addControl('httpWriteValues', buildFormArrayWithEmptyFormGroups(this.httpWrite.writeValues));
  }

  updateForm() {
    this.form.controls.url.setValue(this.httpWrite.url);
    this.form.setControl('httpWriteValues', buildFormArrayWithEmptyFormGroups(this.httpWrite.writeValues));
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

    this.httpWrite.url = url;
    this.httpWrite.writeValues = httpWriteValues;
    return this.httpWrite;
  }
}
