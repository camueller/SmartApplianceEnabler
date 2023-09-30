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

import {ChangeDetectorRef, Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from './settings-service';
import {Settings} from './settings';
import {SettingsDefaults} from './settings-defaults';
import {DialogService} from '../shared/dialog.service';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Logger} from '../log/logger';
import {ErrorMessage, ValidatorType} from '../shared/error-message';
import {SettingsModbusComponent} from './modbus/settings-modbus.component';
import {ModbusSetting} from './modbus/modbus-setting';
import {buildFormArrayWithEmptyFormGroups, isRequired} from '../shared/form-util';
import {FileMode} from '../material/filenameinput/file-mode';
import {FilenameInputComponent} from '../material/filenameinput/filename-input.component';
import {SettingsModel} from './settings.model';
import {SettingsModbusModel} from './modbus/settings-modbus.model';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent  implements OnInit {
  settings: Settings;
  settingsDefaults: SettingsDefaults;
  @ViewChildren('modbusSettings')
  modbusSettingComps: QueryList<SettingsModbusComponent>;
  @ViewChild(FilenameInputComponent, {static: true})
  notificationCommandInput: FilenameInputComponent;
  form: FormGroup<SettingsModel>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  discardChangesMessage: string;

  constructor(private logger: Logger,
              private settingsService: SettingsService,
              private translate: TranslateService,
              private dialogService: DialogService,
              private route: ActivatedRoute,
              private changeDetectorRef: ChangeDetectorRef) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('SettingsComponent.error.', [
      new ErrorMessage('nodeRedDashboardUrl', ValidatorType.pattern),
      new ErrorMessage('holidaysUrl', ValidatorType.pattern),
      new ErrorMessage('mqttHost', ValidatorType.pattern),
      new ErrorMessage('mqttPort', ValidatorType.pattern),
    ], this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.route.data.subscribe((data: { settings: Settings, settingsDefaults: SettingsDefaults }) => {
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
    });
    this.buildForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  buildForm() {
    this.form = new FormGroup({
      mqttHost: new FormControl(this.settings.mqttSettings?.host, Validators.pattern(InputValidatorPatterns.HOSTNAME)),
      mqttPort: new FormControl(this.settings.mqttSettings?.port, Validators.pattern(InputValidatorPatterns.INTEGER)),
      nodeRedDashboardUrl: new FormControl(this.settings.nodeRedDashboardUrl, Validators.pattern(InputValidatorPatterns.URL)),
      mqttUsername: new FormControl(this.settings.mqttSettings?.username),
      mqttPassword: new FormControl(this.settings.mqttSettings?.password),
      holidaysEnabled: new FormControl(this.settings.holidaysEnabled),
      holidaysUrl: new FormControl(this.settings.holidaysUrl, Validators.pattern(InputValidatorPatterns.URL)),
      modbusSettings: buildFormArrayWithEmptyFormGroups(this.settings?.modbusSettings),
    });
    this.setHolidaysUrlEnabled(this.settings.holidaysEnabled);
    this.form.controls.holidaysEnabled.valueChanges.subscribe(value => {
      this.setHolidaysUrlEnabled(value);
      this.form.markAsDirty();
    });
  }

  setHolidaysUrlEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.holidaysUrl.enable();
    } else {
      this.form.controls.holidaysUrl.disable();
    }
  }

  get modbusSettingsFormArray() {
    return this.form.controls.modbusSettings;
  }

  getModbusSettingFormGroup(index: number) {
    return this.modbusSettingsFormArray.controls[index];
  }

  addModbusSetting() {
    if (! this.settings.modbusSettings) {
      this.settings.modbusSettings = [];
    }
    this.settings.modbusSettings.push(new ModbusSetting());
    this.modbusSettingsFormArray.push(new FormGroup({} as SettingsModbusModel));
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  onModbusSettingRemove(index: number) {
    this.settings.modbusSettings.splice(index, 1);
    this.modbusSettingsFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  public get notificationScriptFileModes() {
    return [FileMode.read, FileMode.execute];
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  updateModelFromForm() {
    const mqttHost = this.form.controls.mqttHost.value;
    const mqttPort = this.form.controls.mqttPort.value;
    const mqttUsername = this.form.controls.mqttUsername.value;
    const mqttPassword = this.form.controls.mqttPassword.value;
    if (mqttHost) {
      this.settings.mqttSettings = {host: mqttHost, port: mqttPort, username: mqttUsername, password: mqttPassword};
    }
    this.settings.nodeRedDashboardUrl = this.form.controls.nodeRedDashboardUrl.value;
    this.settings.holidaysEnabled = this.form.controls.holidaysEnabled.value;
    this.settings.holidaysUrl = this.form.controls.holidaysUrl.value;
    this.settings.modbusSettings = [];
    this.modbusSettingComps.forEach(modbusSettingComponent => {
      const modbusSetting = modbusSettingComponent.updateModelFromForm();
      if (modbusSetting) {
        this.settings.modbusSettings.push(modbusSetting);
      }
    });
    this.settings.notificationCommand = this.notificationCommandInput.updateModelFromForm();
  }

  submitForm() {
    this.updateModelFromForm();
    this.settingsService.updateSettings(this.settings).subscribe();
    this.form.markAsPristine();
  }

}
