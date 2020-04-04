import {
  AfterViewChecked,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {ControlDefaults} from '../control/control-defaults';
import {ControlContainer, FormArray, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusSwitch} from './modbus-switch';
import {ModbusSetting} from '../settings-modbus/modbus-setting';
import {SettingsDefaults} from '../settings/settings-defaults';
import {FormHandler} from '../shared/form-handler';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {ModbusWriteComponent} from '../modbus-write/modbus-write.component';
import {ModbusWrite} from '../modbus-write/modbus-write';
import {ControlValueName} from '../control/control-value-name';
import {fixExpressionChangedAfterItHasBeenCheckedError, getValidString} from '../shared/form-util';

@Component({
  selector: 'app-control-modbus',
  templateUrl: './control-modbus.component.html',
  styleUrls: [],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlModbusComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  modbusSwitch: ModbusSwitch;
  @ViewChildren('modbusWriteComponents')
  modbusWriteComps: QueryList<ModbusWriteComponent>;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  modbusSettings: ModbusSetting[];
  @Input()
  settingsDefaults: SettingsDefaults;
  form: FormGroup;
  formHandler: FormHandler;
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
    if (changes.modbusSwitch) {
      if (changes.modbusSwitch.currentValue) {
        this.modbusSwitch = changes.modbusSwitch.currentValue;
      } else {
        this.modbusSwitch = new ModbusSwitch();
        this.modbusSwitch.modbusWrites = [ModbusWrite.createWithSingleChild()];
      }
      this.updateForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlModbusComponent.error.', [
      new ErrorMessage('slaveAddress', ValidatorType.required),
      new ErrorMessage('slaveAddress', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  get valueNames() {
    return [ControlValueName.On, ControlValueName.Off];
  }

  get valueNameTextKeys() {
    return ['ControlModbusComponent.On', 'ControlModbusComponent.Off'];
  }

  get isAddModbusWritePossible() {
    if (this.modbusSwitch.modbusWrites.length === 1) {
      return this.modbusSwitch.modbusWrites[0].writeValues.length < 2;
    }
    return this.modbusSwitch.modbusWrites.length < 2;
  }

  get maxValues() {
    return this.modbusSwitch.modbusWrites.length === 2 ? 1 : 2;
  }

  addModbusWrite() {
    fixExpressionChangedAfterItHasBeenCheckedError(this.form);
    this.modbusSwitch.modbusWrites.push(ModbusWrite.createWithSingleChild());
    this.modbusWritesFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  onModbusWriteRemove(index: number) {
    this.modbusSwitch.modbusWrites.splice(index, 1);
    this.modbusWritesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get modbusWritesFormArray() {
    return this.form.controls.modbusWrites as FormArray;
  }

  getModbusWriteFormGroup(index: number) {
    return this.modbusWritesFormArray.controls[index];
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'idref', this.modbusSwitch.idref,
      [Validators.required]);
    this.formHandler.addFormControl(this.form, 'slaveAddress', this.modbusSwitch.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusWrites',
      this.modbusSwitch.modbusWrites);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'idref', this.modbusSwitch.idref);
    this.formHandler.setFormControlValue(this.form, 'slaveAddress', this.modbusSwitch.slaveAddress);
    this.formHandler.setFormArrayControlWithEmptyFormGroups(this.form, 'modbusWrites',
      this.modbusSwitch.modbusWrites);
  }

  updateModelFromForm(): ModbusSwitch | undefined {
    const idref = getValidString(this.form.controls.idref.value);
    const slaveAddress = getValidString(this.form.controls.slaveAddress.value);
    const modbusWrites = [];
    this.modbusWriteComps.forEach(modbusWriteComponent => {
      const modbusWrite = modbusWriteComponent.updateModelFromForm();
      if (modbusWrite) {
        modbusWrites.push(modbusWrite);
      }
    });

    if (!(idref || slaveAddress || modbusWrites.length > 0)) {
      return undefined;
    }

    this.modbusSwitch.idref = idref;
    this.modbusSwitch.slaveAddress = slaveAddress;
    this.modbusSwitch.modbusWrites = modbusWrites;
    return this.modbusSwitch;
  }
}
