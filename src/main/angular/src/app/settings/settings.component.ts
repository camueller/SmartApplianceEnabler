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
import {SettingsFactory} from './settings-factory';
import {NgForm} from '@angular/forms';
import {SettingsService} from './settings-service';
import {Settings} from './settings';
import {SettingsDefaults} from './settings-defaults';
import {DialogService} from '../shared/dialog.service';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {SettingsErrorMessages} from './settings-error-messages';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {Logger} from '../log/logger';
import {ModbusSettings} from './modbus-settings';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styles: []
})
export class SettingsComponent implements OnInit, CanDeactivate<SettingsComponent> {
  @ViewChild('settingsForm') settingsForm: NgForm;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_HOSTNAME = InputValidatorPatterns.HOSTNAME;
  VALIDATOR_PATTERN_URL = InputValidatorPatterns.URL;
  discardChangesMessage: string;

  constructor(private logger: Logger,
              private settingsService: SettingsService,
              private translate: TranslateService,
              private dialogService: DialogService,
              private route: ActivatedRoute) {
    const settingsFactory = new SettingsFactory(logger);
    this.settingsDefaults = settingsFactory.createEmptySettingsDefaults();
    this.settings = settingsFactory.createEmptySettings();
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.errorMessages =  new SettingsErrorMessages(this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.route.data.subscribe((data: {settings: Settings, settingsDefaults: SettingsDefaults}) => {
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
    });
    this.settingsForm.statusChanges.subscribe(() =>
      this.errors = this.errorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.settingsForm, this.errorMessages));
  }

  addModbusSettings() {
    this.settings.modbusSettings.push({
      modbusTcpId: undefined,
      modbusTcpHost: undefined,
      modbusTcpPort: undefined
    });
  }

  removeModbusSettings(index: number) {
    this.settings.modbusSettings.splice(index, 1);
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.settingsForm.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  submitForm() {
    this.settingsService.updateSettings(this.settings).subscribe();
    this.settingsForm.form.markAsPristine();
  }

}
