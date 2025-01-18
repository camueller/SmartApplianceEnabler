import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {ModbusSetting} from './modbus-setting';
import {TranslateService} from '@ngx-translate/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {SettingsService} from '../settings-service';
import {ERROR_INPUT_REQUIRED, ErrorMessage, ValidatorType} from '../../shared/error-message';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';
import {isRequired} from '../../shared/form-util';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {SettingsDefaults} from '../settings-defaults';
import {SettingsModbusModel} from './settings-modbus.model';

@Component({
    selector: 'app-settings-modbus',
    templateUrl: './settings-modbus.component.html',
    styleUrls: ['./settings-modbus.component.scss'],
    standalone: false
})
export class SettingsModbusComponent implements OnChanges, OnInit {
  @Input()
  modbusSetting: ModbusSetting;
  @Input()
  settingsDefaults: SettingsDefaults;
  @Input()
  form: FormGroup<SettingsModbusModel>;
  @Output()
  remove = new EventEmitter<any>();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private settingsService: SettingsService,
              private translate: TranslateService,
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
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

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('modbusTcpId', new FormControl(this.modbusSetting?.modbusTcpId, Validators.required));
    this.form.addControl('modbusTcpHost', new FormControl(this.modbusSetting?.modbusTcpHost,
      Validators.pattern(InputValidatorPatterns.HOSTNAME)));
    this.form.addControl('modbusTcpPort', new FormControl(this.modbusSetting?.modbusTcpPort, Validators.pattern(InputValidatorPatterns.INTEGER)));
  }

  removeModbus() {
    this.remove.emit();
  }

  updateModelFromForm(): ModbusSetting | undefined {
    const modbusTcpId = this.form.controls.modbusTcpId.value;
    const modbusTcpHost = this.form.controls.modbusTcpHost.value;
    const modbusTcpPort = this.form.controls.modbusTcpPort.value;

    if (!(modbusTcpId || modbusTcpHost || modbusTcpPort)) {
      return undefined;
    }

    this.modbusSetting.modbusTcpId = modbusTcpId;
    this.modbusSetting.modbusTcpHost = modbusTcpHost;
    this.modbusSetting.modbusTcpPort = modbusTcpPort;
    return this.modbusSetting;
  }
}
