import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ModbusReadValue} from './modbus-read-value';
import {ErrorMessages} from '../../shared/error-messages';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {ValueNameChangedEvent} from '../../meter/value-name-changed-event';
import {isRequired} from 'src/app/shared/form-util';
import {ModbusReadValueModel} from './modbus-read-value.model';

@Component({
  selector: 'app-modbus-read-value',
  templateUrl: './modbus-read-value.component.html',
  styleUrls: ['./modbus-read-value.component.scss'],
})
export class ModbusReadValueComponent implements OnChanges, OnInit {
  @Input()
  modbusReadValue: ModbusReadValue;
  @Input()
  valueNames: string[];
  @Input()
  form: FormGroup<ModbusReadValueModel>;
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
    if (changes.modbusReadValue) {
      if (changes.modbusReadValue.currentValue) {
        this.modbusReadValue = changes.modbusReadValue.currentValue;
      } else {
        this.modbusReadValue = new ModbusReadValue();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    if (this.form && !this.form.controls.name.value && this.valueNames.length === 1) {
      this.form.controls.name.setValue(this.valueNames[0]);
    }
    this.errorMessages = new ErrorMessages('ModbusReadValueComponent.error.', [
      new ErrorMessage('name', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('factorToValue', ValidatorType.pattern),
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
    this.form.addControl('name', new FormControl(this.modbusReadValue?.name, Validators.required));
    if (this.modbusReadValue) {
      this.onNameChanged(this.modbusReadValue.name);
    }
    this.form.addControl('extractionRegex', new FormControl(this.modbusReadValue?.extractionRegex));
  }

  updateModelFromForm(): ModbusReadValue | undefined {
    const name = this.form.controls.name.value;
    const extractionRegex = this.form.controls.extractionRegex.value;

    if (!(name || extractionRegex)) {
      return undefined;
    }

    this.modbusReadValue.name = name;
    this.modbusReadValue.extractionRegex = extractionRegex;
    return this.modbusReadValue;
  }
}
