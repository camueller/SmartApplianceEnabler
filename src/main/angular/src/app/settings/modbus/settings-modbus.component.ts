import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {ModbusSetting} from './modbus-setting';
import {TranslateService} from '@ngx-translate/core';
import {UntypedFormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {FormHandler} from '../../shared/form-handler';
import {SettingsService} from '../settings-service';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {getValidInt, getValidString} from '../../shared/form-util';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {SettingsDefaults} from '../settings-defaults';

@Component({
  selector: 'app-settings-modbus',
  templateUrl: './settings-modbus.component.html',
  styleUrls: ['./settings-modbus.component.scss']
})
export class SettingsModbusComponent implements OnChanges, OnInit {
  @Input()
  modbusSetting: ModbusSetting;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Input()
  form: UntypedFormGroup;
  @Output()
  remove = new EventEmitter<any>();
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private settingsService: SettingsService,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.modbusSetting) {
      if (changes.modbusSetting.currentValue) {
        this.modbusSetting = changes.modbusSetting.currentValue;
      } else {
        this.modbusSetting = new ModbusSetting();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('SettingsComponent.error.', [
      new ErrorMessage('modbusTcpId', ValidatorType.required, ERROR_INPUT_REQUIRED, true),
      new ErrorMessage('modbusTcpHost', ValidatorType.pattern),
      new ErrorMessage('modbusTcpPort', ValidatorType.pattern),
    ], this.translate);
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'modbusTcpId',
      this.modbusSetting && this.modbusSetting.modbusTcpId, Validators.required);
    this.formHandler.addFormControl(this.form, 'modbusTcpHost',
      this.modbusSetting && this.modbusSetting.modbusTcpHost, Validators.pattern(InputValidatorPatterns.HOSTNAME));
    this.formHandler.addFormControl(this.form, 'modbusTcpPort',
      this.modbusSetting && this.modbusSetting.modbusTcpPort, Validators.pattern(InputValidatorPatterns.INTEGER));
  }

  removeModbus() {
    this.remove.emit();
  }

  updateModelFromForm(): ModbusSetting | undefined {
    const modbusTcpId = getValidString(this.form.controls.modbusTcpId.value);
    const modbusTcpHost = getValidString(this.form.controls.modbusTcpHost.value);
    const modbusTcpPort = getValidInt(this.form.controls.modbusTcpPort.value);

    if (!(modbusTcpId || modbusTcpHost || modbusTcpPort)) {
      return undefined;
    }

    this.modbusSetting.modbusTcpId = modbusTcpId;
    this.modbusSetting.modbusTcpHost = modbusTcpHost;
    this.modbusSetting.modbusTcpPort = modbusTcpPort;
    return this.modbusSetting;
  }
}
