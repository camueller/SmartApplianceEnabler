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

import { Component, OnInit } from '@angular/core';
import {Appliance} from '../shared/appliance';
import {ActivatedRoute} from '@angular/router';
import {ApplianceService} from '../shared/appliance.service';
import {Meter} from '../shared/meter';
import {S0ElectricityMeter} from '../shared/s0-electricity-meter';
import {ModbusElectricityMeter} from '../shared/modbus-electricity-meter';
import {HttpElectricityMeter} from '../shared/http-electricity-meter';
import {MeterFactory} from '../shared/meter-factory';
import {until} from 'selenium-webdriver';
import elementIsSelected = until.elementIsSelected;

@Component({
  selector: 'app-appliance-meter',
  templateUrl: './appliance-meter.component.html',
  styles: []
})
export class ApplianceMeterComponent implements OnInit {
  applianceId: string;
  meter = MeterFactory.createEmptyMeter();
  TYPE_S0_ELECTRICITY_METER = S0ElectricityMeter.TYPE;
  TYPE_S0_ELECTRICITY_METER_NETWORKED = S0ElectricityMeter.TYPE_NETWORKED;
  TYPE_MODBUS_ELECTRICITY_METER = ModbusElectricityMeter.TYPE;
  TYPE_HTTP_ELECTRICITY_METER = HttpElectricityMeter.TYPE;

  constructor(private applianceService: ApplianceService, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.params.subscribe(val => {
        this.applianceId = this.route.snapshot.paramMap.get('id');
        this.applianceService.getMeter(this.applianceId).subscribe(meter => this.meter = meter);
      }
    );
  }

  typeChanged(newType: string) {
    if (newType === this.TYPE_S0_ELECTRICITY_METER && this.meter.s0ElectricityMeter == null) {
      this.meter.s0ElectricityMeter = new S0ElectricityMeter();
    } else if (newType === this.TYPE_S0_ELECTRICITY_METER_NETWORKED && this.meter.s0ElectricityMeterNetworked == null) {
      this.meter.s0ElectricityMeterNetworked = new S0ElectricityMeter();
    } else if (newType === this.TYPE_MODBUS_ELECTRICITY_METER && this.meter.modbusElectricityMeter == null) {
      this.meter.modbusElectricityMeter = new ModbusElectricityMeter();
    } else if (newType === this.TYPE_HTTP_ELECTRICITY_METER && this.meter.httpElectricityMeter == null) {
      this.meter.httpElectricityMeter = MeterFactory.createHttpElectricityMeter(new Object());
    }
  }

  submitForm() {
    this.applianceService.updateMeter(this.meter, this.applianceId);
  }
}
