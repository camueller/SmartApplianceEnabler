import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Control} from '../control/control';
import {ControlDefaults} from '../control/control-defaults';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {Logger} from '../log/logger';
import {ControlService} from '../control/control-service';
import {TranslateService} from '@ngx-translate/core';
import {ControlModbusErrorMessages} from './control-modbus-error-messages';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {ModbusSwitch} from '../control/modbus-switch';
import {ModbusSettings} from '../settings/modbus-settings';
import {SettingsDefaults} from '../settings/settings-defaults';

@Component({
  selector: 'app-control-modbus',
  templateUrl: './control-modbus.component.html',
  styles: []
})
export class ControlModbusComponent implements OnInit {
  @Input()
  control: Control;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  modbusSettings: ModbusSettings[];
  @Input()
  settingsDefaults: SettingsDefaults;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  modbusForm: FormGroup;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.errorMessages =  new ControlModbusErrorMessages(this.translate);
    this.modbusForm = this.buildModbusFormGroup(this.control.modbusSwitch);
    this.modbusForm.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.modbusForm.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.modbusForm, this.errorMessages);
      console.log('ERRORS=', this.errors);
    });
  }

  buildModbusFormGroup(modbusSwitch: ModbusSwitch): FormGroup {
    return new FormGroup({
      modbusTcpId: new FormControl(modbusSwitch ? modbusSwitch.idref : undefined,
        [Validators.required]),
      slaveAddress: new FormControl(modbusSwitch ? modbusSwitch.slaveAddress : undefined,
        [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]),
      registerAddress: new FormControl(modbusSwitch ? modbusSwitch.registerAddress : undefined,
        [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]),
      registerType: new FormControl(modbusSwitch ? modbusSwitch.registerType : undefined,
        [Validators.required]),
      onValue: new FormControl(modbusSwitch ? modbusSwitch.onValue : undefined,
        [Validators.required]),
      offValue: new FormControl(modbusSwitch ? modbusSwitch.offValue : undefined,
        [Validators.required]),
    });
  }
}
