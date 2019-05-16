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

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, CanDeactivate} from '@angular/router';
import {S0ElectricityMeter} from '../meter-s0/s0-electricity-meter';
import {ModbusElectricityMeter} from '../meter-modbus/modbus-electricity-meter';
import {HttpElectricityMeter} from '../meter-http/http-electricity-meter';
import {MeterFactory} from './meter-factory';
import {TranslateService} from '@ngx-translate/core';
import {MeterErrorMessages} from './meter-error-messages';
import {FormControl, FormGroup} from '@angular/forms';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ErrorMessages} from '../shared/error-messages';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';
import {MeterDefaults} from './meter-defaults';
import {MeterService} from './meter-service';
import {Meter} from './meter';
import {DialogService} from '../shared/dialog.service';
import {Observable} from 'rxjs';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {Settings} from '../settings/settings';
import {NestedFormService} from '../shared/nested-form-service';

@Component({
  selector: 'app-appliance-meter',
  templateUrl: './meter.component.html',
  styles: []
})
export class MeterComponent implements OnInit, CanDeactivate<MeterComponent> {
  form: FormGroup;
  applianceId: string;
  meterDefaults: MeterDefaults;
  meterFactory: MeterFactory;
  meter: Meter;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  discardChangesMessage: string;
  TYPE_S0_ELECTRICITY_METER = S0ElectricityMeter.TYPE;
  TYPE_S0_ELECTRICITY_METER_NETWORKED = S0ElectricityMeter.TYPE_NETWORKED;
  TYPE_MODBUS_ELECTRICITY_METER = ModbusElectricityMeter.TYPE;
  TYPE_HTTP_ELECTRICITY_METER = HttpElectricityMeter.TYPE;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_FLOAT = InputValidatorPatterns.FLOAT;
  VALIDATOR_PATTERN_URL = InputValidatorPatterns.URL;

  constructor(private logger: Logger,
              private meterService: MeterService,
              private route: ActivatedRoute,
              private nestedFormService: NestedFormService,
              private dialogService: DialogService,
              private translate: TranslateService) {
    this.meterFactory = new MeterFactory(logger);
    this.meter = this.meterFactory.createEmptyMeter();
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnInit() {
    this.form = this.buildFormGroup();
    this.errorMessages =  new MeterErrorMessages(this.translate);
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {meter: Meter, meterDefaults: MeterDefaults,
      settings: Settings, settingsDefaults: SettingsDefaults}) => {
      if (data.meter) {
        this.meter = data.meter;
        console.log('meter=', this.meter);
        this.form.setControl('meterType', new FormControl(this.meter && this.meter.type));
      }
      this.meterDefaults = data.meterDefaults;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.form.markAsPristine();
    });
    // this.form.statusChanges.subscribe(() =>
    //   this.errors = this.errorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.form, this.errorMessages));
  }

  buildFormGroup(): FormGroup {
    const fg = new FormGroup({});
    fg.addControl('meterType', new FormControl(this.meter && this.meter.type));
    return fg;
  }

  get meterType() {
    return this.form.controls['meterType'].value;
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  typeChanged(newType: string) {
    if (newType === this.TYPE_S0_ELECTRICITY_METER && this.meter.s0ElectricityMeter == null) {
      this.meter.s0ElectricityMeter = new S0ElectricityMeter();
    } else if (newType === this.TYPE_S0_ELECTRICITY_METER_NETWORKED && this.meter.s0ElectricityMeterNetworked == null) {
      this.meter.s0ElectricityMeterNetworked = new S0ElectricityMeter();
    } else if (newType === this.TYPE_MODBUS_ELECTRICITY_METER && this.meter.modbusElectricityMeter == null) {
      this.meter.modbusElectricityMeter = new ModbusElectricityMeter();
    } else if (newType === this.TYPE_HTTP_ELECTRICITY_METER && this.meter.httpElectricityMeter == null) {
      this.meter.httpElectricityMeter = this.meterFactory.createHttpElectricityMeter(new Object());
    }
  }

  submitForm() {
    this.nestedFormService.completed.subscribe(() => {
      this.nestedFormService.completed.unsubscribe();
      this.meterService.updateMeter(this.meter, this.applianceId).subscribe();
      this.form.markAsPristine();
    });
    this.nestedFormService.submit();
  }
}
