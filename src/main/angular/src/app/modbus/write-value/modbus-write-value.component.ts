import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ModbusWriteValue} from './modbus-write-value';
import {ErrorMessages} from '../../shared/error-messages';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {ModbusWriteValueModel} from './modbus-write-value.model';
import { isRequired } from 'src/app/shared/form-util';

@Component({
    selector: 'app-modbus-write-value',
    templateUrl: './modbus-write-value.component.html',
    styleUrls: ['./modbus-write-value.component.scss'],
    standalone: false
})
export class ModbusWriteValueComponent implements OnChanges, OnInit {
  @Input()
  modbusWriteValue: ModbusWriteValue;
  @Input()
  valueNames: string[];
  @Input()
  form: FormGroup<ModbusWriteValueModel>;
  @Input()
  translationPrefix = '';
  @Input()
  translationKeys: string[];
  translatedStrings: { [key: string]: string } = {};
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  @Output()
  nameChanged = new EventEmitter<any>();

  constructor(private logger: Logger,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.modbusWriteValue) {
      if (changes.modbusWriteValue.currentValue) {
        this.modbusWriteValue = changes.modbusWriteValue.currentValue;
      } else {
        this.modbusWriteValue = new ModbusWriteValue();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ModbusReadValueComponent.error.', [
      new ErrorMessage('name', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('value', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    this.translate.get(this.translationKeys).subscribe(translatedStrings => {
      this.translatedStrings = translatedStrings;
    });
  }

  public getTranslatedValueName(valueName: string) {
    const textKey = `${this.translationPrefix}${valueName}`;
    return this.translatedStrings[textKey];
  }

  onNameChanged(newName?: string) {
    if (newName) {
      const event: ValueNameChangedEvent = {name: newName};
      this.nameChanged.emit(event);
    }
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('name', new FormControl(this.modbusWriteValue?.name, Validators.required));
    if (this.modbusWriteValue) {
      this.onNameChanged(this.modbusWriteValue.name);
    }
    this.form.addControl('value', new FormControl(this.modbusWriteValue?.value, Validators.required));
  }

  updateModelFromForm(): ModbusWriteValue | undefined {
    const name = this.form.controls.name.value;
    const value = this.form.controls.value.value;

    if (!(name || value)) {
      return undefined;
    }

    this.modbusWriteValue.name = name;
    this.modbusWriteValue.value = value;
    return this.modbusWriteValue;
  }
}
