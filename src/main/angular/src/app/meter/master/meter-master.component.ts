/*
 * Copyright (C) 2021 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {UntypedFormGroup} from '@angular/forms';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {MasterElectricityMeter} from './master-electricity-meter';
import {ListItem} from '../../shared/list-item';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';

@Component({
  selector: 'app-meter-master',
  templateUrl: './meter-master.component.html',
  styleUrls: ['./meter-master.component.scss']
})
export class MeterMasterComponent implements OnChanges, OnInit {
  @Input()
  masterMeter: MasterElectricityMeter;
  @Input()
  form: UntypedFormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  switchOnOptions: ListItem[] = [];
  private readonly switchOnUndefined = 'switchOn.undefined';
  private readonly switchOnTrue = 'switchOn.true';
  private readonly switchOnFalse = 'switchOn.false';

  private masterSlaveControlValidator = (): { [key: string]: boolean } => {
    const masterSwitchOn = this.form.get('masterSwitchOn');
    const slaveSwitchOn = this.form.get('slaveSwitchOn');
    masterSwitchOn?.setErrors(null);
    slaveSwitchOn?.setErrors(null);
    const valid = !!masterSwitchOn?.value || !!slaveSwitchOn?.value;
    return !valid ? {custom: true} : null;
  }

  constructor(private logger: Logger,
              private translate: TranslateService
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.masterMeter) {
      if (changes.masterMeter.currentValue) {
        this.masterMeter = changes.masterMeter.currentValue;
      } else {
        this.masterMeter = new MasterElectricityMeter();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('MeterMasterComponent.error.', [
      new ErrorMessage('masterSwitchOn', ValidatorType.custom),
      new ErrorMessage('slaveSwitchOn', ValidatorType.custom),
    ], this.translate);
    const switchOnKeys = [this.switchOnTrue, this.switchOnFalse];
    const translateKeys = [...switchOnKeys].map(key => this.toTranslateKey(key));
    this.translate.get(translateKeys).subscribe(translatedStrings => {
      this.switchOnOptions.push({value: this.switchOnUndefined, viewValue: ''} as ListItem);
      switchOnKeys.forEach(key => {
        this.switchOnOptions.push({value: key, viewValue: translatedStrings[this.toTranslateKey(key)]} as ListItem);
      });
    });
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
    // form is already invalid even without status changes
    this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
  }

  toTranslateKey(key: string): string {
    // full key is too long for Angular (max. length seems to be 36)
    return `MeterMasterComponent.${key}`;
  }

  toSwitchOnKey(switchOn: boolean | null | undefined): string | undefined {
    if (switchOn !== null && switchOn !== undefined) {
      return switchOn ? this.switchOnTrue : this.switchOnFalse;
    }
    return undefined;
  }

  fromSwitchOnKey(switchOnKey: string): boolean | null {
    if (switchOnKey === this.switchOnTrue) {
      return true;
    }
    if (switchOnKey === this.switchOnFalse) {
      return false;
    }
    return null;
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'masterSwitchOn', this.toSwitchOnKey(this.masterMeter?.masterSwitchOn),
      this.masterSlaveControlValidator);
    this.formHandler.addFormControl(this.form, 'slaveSwitchOn', this.toSwitchOnKey(this.masterMeter?.slaveSwitchOn),
      this.masterSlaveControlValidator);
  }

  updateModelFromForm(): MasterElectricityMeter {
    const masterSwitchOn = this.fromSwitchOnKey(this.form.controls.masterSwitchOn.value);
    const slaveSwitchOn = this.fromSwitchOnKey(this.form.controls.slaveSwitchOn.value);

    this.masterMeter.masterSwitchOn = masterSwitchOn;
    this.masterMeter.slaveSwitchOn = slaveSwitchOn;
    return this.masterMeter;
  }
}
