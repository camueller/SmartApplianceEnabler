/*
Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, CanDeactivate} from '@angular/router';
import {ControlFactory} from './control-factory';
import {Switch} from './switch';
import {ModbusSwitch} from './modbus-switch';
import {HttpSwitch} from './http-switch';
import {StartingCurrentSwitch} from './starting-current-switch';
import {NgForm} from '@angular/forms';
import {ControlErrorMessages} from './control-error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessages} from '../shared/error-messages';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {AlwaysOnSwitch} from './always-on-switch';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {ControlDefaults} from './control-defaults';
import {ControlService} from './control-service';
import {Control} from './control';
import {Observable} from 'rxjs/Observable';
import {DialogService} from '../shared/dialog.service';
import {MockSwitch} from './mock-switch';
import {Logger} from '../log/logger';
import {Settings} from '../settings/settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {ModbusRegisterConfguration} from '../shared/modbus-register-confguration';
import {EvCharger} from './ev-charger';
import {Appliance} from '../appliance/appliance';

@Component({
  selector: 'app-appliance-switch',
  templateUrl: './control.component.html',
  styles: []
})
export class ControlComponent implements OnInit, CanDeactivate<ControlComponent> {
  @ViewChild('controlForm') controlForm: NgForm;
  applianceId: string;
  controlDefaults: ControlDefaults;
  control: Control;
  appliance: Appliance;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  translatedStrings: string[];
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  discardChangesMessage: string;
  APPLIANCE_TYPE_EVCHARGER = 'EVCharger';
  TYPE_ALWAYS_ON_SWITCH = AlwaysOnSwitch.TYPE;
  TYPE_SWITCH = Switch.TYPE;
  TYPE_MODBUS_SWITCH = ModbusSwitch.TYPE;
  TYPE_MOCK_SWITCH = MockSwitch.TYPE;
  TYPE_HTTP_SWITCH = HttpSwitch.TYPE;
  TYPE_EVCHARGER = EvCharger.TYPE;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_INTEGER_OR_HEX = InputValidatorPatterns.INTEGER_OR_HEX;
  VALIDATOR_PATTERN_URL = InputValidatorPatterns.URL;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private dialogService: DialogService,
              private translate: TranslateService) {
    const controlFactory = new ControlFactory(logger);
    this.control = controlFactory.createEmptyControl();
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.translate.get([
      'ControlComponent.evcharger_VehicleNotConnected',
      'ControlComponent.evcharger_VehicleConnected',
      'ControlComponent.evcharger_Charging',
      'ControlComponent.evcharger_ChargingCompleted',
      'ControlComponent.evcharger_StartCharging',
      'ControlComponent.evcharger_StopCharging',
      'ControlComponent.evcharger_ChargingCurrent'
    ]).subscribe(translatedStrings => this.translatedStrings = translatedStrings);
    this.errorMessages =  new ControlErrorMessages(this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {control: Control, controlDefaults: ControlDefaults, appliance: Appliance,
      settings: Settings, settingsDefaults: SettingsDefaults}) => {
      this.control = data.control;
      this.controlDefaults = data.controlDefaults;
      this.appliance = data.appliance;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.controlForm.form.markAsPristine();
    });
    this.controlForm.statusChanges.subscribe(() =>
      this.errors = this.errorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.controlForm, this.errorMessages));
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.controlForm.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  typeChanged(newType: string) {
    if (newType === '') {
      this.controlForm.form.controls.startingCurrentDetection.setValue(false);
    } else if (newType === this.TYPE_ALWAYS_ON_SWITCH && this.control.alwaysOnSwitch == null) {
      this.control.alwaysOnSwitch = new AlwaysOnSwitch();
    } else if (newType === this.TYPE_SWITCH && this.control.switch_ == null) {
      this.control.switch_ = new Switch();
    } else if (newType === this.TYPE_MODBUS_SWITCH && this.control.modbusSwitch == null) {
      this.control.modbusSwitch = new ModbusSwitch();
    } else if (newType === this.TYPE_MOCK_SWITCH && this.control.mockSwitch == null) {
      this.control.mockSwitch = new MockSwitch();
    } else if (newType === this.TYPE_HTTP_SWITCH && this.control.httpSwitch == null) {
      this.control.httpSwitch = new HttpSwitch();
    }
  }

  startingCurrentDetectionChanged(startingCurrentDetection: boolean) {
    if (startingCurrentDetection) {
      this.control.startingCurrentSwitch = new StartingCurrentSwitch();
      this.control.startingCurrentDetection = true;
    } else {
      this.control.startingCurrentSwitch = null;
      this.control.startingCurrentDetection = false;
    }
  }

  isStartingCurrentDetectedDisabled(): boolean {
    if (this.controlForm.form.contains('controlType')) {
      return  this.controlForm.form.controls.controlType.value === this.TYPE_ALWAYS_ON_SWITCH;
    }
    return false;
  }

  addModbusConfiguration() {
    this.control.evCharger.control.configuration.push({} as ModbusRegisterConfguration);
  }

  removeModbusConfiguration(index: number) {
    this.control.evCharger.control.configuration.splice(index, 1);
  }

  getModbusRegisterNames(): string[] {
    return this.control.evCharger.control.configuration.map(configuration => configuration.name);
  }

  getTranslatedModbusRegisterName(name: string) {
    return this.translatedStrings[this.toTextKeyModbusRegisterName(name)];
  }

  toTextKeyModbusRegisterName(name: string) {
    return 'ControlComponent.evcharger_' + name;
  }

  getModbusRegisterTypes(write: boolean): string[] {
    if (write) {
      return this.settingsDefaults.modbusWriteRegisterTypes;
    }
    return this.settingsDefaults.modbusReadRegisterTypes;
  }

  submitForm() {
    this.controlService.updateControl(this.control, this.applianceId).subscribe(() => this.appliancesReloadService.reload());
    this.controlForm.form.markAsPristine();
  }
}
