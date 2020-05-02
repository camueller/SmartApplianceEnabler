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
import {TranslateService} from '@ngx-translate/core';
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
import {FormHandler} from '../shared/form-handler';
import {Switch} from './switch/switch';
import {ControlSwitchComponent} from './switch/control-switch.component';
import {ControlHttpComponent} from './http/control-http.component';
import {HttpSwitch} from './http/http-switch';
import {AlwaysOnSwitch} from './alwayson/always-on-switch';
import {ModbusSwitch} from './modbus/modbus-switch';
import {StartingCurrentSwitch} from './startingcurrent/starting-current-switch';
import {ControlModbusComponent} from './modbus/control-modbus.component';
import {ControlEvchargerComponent} from './evcharger/control-evcharger.component';
import {ControlStartingcurrentComponent} from './startingcurrent/control-startingcurrent.component';
import {EvCharger} from './evcharger/ev-charger';
import {ListItem} from '../shared/list-item';
import {simpleControlType} from '../shared/form-util';

@Component({
  selector: 'app-control',
  templateUrl: './control.component.html',
  styleUrls: ['./control.component.scss'],
})
export class ControlComponent implements OnChanges, OnInit, CanDeactivate<ControlComponent> {
  @ViewChild(ControlSwitchComponent)
  controlSwitchComp: ControlSwitchComponent;
  @ViewChild(ControlModbusComponent)
  controlModbusComp: ControlModbusComponent;
  @ViewChild(ControlHttpComponent)
  controlHttpComp: ControlHttpComponent;
  @ViewChild(ControlEvchargerComponent)
  controlEvchargerComp: ControlEvchargerComponent;
  @ViewChild(ControlStartingcurrentComponent)
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
  controlTypes: ListItem[] = [];

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
    const controlTypeKeys = [Switch.TYPE, ModbusSwitch.TYPE, HttpSwitch.TYPE, AlwaysOnSwitch.TYPE];
    this.translate.get(controlTypeKeys).subscribe(translatedStrings => {
      Object.keys(translatedStrings).forEach(key => {
        this.controlTypes.push({value: simpleControlType(key), viewValue: translatedStrings[key]} as ListItem);
      });
    });
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
      if (this.appliance.type === 'EVCharger') {
        this.control.type = EvCharger.TYPE;
      }
      this.updateForm();
      if (this.form) {
        this.form.markAsPristine();
      }
    });
    this.buildForm();
  }

  buildForm() {
    this.form = new FormGroup({});
    this.formHandler.addFormControl(this.form, 'controlType', this.control && simpleControlType(this.control.type));
    this.formHandler.addFormControl(this.form, 'startingCurrentDetection',
      this.control && this.control.startingCurrentDetection);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'controlType', this.control && this.control.type);
    this.formHandler.setFormControlValue(this.form, 'startingCurrentDetection',
      this.control && this.control.startingCurrentDetection);
  }

  canDeactivate(): Observable<boolean> | boolean {
    if (this.form.pristine) {
      return true;
    }
    return this.dialogService.confirm(this.discardChangesMessage);
  }

  get isAlwaysOnSwitch() {
    return this.control && this.control.type === AlwaysOnSwitch.TYPE;
  }

  get isSwitch() {
    return this.control && this.control.type === Switch.TYPE;
  }

  get isModbusSwitch() {
    return this.control && this.control.type === ModbusSwitch.TYPE;
  }

  get isHttpSwitch() {
    return this.control && this.control.type === HttpSwitch.TYPE;
  }

  get isEvCharger() {
    return this.control && this.control.type === EvCharger.TYPE;
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
    this.control.type = `de.avanux.smartapplianceenabler.control.${newType}`;
    if (!this.control.type) {
      this.control.startingCurrentDetection = false;
    } else if (this.isAlwaysOnSwitch) {
      this.control.alwaysOnSwitch = this.controlFactory.createAlwaysOnSwitch();
    } else if (this.isEvCharger) {
      this.control.startingCurrentDetection = false;
    }
    this.buildForm();
    if (this.isAlwaysOnSwitch) {
      this.form.markAsDirty();
    }
  }

  get canHaveStartingCurrentDetection(): boolean {
    return !this.isAlwaysOnSwitch && this.control.type !== MockSwitch.TYPE;
  }

  toggleStartingCurrentDetection() {
    this.setStartingCurrentDetection(!this.control.startingCurrentDetection);
  }

  setStartingCurrentDetection(startingCurrentDetection: boolean) {
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
