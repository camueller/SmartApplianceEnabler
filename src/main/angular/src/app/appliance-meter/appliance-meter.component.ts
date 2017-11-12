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
import {ActivatedRoute} from '@angular/router';
import {ApplianceService} from '../shared/appliance.service';
import {S0ElectricityMeter} from '../shared/s0-electricity-meter';
import {ModbusElectricityMeter} from '../shared/modbus-electricity-meter';
import {HttpElectricityMeter} from '../shared/http-electricity-meter';
import {MeterFactory} from '../shared/meter-factory';
import {TranslateService} from '@ngx-translate/core';
import {ApplianceMeterErrorMessages} from './appliance-meter-error-messages';
import {NgForm} from '@angular/forms';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ErrorMessages} from '../shared/error-messages';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';

@Component({
  selector: 'app-appliance-meter',
  templateUrl: './appliance-meter.component.html',
  styles: []
})
export class ApplianceMeterComponent implements OnInit {
  @ViewChild('meterForm') meterForm: NgForm;
  applianceId: string;
  meter = MeterFactory.createEmptyMeter();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  TYPE_S0_ELECTRICITY_METER = S0ElectricityMeter.TYPE;
  TYPE_S0_ELECTRICITY_METER_NETWORKED = S0ElectricityMeter.TYPE_NETWORKED;
  TYPE_MODBUS_ELECTRICITY_METER = ModbusElectricityMeter.TYPE;
  TYPE_HTTP_ELECTRICITY_METER = HttpElectricityMeter.TYPE;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_FLOAT = InputValidatorPatterns.FLOAT;
  VALIDATOR_PATTERN_URL = InputValidatorPatterns.URL;

  constructor(private applianceService: ApplianceService,
              private route: ActivatedRoute,
              private translate: TranslateService) {
    this.errorMessages =  new ApplianceMeterErrorMessages(this.translate);
  }

  ngOnInit() {
    this.route.params.subscribe(val => {
        this.applianceId = this.route.snapshot.paramMap.get('id');
        this.applianceService.getMeter(this.applianceId).subscribe(meter => this.meter = meter);
      }
    );

    this.meterForm.statusChanges.subscribe(() =>
      this.errors = ErrorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.meterForm, this.errorMessages));
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
