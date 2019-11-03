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
import {S0ElectricityMeter} from '../meter-s0/s0-electricity-meter';
import {ModbusElectricityMeter} from '../meter-modbus/modbus-electricity-meter';
import {HttpElectricityMeter} from '../meter-http/http-electricity-meter';
import {MeterFactory} from './meter-factory';
import {TranslateService} from '@ngx-translate/core';
import {FormControl, FormGroup} from '@angular/forms';
import {MeterDefaults} from './meter-defaults';
import {MeterService} from './meter-service';
import {Meter} from './meter';
import {DialogService} from '../shared/dialog.service';
import {Observable} from 'rxjs';
import {Logger} from '../log/logger';
import {SettingsDefaults} from '../settings/settings-defaults';
import {Settings} from '../settings/settings';
import {MeterS0Component} from '../meter-s0/meter-s0.component';
import {MeterModbusComponent} from '../meter-modbus/meter-modbus.component';
import {MeterHttpComponent} from '../meter-http/meter-http.component';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';

@Component({
  selector: 'app-appliance-meter',
  templateUrl: './meter.component.html',
  styles: []
})
export class MeterComponent implements OnInit, CanDeactivate<MeterComponent> {
  form: FormGroup;
  @ViewChild(MeterS0Component, { static: false })
  meterS0Comp: MeterS0Component;
  @ViewChild(MeterModbusComponent, { static: false })
  meterModbusComp: MeterModbusComponent;
  @ViewChild(MeterHttpComponent, { static: false })
  meterHttpComp: MeterHttpComponent;
  applianceId: string;
  meterDefaults: MeterDefaults;
  meterFactory: MeterFactory;
  meter: Meter;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  discardChangesMessage: string;
  TYPE_S0_ELECTRICITY_METER = S0ElectricityMeter.TYPE;
  TYPE_MODBUS_ELECTRICITY_METER = ModbusElectricityMeter.TYPE;
  TYPE_HTTP_ELECTRICITY_METER = HttpElectricityMeter.TYPE;

  constructor(private logger: Logger,
              private meterService: MeterService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private dialogService: DialogService,
              private translate: TranslateService) {
    this.meterFactory = new MeterFactory(logger);
    this.meter = this.meterFactory.createEmptyMeter();
  }

  ngOnInit() {
    this.form = this.buildFormGroup();
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {
      meter: Meter, meterDefaults: MeterDefaults,
      settings: Settings, settingsDefaults: SettingsDefaults
    }) => {
      if (data.meter) {
        this.meter = data.meter;
        this.form.setControl('meterType', new FormControl(this.meter && this.meter.type));
      }
      this.meterDefaults = data.meterDefaults;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.form.markAsPristine();
    });
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
    this.meter.type = newType;
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
    this.meterService.updateMeter(this.meter, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
  }
}
