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
import {FormGroup} from '@angular/forms';
import {EvCharger} from '../control-evcharger/ev-charger';
import {ControlEvchargerComponent} from '../control-evcharger/control-evcharger.component';
import {ControlHttpComponent} from '../control-http/control-http.component';
import {ControlSwitchComponent} from '../control-switch/control-switch.component';
import {ControlModbusComponent} from '../control-modbus/control-modbus.component';
import {ControlStartingcurrentComponent} from '../control-startingcurrent/control-startingcurrent.component';
import {FormHandler} from '../shared/form-handler';

@Component({
  selector: 'app-control',
  templateUrl: './control.component.html',
  styleUrls: ['../global.css'],
})
export class ControlComponent implements OnChanges, OnInit, CanDeactivate<ControlComponent> {
  @ViewChild(ControlSwitchComponent, {static: false})
  controlSwitchComp: ControlSwitchComponent;
  @ViewChild(ControlModbusComponent, {static: false})
  controlModbusComp: ControlModbusComponent;
  @ViewChild(ControlHttpComponent, {static: false})
  controlHttpComp: ControlHttpComponent;
  @ViewChild(ControlEvchargerComponent, {static: false})
  controlEvchargerComp: ControlEvchargerComponent;
  @ViewChild(ControlStartingcurrentComponent, {static: false})
  controlStartingcurrentComp: ControlStartingcurrentComponent;
  form: FormGroup;
  formHandler: FormHandler;
  applianceId: string;
  controlDefaults: ControlDefaults;
  control: Control;
  controlFactory: ControlFactory;
  appliance: Appliance;
  settingsDefaults: SettingsDefaults;
  settings: Settings;
  discardChangesMessage: string;
  confirmDeleteMessage: string;
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
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.control && changes.control.currentValue) {
      this.control = changes.control.currentValue;
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
      control: Control,
      controlDefaults: ControlDefaults,
      appliance: Appliance,
      settings: Settings,
      settingsDefaults: SettingsDefaults
    }) => {
      this.control = data.control;
      this.controlDefaults = data.controlDefaults;
      this.appliance = data.appliance;
      this.settings = data.settings;
      this.settingsDefaults = data.settingsDefaults;
      this.updateForm();
      if (!this.control.evCharger && this.appliance.type === 'EVCharger') {
        // there is not type change for ev charger since it is determined by appliance type
        this.typeChanged(EvCharger.TYPE);
      }
      if (this.form) {
        this.form.markAsPristine();
      }
    });
    this.buildForm();
  }

  buildForm() {
    this.form = new FormGroup({});
    this.formHandler.addFormControl(this.form, 'controlType', this.control && this.control.type);
    this.formHandler.addFormControl(this.form, 'startingCurrentDetection',
      this.control && this.control.startingCurrentDetection);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'controlType', this.control.type);
    this.formHandler.setFormControlValue(this.form, 'startingCurrentDetection',
      this.control.startingCurrentDetection);
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
        this.controlService.deleteControl(this.applianceId).subscribe(() => this.typeChanged());
      }
    });
  }

  isDeleteEnabled() {
    return this.control != null && this.control.type != null;
  }

  typeChanged(newType?: string | undefined) {
    if (!newType) {
      this.control.startingCurrentDetection = false;
    } else if (newType === this.TYPE_ALWAYS_ON_SWITCH) {
      this.control.alwaysOnSwitch = this.controlFactory.createAlwaysOnSwitch();
    } else if (newType === EvCharger.TYPE) {
      this.control.startingCurrentDetection = false;
    }
    this.control.type = newType;
    this.buildForm();
    if (newType === this.TYPE_ALWAYS_ON_SWITCH) {
      this.form.markAsDirty();
    }
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
    if (this.controlSwitchComp) {
      this.control.switch_ = this.controlSwitchComp.updateModelFromForm();
    }
    if (this.controlModbusComp) {
      this.control.modbusSwitch = this.controlModbusComp.updateModelFromForm();
    }
    if (this.controlHttpComp) {
      this.control.httpSwitch = this.controlHttpComp.updateModelFromForm();
    }
    if (this.controlEvchargerComp) {
      this.control.evCharger = this.controlEvchargerComp.updateModelFromForm();
    }
    if (this.control.startingCurrentDetection) {
      this.control.startingCurrentSwitch = this.controlStartingcurrentComp.updateModelFromForm();
    }
    this.controlService.updateControl(this.control, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
  }
}
