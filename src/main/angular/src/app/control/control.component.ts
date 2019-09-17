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
import {ControlFactory} from './control-factory';
import {Switch} from '../control-switch/switch';
import {ModbusSwitch} from '../control-modbus/modbus-switch';
import {HttpSwitch} from '../control-http/http-switch';
import {StartingCurrentSwitch} from '../control-startingcurrent/starting-current-switch';
import {TranslateService} from '@ngx-translate/core';
import {AlwaysOnSwitch} from '../control-alwayson/always-on-switch';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';
import {ControlDefaults} from './control-defaults';
import {ControlService} from './control-service';
import {Control} from './control';
import {Observable} from 'rxjs';
import {DialogService} from '../shared/dialog.service';
import {MockSwitch} from './mock-switch';
import {Logger} from '../log/logger';
import {Settings} from '../settings/settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {Appliance} from '../appliance/appliance';
import {FormControl, FormGroup} from '@angular/forms';
import {EvCharger} from '../control-evcharger/ev-charger';
import {ControlEvchargerComponent} from '../control-evcharger/control-evcharger.component';
import {ControlHttpComponent} from '../control-http/control-http.component';

@Component({
  selector: 'app-appliance-switch',
  templateUrl: './control.component.html',
  styleUrls: ['../global.css']
})
export class ControlComponent implements OnInit, CanDeactivate<ControlComponent> {
  form: FormGroup;
  @ViewChild(ControlHttpComponent)
  controlHttpComp: ControlHttpComponent;
  @ViewChild(ControlEvchargerComponent)
  evChargerComp: ControlEvchargerComponent;
  applianceId: string;
  controlDefaults: ControlDefaults;
  control: Control;
  controlFactory: ControlFactory;
  appliance: Appliance;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  discardChangesMessage: string;
  TYPE_ALWAYS_ON_SWITCH = AlwaysOnSwitch.TYPE;
  TYPE_SWITCH = Switch.TYPE;
  TYPE_MODBUS_SWITCH = ModbusSwitch.TYPE;
  TYPE_MOCK_SWITCH = MockSwitch.TYPE;
  TYPE_HTTP_SWITCH = HttpSwitch.TYPE;
  TYPE_EVCHARGER = EvCharger.TYPE;

  constructor(private logger: Logger,
              private controlService: ControlService,
              private appliancesReloadService: AppliancesReloadService,
              private route: ActivatedRoute,
              private dialogService: DialogService,
              private translate: TranslateService) {
    this.controlFactory = new ControlFactory(logger);
    this.control = this.controlFactory.createEmptyControl();
  }

  ngOnInit() {
    this.translate.get('dialog.candeactivate').subscribe(translated => this.discardChangesMessage = translated);
    this.route.paramMap.subscribe(() => this.applianceId = this.route.snapshot.paramMap.get('id'));
    this.route.data.subscribe((data: {
      control: Control, controlDefaults: ControlDefaults, appliance: Appliance,
      settings: Settings, settingsDefaults: SettingsDefaults
    }) => {
      this.control = data.control;
      this.controlDefaults = data.controlDefaults;
      this.appliance = data.appliance;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.form = this.buildFormGroup();
      if (!this.control.evCharger && this.appliance.type === 'EVCharger') {
        // there is not type change for ev charger since it is determined by appliance type
        this.typeChanged(EvCharger.TYPE);
      }
    });
    this.form.markAsPristine();
    console.log('FORM=', this.form);
  }

  buildFormGroup(): FormGroup {
    const fg = new FormGroup({});
    fg.addControl('controlType', new FormControl(this.control && this.control.type));
    fg.addControl('startingCurrentDetection', new FormControl(this.control && this.control.startingCurrentDetection));
    return fg;
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  typeChanged(newType: string) {
    let newControl;
    if (newType === '') {
      this.control.startingCurrentDetection = false;
    } else if (newType === this.TYPE_ALWAYS_ON_SWITCH) {
      this.control.type = AlwaysOnSwitch.TYPE;
      newControl = this.control.alwaysOnSwitch;
    } else if (newType === this.TYPE_SWITCH) {
      this.control.type = Switch.TYPE;
      newControl = this.control.switch_;
    } else if (newType === this.TYPE_MODBUS_SWITCH) {
      this.control.type = ModbusSwitch.TYPE;
      newControl = this.control.modbusSwitch;
    } else if (newType === this.TYPE_MOCK_SWITCH) {
      this.control.type = MockSwitch.TYPE;
      newControl = this.control.mockSwitch;
    } else if (newType === this.TYPE_HTTP_SWITCH) {
      this.control.type = HttpSwitch.TYPE;
      newControl = this.control.httpSwitch;
    } else if (newType === EvCharger.TYPE) {
      this.control.type = EvCharger.TYPE;
      newControl = this.control.evCharger;
      this.control.startingCurrentDetection = false;
    }
    this.controlFactory.initializeByType(this.control, newControl, newType);
  }

  get canHaveStartingCurrentDetection(): boolean {
    return this.control.type !== AlwaysOnSwitch.TYPE
      && this.control.type !== MockSwitch.TYPE;
  }

  startingCurrentDetectionChanged(startingCurrentDetection: boolean) {
    if (startingCurrentDetection) {
      this.control.startingCurrentSwitch = new StartingCurrentSwitch();
      this.control.startingCurrentDetection = true;
    } else {
      this.control.startingCurrentSwitch = null;
      this.control.startingCurrentDetection = false;
    }
    this.form.markAsDirty();
  }

  submitForm() {
    if (this.controlHttpComp) {
      this.control.httpSwitch = this.controlHttpComp.updateModelFromForm();
    }
    if (this.evChargerComp) {
      this.control.evCharger = this.evChargerComp.updateModelFromForm();
    }

    console.log('CONTROL=', this.control);
    // const subscription = this.nestedFormService.completed.subscribe(() => {
    //   console.log('CONTROL=', this.control);
    //   // this.controlService.updateControl(this.control, this.applianceId).subscribe(
    //   //   () => this.appliancesReloadService.reload());
    //   this.form.markAsPristine();
    //   subscription.unsubscribe();
    // });
    // this.nestedFormService.submit();
  }
}
