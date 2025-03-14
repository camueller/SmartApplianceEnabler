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
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Logger} from '../../log/logger';
import {ListItem} from '../../shared/list-item';
import {SlaveElectricityMeter} from './master-electricity-meter';
import {isRequired} from '../../shared/form-util';
import {MeterSlaveModel} from './meter-slave.model';

@Component({
    selector: 'app-meter-slave',
    templateUrl: './meter-slave.component.html',
    styleUrls: ['./meter-slave.component.scss'],
    standalone: false
})
export class MeterSlaveComponent implements OnChanges, OnInit {
  @Input()
  slaveMeter: SlaveElectricityMeter;
  @Input()
  masterElectricityMeterApplianceIdWithApplianceName: Object;
  @Input()
  form: FormGroup<MeterSlaveModel>;
  masterMeterOptions: ListItem[] = [];

  constructor(private logger: Logger,
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.slaveMeter) {
      if (changes.slaveMeter.currentValue) {
        this.slaveMeter = changes.slaveMeter.currentValue;
      } else {
        this.slaveMeter = new SlaveElectricityMeter();
      }
      this.expandParentForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit() {
    Object.keys(this.masterElectricityMeterApplianceIdWithApplianceName).forEach((applianceId) => this.masterMeterOptions.push(
      {value: applianceId, viewValue: this.masterElectricityMeterApplianceIdWithApplianceName[applianceId]} as ListItem));
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('masterMeterAppliance', new FormControl(this.slaveMeter?.masterElectricityMeterApplianceId,
      Validators.required));
  }

  updateModelFromForm(): SlaveElectricityMeter {
    const masterElectricityMeterApplianceId = this.form.controls.masterMeterAppliance.value;

    if (!masterElectricityMeterApplianceId) {
      return undefined;
    }

    this.slaveMeter.masterElectricityMeterApplianceId = masterElectricityMeterApplianceId;
    return this.slaveMeter;
  }
}
