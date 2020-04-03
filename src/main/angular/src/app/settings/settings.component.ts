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

import {Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ActivatedRoute, CanDeactivate} from '@angular/router';
import {FormArray, FormGroup, Validators} from '@angular/forms';
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
import {FormHandler} from '../shared/form-handler';
import {ModbusSetting} from '../settings-modbus/modbus-setting';
import {SettingsModbusComponent} from '../settings-modbus/settings-modbus.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['../global.css']
})
export class SettingsComponent implements OnInit, CanDeactivate<SettingsComponent> {
  settings: Settings;
  settingsDefaults: SettingsDefaults;
  @ViewChildren('modbusSettings')
  modbusSettingComps: QueryList<SettingsModbusComponent>;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  discardChangesMessage: string;

  constructor(private logger: Logger,
              private settingsService: SettingsService,
              private translate: TranslateService,
              private dialogService: DialogService,
              private route: ActivatedRoute) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('SettingsComponent.error.', [
      new ErrorMessage('holidaysUrl', ValidatorType.pattern),
      new ErrorMessage('modbusTcpHost', ValidatorType.pattern),
      new ErrorMessage('modbusTcpPort', ValidatorType.pattern),
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

  buildForm() {
    this.form = new FormGroup({});
    this.formHandler.addFormControl(this.form, 'holidaysEnabled', this.settings.holidaysEnabled);
    this.formHandler.addFormControl(this.form, 'holidaysUrl', this.settings.holidaysUrl,
      [Validators.pattern(InputValidatorPatterns.URL)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusSettings', this.settings.modbusSettings);
    this.setHolidaysUrlEnabled(this.settings.holidaysEnabled);
  }

  isHolidaysEnabled() {
    return this.form.controls.holidaysEnabled.value;
  }

  setHolidaysUrlEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.holidaysUrl.enable();
    } else {
      this.form.controls.holidaysUrl.disable();
    }
  }

  toggleHolidaysEnabled() {
    this.setHolidaysUrlEnabled(!this.isHolidaysEnabled());
  }

  get modbusSettingsFormArray() {
    return this.form.controls.modbusSettings as FormArray;
  }

  getModbusSettingFormGroup(index: number) {
    return this.modbusSettingsFormArray.controls[index];
  }

  addModbusSetting() {
    this.settings.modbusSettings.push(new ModbusSetting());
    this.modbusSettingsFormArray.push(new FormGroup({}));
    this.form.markAsDirty();
  }

  onModbusSettingRemove(index: number) {
    this.settings.modbusSettings.splice(index, 1);
    this.modbusSettingsFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  updateModelFromForm() {
    this.settings.holidaysEnabled = this.form.controls.holidaysEnabled.value;
    this.settings.holidaysUrl = this.form.controls.holidaysUrl.value;
    this.settings.modbusSettings = [];
    this.modbusSettingComps.forEach(modbusSettingComponent => {
      const modbusSetting = modbusSettingComponent.updateModelFromForm();
      if (modbusSetting) {
        this.settings.modbusSettings.push(modbusSetting);
      }
    });
  }

  submitForm() {
    this.updateModelFromForm();
    this.settingsService.updateSettings(this.settings).subscribe();
    this.form.markAsPristine();
  }

}
