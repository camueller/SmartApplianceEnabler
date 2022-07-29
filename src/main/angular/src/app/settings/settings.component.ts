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
import {ActivatedRoute, CanDeactivate} from '@angular/router';
import {UntypedFormArray, UntypedFormGroup, Validators} from '@angular/forms';
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
import {SettingsModbusComponent} from './modbus/settings-modbus.component';
import {ModbusSetting} from './modbus/modbus-setting';
import {getValidString} from '../shared/form-util';
import {FileMode} from '../material/filenameinput/file-mode';
import {FilenameInputComponent} from '../material/filenameinput/filename-input.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit, CanDeactivate<SettingsComponent> {
  settings: Settings;
  settingsDefaults: SettingsDefaults;
  @ViewChildren('modbusSettings')
  modbusSettingComps: QueryList<SettingsModbusComponent>;
  @ViewChild(FilenameInputComponent, {static: true})
  notificationCommandInput: FilenameInputComponent;
  form: UntypedFormGroup;
  formHandler: FormHandler;
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
    this.form = new UntypedFormGroup({});
    this.formHandler.addFormControl(this.form, 'holidaysEnabled', this.settings.holidaysEnabled);
    this.formHandler.addFormControl(this.form, 'holidaysUrl', this.settings.holidaysUrl,
      [Validators.pattern(InputValidatorPatterns.URL)]);
    this.formHandler.addFormArrayControlWithEmptyFormGroups(this.form, 'modbusSettings', this.settings.modbusSettings);
    // this.formHandler.addFormControl(this.form, 'notificationCommand', this.settings.notificationCommand);
    this.setHolidaysUrlEnabled(this.settings.holidaysEnabled);
    this.form.controls.holidaysEnabled.valueChanges.subscribe(value => {
      this.setHolidaysUrlEnabled(value);
      this.form.markAsDirty();
    });
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

  get modbusSettingsFormArray() {
    return this.form.controls.modbusSettings as UntypedFormArray;
  }

  getModbusSettingFormGroup(index: number) {
    return this.modbusSettingsFormArray.controls[index];
  }

  addModbusSetting() {
    if (! this.settings.modbusSettings) {
      this.settings.modbusSettings = [];
    }
    this.settings.modbusSettings.push(new ModbusSetting());
    this.modbusSettingsFormArray.push(new UntypedFormGroup({}));
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
    this.settings.holidaysEnabled = this.form.controls.holidaysEnabled.value;
    this.settings.holidaysUrl = getValidString(this.form.controls.holidaysUrl.value);
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
