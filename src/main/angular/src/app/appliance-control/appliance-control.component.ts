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
import {ControlFactory} from '../shared/control-factory';
import {Switch} from '../shared/switch';
import {ModbusSwitch} from '../shared/modbus-switch';
import {HttpSwitch} from '../shared/http-switch';
import {StartingCurrentSwitch} from '../shared/starting-current-switch';
import {NgForm} from '@angular/forms';
import {ApplianceControlErrorMessages} from './appliance-control-error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {TranslateService} from '@ngx-translate/core';
import {ErrorMessages} from '../shared/error-messages';
import {InputValidatorPatterns} from '../shared/input-validator-patterns';

@Component({
  selector: 'app-appliance-switch',
  templateUrl: './appliance-control.component.html',
  styles: []
})
export class ApplianceControlComponent implements OnInit {
  @ViewChild('controlForm') controlForm: NgForm;
  applianceId: string;
  control = ControlFactory.createEmptyControl();
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  TYPE_SWITCH = Switch.TYPE;
  TYPE_MODBUS_SWITCH = ModbusSwitch.TYPE;
  TYPE_HTTP_SWITCH = HttpSwitch.TYPE;
  VALIDATOR_PATTERN_INTEGER = InputValidatorPatterns.INTEGER;
  VALIDATOR_PATTERN_URL = InputValidatorPatterns.URL;

  constructor(private applianceService: ApplianceService,
              private route: ActivatedRoute,
              private translate: TranslateService) {
    this.errorMessages =  new ApplianceControlErrorMessages(this.translate);
  }

  ngOnInit() {
    this.route.params.subscribe(val => {
        this.applianceId = this.route.snapshot.paramMap.get('id');
        this.applianceService.getControl(this.applianceId).subscribe(control => {
          this.controlForm.reset(control);
          this.control = control;
          // somehow the control type is not set after switching between controls of different appliances
          this.controlForm.form.controls.controlType.setValue(this.control.type);
        });
      }
    );

    this.controlForm.statusChanges.subscribe(() =>
      this.errors = ErrorMessageHandler.applyErrorMessages4TemplateDrivenForm(this.controlForm, this.errorMessages));
  }

  typeChanged(newType: string) {
    if (newType === this.TYPE_SWITCH && this.control.switch_ == null) {
      this.control.switch_ = new Switch();
    } else if (newType === this.TYPE_MODBUS_SWITCH && this.control.modbusSwitch == null) {
      this.control.modbusSwitch = new ModbusSwitch();
    } else if (newType === this.TYPE_HTTP_SWITCH && this.control.httpSwitch == null) {
      this.control.httpSwitch = new HttpSwitch();
    }
  }

  startingCurrentDetectionChanged(startingCurrentDetection: boolean) {
    if (startingCurrentDetection) {
      this.control.startingCurrentSwitch = new StartingCurrentSwitch();
    }
  }

  submitForm() {
    this.applianceService.updateControl(this.control, this.applianceId);
  }
}
