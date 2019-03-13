import {AfterViewChecked, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
import {ModbusSwitch} from './modbus-switch';
import {ModbusSettings} from '../settings/modbus-settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {FormMarkerService} from '../shared/form-marker-service';
import {FormHandler} from '../shared/form-handler';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';

@Component({
  selector: 'app-control-modbus',
  templateUrl: './control-modbus.component.html',
  styleUrls: ['../global.css']
})
export class ControlModbusComponent implements OnInit, AfterViewChecked {
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
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages =  new ControlModbusErrorMessages(this.translate);
    this.form = this.buildModbusFormGroup(this.control.modbusSwitch);
    this.form.statusChanges.subscribe(() => {
      this.childFormChanged.emit(this.form.valid);
      this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    });
    this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    this.formHandler.markLabelsRequired();
  }

  buildModbusFormGroup(modbusSwitch: ModbusSwitch): FormGroup {
    const fg =  new FormGroup({});
    this.formHandler.addFormControl(fg, 'modbusTcpId', modbusSwitch && modbusSwitch.idref,
      [Validators.required]);
    this.formHandler.addFormControl(fg, 'slaveAddress', modbusSwitch && modbusSwitch.slaveAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER)]);
    this.formHandler.addFormControl(fg, 'registerAddress', modbusSwitch && modbusSwitch.registerAddress,
      [Validators.required, Validators.pattern(InputValidatorPatterns.INTEGER_OR_HEX)]);
    this.formHandler.addFormControl(fg, 'registerType', modbusSwitch && modbusSwitch.registerType,
      [Validators.required]);
    this.formHandler.addFormControl(fg, 'onValue', modbusSwitch && modbusSwitch.onValue,
      [Validators.required]);
    this.formHandler.addFormControl(fg, 'offValue', modbusSwitch && modbusSwitch.offValue,
      [Validators.required]);
    return fg;
  }

  updateModbusSwitch(form: FormGroup, modbusSwitch: ModbusSwitch) {
    modbusSwitch.idref = form.controls.modbusTcpId.value;
    modbusSwitch.slaveAddress = form.controls.slaveAddress.value;
    modbusSwitch.registerAddress = form.controls.registerAddress.value;
    modbusSwitch.registerType = form.controls.registerType.value;
    modbusSwitch.onValue = form.controls.onValue.value;
    modbusSwitch.offValue = form.controls.offValue.value;
  }

  submitForm() {
    this.updateModbusSwitch(this.form, this.control.modbusSwitch);
    this.controlService.updateControl(this.control, this.applianceId).subscribe(() => this.appliancesReloadService.reload());
    this.form.markAsPristine();
  }
}
