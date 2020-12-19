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

import {Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {ActivatedRoute, CanDeactivate} from '@angular/router';
import {MeterFactory} from './meter-factory';
import {TranslateService} from '@ngx-translate/core';
import {FormGroup} from '@angular/forms';
import {MeterDefaults} from './meter-defaults';
import {MeterService} from './meter-service';
import {Meter} from './meter';
import {DialogService} from '../shared/dialog.service';
import {Observable} from 'rxjs';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {Settings} from '../settings/settings';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {FormHandler} from '../shared/form-handler';
import {S0ElectricityMeter} from './s0/s0-electricity-meter';
import {MeterModbusComponent} from './modbus/meter-modbus.component';
import {ModbusElectricityMeter} from './modbus/modbus-electricity-meter';
import {MeterHttpComponent} from './http/meter-http.component';
import {HttpElectricityMeter} from './http/http-electricity-meter';
import {MeterS0Component} from './s0/meter-s0.component';
import {ListItem} from '../shared/list-item';
import {simpleMeterType} from '../shared/form-util';
import {Appliance} from '../appliance/appliance';
import {ApplianceType} from '../appliance/appliance-type';
import {NotificationType} from '../notification/notification-type';
import {NotificationComponent} from '../notification/notification.component';

@Component({
  selector: 'app-meter',
  templateUrl: './meter.component.html',
  styleUrls: ['./meter.component.scss'],
})
export class MeterComponent implements OnChanges, OnInit, CanDeactivate<MeterComponent> {
  @ViewChild(MeterS0Component)
  meterS0Comp: MeterS0Component;
  @ViewChild(MeterModbusComponent)
  meterModbusComp: MeterModbusComponent;
  @ViewChild(MeterHttpComponent)
  meterHttpComp: MeterHttpComponent;
  @ViewChild(NotificationComponent)
  notificationComp: NotificationComponent;
  form: FormGroup;
  formHandler: FormHandler;
  applianceId: string;
  meterDefaults: MeterDefaults;
  meterFactory: MeterFactory;
  meter: Meter;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  isEvCharger: boolean;
  discardChangesMessage: string;
  confirmDeleteMessage: string;
  meterTypes: ListItem[] = [];

  constructor(private logger: Logger,
              private meterService: MeterService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private dialogService: DialogService,
              private translate: TranslateService) {
    this.meterFactory = new MeterFactory(logger);
    this.meter = this.meterFactory.createEmptyMeter();
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.control && changes.control.currentValue) {
      this.meter = changes.meter.currentValue;
    }
    if (this.form) {
      this.updateForm();
    }
  }

  ngOnInit() {
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.translate.get('dialog.confirmDelete').subscribe(translated => this.confirmDeleteMessage = translated);
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {
      meter: Meter, meterDefaults: MeterDefaults,
      settings: Settings, settingsDefaults: SettingsDefaults,
      appliance: Appliance
    }) => {
      this.meter = data.meter;
      this.meterDefaults = data.meterDefaults;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.isEvCharger = data.appliance.type === ApplianceType.EV_CHARGER.toString();
      const meterTypeKeys = this.settings.modbusSettings
        ? [S0ElectricityMeter.TYPE, ModbusElectricityMeter.TYPE, HttpElectricityMeter.TYPE]
        : [S0ElectricityMeter.TYPE, HttpElectricityMeter.TYPE];
      this.translate.get(meterTypeKeys).subscribe(translatedStrings => {
        Object.keys(translatedStrings).forEach(key => {
          this.meterTypes.push({value: simpleMeterType(key), viewValue: translatedStrings[key]} as ListItem);
        });
      });
      this.buildForm();
      if (this.form) {
        this.form.markAsPristine();
      }
    });
  }

  buildForm() {
    this.form = new FormGroup({});
    this.formHandler.addFormControl(this.form, 'meterType', this.meter && simpleMeterType(this.meter.type));
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'meterType', simpleMeterType(this.meter.type));
  }

  get notficationTypes() {
    return [NotificationType.COMMUNICATION_ERROR];
  }

  get isNotifcationEnabled() {
    return !!this.settings.notificationCommand;
  }

  get isS0ElectricityMeter() {
    return this.form.controls.meterType.value === simpleMeterType(S0ElectricityMeter.TYPE);
  }

  get isModbusElectricityMeter() {
    return this.form.controls.meterType.value === simpleMeterType(ModbusElectricityMeter.TYPE);
  }

  get isHttpElectricityMeter() {
    return this.form.controls.meterType.value === simpleMeterType(HttpElectricityMeter.TYPE);
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  delete() {
    this.dialogService.confirm(this.confirmDeleteMessage).subscribe(confirmed => {
      if (confirmed) {
        this.meterService.deleteMeter(this.applianceId).subscribe(() => this.typeChanged());
      }
    });
  }

  isDeleteEnabled() {
    return this.meter && (this.meter.s0ElectricityMeter || this.meter.modbusElectricityMeter || this.meter.httpElectricityMeter);
  }

  typeChanged(newType?: string) {
    this.meter.type = `de.avanux.smartapplianceenabler.meter.${newType}`;
  }

  submitForm() {
    if (this.meterS0Comp) {
      this.meter.s0ElectricityMeter = this.meterS0Comp.updateModelFromForm();
    }
    if (this.meterModbusComp) {
      this.meter.modbusElectricityMeter = this.meterModbusComp.updateModelFromForm();
    }
    if (this.meterHttpComp) {
      this.meter.httpElectricityMeter = this.meterHttpComp.updateModelFromForm();
    }
    if (this.notificationComp) {
      this.meter.notifications = this.notificationComp.updateModelFromForm();
    }
    this.meterService.updateMeter(this.meter, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
  }
}
